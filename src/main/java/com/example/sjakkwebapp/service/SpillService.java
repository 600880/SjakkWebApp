package com.example.sjakkwebapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import brikke.Farge;
import brikke.Brikke;
import spill.Trekk;
import spill.Spiller;
import spill.Parti;
import com.example.sjakkwebapp.model.FlerspillerParti;

import java.util.concurrent.ExecutorService;

@Service
public class SpillService {

    @Autowired
    private SSEService sseService;

    @Autowired
    private ExecutorService gameExecutor;

    private final Map<String, FlerspillerParti> activePvPMatches = new ConcurrentHashMap<>();

    public FlerspillerParti getMatch(String bruker) {
        return activePvPMatches.get(bruker);
    }

    public Parti createMatch(String hvit, String svart) {
        FlerspillerParti match = new FlerspillerParti(hvit, svart);
        activePvPMatches.put(hvit, match);
        activePvPMatches.put(svart, match);
        Parti parti = match.parti;
        parti.setMoveNotifier(sseService);
        return parti;
    }

    public void endMatch(String bruker) {
        FlerspillerParti match = activePvPMatches.remove(bruker);
        if (match != null) {
            activePvPMatches.remove(match.hvit);
            activePvPMatches.remove(match.svart);
        }
    }

    public String getOpponent(String bruker) {
        FlerspillerParti match = activePvPMatches.get(bruker);
        if (match == null) return null;
        return bruker.equals(match.hvit) ? match.svart : match.hvit;
    }

    public String handleMove(String bruker, String from, String to, Parti sessionParti, Spiller cpu) {
        FlerspillerParti match = getMatch(bruker);
        if (match != null) {
            return handlePvPMove(bruker, from, to, match);
        } else {
            Farge cpuColor = (cpu != null) ? cpu.getFarge() : Farge.SVART;
            return handleCpuMove(from, to, sessionParti, cpu, cpuColor);
        }
    }

    private String handlePvPMove(String bruker, String from, String to, FlerspillerParti match) {
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

    private String handleCpuMove(String from, String to, Parti parti, Spiller cpu, Farge cpuColor) {
        if (parti == null) return "No active game.";

        Farge currentTurn = getCurrentTurn(parti);
        Farge userColor = (cpuColor == Farge.HVIT) ? Farge.SVART : Farge.HVIT;

        if (currentTurn != userColor) return "Not your turn (CPU's turn)";

        Brikke brikke = getBrikke(parti, from);
        if (brikke != null && brikke.getFarge() == currentTurn) {
            executeMove(parti, brikke, to);
            if (cpu != null) {
                gameExecutor.submit(() -> {
                    parti.spillTrekk(cpu, cpuColor);
                });
            }
            return "OK";
        }

        return "Invalid move";
    }


    /* Helper methods */

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
