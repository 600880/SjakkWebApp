package util;

public class Utils {
	
	public static final int MAKSVERDI = 8000;
	public static final int MINVERDI = -8000;
	
	/**
	 * Oversetter x-koordinater til bokstaver.
	 * @param x-koordinat som tall
	 * @return x-koordinat som bokstav
	 */
	public static char intToChar(int i) {
		
		char c = '?';
		
		switch (i) {
			case 1:
				c = 'a';
				break;
			case 2:
				c = 'b';
				break;
			case 3:
				c = 'c';
				break;
			case 4:
				c = 'd';
				break;
			case 5:
				c = 'e';
				break;
			case 6:
				c = 'f';
				break;
			case 7:
				c = 'g';
				break;
			case 8:
				c = 'h';
		}
		
		return c;
		
	}
	
	/**
	 * Oversetter bokstaver til x-koordinater.
	 * @param x-koordinat som bokstav
	 * @return x-koordinat som tall
	 */
	public static int charToInt(char c) {
		
		int i = 0;
		
		switch (c) {
			case 'a':
				i = 1;
				break;
			case 'b':
				i = 2;
				break;
			case 'c':
				i = 3;
				break;
			case 'd':
				i = 4;
				break;
			case 'e':
				i = 5;
				break;
			case 'f':
				i = 6;
				break;
			case 'g':
				i = 7;
				break;
			case 'h':
				i = 8;
				break;
			}
		
		return i;
		
	}

}
