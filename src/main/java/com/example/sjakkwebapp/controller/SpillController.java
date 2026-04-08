package com.example.sjakkwebapp.controller;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import com.example.sjakkwebapp.model.Parti;
import com.example.sjakkwebapp.service.PartiService;
import com.example.sjakkwebapp.util.LoginUtil;

import brikke.Farge;
import brikke.Brikke;
import brett.Rute;
import spill.Trekk;
import jakarta.servlet.http.HttpSession;
import spill.Spiller;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class SpillController {
	
	@Autowired 
    private PartiService s;
	
    @GetMapping("/spill")
    public ResponseEntity<String> sjakk(@RequestParam(defaultValue = "3") int dybde, HttpSession session) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
    	
    	String spillerHvit = (String) session.getAttribute("bruker");
    	String spillerSvart = "cpu@cpu.no";

    	spill.Parti parti = new spill.Parti();
        parti.setBruker(spillerHvit);
        // Interactive: Human is White, CPU is Black
        Spiller hvit = new Spiller(spillerHvit, Farge.HVIT, dybde, false, parti);
        Spiller svart = new Spiller(spillerSvart, Farge.SVART, dybde, true, parti);
        
        session.setAttribute("parti", parti);
        session.setAttribute("hvit", hvit);
        session.setAttribute("svart", svart);

        return ResponseEntity.ok("Interactive game initialized with depth " + dybde);
    }

    @GetMapping("/simulate")
    public ResponseEntity<String> simulate(@RequestParam(defaultValue = "3") int dybde, HttpSession session) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
    	
    	String spillerHvit = (String) session.getAttribute("bruker");
    	String spillerSvart = "cpu@cpu.no";

    	spill.Parti parti = new spill.Parti();
        parti.setBruker(spillerHvit);
        // Simulation: Both are CPU
        Spiller hvit = new Spiller("CPU-White", Farge.HVIT, dybde, true, parti);
        Spiller svart = new Spiller("CPU-Black", Farge.SVART, dybde, true, parti);
        
        // Run simulation in a separate thread so it doesn't block
        new Thread(() -> {
            try {
                parti.spill(hvit, svart);
                s.leggTilParti(spillerHvit, spillerSvart, parti.getPNG());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return ResponseEntity.ok("Simulation started with depth " + dybde);
    }

    @PostMapping("/move")
    public ResponseEntity<String> userMove(@RequestParam String from, @RequestParam String to, HttpSession session) {
        spill.Parti parti = (spill.Parti) session.getAttribute("parti");
        Spiller hvit = (Spiller) session.getAttribute("hvit");
        Spiller svart = (Spiller) session.getAttribute("svart");

        if (parti == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No active game. Start by clicking Simulate.");
        }

        // 1. Human Move
        int fx = util.Utils.charToInt(from.charAt(0));
        int fy = Character.getNumericValue(from.charAt(1));
        Brikke brikke = parti.getBrett().finnRute(fx, fy).getBrikke();
        
        if (brikke != null && brikke.getFarge() == Farge.HVIT) {
            int tx = util.Utils.charToInt(to.charAt(0));
            int ty = Character.getNumericValue(to.charAt(1));
            Rute tilRute = parti.getBrett().finnRute(tx, ty);
            
            // Execute move
            brikke.flytt(tilRute);
            
            // 2. CPU Counter-move
            // We use a separate thread or just run it here since it might take a second
            new Thread(() -> {
                parti.spillTrekk(svart, Farge.SVART);
            }).start();

            return ResponseEntity.ok("Move accepted");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid move");
    }
    
    @GetMapping("/minePartier")
    public ResponseEntity<List<Parti>> minePartier(HttpSession session) {
        
        if (!LoginUtil.erBrukerInnlogget(session)) {
            // Return 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        String bruker = (String) session.getAttribute("bruker");
        List<Parti> partier = s.finnPartierForBruker(bruker);
        
        return ResponseEntity.ok(partier);
    }
    
    @GetMapping("/partier/{id}")
    public ResponseEntity<Parti> getParti(@PathVariable int id, HttpSession session) {
        
        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        return s.finnPartiById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
    
    // Keep track of all clients by username
    private static final java.util.Map<String, SseEmitter> clients = new java.util.concurrent.ConcurrentHashMap<>();

    // Endpoint for clients to connect
    @GetMapping("/moves/stream")
    public SseEmitter streamMoves(HttpSession session) {
        String bruker = (String) session.getAttribute("bruker");
        if (bruker == null) return null;

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // no timeout
        clients.put(bruker, emitter);

        // Remove emitter if connection is closed
        emitter.onCompletion(() -> clients.remove(bruker));
        emitter.onTimeout(() -> clients.remove(bruker));
        emitter.onError((e) -> clients.remove(bruker));

        return emitter;
    }
    
    public static void makeAIMove(String bruker, String move) {
        SseEmitter emitter = clients.get(bruker);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("move").data(move));
            } catch (IOException e) {
                clients.remove(bruker);
            }
        }
    }
    
}