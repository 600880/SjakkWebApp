package com.example.sjakkwebapp.controller;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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
import com.example.sjakkwebapp.service.SpillService;
import com.example.sjakkwebapp.util.LoginUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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
    private SpillService spillService;

    @Autowired
    private ExecutorService gameExecutor;
	
    @GetMapping("/spill")
    public ResponseEntity<String> sjakk(@RequestParam(defaultValue = "3") int dybde, @RequestParam(defaultValue = "white") String color, HttpSession session) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

    	spill.Parti parti = new spill.Parti();
        String bruker = (String) session.getAttribute("bruker");
        parti.setBruker(bruker);
        parti.setMoveNotifier(sseService);

        boolean isWhite = "white".equalsIgnoreCase(color);
        Spiller hvit, svart;

        if (isWhite) {
            hvit = new Spiller(bruker, Farge.HVIT, dybde, parti);
            svart = new Spiller("cpu@cpu.no", Farge.SVART, dybde, parti);
            session.setAttribute("CPU", svart);
        } else {
            hvit = new Spiller("cpu@cpu.no", Farge.HVIT, dybde, parti);
            svart = new Spiller(bruker, Farge.SVART, dybde, parti);
            session.setAttribute("CPU", hvit);
            
            // If CPU is white, it must make the first move
            gameExecutor.submit(() -> {
                parti.spillTrekk(hvit, Farge.HVIT);
            });
        }
        
        parti.spill(hvit, svart);
        session.setAttribute("parti", parti);

        return ResponseEntity.ok("Interactive game initialized as " + color + " with depth " + dybde);
    }

    @GetMapping("/simulate")
    public ResponseEntity<String> simulate(@RequestParam(defaultValue = "3") int dybde, HttpSession session) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        spill.Parti parti = new spill.Parti();
        String bruker = (String) session.getAttribute("bruker");
        parti.setBruker(bruker);
        parti.setMoveNotifier(sseService);

        // Simulation: Both are CPU
        Spiller hvit = new Spiller("CPU-White", Farge.HVIT, dybde, parti);
        Spiller svart = new Spiller("CPU-Black", Farge.SVART, dybde, parti);
        parti.spill(hvit, svart);
        
        // Run simulation in a separate thread so it doesn't block
        session.setAttribute("parti", parti);
        Future<?> simulationTask = gameExecutor.submit(() -> {
            try {
                while (true) {
                    if (!parti.spillTrekk(hvit, Farge.HVIT)) break;
			        if (!parti.spillTrekk(svart, Farge.SVART)) break;
                }
                s.leggTilParti(bruker, "cpu@cpu.no", parti.getPNG());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        session.setAttribute("simulationTask", simulationTask);

        return ResponseEntity.ok("Simulation started with depth " + dybde);
    }

    @PostMapping("/move")
    public ResponseEntity<String> userMove(@RequestParam String from, @RequestParam String to, HttpSession session) {
        
        spill.Parti sessionParti = (spill.Parti) session.getAttribute("parti");
        String bruker = (String) session.getAttribute("bruker");
        Spiller cpu = (Spiller) session.getAttribute("CPU");
        
        String result = spillService.handleMove(bruker, from, to, sessionParti, cpu);
        
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
    public ResponseEntity<String> challengeUser(@RequestParam String opponent, @RequestParam(defaultValue = "white") String color, HttpSession session) {
        String self = (String) session.getAttribute("bruker");
        if (self == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        if (!sseService.isUserOnline(opponent)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Opponent not online");
        
        // Send challenge as "challenger_name:challenger_color"
        sseService.notifyUser(opponent, "challenge", self + ":" + color);
        
        return ResponseEntity.ok("Challenge sent");
    }

    @PostMapping("/challenge/accept")
    public ResponseEntity<String> acceptChallenge(@RequestParam String opponent, @RequestParam(defaultValue = "white") String challengerColor, HttpSession session) {
        String self = (String) session.getAttribute("bruker");
        if (self == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        spill.Parti parti;
        if ("white".equalsIgnoreCase(challengerColor)) {
            parti = spillService.createMatch(opponent, self);
            sseService.notifyUser(opponent, "game_started", "white");
            sseService.notifyUser(self, "game_started", "black");
        } else {
            parti = spillService.createMatch(self, opponent);
            sseService.notifyUser(opponent, "game_started", "black");
            sseService.notifyUser(self, "game_started", "white");
        }
        
        session.setAttribute("parti", parti);
        
        return ResponseEntity.ok("Game started");
    }

    @PostMapping("/abort")
    public ResponseEntity<String> abortGame(HttpSession session) {
        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        String bruker = (String) session.getAttribute("bruker");
        spillService.endMatch(bruker);
        session.removeAttribute("parti");

        // Cancel the background simulation task if it exists
        Future<?> simulationTask = (Future<?>) session.getAttribute("simulationTask");
        if (simulationTask != null && !simulationTask.isDone()) {
            simulationTask.cancel(true);
        }

        return ResponseEntity.ok("Game aborted");
    }

    @PostMapping("/chat")
    public ResponseEntity<String> sendChat(@RequestParam String melding, HttpSession session) {
        String self = (String) session.getAttribute("bruker");
        if (self == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        String opponent = spillService.getOpponent(self);
        if (opponent != null) {
            sseService.notifyUser(opponent, "chat", self + ": " + melding);
            return ResponseEntity.ok("Message sent");
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No active PvP game");
    }

    @PostMapping("/ask-ai")
    public ResponseEntity<String> askAI(HttpSession session) {
        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        spill.Parti parti = (spill.Parti) session.getAttribute("parti");
        String bruker = (String) session.getAttribute("bruker");

        // If no game in session, check if there's an active PvP game for this user
        if (parti == null && bruker != null) {
            com.example.sjakkwebapp.model.FlerspillerParti match = spillService.getMatch(bruker);
            if (match != null) {
                parti = match.parti;
            }
        }

        if (parti == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No active game.");
        }

        spill.Trekk siste = parti.getSisteTrekk();
        boolean hvitITrekket = (siste == null) || (siste.getBrikke().getFarge() == brikke.Farge.SVART);
        String fen = parti.getBrett().stillingTilFEN(hvitITrekket);
        // String bestMove = aiService.getBestMoveFromStockfish(fen);
        String bestMove = "e2e4"; // For testing - The agent decides when to use the tool.

        try {
            String aiResponse = aiService.askAzureAI(fen, bestMove);
            return ResponseEntity.ok(aiResponse);
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Request failed: " + e.getMessage());
        }
    }
    
}
