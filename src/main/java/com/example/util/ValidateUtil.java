package com.example.util;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sjakkwebapp.BrukerService;

public class ValidateUtil {
	
	static public boolean validering(String epost, String passord, String passordRepetert,
			RedirectAttributes ra, BrukerService s) {
		
		if (s.finnBruker(epost) != null) {
			ra.addFlashAttribute("redirectMessage", "E-postadresse er allerede i bruk!");
			return false;
		}
		
		if (!epost.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
			ra.addFlashAttribute("redirectMessage", "Ugyldig e-postadresse!");
			return false;
		}
		
		if (!passord.matches(".{8,}")) {
			ra.addFlashAttribute("redirectMessage", "Passord må ha minst åtte tegn!");
			return false;
		}
		if (!passord.equals(passordRepetert)) {
			ra.addFlashAttribute("redirectMessage", "Passordene må være like!");
			return false;
		}
		return true;
	}

}