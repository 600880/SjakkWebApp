package com.example.sjakkwebapp.controller;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Value("${azure.ai.key:}")
    private String azureAiKey;
	
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

    private String getBestMoveFromStockfish(String fen) {
        String url = "https://stockfish-app.happyfield-52deb28b.eastus.azurecontainerapps.io/analyze";
        String jsonPayload = "{\"fen\": \"" + fen + "\", \"depth\": 15}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                String move = "not found";
                String score = "unknown";

                Pattern movePattern = Pattern.compile("\"best_move\":\\s*\"(.*?)\"");
                Matcher moveMatcher = movePattern.matcher(body);
                if (moveMatcher.find()) {
                    move = moveMatcher.group(1);
                }

                Pattern scorePattern = Pattern.compile("\"score_cp\":\\s*(-?\\d+)");
                Matcher scoreMatcher = scorePattern.matcher(body);
                if (scoreMatcher.find()) {
                    score = scoreMatcher.group(1);
                }

                return move + " (Score CP: " + score + ")";
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Stockfish request failed: " + e.getMessage());
        }
        return "not found";
    }

    @PostMapping("/ask-ai")
    public ResponseEntity<String> askAI(HttpSession session) {
        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        String bruker = (String) session.getAttribute("bruker");
        spill.Parti parti = (spill.Parti) session.getAttribute("parti");
        PvPMatch match = activePvPMatches.get(bruker);
        if (match != null) {
            parti = match.parti;
        }

        if (parti == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No active game.");
        }

        spill.Trekk siste = parti.getSisteTrekk();
        boolean hvitITrekket = (siste == null) || (siste.getBrikke().getFarge() == brikke.Farge.SVART);
        String fen = parti.getBrett().stillingTilFEN(hvitITrekket);
        String bestMove = getBestMoveFromStockfish(fen);

        // Azure AI Details (Provided by user)
        String url = "https://dstub-8074-resource.services.ai.azure.com/api/projects/dstub-8074/applications/agent-torr/protocols/openai/responses?api-version=2025-11-15-preview";
        
        // Use the injected key from application.properties, fallback to env or hardcoded for safety
        String apiKey = azureAiKey;
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv("AZURE_AI_KEY");
        }

        HttpClient client = HttpClient.newHttpClient();
        
        // Updated for Azure AI Responses API, now sending FEN and best move
        String prompt = "FEN: " + fen + ". Stockfish's best move: " + bestMove;
        String json = "{" +
                "\"input\": [" +
                "  {\"role\": \"user\", \"content\": \"" + prompt + "\"}" +
                "]" +
                "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("api-key", apiKey) // Azure typically uses api-key
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Extract "text" content using regex
                Pattern pattern = Pattern.compile("\"text\":\\s*\"(.*?)\"");
                Matcher matcher = pattern.matcher(response.body());
                if (matcher.find()) {
                    return ResponseEntity.ok(matcher.group(1));
                }
                return ResponseEntity.ok(response.body());
            } else {
                return ResponseEntity.status(response.statusCode()).body("Error from Azure AI: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Request failed: " + e.getMessage());
        }
    }
    
}