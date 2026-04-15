package com.example.sjakkwebapp.controller;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;

import com.example.sjakkwebapp.service.PartiService;
import com.example.sjakkwebapp.service.SSEService;
import com.example.sjakkwebapp.service.AIService;
import com.example.sjakkwebapp.service.GameService;
import com.example.sjakkwebapp.util.LoginUtil;

import brikke.Farge;
import jakarta.servlet.http.HttpSession;
import spill.Spiller;

@RestController
public class SpillController {
	
	@Autowired 
    private PartiService s;

    @Autowired
    private SSEService sseService;

    @Autowired
    private AIService aiService;

    @Autowired
    private GameService gameService;
	
    @GetMapping("/spill")
    public ResponseEntity<String> sjakk(@RequestParam(defaultValue = "3") int dybde, HttpSession session) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
    	
    	String spillerHvit = (String) session.getAttribute("bruker");
    	String spillerSvart = "cpu@cpu.no";

    	spill.Parti parti = new spill.Parti();
        parti.setBruker(spillerHvit);
        parti.setMoveNotifier(sseService);
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

        spill.Parti parti = new spill.Parti();

        // Simulation: Both are CPU
        Spiller hvit = new Spiller("CPU-White", Farge.HVIT, dybde, true, parti);
        Spiller svart = new Spiller("CPU-Black", Farge.SVART, dybde, true, parti);
        String spillerHvit = (String) session.getAttribute("bruker");
    	String spillerSvart = "cpu@cpu.no";

        parti.setBruker(spillerHvit);
        parti.setMoveNotifier(sseService);
        
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
        String bruker = (String) session.getAttribute("bruker");
        spill.Parti sessionParti = (spill.Parti) session.getAttribute("parti");
        Spiller cpuSvart = (Spiller) session.getAttribute("svart");
        
        String result = gameService.handleMove(bruker, from, to, sessionParti, cpuSvart);
        
        if ("OK".equals(result)) {
            return ResponseEntity.ok("Move accepted");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }
    
    // Endpoint for clients to connect
    @GetMapping("/moves/stream")
    public SseEmitter streamMoves(HttpSession session) {
        String bruker = (String) session.getAttribute("bruker");
        if (bruker == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return sseService.createEmitter(bruker);
    }
    
    @GetMapping("/users/online")
    public ResponseEntity<List<String>> getOnlineUsers(HttpSession session) {
        String self = (String) session.getAttribute("bruker");
        if (self == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(sseService.getOnlineUsers().stream().filter(u -> !u.equals(self)).toList());
    }

    @PostMapping("/challenge")
    public ResponseEntity<String> challengeUser(@RequestParam String opponent, HttpSession session) {
        String self = (String) session.getAttribute("bruker");
        if (self == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        if (!sseService.isUserOnline(opponent)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Opponent not online");
        
        sseService.notifyUser(opponent, "challenge", self);
        
        return ResponseEntity.ok("Challenge sent");
    }

    @PostMapping("/challenge/accept")
    public ResponseEntity<String> acceptChallenge(@RequestParam String opponent, HttpSession session) {
        String self = (String) session.getAttribute("bruker");
        if (self == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        GameService.PvPMatch match = gameService.createMatch(opponent, self); // Challenger is White
        
        // Notify both that game started
        sseService.notifyUser(self, "game_started", "black");
        sseService.notifyUser(opponent, "game_started", "white");
        
        return ResponseEntity.ok("Game started");
    }

    @PostMapping("/ask-ai")
    public ResponseEntity<String> askAI(HttpSession session) {
        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        String bruker = (String) session.getAttribute("bruker");
        spill.Parti parti = (spill.Parti) session.getAttribute("parti");
        GameService.PvPMatch match = gameService.getMatch(bruker);
        if (match != null) {
            parti = match.parti;
        }

        if (parti == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No active game.");
        }

        spill.Trekk siste = parti.getSisteTrekk();
        boolean hvitITrekket = (siste == null) || (siste.getBrikke().getFarge() == brikke.Farge.SVART);
        String fen = parti.getBrett().stillingTilFEN(hvitITrekket);
        String bestMove = aiService.getBestMoveFromStockfish(fen);

        try {
            String aiResponse = aiService.askAzureAI(fen, bestMove);
            return ResponseEntity.ok(aiResponse);
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Request failed: " + e.getMessage());
        }
    }
    
}
