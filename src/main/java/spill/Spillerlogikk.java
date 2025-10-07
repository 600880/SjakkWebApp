package spill;

import java.util.List;
import java.util.Random;

import brett.Brett;
import brett.Rute;
import brikke.Bonde;
import brikke.Brikke;
import brikke.Farge;
import brikke.Hest;
import brikke.Konge;
import brikke.Loeper;
import brikke.Taarn;
import util.Utils;

public class Spillerlogikk {
	
	private final Parti partiKopi;
	private final Brikke brikke;
	private final Rute rute;
	private final int fortegn;
	private final Farge spillerfarge;
	private final Farge motspillerfarge;
	private final Rute motspillersKongerute;
	private final List<Brikke> spillersBrikker;
	private final List<Brikke> motspillersBrikker;

	
	public Spillerlogikk(Parti partiKopi, Brikke brikke, Rute rute) {
		
		this.partiKopi = partiKopi;
		this.brikke = brikke;
		this.rute = rute;
		spillerfarge = brikke.getFarge();
		
		if (spillerfarge == Farge.HVIT) {
			fortegn = 1;
			motspillerfarge = Farge.SVART;
			spillersBrikker = partiKopi.getBrikkerHvit();
			motspillersBrikker = partiKopi.getBrikkerSvart();
		} else {
			fortegn = -1;
			motspillerfarge = Farge.HVIT;
			spillersBrikker = partiKopi.getBrikkerSvart();
			motspillersBrikker = partiKopi.getBrikkerHvit();
		}
		
		motspillersKongerute = motspillersBrikker.get(0).getRute();
		
	}
	
	/**
	 * Utfører trekket og kaller alle evalueringsmetoder.
	 * @param dybde
	 * @return evaluering
	 */
	public int evaluerTrekk(int dybde) {
		
		int verdiAktivtTrekk = 0;
		verdiAktivtTrekk += avbytte();
		verdiAktivtTrekk += distanseTilKonge();
		verdiAktivtTrekk += brikkestrategi();

		// Utfører trekket.
		brikke.flytt(rute);
		
		if (trekkrepetisjon(partiKopi, motspillerfarge)) return 0;
		
		if (dybde > 1) {
			verdiAktivtTrekk += rekursivEvaluering(dybde-2, Utils.MINVERDI, Utils.MAKSVERDI);
		} else {
			verdiAktivtTrekk += sjakk();
			verdiAktivtTrekk += sumBrikker();
			verdiAktivtTrekk += brikkerUnderAngrep();
		}
		
		// Reduserer trekkgjentakelse ved 0-evaluering.
		if (partiKopi.getFemtiTrekk() > 10) verdiAktivtTrekk -= 2 * fortegn;
		else if (partiKopi.getFemtiTrekk() > 6) verdiAktivtTrekk -= 1 * fortegn;
		
		return verdiAktivtTrekk;
		
	}
	
