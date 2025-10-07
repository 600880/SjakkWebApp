package brikke;

import java.util.ArrayList;

import brett.Rute;
import spill.Parti;
import spill.Trekk;

public abstract class Brikke {
	
	Rute rute;
	final Farge farge;
	final Parti parti;
	final int verdi;
	final ArrayList<Rute> lovligeTrekk = new ArrayList<Rute>();
	boolean uroert = true;
	
	
	public Brikke(Rute rute, Farge farge, Parti parti, int verdi) {
		this.rute = rute;
		this.farge = farge;
		this.parti = parti;
		this.verdi = verdi;
		rute.setBrikke(this);
		if (farge == Farge.HVIT) parti.getBrikkerHvit().add(this);
		else parti.getBrikkerSvart().add(this);
	}
	
	/**
	 * @param rute
	 * @param farge
	 * @param parti
	 * Metode brukes av kopikonstruktør for parti.
	 */
	public abstract void nyInstanse(Rute rute, Farge farge, Parti parti);
	
	/**
	 * Går gjennom alle mulige trekk og tar vare på lovlige trekk i en liste.
	 * @return liste av lovlige trekk
	 */
	public abstract ArrayList<Rute> finnLovligeTrekk();
	
	/**
	 * Kontrollerer om brikke angriper rute uten å ta forbehold om at brikke kan flytte.
	 * @param rute
	 * @return brikke angriper rute
	 */
	public abstract boolean angriperRute(Rute rute);
	
	/**
	 * Brikke på PGN-format.
	 * @return bokstav
	 */
	public abstract char tilPGN();
	
	/**
	 * Trekk opprettes og sendes til parti.
	 * @param rute
	 * @return trekk
	 */
	public Trekk flytt(Rute nyRute) {
		Trekk trekk = new Trekk(this, this.rute, nyRute);
		parti.nyttTrekk(trekk);
		uroert = false;
		return trekk;
	}
	
	/**
	 * Kopierer trekk fra brikke.
	 * @param brikke
	 * @param indeks
	 * @return rute
	 */
	public Rute kopierTrekk(Brikke brikke, int i) {
		
		Rute ruteKopi = brikke.getLovligeTrekk().get(i);
		Rute rute = parti.getBrett().finnRute(ruteKopi.getX(), ruteKopi.getY());
		return rute;
		
	}
	
	public Rute getRute() {
		return rute;
	}
	
	public void setRute(Rute rute) {
		this.rute = rute;
	}
	
	public Farge getFarge() {
		return farge;
	}
	
	public int getVerdi() {
		return verdi;
	}
	
	public boolean getUroert() {
		return uroert;
	}
	
	public void setUroert(boolean uroert) {
		this.uroert = uroert;
	}
	
	public ArrayList<Rute> getLovligeTrekk() {
		return lovligeTrekk;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + farge + "]";
	}
	
	/**
	 * Hjelpemetode som sjekker om brikke kan flytte til rute, og om brikke stoppes. 
	 * @param nyRute
	 * @return hindring møtt
	 */
	protected boolean leggTil(Rute nyRute) {
		
		if (nyRute.harBrikke()) {
			if (nyRute.getBrikke().getFarge() != farge && !parti.sjakk(this, nyRute, false)) {
				lovligeTrekk.add(nyRute);
			}
			return true;
		} else if (!parti.sjakk(this, nyRute, false)) {
			lovligeTrekk.add(nyRute);
		}	
		return false;
		
	}

}
