package spill;

import brett.Rute;
import brikke.Bonde;
import brikke.Brikke;
import brikke.Dronning;
import brikke.Hest;
import brikke.Konge;
import brikke.Loeper;
import brikke.Taarn;

public class Trekk {
	
	private final Brikke brikke;
	private final Rute startPos;
	private final Rute nyPos;
	
	
	public Trekk(Brikke brikke, Rute startPos, Rute nyPos) {
		this.brikke = brikke;
		this.startPos = startPos;
		this.nyPos = nyPos;
	}
	
	public Brikke getBrikke() {
		return brikke;
	}
	
	public Rute getStartPos() {
		return startPos;
	}
	
	public Rute getNyPos() {
		return nyPos;
	}
	
	/**
	 * Oversetter trekk til PGN-format.
	 * @return trekk
	 */
	public String toPGN() {
		
		String r = nyPos.toString();
		String s = null;
		
		if (brikke instanceof Bonde) {
			s = startPos + r;
			if (nyPos.getY() == 8 || nyPos.getY() == 1) s += ((Bonde) brikke).getPromotering();
		} else if (brikke instanceof Hest) {
			s = "N" + startPos + r;
		} else if (brikke instanceof Loeper) {
			s = "B" + startPos + r;
		} else if (brikke instanceof Taarn) {
			s = "R" + startPos + r;
		} else if (brikke instanceof Dronning) {
			s = "Q" + startPos + r;
		} else if (brikke instanceof Konge) {
			if (nyPos.getX() == 7 && startPos.getX() == 5) s = "O-O";
			else if (nyPos.getX() == 3 && startPos.getX() == 5) s = "O-O-O";
			else s = "K" + r;
		}
		
		return s;
		
	}
	
	@Override
	public String toString() {
		return brikke.toString() + startPos.toString() + "->" + nyPos.toString();
	}

}