	/**
	 * Spiller rekursivt hvert lovlig trekk for motspiller og returnerer verdi for trekk med best evaluering.
	 * @param dybde
	 * @param alpha
	 * @param beta
	 * @return evaluering
	 */
	private int rekursivEvaluering(int dybde, int alpha, int beta) {
		
		int besteEvaluering = spillerfarge == Farge.HVIT ? Utils.MAKSVERDI : Utils.MINVERDI;
		
		// Oppdaterer liste av lovlige trekk for alle brikker.
		int antallTrekk = 0;
		for (Brikke b : motspillersBrikker) antallTrekk += b.finnLovligeTrekk().size();
		
		// Motspiller har ingen lovlige trekk.
		if (antallTrekk == 0) {
			
			for (Brikke b : spillersBrikker) {
				if (b.angriperRute(motspillersKongerute)) {
					// Sjakk matt - dybde differensierer mellom brikker som har funnet ulike mattmønster.
					return besteEvaluering / 2 + dybde * fortegn;
				}
			}
			return 0; // Patt.
		}
		
		// Sorterer brikkene etter antatt sterkeste trekk, og lagrer indeks til brikke og trekk.
		int[][] indekstabell = sorter(antallTrekk);		
		
		// Spiller alle trekk for alle brikker etter sortert rekkefølge.
		label:
		for (int i = antallTrekk - 1; i >= 0; i--) {
			
			int brikkeindeks = indekstabell[0][i];
			int ruteindeks = indekstabell[1][i];
			
			// Ny kopi som trekket skal spilles på.
			Parti partiKopiRek = new Parti(partiKopi);
			Brikke brikkeKopi = motspillerfarge == Farge.HVIT
				? partiKopiRek.getBrikkerHvit().get(brikkeindeks)
				: partiKopiRek.getBrikkerSvart().get(brikkeindeks);
			
			Rute rute = motspillersBrikker.get(brikkeindeks).getLovligeTrekk().get(ruteindeks);
			Rute ruteKopi = partiKopiRek.getBrett().finnRute(rute.getX(), rute.getY());
			Spillerlogikk logikk = new Spillerlogikk(partiKopiRek, brikkeKopi, ruteKopi);
			
			// Utfører trekket.
			brikkeKopi.flytt(ruteKopi);
			
			// Sjekker for trekkrepetisjon.
			if (dybde > 1 && trekkrepetisjon(partiKopiRek, spillerfarge)) return 0;
			
			int verdiAktivtTrekk;
			
			if (dybde == 0) {
				verdiAktivtTrekk = logikk.sjakk();
				verdiAktivtTrekk += logikk.sumBrikker();
				verdiAktivtTrekk += logikk.brikkerUnderAngrep();
			} else {
				verdiAktivtTrekk = logikk.rekursivEvaluering(dybde-1, alpha, beta);
			}
							
			// Hvis verdidifferansen er mer i spillers favør lagres indeksene til trekket.
			if (motspillerfarge == Farge.HVIT && verdiAktivtTrekk > besteEvaluering
					|| motspillerfarge == Farge.SVART && verdiAktivtTrekk < besteEvaluering) {
				
				besteEvaluering = verdiAktivtTrekk;
			}
			
			// Beskjæring av evalueringstre.
			if (motspillerfarge == Farge.HVIT && verdiAktivtTrekk > alpha) {
				alpha = verdiAktivtTrekk;
			} else if (motspillerfarge == Farge.SVART && verdiAktivtTrekk < beta) {
				beta = verdiAktivtTrekk;
			}
			if (beta <= alpha) break label;
			
		}

		return besteEvaluering;
		
	}
	
	/**
	 * Kontrollerer om stilling allerede har oppstått to ganger.
	 * @param parti
	 * @return stilling nådd to ganger
	 */
	private boolean trekkrepetisjon(Parti parti, Farge farge) {
		
		String stilling = parti.getBrett().stillingTilString() + farge;
		
		for (String s : parti.getStillingerRepetert()) {
			if (s.equals(stilling)) {
				return true;
			}
		}
		return false;
		
	}
	
	/**
	 * Vektlegger trekk som har sjakket motstanders konge med ekstra poeng.
	 * @return evaluering
	 */
	private int sjakk() {
		
		for (Brikke b : spillersBrikker) {
			if (b.angriperRute(motspillersKongerute)) {
				
				for (Brikke b2 : motspillersBrikker) {
					if (b2.finnLovligeTrekk().size() != 0) {
						return 4 * fortegn; // Sjakk, men ikke matt.
					}
				}
				return Utils.MAKSVERDI / 8 * fortegn; // Sjakk matt.
				
			}
		}
		return 0;
		
	}
	
	/**
	 * Summerer verdi for hvite brikker og trekker fra verdi for svarte brikker.
	 * @return beregnet sum
	 */
	private int sumBrikker() {
		
		int verdiHvit = 0;
		int verdiSvart = 0;
		
		for (Brikke b : partiKopi.getBrikkerHvit()) {
			verdiHvit += b.getVerdi();
		}
		for (Brikke b : partiKopi.getBrikkerSvart()) {
			verdiSvart += b.getVerdi();
		}
		return verdiHvit - verdiSvart;
		
	}
	
