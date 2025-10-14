package spill;

import java.util.ArrayList;
import java.util.List;

import brett.Brett;
import brett.ChessBoard;
import brett.Rute;
import brikke.Bonde;
import brikke.Brikke;
import brikke.Dronning;
import brikke.Konge;
import brikke.Loeper;
import brikke.Taarn;
import brikke.Farge;
import brikke.Hest;

import com.example.sjakkwebapp.controller.SpillController;

public class Parti {
	
	private final Brett brett = new Brett();
	private final List<Brikke> brikkerHvit = new ArrayList<Brikke>(16);
	private final List<Brikke> brikkerSvart = new ArrayList<Brikke>(16);
	private Trekk sisteTrekk;
	private int femtiTrekk;
	
	private final static List<String> stillinger = new ArrayList<String>();
	private final static List<String> stillingerRepetert = new ArrayList<String>();
	private static String PNG;

	/**
	 * Brikker opprettes og plasseres p� brettet.
	 */
	public Parti() {

		new Konge(brett.finnRute(5, 1), Farge.HVIT, this);
		new Konge(brett.finnRute(5, 8), Farge.SVART, this);
		
		new Dronning(brett.finnRute(4, 1), Farge.HVIT, this);
		new Dronning(brett.finnRute(4, 8), Farge.SVART, this);
		
		for (int i = 1; i < 9; i+=7) {
			new Taarn(brett.finnRute(i, 1), Farge.HVIT, this);
			new Taarn(brett.finnRute(i, 8), Farge.SVART, this);
		}
		
		for (int i = 3; i < 7; i+=3) {
			new Loeper(brett.finnRute(i, 1), Farge.HVIT, this);
			new Loeper(brett.finnRute(i, 8), Farge.SVART, this);
		}
		
		for (int i = 2; i < 8; i+=5) {
			new Hest(brett.finnRute(i, 1), Farge.HVIT, this);
			new Hest(brett.finnRute(i, 8), Farge.SVART, this);
		}
		
		for (int i = 1; i < 9; i++) {
			new Bonde(brett.finnRute(i, 2), Farge.HVIT, this);
			new Bonde(brett.finnRute(i, 7), Farge.SVART, this);
		}
	
	}
	
	/**
	 * Kopierer parti, brett, brikker.
	 * Tar vare p� siste trekk og teller for 50-trekk.
	 * @param parti
	 */
	public Parti (Parti parti) {
		
		this.sisteTrekk = parti.sisteTrekk;
		this.femtiTrekk = parti.femtiTrekk;
		
		for (Brikke b : parti.brikkerHvit) {
			b.nyInstanse(
					this.brett.finnRute(b.getRute().getX(), b.getRute().getY()),
					Farge.HVIT,
					this
					);
		}
		for (Brikke b : parti.brikkerSvart) {
			b.nyInstanse(
					this.brett.finnRute(b.getRute().getX(), b.getRute().getY()),
					Farge.SVART,
					this
					);
		}
		
	}
	
	public Brett getBrett() {
		return brett;
	}
	
	public List<Brikke> getBrikkerHvit() {
		return brikkerHvit;
	}
	
	public List<Brikke> getBrikkerSvart() {
		return brikkerSvart;
	}
	
	public List<String> getStillingerRepetert() {
		return stillingerRepetert;
	}
	
	public int getFemtiTrekk() {
		return femtiTrekk;
	}
	
	/**
	 * Antall trekk f�r det har g�tt 50 trekk blir tilbakestilt til 0.
	 */
	public void tilbakestillFemtiTrekk() {
		femtiTrekk = 0;
	}
	
	public Trekk getSisteTrekk() {
		return sisteTrekk;
	}
	
	public String getPNG() {
		return PNG;
	}
	
	/**
	 * Legger tekst til PNG.
	 * @param trekk som PNG
	 */
	private void leggTilPNG(String trekk) {
		PNG += trekk + " ";
	}
	
	/**
	 * Oppretter et nytt brett.
	 */
	public void toemBrett() {
		
		brikkerHvit.clear();
		brikkerSvart.clear();
		brett.fjernBrikker();
		
	}
	
