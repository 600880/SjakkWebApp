package com.example.sjakkwebapp;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.util.ValidateUtil;
import com.example.util.LoginUtil;


@Controller
public class AutentiseringController {
	
	@Autowired 
    private BrukerService s;
	
    @GetMapping("/index")
    public String index() {
    	return "redirect:/index.html";
    }

    @GetMapping("/login")
    public String innlogget(HttpSession session) {
    	if (!LoginUtil.erBrukerInnlogget(session))
    		return "redirect:/index.html";
        return "loginView";
    }
    
	@PostMapping("/login")
	public String innlogging(@RequestParam String bruker, String passord,
			HttpServletRequest request, RedirectAttributes ra)
					throws UnsupportedEncodingException, NoSuchAlgorithmException {
		
		if (LoginUtil.innloggingsForsok(bruker, passord, request, ra, s))
			return "redirect:login";
		return "redirect:index";

	}
	
	@PostMapping("/logout")
	public String loggUt(HttpSession session) {
		LoginUtil.loggUtBruker(session);
		return "redirect:index";
	}
	
    @GetMapping("/registrer")
    public String registrer() {
        return "redirect:/registrer.html";
    }

    @PostMapping("/registrer")
    public String registrer(@RequestParam String epost, String passord, String passordRepetert, RedirectAttributes ra)
					throws UnsupportedEncodingException, NoSuchAlgorithmException {
    	
		if (!ValidateUtil.validering(epost, passord, passordRepetert, ra))
			return "redirect:registrer";
    	
    	s.leggTilBruker(epost, passord);
    	
        return "redirect:login";
    }
   

}