	/**
	 * Trekker fra verdi for spillers brikke med høyest verdi som står i slag og ikke kan forsvares.
	 * Metode teller ikke forsvarere, eller kontrollerer om forsvarstrekk er lovlig i gitt kontekst.
	 * @return evaluering
	 */
	private int brikkerUnderAngrep() {
		
		// Liste med alle ruter som spiller har brikker på, untatt kongerute.
		int antallBrikker = spillersBrikker.size() - 1;
		Rute[] brikkeruter = new Rute[antallBrikker];
		
		for (int i = 0; i < antallBrikker; i++) {
			brikkeruter[i] = spillersBrikker.get(i+1).getRute();
		}
		
		int verdivurdering = 0;
		
		for (Brikke b : motspillersBrikker) {
			label:
			for (int i = 0; i < antallBrikker; i++) {
				
				Rute r = brikkeruter[i];
				
				if (b.angriperRute(r)
						&& r.getBrikke().getVerdi() > verdivurdering
						&& !partiKopi.sjakk(b, r, false)) {
					
					// En brikke står i slag og har høyere verdi enn tidligere brikke i slag.
					Brikke brikkeISlag = r.getBrikke();
					
					for (Brikke b2 : spillersBrikker) { // Går gjennom spillers egne brikker.
						
						if (!b2.equals(brikkeISlag) && b2.angriperRute(r)) { // Spillers brikke kan forsvare ruten.
							
							int beregnetVerdi = brikkeISlag.getVerdi() - b.getVerdi();
							if (beregnetVerdi > verdivurdering) {						
								verdivurdering = beregnetVerdi;
							}
							continue label; // Ruten er allerede forsvart.
						}	
						
					}
					verdivurdering = brikkeISlag.getVerdi(); // Spiller har ingen forsvarer.
				}
				
			}
		}
		return verdivurdering * -fortegn;		
		
	}
	
	/**
	 * Vektlegger byttehandler lavere.
	 * @return -1 eller 0
	 */
	private int avbytte() {
		
		boolean avbytte = rute.harBrikke() && !(rute.getBrikke() instanceof Bonde);
		return avbytte ? (-1 * fortegn) : 0;
		
	}
	
	/**
	 * Regner ut distanse mellom brikke og motstanders konge før og etter trekk.
	 * Metode kalles før trekk spilles.
	 * @return 1 eller 0 avhengig om brikke står nærmere.
	 */
	private int distanseTilKonge() {
		
		int x1 = motspillersKongerute.getX();
		int y1 = motspillersKongerute.getY();
		int x2 = brikke.getRute().getX();
		int y2 = brikke.getRute().getY();
		int x3 = rute.getX();
		int y3 = rute.getY();
		
		double distanse = Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
		double nyDistanse = Math.sqrt((y3 - y1) * (y3 - y1) + (x3 - x1) * (x3 - x1));
		
		return nyDistanse + 0.1 < distanse ? (1 * fortegn) : 0;
		
	}
	
	/**
	 * Vektlegger logiske trekk høyere. Metode kalles før trekk spilles.
	 * + Trekk med uflyttede brikker, rokadetrekk.
	 * + Bondetrekk som angriper brikker.
	 * - Bondetrekk forran egen konge.
	 * - Bondetrekk som gir dobbelbonde.
	 * - Tårntrekk på samme fil som egen bonde før sluttspill.
	 * - Trekk med konge før sluttspill.
	 * @return evaluering
	 */
	private int brikkestrategi() {
		
		Random rand = new Random();
		int verdivurdering = 0;
		Rute spillersKongerute = spillersBrikker.get(0).getRute();
		boolean semiaapenRad = semiaapenRad();
		int startX = brikke.getRute().getX();
		int i;
		int j;
		int k;
		int l;
		
		if (spillerfarge == Farge.HVIT) {
			i = 4;
			j = 3;
			k = 2;
			l = 1;
		} else {
			i = 5;
			j = 6;
			k = 7;
			l = 8;
		}
		
		// Brikke flyttes tilbake til første rad.
		if (brikke.getUroert() == false && rute.getY() == l && spillersBrikker.size() > 10) verdivurdering--;	
			
		if (brikke instanceof Bonde) {
		
			// Legger til poeng hvis sentrumsbonde er urørt.
			if (brikke.getRute().getY() == k && (rute.getX() == 4 || rute.getX() == 5)) {
				if ((rand.nextBoolean() || rute.getX() == 5) && rute.getY() == i) {
					verdivurdering += 2;
				} else {
					verdivurdering++;
				}
			} else {
				
				// Trekker fra poeng hvis bonde forran egen konge flyttes.
				if (motspillersBrikker.size() > 10
						&& (spillersKongerute.getX() < 4 && (startX == 2 || startX == 3)
						|| spillersKongerute.getX() > 5	&& (startX == 6 || startX == 7))) {
					
					verdivurdering -= 2;
				}
				// Trekker fra poeng hvis bonde flyttes til rad med egen bonde.
				if (!semiaapenRad) verdivurdering -= 2;
				
				// Legger til poeng hvis 6 trekk uten progresjon.
				if (partiKopi.getFemtiTrekk() > 6) verdivurdering++;
				
				// Legger til poeng hvis bondetrekk angriper brikke.
				Brikke b1 = null;
				Brikke b2 = null;
				if (rute.getY() != 1 && rute.getY() != 8) {
					if (rute.getX() > 1) {
						Rute r1 = partiKopi.getBrett().finnRute(rute.getX() - 1, rute.getY() + fortegn);
						b1 = r1.getBrikke();
					}
					if (rute.getX() < 8) {
						Rute r2 = partiKopi.getBrett().finnRute(rute.getX() + 1, rute.getY() + fortegn);
						b2 = r2.getBrikke();
					}
					if (b1 != null && b1.getFarge() != spillerfarge || b2 != null && b2.getFarge() != spillerfarge) {
						verdivurdering += 2;
					}
				}
			}
		
		} else if (brikke instanceof Loeper && brikke.getUroert() && rute.getY() != j) {
		
			verdivurdering += 2;
		
		} else if (brikke instanceof Hest) {
		
			// Legger til poeng hvis hest er urørt og kan flyttes mot midten.
			if (brikke.getUroert() && (rute.getX() == 3 || rute.getX() == 6)) {
				if (rand.nextBoolean()) verdivurdering += 2;
				else verdivurdering++;
			}
		
		} else if (brikke instanceof Taarn) {
			
			if (spillersBrikker.size() > 8 && !semiaapenRad) verdivurdering--;
			else if (brikke.getUroert() == true) verdivurdering += 2;
			
		} else if (brikke instanceof Konge) {
		
			if (rute.getX() == 7 && brikke.getUroert() || rute.getX() == 3 && brikke.getUroert()) {
				verdivurdering += 4;
			// Ønsker ikke å flytte konge hvis spiller har flere andre brikker.
			} else if (spillersBrikker.size() > 3) verdivurdering--;
		
		}
		return verdivurdering * fortegn;
			
	}
	