	/**
	 * Nytt trekk spilles helt til en av betingelsene for at spillet er ferdig er oppfylt.
	 * @param hvit spiller
	 * @param svart spiller
	 * @return antall trekk
	 */
	public int spill(Spiller hvit, Spiller svart) {
		
		stillinger.clear();
		stillingerRepetert.clear();
		PNG = "";
		
        //ChessBoard chessBoard = new ChessBoard(this, hvit, svart);
		
		int i = 1;
		while (true) {
			
			leggTilPNG(i + ".");
			
			for (Brikke b : brikkerHvit) b.finnLovligeTrekk();
			for (Brikke b : brikkerSvart) b.finnLovligeTrekk();
			if (partiFerdig(Farge.HVIT)) break;

			Trekk trekk = hvit.trekk();
			
			leggTilPNG(trekk.toPGN());
			//chessBoard.repaint();
			String trekkStr = trekk.getStartPos().toString() + "-" + trekk.getNyPos().toString();
			SpillController.makeAIMove(trekkStr);
			if (trekk.toPGN().equals("O-O")) SpillController.makeAIMove("h1-f1");
			else if (trekk.toPGN().equals("O-O-O")) SpillController.makeAIMove("a1-d1");
			
			for (Brikke b : brikkerHvit) b.finnLovligeTrekk();
			for (Brikke b : brikkerSvart) b.finnLovligeTrekk();
			if (partiFerdig(Farge.SVART)) break;
			
			trekk = svart.trekk();
			
			leggTilPNG(trekk.toPGN());
			//chessBoard.repaint();
			trekkStr = trekk.getStartPos().toString() + "-" + trekk.getNyPos().toString();
			SpillController.makeAIMove(trekkStr);
			if (trekk.toPGN().equals("O-O")) SpillController.makeAIMove("h8-f8");
			else if (trekk.toPGN().equals("O-O-O")) SpillController.makeAIMove("a8-d8");
			
			System.out.print(".");
			i++;
			
		}
		System.out.println();
		return i;
		
	}
	
	/**
	 * Nytt trekk utf�res, og aktuelle ruter og brikker f�r nye verdier. 
	 * @param trekk
	 */
	public void nyttTrekk(Trekk trekk) {
		
		Rute nyRute = trekk.getNyPos();
		
		if (nyRute.harBrikke()) {
			
			if (nyRute.getBrikke().getFarge() == Farge.HVIT) brikkerHvit.remove(nyRute.getBrikke());
			else brikkerSvart.remove(nyRute.getBrikke());
			
			tilbakestillFemtiTrekk();
			
		} else femtiTrekk++;
		
		trekk.getBrikke().setRute(nyRute);
		nyRute.setBrikke(trekk.getBrikke());
		trekk.getStartPos().setBrikke(null);
		
		sisteTrekk = trekk;
		
	}
	
	/**
	 * Sjekker om spiller st�r i sjakk etter trekk.
	 *  - Utf�rer trekket.
	 *  - Finner rute til spillers konge, og g�r gjennom motspillers brikker.
	 *  - Hvis en brikke angriper kongerute st�r konge i sjakk.
	 *  - Reverserer trekket igjen.
	 * @param brikke
	 * @param nyRute
	 * @param en passant-trekk
	 * @return sjakk
	 */
	public boolean sjakk(Brikke brikke, Rute nyRute, boolean enPassant) {
		
		Brikke utslaattBrikke = nyRute.getBrikke();
		
		nyRute.setBrikke(brikke);
		brikke.getRute().setBrikke(null);
		
		if (enPassant) {
			utslaattBrikke = sisteTrekk.getNyPos().getBrikke();
			sisteTrekk.getNyPos().setBrikke(null);
		}

		Rute kongerute;
		List<Brikke> brikker;
		
		if (brikke.getFarge() == Farge.HVIT) {
			kongerute = brikke.getVerdi() == 80 ? nyRute : brikkerHvit.get(0).getRute();
			brikker = brikkerSvart;
		} else {
			kongerute = brikke.getVerdi() == 80 ? nyRute : brikkerSvart.get(0).getRute();
			brikker = brikkerHvit;
		}

		boolean sjakk = false;

		for (Brikke b : brikker) {
			
			if (!b.equals(utslaattBrikke) && b.angriperRute(kongerute)) {
				sjakk = true;
				break;
			}
			
		}
		
		// Reversering av trekk.
		nyRute.setBrikke(utslaattBrikke);
		brikke.getRute().setBrikke(brikke);
		
		if (enPassant) {
			nyRute.setBrikke(null);
			sisteTrekk.getNyPos().setBrikke(utslaattBrikke);
		}
		
		return sjakk;
		
	}
	
