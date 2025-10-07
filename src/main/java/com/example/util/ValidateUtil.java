package com.example.util;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class ValidateUtil {
	
	static public boolean validering(String epost, String passord, String passordRepetert, RedirectAttributes ra) {
		
		if (!epost.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
			ra.addFlashAttribute("feilF", "Ugyldig e-postadresse!");
			return false;
		}
		
		if (!passord.matches(".{8,}")) {
			ra.addFlashAttribute("feilP", "Passord må ha minst åtte tegn!");
			return false;
		}
		if (!passord.equals(passordRepetert)) {
			ra.addFlashAttribute("feilP", "Passordene må være like!");
			return false;
		}
		return true;
	}

}