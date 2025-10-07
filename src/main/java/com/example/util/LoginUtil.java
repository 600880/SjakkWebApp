package com.example.util;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sjakkwebapp.Bruker;
import com.example.sjakkwebapp.BrukerService;

public class LoginUtil {
	
	public static void loggInnBruker(HttpServletRequest request, String bruker) {
		
		loggUtBruker(request.getSession());
		HttpSession session = request.getSession();
		session.setAttribute("bruker", bruker);
		session.setMaxInactiveInterval(600);
		
	}
	
	public static void loggUtBruker(HttpSession session) {
		session.invalidate();	
	}
	
	public static boolean erBrukerInnlogget(HttpSession session) {
		return session != null && session.getAttribute("bruker") != null;
	}
	
	public static boolean innloggingsForsok(String bruker, String passord,
			HttpServletRequest request, RedirectAttributes ra, BrukerService s)
					throws UnsupportedEncodingException, NoSuchAlgorithmException {
		
		Bruker b = s.finnBruker(bruker);
		
		if (b == null) {
			ra.addFlashAttribute("redirectMessage", "Feil brukernavn/passord!");
			return false;
		}
		
		String salt = b.getPassordsalt();
		String hashetPassord = PasswordUtil.passordHashing(passord, salt);
		
		if (!hashetPassord.equals(b.getPassord())) {
			ra.addFlashAttribute("redirectMessage", "Feil brukernavn/passord!");
			return false;
		}
		
		loggInnBruker(request, bruker);
		return true;
		
	}

}