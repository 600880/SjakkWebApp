package brikke;

import java.util.ArrayList;
import java.util.List;

import brett.Brett;
import brett.Rute;
import spill.Parti;
import spill.Trekk;

public class Konge extends Brikke {

		
	public Konge(Rute rute, Farge farge, Parti parti) {
		super(rute, farge, parti, 80);
	}
	
	@Override
	public void nyInstanse(Rute rute, Farge farge, Parti parti) {
		new Konge(rute, farge, parti).setUroert(uroert);
	}
	
	/**
	 * Trekk opprettes og sendes til parti.
	 * Det kontrolleres om trekk er en rokade.
	 * @param rute
	 * @return trekk
	 */
	@Override
	public Trekk flytt(Rute nyRute) {
		
		if (uroert == true && (nyRute.getX() == 7 || nyRute.getX() == 3)) {
			parti.utfoerRokade(nyRute.getX(), nyRute.getY());
		}
		
		Trekk trekk = new Trekk(this, this.rute, nyRute);
		parti.nyttTrekk(trekk);

		uroert = false;
		
		return trekk;
		
	}

	/**
	 * Går gjennom alle mulige trekk og tar vare på lovlige trekk i en liste.
	 * @return liste av lovlige trekk
	 */
	@Override
	public ArrayList<Rute> finnLovligeTrekk() {
		
		lovligeTrekk.clear();
		Brett brett = parti.getBrett();
		
		int x = rute.getX();
		int y = rute.getY();
		int[] xr = {1,1,-1,-1,0,0,1,-1};
		int[] yr = {1,-1,1,-1,1,-1,0,0};
		
		for (int i = 0; i < 8; i++) {
			
			int xx = x + xr[i];
			int yy = y + yr[i];
			
			if (xx < 1 || yy < 1 || xx > 8 || yy > 8) continue;

			leggTil(brett.finnRute(xx, yy));
			
		}
		
		// Rokadetrekk.
		if (uroert) {
			if (kortRokade()) {
				if (farge == Farge.HVIT) {
					lovligeTrekk.add(brett.finnRute(7, 1));
				} else {
					lovligeTrekk.add(brett.finnRute(7, 8));
				}
			}
			if (langRokade()) {
				if (farge == Farge.HVIT) {
					lovligeTrekk.add(brett.finnRute(3, 1));
				} else {
					lovligeTrekk.add(brett.finnRute(3, 8));
				}
			}
		}
		
		return lovligeTrekk;
		
	}
	
	/**
	 * Kontrollerer om brikke angriper rute uten å ta forbehold om at brikke kan flytte.
	 * @param rute
	 * @return brikke angriper rute
	 */
	@Override
	public boolean angriperRute(Rute rute) {
		
		return Math.abs(this.rute.getX() - rute.getX()) < 2
				&& Math.abs(this.rute.getY() - rute.getY()) < 2;
		
	}
	
	@Override
	public char tilPGN() {
		return farge == Farge.HVIT ? 'K' : 'k';
	}
	
	/**
	 * Metode kontrollerer at det er ingen brikker mellom konge og tårn, at tårn ikke har flyttet,
	 * og at rutene konge skal flytte over ikke er under angrep.
	 * @return rokade mulig
	 */
	private boolean kortRokade() {
		
		int y = farge == Farge.HVIT ? 1 : 8;	
		Brett brett = parti.getBrett();
		Rute[] rokkeringsruter = new Rute[3];
		
		for (int i = 7; i >= 5; i--) {
			Rute rute = brett.finnRute(i, y);
			if (i > 5 && rute.harBrikke()) return false;
			rokkeringsruter[i-5] = rute;
		}
		
		Brikke brikke = brett.finnRute(8, y).getBrikke();
		if (brikke instanceof Taarn && brikke.getUroert() == true) {
			
			List<Brikke> motspillersBrikker = farge == Farge.HVIT ? parti.getBrikkerSvart() : parti.getBrikkerHvit();
			
			for (Brikke b : motspillersBrikker) {
				for (int i = 0; i < 3; i++) {
					if (b.angriperRute(rokkeringsruter[i])) return false;
				}
			}
			return true;
		}
		return false;
		
	}
	
	/**
	 * Metode kontrollerer at det er ingen brikker mellom konge og tårn, at tårn ikke har flyttet,
	 * og at rutene konge skal flytte over ikke er under angrep.
	 * @return rokade mulig
	 */
	private boolean langRokade() {
		
		int y = farge == Farge.HVIT ? 1 : 8;
		Brett brett = parti.getBrett();
		Rute[] rokkeringsruter = new Rute[3];
		
		for (int i = 3; i <= 5; i++) {
			if (brett.finnRute(i-1, y).harBrikke()) return false;
			rokkeringsruter[i-3] = brett.finnRute(i, y);
		}
		
		Brikke brikke = brett.finnRute(1, y).getBrikke();
		if (brikke instanceof Taarn && brikke.getUroert() == true) {

			List<Brikke> motspillersBrikker = farge == Farge.HVIT ? parti.getBrikkerSvart() : parti.getBrikkerHvit();
			
			for (Brikke b : motspillersBrikker) {
				for (int i = 0; i < 3; i++) {
					if (b.angriperRute(rokkeringsruter[i])) return false;
				}
			}
			return true;
		}
		return false;
		
	}

}
