package brett;

import brikke.Brikke;
import util.Utils;

public class Rute {
	
	private final int x;
	private final int y;
	private Brikke brikke = null;
	
	/**
	 * Rute med gitte koordinater opprettes.
	 * @param x
	 * @param y
	 */
	public Rute(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public Brikke getBrikke() {
		return brikke;
	}
	
	public void setBrikke(Brikke brikke) {
		this.brikke = brikke;
	}
	
	public boolean harBrikke() {
		return brikke != null;
	}
	
	@Override
	public String toString() {
		return "" + Utils.intToChar(x) + y;
	}

}
