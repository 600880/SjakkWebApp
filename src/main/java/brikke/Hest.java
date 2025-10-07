package brikke;

import java.util.ArrayList;

import brett.Brett;
import brett.Rute;
import spill.Parti;

public class Hest extends Brikke {

	
	public Hest(Rute rute, Farge farge, Parti parti) {
		super(rute, farge, parti, 24);
	}
	
	@Override
	public void nyInstanse(Rute rute, Farge farge, Parti parti) {
		new Hest(rute, farge, parti).setUroert(uroert);
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
		int[] xr = {1,1,-1,-1,2,2,-2,-2};
		int[] yr = {2,-2,2,-2,1,-1,1,-1};
		
		for (int i = 0; i < 8; i++) {
			
			int xx = x + xr[i];
			int yy = y + yr[i];
			
			if (xx < 1 || yy < 1 || xx > 8 || yy > 8) continue;
			
			leggTil(brett.finnRute(xx, yy));
		
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
		
		int x = Math.abs(this.rute.getX() - rute.getX());
		int y = Math.abs(this.rute.getY() - rute.getY());
		
		return x + y == 3 && x * y != 0;

	}
	
	@Override
	public char tilPGN() {
		return farge == Farge.HVIT ? 'N' : 'n';
	}

}
