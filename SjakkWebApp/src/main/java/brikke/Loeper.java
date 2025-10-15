package brikke;

import java.util.ArrayList;

import brett.Brett;
import brett.Rute;
import spill.Parti;

public class Loeper extends Brikke {
	
	private final Brett brett = parti.getBrett();

	
	public Loeper(Rute rute, Farge farge, Parti parti) {
		super(rute, farge, parti, 24);
	}
	
	@Override
	public void nyInstanse(Rute rute, Farge farge, Parti parti) {
		new Loeper(rute, farge, parti).setUroert(uroert);
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
		int[] xr = {1,-1,1,-1};
		int[] yr = {1,-1,-1,1};
		
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
		
		int xDifferanse = Math.abs(x1-x2);
		
		if (xDifferanse != Math.abs(y1-y2)) return false;
		
		int xRetning = x1 < x2 ? 1 : -1;
		int yRetning = y1 < y2 ? 1 : -1;
		
		for (int i = 1; i < xDifferanse; i++) {
			x1 += xRetning;
			y1 += yRetning;
			if (brett.finnRute(x1, y1).harBrikke()) return false;
		}
		return true;
		
	}
	
	@Override
	public char tilPGN() {
		return farge == Farge.HVIT ? 'B' : 'b';
	}

}
