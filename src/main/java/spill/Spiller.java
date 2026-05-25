package spill;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

import com.example.sjakkwebapp.*;

import brett.Rute;
import brikke.Brikke;
import brikke.Farge;
import util.Utils;

public class Spiller {
	
	private final String navn;
	private final Farge farge;
	private int dybde;
	private final Parti parti;
	private final List<Brikke> spillerBrikker;
	private final Random rand = new Random();
	private final int[][] indeksTabell;
	private int evaluering;
	
	
	public Spiller(String navn, Farge farge, int dybde, Parti parti) {
		this.navn = navn;
		this.farge = farge;
		this.dybde = dybde;
		this.parti = parti;
		spillerBrikker = farge == Farge.HVIT ? parti.getBrikkerHvit() : parti.getBrikkerSvart();
		indeksTabell = new int[2][spillerBrikker.size()];
	}
	
	/**
	 * Direkte input for trekk.
	 * @param startrute
	 * @param ny rute
	 * @return trekk
	 */
	/*private Trekk trekkInput(String a, String b) {
		
		int x = Utils.charToInt(a.charAt(0));
		int y = Character.getNumericValue(a.charAt(1));
		Brikke brikke = parti.getBrett().finnRute(x, y).getBrikke();
		
		x = Utils.charToInt(b.charAt(0));
		y = Character.getNumericValue(b.charAt(1));
		Rute rute = parti.getBrett().finnRute(x, y);
		
		if (b.contains("=")) {
			((Bonde) brikke).setPromotering(b.substring(2,3));
		}
		
		return brikke.flytt(rute);
		
	}*/
	
	/**
	 * Strategi for å velge trekk.
	 * @return trekk
	 */
	public Trekk trekk() {
		
		// EvalueringRunnable evaluerer hver brikke "i", og lagrer indeks til beste trekk i indeksTabell posisjon "i".
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < spillerBrikker.size(); i++) {
			Future<?> f = SjakkWebAppApplication.traadsamling.submit(new EvalueringRunnable(i, parti, spillerBrikker.get(i), this));
			futures.add(f);
		}
		for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Throwable thrown) {
            }
		}
		
		int besteEvaluering = farge == Farge.HVIT ? Utils.MINVERDI : Utils.MAKSVERDI;
		List<Integer> besteTrekk = new ArrayList<Integer>();
		
		// Finner beste trekk fra indeksTabell.
		for (int i = 0; i < spillerBrikker.size(); i++) {
			
			int verdi = indeksTabell[0][i];
		    if (verdi == besteEvaluering) {
		    	
		    	besteTrekk.add(i);
		    	besteTrekk.add(indeksTabell[1][i]);
		    	
		    } else if (farge == Farge.HVIT && verdi > besteEvaluering
		    		|| farge == Farge.SVART && verdi < besteEvaluering) {
		    	
		    	besteEvaluering = verdi;
				besteTrekk.clear();
				besteTrekk.add(i);
				besteTrekk.add(indeksTabell[1][i]);
				
		    }
				
		}
		
		int tilfeldigTrekk = rand.nextInt(besteTrekk.size() / 2) * 2;
		int brikkeIndeks = besteTrekk.get(tilfeldigTrekk);
		int ruteIndeks = besteTrekk.get(tilfeldigTrekk + 1);
		
		Brikke brikke = spillerBrikker.get(brikkeIndeks);
		Rute rute = brikke.getLovligeTrekk().get(ruteIndeks);
		Trekk trekk = brikke.flytt(rute);
		
		evaluering = indeksTabell[0][brikkeIndeks] / 8;
		
		return trekk;
		
	}
	
	/**
	 * Lagrer verdievaluering for et trekk.
	 * @param verdi
	 * @param ruteIndeks
	 * @param brikkeIndeks
	 */
	public void settInnTrekk(int verdi, int ruteIndeks, int brikkeIndeks) {
		indeksTabell[0][brikkeIndeks] = verdi;
		indeksTabell[1][brikkeIndeks] = ruteIndeks;
	}
	
	public int getDybde() {
		return dybde;
	}
	
	public int getEvaluering() {
		return evaluering;
	}
	
	public Farge getFarge() {
		return farge;
	}
	
	public void setDybde(int dybde) {
		this.dybde = dybde;
	}
	
	@Override
	public String toString() {
		return navn + "[" + farge + "]";
	}

}
