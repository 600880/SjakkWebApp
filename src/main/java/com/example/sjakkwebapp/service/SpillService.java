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
public class SpillService {

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
            if (match.parti != null) {
                match.parti.stop();
            }
            activePvPMatches.remove(match.hvit);
            activePvPMatches.remove(match.svart);
        }
    }

    public String handleMove(String bruker, String from, String to, Parti sessionParti, Spiller cpuSvart) {
        PvPMatch match = getMatch(bruker);
        return (match != null)
            ? handlePvPMove(bruker, from, to, match)
            : handleCpuMove(from, to, sessionParti, cpuSvart);
    }

    private String handlePvPMove(String bruker, String from, String to, PvPMatch match) {
        Parti parti = match.parti;
        if (parti == null) return "No active game.";

        Farge currentTurn = getCurrentTurn(parti);

        boolean isWhite = bruker.equals(match.hvit);
        if (isWhite && currentTurn != Farge.HVIT) return "Not your turn (Black's turn)";
        if (!isWhite && currentTurn != Farge.SVART) return "Not your turn (White's turn)";

        Brikke brikke = getBrikke(parti, from);
        if (brikke != null && brikke.getFarge() == currentTurn) {
            executeMove(parti, brikke, to);
            String opponent = bruker.equals(match.hvit) ? match.svart : match.hvit;
            sseService.notifyUser(opponent, "move", from + "-" + to);
            return "OK";
        }

        return "Invalid move";
    }

    private String handleCpuMove(String from, String to, Parti parti, Spiller cpuSvart) {
        if (parti == null) return "No active game.";

        Farge currentTurn = getCurrentTurn(parti);
        if (currentTurn != Farge.HVIT) return "Not your turn (CPU's turn)";

        Brikke brikke = getBrikke(parti, from);
        if (brikke != null && brikke.getFarge() == currentTurn) {
            executeMove(parti, brikke, to);
            if (cpuSvart != null) {
                new Thread(() -> {
                    parti.spillTrekk(cpuSvart, Farge.SVART);
                }).start();
            }
            return "OK";
        }

        return "Invalid move";
    }


    // *** Helper methods ***

    private Farge getCurrentTurn(Parti parti) {
        Trekk siste = parti.getSisteTrekk();
        return (siste == null) ? Farge.HVIT
        : (siste.getBrikke().getFarge() == Farge.HVIT ? Farge.SVART : Farge.HVIT);
    }

    private Brikke getBrikke(Parti parti, String square) {
        int x = util.Utils.charToInt(square.charAt(0));
        int y = Character.getNumericValue(square.charAt(1));
        return parti.getBrett().finnRute(x, y).getBrikke();
    }

    private void executeMove(Parti parti, Brikke brikke, String to) {
        int tx = util.Utils.charToInt(to.charAt(0));
        int ty = Character.getNumericValue(to.charAt(1));
        brikke.flytt(parti.getBrett().finnRute(tx, ty));
    }
}
