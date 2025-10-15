package brikke;

import java.util.ArrayList;

import brett.Brett;
import brett.Rute;
import spill.Parti;

public class Taarn extends Brikke {
	
	private final Brett brett = parti.getBrett();
	
	
	public Taarn(Rute rute, Farge farge, Parti parti) {
		super(rute, farge, parti, 40);
	}
	
	@Override
	public void nyInstanse(Rute rute, Farge farge, Parti parti) {
		new Taarn(rute, farge, parti).setUroert(uroert);
	}

	/**
	 * Går gjennom alle mulige trekk og tar vare på lovlige trekk i en liste.
	 * @return liste av lovlige trekk
	 */
	@Override
	public ArrayList<Rute> finnLovligeTrekk() {
		
		lovligeTrekk.clear();
		
		int x = rute.getX();
		int y = rute.getY();
		int[] xr = {0,0,1,-1};
		int[] yr = {1,-1,0,0};
		
		for (int i = 0; i < 4; i++) {
			int xx = x;
			int yy = y;
			while (true) {
			
				xx += xr[i];
				yy += yr[i];
				
				if (xx < 1 || yy < 1 || xx > 8 || yy > 8) break;
				
				if (leggTil(brett.finnRute(xx, yy))) break;

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
		
		int x1 = this.rute.getX();
		int y1 = this.rute.getY();
		int x2 = rute.getX();
		int y2 = rute.getY();
		
		if (x1 != x2 && y1 != y2) return false;
		
		if (x1 == x2) {
			int yRetning = y1 < y2 ? 1 : -1;
			y1 += yRetning;
			
			while (y1 != y2) {
				if (brett.finnRute(x1, y1).harBrikke()) return false;
				y1 += yRetning;
			}	
		} else {
			int xRetning = x1 < x2 ? 1 : -1;
			x1 += xRetning;
			
			while (x1 != x2) {
				if (brett.finnRute(x1, y1).harBrikke()) return false;
				x1 += xRetning;
			}
		}
		return true;
		
	}
	
	@Override
	public char tilPGN() {
		return farge == Farge.HVIT ? 'R' : 'r';
	}

}
