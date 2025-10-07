package spill;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

//import javax.swing.JOptionPane;

import brett.ChessBoard;
import brett.Rute;
import brikke.Brikke;
//import brikke.Bonde;
import brikke.Farge;
import util.Utils;
import com.example.sjakkapp.*;

public class Spiller {
	
	private final String navn;
	private final Farge farge;
	private int dybde;
	private final Parti parti;
	private final List<Brikke> spillerBrikker;
	private final Random rand = new Random();
	private final int[][] indeksTabell;
	private int evaluering;
	private boolean CPU;
	private ChessBoard chessBoard;
	
	
	public Spiller(String navn, Farge farge, int dybde, boolean CPU, Parti parti) {
		this.navn = navn;
		this.farge = farge;
		this.dybde = dybde;
		this.CPU = CPU;
		this.parti = parti;
		spillerBrikker = farge == Farge.HVIT ? parti.getBrikkerHvit() : parti.getBrikkerSvart();
		indeksTabell = new int[2][spillerBrikker.size()];
	}
	
	public Trekk trekk() {
		return CPU ? trekkCPU() : trekkBruker();
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
	 * Bruker velger trekk.
	 * @return trekk
	 */
	private Trekk trekkBruker() {

		// Tegn opp brett.
		//if (parti.getSisteTrekk() != null) System.out.println(parti.getSisteTrekk().toString());
		
		boolean fortsett = true;
		Trekk trekk = null;
		
		while (fortsett) {
			// Hent x og y.
			int x = chessBoard.getX();
			int y = chessBoard.getY();
			
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
			
			/*int x;
			int y;
			
			try {
				String koordinat = JOptionPane.showInputDialog("Brikke");
				x = Utils.charToInt(koordinat.charAt(0));
				y = Character.getNumericValue(koordinat.charAt(1));
			} catch (Exception e) {
				continue;
			}
			*/
			
			if (x < 1 || x > 8 || y < 1 || y > 8) continue;
			
					
			Rute rute = parti.getBrett().finnRute(x, y);
			if (!rute.harBrikke() || !(rute.getBrikke().getFarge() == farge)) continue;

			Brikke brikke = rute.getBrikke();

			// Marker rute og hent ny x og y.
			chessBoard.repaint();
			
			/*
			try {
				String koordinat = JOptionPane.showInputDialog(brikke.getClass().getSimpleName()
						+ " [" + rute.toString() + "]\nNy Rute");
				x = Utils.charToInt(koordinat.charAt(0));
				y = Character.getNumericValue(koordinat.charAt(1));
			} catch (Exception e) {
				continue;
			}
			*/
			
			int tmpX = x;
			int tmpY = y;
			
			while (tmpX == x && tmpY == y) {
	            try {
	                Thread.sleep(10);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
				x = chessBoard.getX();
				y = chessBoard.getY();
			}
			
			if (x < 1 || x > 8 || y < 1 || y > 8) continue;
			
			rute = parti.getBrett().finnRute(x, y);
			for (Rute r : brikke.getLovligeTrekk()) {
				if (r.equals(rute)) {
					trekk = brikke.flytt(rute);
					fortsett = false;
					break;
				}
			}
			// Fjern markering
			chessBoard.setX(-1);
			chessBoard.setY(-1);
			chessBoard.repaint();
		}
		System.out.println(trekk.toString());
		return trekk;
		
	}
	
	/**
	 * Strategi for � velge trekk.
	 * @return trekk
	 */
	private Trekk trekkCPU() {
		
		// EvalueringRunnable evaluerer hver brikke "i", og lagrer indeks til beste trekk i indeksTabell posisjon "i".
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < spillerBrikker.size(); i++) {
			Future<?> f = SjakkappApplication.traadsamling.submit(new EvalueringRunnable(i, parti, spillerBrikker.get(i), this));
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
	
	public void setDybde(int dybde) {
		this.dybde = dybde;
	}
	
	public void setCPU(boolean CPU) {
		this.CPU = CPU;
	}
	
	public void setChessBoard(ChessBoard chessBoard) {
		this.chessBoard = chessBoard;
	}
	
	@Override
	public String toString() {
		return navn + "[" + farge + "]";
	}

}
