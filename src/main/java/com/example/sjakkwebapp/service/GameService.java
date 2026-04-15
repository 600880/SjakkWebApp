package com.example.sjakkwebapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import brikke.Farge;
import brikke.Brikke;
import brett.Rute;
import spill.Trekk;
import spill.Spiller;
import spill.Parti;

@Service
public class GameService {

    @Autowired
    private SSEService sseService;

    public static class PvPMatch {
        public Parti parti;
        public String hvit;
        public String svart;

        PvPMatch(String hvit, String svart, SSEService sseService) {
            this.hvit = hvit;
            this.svart = svart;
            this.parti = new Parti();
            this.parti.setMoveNotifier(sseService);
        }
    }

    private final Map<String, PvPMatch> activePvPMatches = new ConcurrentHashMap<>();

    public PvPMatch getMatch(String bruker) {
        return activePvPMatches.get(bruker);
    }

    public PvPMatch createMatch(String hvit, String svart) {
        PvPMatch match = new PvPMatch(hvit, svart, sseService);
        activePvPMatches.put(hvit, match);
        activePvPMatches.put(svart, match);
        return match;
    }

    public void endMatch(String bruker) {
        PvPMatch match = activePvPMatches.remove(bruker);
        if (match != null) {
            activePvPMatches.remove(match.hvit);
            activePvPMatches.remove(match.svart);
        }
    }

    public String handleMove(String bruker, String from, String to, Parti sessionParti, Spiller cpuSvart) {
        PvPMatch match = getMatch(bruker);
        boolean isPvP = (match != null);
        Parti parti = isPvP ? match.parti : sessionParti;

        if (parti == null) {
            return "No active game.";
        }

        // Determine whose turn it is
        Trekk siste = parti.getSisteTrekk();
        Farge currentTurn = (siste == null) ? Farge.HVIT : (siste.getBrikke().getFarge() == Farge.HVIT ? Farge.SVART : Farge.HVIT);

        // Verify it's the user's turn and they own the color
        if (isPvP) {
            boolean isWhite = bruker.equals(match.hvit);
            if (isWhite && currentTurn != Farge.HVIT) return "Not your turn (Black's turn)";
            if (!isWhite && currentTurn != Farge.SVART) return "Not your turn (White's turn)";
        } else {
            if (currentTurn != Farge.HVIT) return "Not your turn (CPU's turn)";
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
                sseService.notifyUser(opponent, "move", from + "-" + to);
            } else if (cpuSvart != null) {
                // CPU Counter-move
                final Parti cpuParti = parti;
                new Thread(() -> {
                    cpuParti.spillTrekk(cpuSvart, Farge.SVART);
                }).start();
            }

            return "OK";
        }

        return "Invalid move";
    }
}
