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
	 * Representerer stilling som FEN-streng.
	 * @param hvitITrekket det er hvit sitt trekk
	 * @return FEN-stilling
	 */
	public String stillingTilFEN(boolean hvitITrekket) {
		StringBuilder fen = new StringBuilder();
		
		// 1. Piece placement
		for (int y = 8; y >= 1; y--) {
			int tommeRuter = 0;
			for (int x = 1; x <= 8; x++) {
				Rute rute = finnRute(x, y);
				if (!rute.harBrikke()) {
					tommeRuter++;
				} else {
					if (tommeRuter > 0) {
						fen.append(tommeRuter);
						tommeRuter = 0;
					}
					fen.append(rute.getBrikke().tilPGN());
				}
			}
			if (tommeRuter > 0) {
				fen.append(tommeRuter);
			}
			if (y > 1) {
				fen.append('/');
			}
		}
		
		// 2. Active color
		fen.append(" ").append(hvitITrekket ? 'w' : 'b');
		
		return fen.toString();
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
