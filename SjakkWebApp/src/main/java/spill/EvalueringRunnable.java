package spill;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import brett.Rute;
import brikke.Bonde;
import brikke.Brikke;
import brikke.Farge;
import util.Utils;

public class EvalueringRunnable implements Runnable {

	private final int i;
	private final Parti parti;
	private final Brikke brikke;
	private final Spiller spiller;
	private final Farge farge;
	
	
	public EvalueringRunnable(int i, Parti parti, Brikke brikke, Spiller spiller) {
		this.i = i;
		this.parti = parti;
		this.brikke = brikke;
		this.spiller = spiller;
		this.farge = brikke.getFarge();
	}
	
	@Override
	public void run() {
			
		int besteEvaluering;
		List<Integer> besteTrekk = new ArrayList<Integer>();
		
		List<Brikke> spillersBrikker;
		List<Brikke> motspillersBrikker;
		
		if (farge == Farge.HVIT) {
			besteEvaluering = Utils.MINVERDI;
			spillersBrikker = parti.getBrikkerHvit();
			motspillersBrikker = parti.getBrikkerSvart();
			
		} else {
			besteEvaluering = Utils.MAKSVERDI;
			spillersBrikker = parti.getBrikkerSvart();
			motspillersBrikker = parti.getBrikkerHvit();
		}
		
		int antallBrikker = spillersBrikker.size();
		int antallTrekk = 0;
		
		// Teller lovlige trekk for hver spiller.
		for (Brikke b : spillersBrikker) antallTrekk += b.getLovligeTrekk().size();
		for (Brikke b : motspillersBrikker) antallTrekk += b.getLovligeTrekk().size();
		
		int dynamiskDybde = spiller.getDybde();
		
		if (dynamiskDybde < 0) { // Statisk dybde settes med negativt fortegn.
			dynamiskDybde = -dynamiskDybde;
		} else if (antallBrikker <= 4 && antallTrekk <= 30 // Mattangrep.
				&& motspillersBrikker.size() == 1
				&& !spillersBrikker.stream().anyMatch(b -> b instanceof Bonde)) {
			dynamiskDybde += 3;
		} else if (antallBrikker <= 4 && antallTrekk <= 45) {
			dynamiskDybde += 2;
		} else if (antallBrikker < 16 && antallTrekk <= 60) {
			dynamiskDybde++;
		}
		
		// Spiller hvert lovlige trekk for brikke.
		for (int j = 0, trekk = brikke.getLovligeTrekk().size(); j < trekk; j++) {
			
			// Ny kopi som trekket skal spilles på.
			Parti partiKopi = new Parti(parti);
			Brikke brikke = farge == Farge.HVIT ? partiKopi.getBrikkerHvit().get(i) : partiKopi.getBrikkerSvart().get(i);			
			Rute rute = brikke.kopierTrekk(this.brikke, j);
			Spillerlogikk logikk = new Spillerlogikk(partiKopi, brikke, rute);
			
			int verdiAktivtTrekk = logikk.evaluerTrekk(dynamiskDybde);
			
			// Hvis verdidifferansen er mer i spillers favør lagres indeksene til trekket.
			if (farge == Farge.HVIT && verdiAktivtTrekk > besteEvaluering
					|| farge == Farge.SVART && verdiAktivtTrekk < besteEvaluering) {
				
				besteEvaluering = verdiAktivtTrekk;
				besteTrekk.clear();
				besteTrekk.add(j);
				
			} else if (verdiAktivtTrekk == besteEvaluering) {
				besteTrekk.add(j);
			}
			
		}
		if (besteTrekk.size() == 0) {
			spiller.settInnTrekk(besteEvaluering, 0, i);
		} else {
			Random rand = new Random();
			int tilfeldigTrekk = rand.nextInt(besteTrekk.size());
			int ruteIndeks = besteTrekk.get(tilfeldigTrekk);
			spiller.settInnTrekk(besteEvaluering, ruteIndeks, i);
		}

	}

}
