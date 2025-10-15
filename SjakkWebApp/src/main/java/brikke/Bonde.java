package brikke;

import java.util.ArrayList;

import brett.Brett;
import brett.Rute;
import spill.Parti;
import spill.Trekk;

public class Bonde extends Brikke {
	
	private String promotering;
	private boolean enPassant;
	private final int en = farge == Farge.HVIT ? 1 : -1;
	
	
	public Bonde(Rute rute, Farge farge, Parti parti) {
		super(rute, farge, parti, 8);
	}
	
	@Override
	public void nyInstanse(Rute rute, Farge farge, Parti parti) {
		new Bonde(rute, farge, parti);
	}

	/**
	 * Trekk opprettes og sendes til parti.
	 * Kontrollerer om bonde utfører en passant.
	 * Hvis bonde når siste rad vil den bli promotert.
	 * Regel for 50 trekk tilbakestilles.
	 * @param rute
	 * @return trekk
	 */
	@Override
	public Trekk flytt(Rute nyRute) {
		
		if (enPassant && nyRute.equals(lovligeTrekk.get(0))) {
			parti.enPassant();
		}
		
		Trekk trekk = new Trekk(this, this.rute, nyRute);
		parti.nyttTrekk(trekk);

		if (nyRute.getY() == 8 || nyRute.getY() == 1) {
			if (promotering == null) promotering = "=Q";
			parti.promoter(this);
		}
		
		parti.tilbakestillFemtiTrekk();

		return trekk;

	}
	
	/**
	 * Går gjennom alle mulige trekk og tar vare på lovlige trekk i en liste.
	 * @return liste av lovlige trekk
	 */
	@Override
	public ArrayList<Rute> finnLovligeTrekk() {
		
		lovligeTrekk.clear();
		enPassant = false;
		Brett brett = parti.getBrett();
		
		int x = rute.getX();
		int y = rute.getY();
		Rute nyRute;
		
		int to;
		int start;
		int rekkeSyv;
		int rekkeFem;
		
		if (farge == Farge.HVIT) {
			to = 2;
			start = 2;
			rekkeSyv = 7;
			rekkeFem = 5;
		} else {
			to = -2;
			start = 7;
			rekkeSyv = 2;
			rekkeFem = 4;
		}
		
		// En passant.
		Trekk sisteTrekk = parti.getSisteTrekk();
		if (y == rekkeFem
				&& sisteTrekk != null
				&& sisteTrekk.getStartPos().getY() == rekkeSyv
				&& sisteTrekk.getNyPos().getY() == rekkeFem
				&& sisteTrekk.getBrikke() instanceof Bonde
				&& (Math.abs(x - sisteTrekk.getNyPos().getX())) == 1) {
			
			nyRute = brett.finnRute(sisteTrekk.getNyPos().getX(), y + en);
			
			if (!parti.sjakk(this, nyRute, true)) {
				lovligeTrekk.add(nyRute);
				enPassant = true;
			}
		}
		
		// Slår mot høyre.
		if (x < 8) {
			nyRute = brett.finnRute(x + 1, y + en);
			
			if (nyRute.harBrikke()
					&& (nyRute.getBrikke().getFarge() != farge && !parti.sjakk(this, nyRute, false))) {
				
				lovligeTrekk.add(nyRute);
			}
		}
		
		// Slår mot venstre.
		if (x > 1) {
			nyRute = brett.finnRute(x - 1, y + en);
			
			if (nyRute.harBrikke()
					&& (nyRute.getBrikke().getFarge() != farge && !parti.sjakk(this, nyRute, false))) {
				
				lovligeTrekk.add(nyRute);
			}
		}
			
		// Flytter to ruter fram.
		if (y == start) {
			nyRute = brett.finnRute(x, y + to);
			
			if (!nyRute.harBrikke()
					&& !brett.finnRute(x, y + en).harBrikke()
					&& !parti.sjakk(this, nyRute, false)) {
				
				lovligeTrekk.add(nyRute);
			}
		}
		
		// Flytter en rute fram.
		nyRute = brett.finnRute(x, y + en);
		
		if (!nyRute.harBrikke() && !parti.sjakk(this, nyRute, false)) {
			
			lovligeTrekk.add(nyRute);
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
		
		return this.rute.getY() + en == rute.getY()
				&& Math.abs(this.rute.getX() - rute.getX()) == 1;
		
	}
	
	@Override
	public char tilPGN() {
		return farge == Farge.HVIT ? 'P' : 'p';
	}
	
	public String getPromotering() {
		return promotering;
	}
	
	public void setPromotering(String promotering) {
		this.promotering = promotering;
	}

}
