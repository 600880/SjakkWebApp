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
        String bruker = (String) session.getAttribute("bruker");
        spill.Parti parti = (spill.Parti) session.getAttribute("parti");
        
        PvPMatch match = activePvPMatches.get(bruker);
        boolean isPvP = (match != null);
        if (isPvP) {
            parti = match.parti;
        }

        if (parti == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No active game.");
        }

        // Determine whose turn it is
        Trekk siste = parti.getSisteTrekk();
        Farge currentTurn = (siste == null) ? Farge.HVIT : (siste.getBrikke().getFarge() == Farge.HVIT ? Farge.SVART : Farge.HVIT);

        // Verify it's the user's turn and they own the color
        if (isPvP) {
            boolean isWhite = bruker.equals(match.hvit);
            if (isWhite && currentTurn != Farge.HVIT) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not your turn (Black's turn)");
            if (!isWhite && currentTurn != Farge.SVART) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not your turn (White's turn)");
        } else {
            if (currentTurn != Farge.HVIT) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not your turn (CPU's turn)");
        }

        int fx = util.Utils.charToInt(from.charAt(0));
        int fy = Character.getNumericValue(from.charAt(1));
        Brikke brikke = parti.getBrett().finnRute(fx, fy).getBrikke();
        
        if (brikke != null && brikke.getFarge() == currentTurn) {
            int tx = util.Utils.charToInt(to.charAt(0));
            int ty = Character.getNumericValue(to.charAt(1));
            Rute tilRute = parti.getBrett().finnRute(tx, ty);
            
            // Execute move
            brikke.flytt(tilRute);
            
            if (isPvP) {
                // Notify opponent
                String opponent = bruker.equals(match.hvit) ? match.svart : match.hvit;
                notifyUser(opponent, "move", from + "-" + to);
            } else {
                // CPU Counter-move
                Spiller svart = (Spiller) session.getAttribute("svart");
                final spill.Parti cpuParti = parti;
                new Thread(() -> {
                    cpuParti.spillTrekk(svart, Farge.SVART);
                }).start();
            }

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
    private static final java.util.Map<String, PvPMatch> activePvPMatches = new java.util.concurrent.ConcurrentHashMap<>();

    private static class PvPMatch {
        spill.Parti parti;
        String hvit;
        String svart;
        PvPMatch(String hvit, String svart) {
            this.hvit = hvit;
            this.svart = svart;
            this.parti = new spill.Parti();
        }
    }

    // Endpoint for clients to connect
    @GetMapping("/moves/stream")
    public SseEmitter streamMoves(HttpSession session) {
        String bruker = (String) session.getAttribute("bruker");
        if (bruker == null) return null;

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // no timeout
        clients.put(bruker, emitter);

        // Send a heartbeat every 20 seconds to keep the connection alive
        new Thread(() -> {
            try {
                while (clients.containsKey(bruker)) {
                    Thread.sleep(20000);
                    emitter.send(SseEmitter.event().name("ping").data("heartbeat"));
                }
            } catch (Exception e) {
                clients.remove(bruker);
            }
        }).start();

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

    @GetMapping("/users/online")
    public ResponseEntity<List<String>> getOnlineUsers(HttpSession session) {
        String self = (String) session.getAttribute("bruker");
        if (self == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(clients.keySet().stream()
                .filter(u -> !u.equals(self))
                .collect(java.util.stream.Collectors.toList()));
    }

    @PostMapping("/challenge")
    public ResponseEntity<String> challengeUser(@RequestParam String opponent, HttpSession session) {
        String self = (String) session.getAttribute("bruker");
        if (self == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        SseEmitter emitter = clients.get(opponent);
        if (emitter == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Opponent not online");
        
        try {
            emitter.send(SseEmitter.event().name("challenge").data(self));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send challenge");
        }
        
        return ResponseEntity.ok("Challenge sent");
    }

    @PostMapping("/challenge/accept")
    public ResponseEntity<String> acceptChallenge(@RequestParam String opponent, HttpSession session) {
        String self = (String) session.getAttribute("bruker");
        if (self == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        PvPMatch match = new PvPMatch(opponent, self); // Challenger is White
        activePvPMatches.put(self, match);
        activePvPMatches.put(opponent, match);
        
        // Notify both that game started
        notifyUser(self, "game_started", "black");
        notifyUser(opponent, "game_started", "white");
        
        return ResponseEntity.ok("Game started");
    }

    private void notifyUser(String bruker, String eventName, String data) {
        SseEmitter emitter = clients.get(bruker);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                clients.remove(bruker);
            }
        }
    }
    
}