	/**
	 * Hjelpemetode som kontrollerer brikke flytter til en rad med spillerbonde.
	 * @return rad har spillerbonde
	 */
	private boolean semiaapenRad() {
		
		Brett brett = partiKopi.getBrett();
		int x = rute.getX();
		
		for (int y = 1; y < 9; y++) {
			Brikke brikke = brett.finnRute(x, y).getBrikke();
			if (brikke != null
					&& brikke.getFarge() == spillerfarge
					&& brikke instanceof Bonde) {
				
				return false;
			}
		}
		return true;
		
	}
	
	/**
	 * Hjelpemetode som først beregner verdien for hvert trekk, og deretter sorterer tabellen.
	 * @param antall mulige trekk
	 * @return sortert indekstabell
	 */
	private int[][] sorter(int lengde) {
		
		int[][] indekstabell = new int[3][lengde];
		int indeks = 0;
		
		for (int i = 0, antallBrikker = motspillersBrikker.size(); i < antallBrikker; i++) {
			
			Brikke b = motspillersBrikker.get(i);
			
			for (int j = 0, antallTrekk = b.getLovligeTrekk().size(); j < antallTrekk; j++) {
				
				Rute r = b.getLovligeTrekk().get(j);
				
				// Forenklet beregning av trekkets verdi.
				int beregnetVerdi = 0;
				
				if (r.harBrikke()) {
					beregnetVerdi = r.getBrikke().getVerdi() - b.getVerdi() / 10;
				}
				
				indekstabell[0][indeks] = i;
				indekstabell[1][indeks] = j;
				indekstabell[2][indeks] = beregnetVerdi;
				
				indeks++;
				
			}

		}

      	for (int i = 1; i < lengde; i++) {

			int verdi = indekstabell[2][i];
			int trekk = indekstabell[1][i];
			int brikke = indekstabell[0][i];

			int j = i-1;
			while (j >= 0 && indekstabell[2][j] > verdi) {
				indekstabell[2][j+1] = indekstabell[2][j];
				indekstabell[1][j+1] = indekstabell[1][j];
				indekstabell[0][j+1] = indekstabell[0][j];
				j--;
			}

			indekstabell[2][j+1] = verdi;
			indekstabell[1][j+1] = trekk;
			indekstabell[0][j+1] = brikke;

		}
		
		return indekstabell;
		
	}

}