	/**
	 * Kontrollerer om stilling har forekommet flere ganger, og lagrer stilling.
	 * @param spiller i trekket
	 * @return stilling gjentatt tre ganger
	 */
	private boolean trekkgjentakelse(Farge farge) {
		
		String stilling = brett.stillingTilString() + farge.toString();
		
		for (String s : stillingerRepetert) {
			if (s.equals(stilling)) {
				return true;
			}
		}
		
		for (String s : stillinger) {
			if (s.equals(stilling)) {
				stillingerRepetert.add(stilling);
				return false;
			}
		}
		
		stillinger.add(stilling);
		return false;
		
	}
	
	/**
	 * Kontrollerer om partiet er slutt gjennom 50-trekksregelen, trekkgjentakelse, matt eller patt.
	 * @param spiller i trekket
	 * @return parti ferdig
	 */
	private boolean partiFerdig(Farge farge) {
		
		if (femtiTrekk > 100) {
			leggTilPNG("\n1/2 - 1/2 (50-trekksregel)");
			return true;
		}
		
		if (trekkgjentakelse(farge)) {
			leggTilPNG("\n1/2 - 1/2 (trekkgjentakelse)");
			return true;
		}
		
		List<Brikke> spillerbrikker = farge == Farge.HVIT ? brikkerHvit : brikkerSvart;
		
		for (Brikke b : spillerbrikker) {
			if (b.getLovligeTrekk().size() != 0) return false;
		}
		
		List<Brikke> motspillerbrikker;
		Rute kongerute;
		String resultat;
		
		if (farge == Farge.HVIT) {
			motspillerbrikker = brikkerSvart;
			kongerute = brikkerHvit.get(0).getRute();
			resultat = "\n0 - 1";
		} else {
			motspillerbrikker = brikkerHvit;
			kongerute = brikkerSvart.get(0).getRute();
			resultat = "\n1 - 0";
		}
		
		for (Brikke b : motspillerbrikker) {
			if (b.angriperRute(kongerute)) {
				leggTilPNG(resultat);
				return true;
			}
		}
		
		leggTilPNG("\n1/2 - 1/2 (patt)");
		return true;
		
	}
	
	/**
	 * Flytter t�rn til korrekt posisjon ved rokadetrekk.
	 * @param x-koordinat konge
	 * @param y-koordinat konge
	 */
	public void utfoerRokade(int x, int y) {
		
		if (x == 7) {
			Brikke taarn = brett.finnRute(8, y).getBrikke();
			Rute taarnRute = brett.finnRute(6, y);
			taarn.setRute(taarnRute);
			taarnRute.setBrikke(taarn);
			brett.finnRute(8, y).setBrikke(null);
		}	
		else {
			Brikke taarn = brett.finnRute(1, y).getBrikke();
			Rute taarnRute = brett.finnRute(4, y);
			taarn.setRute(taarnRute);
			taarnRute.setBrikke(taarn);
			brett.finnRute(1, y).setBrikke(null);
		}
		
	}
	
	/**
	 * Bonde promoteres til en annen brikke.
	 * @param bonde
	 */
	public void promoter(Bonde bonde) {
		
		switch (bonde.getPromotering()) {
		case "=Q":
			new Dronning(bonde.getRute(), bonde.getFarge(), this);
			break;
		case "=R":
			new Taarn(bonde.getRute(), bonde.getFarge(), this);
			break;
		case "=B":
			new Loeper(bonde.getRute(), bonde.getFarge(), this);
			break;
		case "=N":
			new Hest(bonde.getRute(), bonde.getFarge(), this);
			break;
		default:
			new Dronning(bonde.getRute(), bonde.getFarge(), this);
			System.err.println("Ugyldig promoteringsnotasjon.");
		}
		
		if (bonde.getFarge() == Farge.HVIT) brikkerHvit.remove(bonde);
		else brikkerSvart.remove(bonde);
		
	}
	
	/**
	 * Sletter utsl�tt bonde fra brikker og rute.
	 */
	public void enPassant() {
		
		Rute faktiskRute = brett.finnRute(sisteTrekk.getNyPos().getX(), sisteTrekk.getNyPos().getY());
		Brikke bonde = faktiskRute.getBrikke();
		faktiskRute.setBrikke(null);
		if (bonde.getFarge() == Farge.HVIT) brikkerHvit.remove(bonde);
		else brikkerSvart.remove(bonde);
		
	}

}
