package brett;

public class Brett {
	
	private final Rute[] ruter = new Rute[64];
	
	/**
	 *  8x8 brett opprettes. 
	 */
	public Brett() {
		
		int teller = 0;
		
		for (int i = 1; i < 9; i++) {
			for (int j = 1; j < 9; j++) {
				ruter[teller++] = new Rute(i, j);
			}
		}
		
	}
	
	/**
	 * Finner rute til gitte koordinater.
	 * @param x
	 * @param y
	 * @return rute
	 */
	public Rute finnRute(int x, int y) {
		
		return ruter[8*x+y-9];
		
	}
	
	/**
	 *  Brett tømmes for brikker.
	 */
	public void fjernBrikker() {
		
		for (int i = 0; i < 64; i++) {
			ruter[i].setBrikke(null);
		}
		
	}
	
	/**
	 * Representerer stilling som tekststreng.
	 * @return stilling
	 */
	public String stillingTilString() {
		
		StringBuilder stilling = new StringBuilder(64);
		
		for (int i = 0; i < 64; i++) {
			stilling.append(ruter[i].harBrikke() ? ruter[i].getBrikke().tilPGN() : '.');
		}
		
		return stilling.toString();
		
	}
	
}
