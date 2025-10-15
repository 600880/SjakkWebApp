package com.example.sjakkwebapp.controller;

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

import com.example.sjakkwebapp.util.ValidateUtil;
import com.example.sjakkwebapp.service.BrukerService;
import com.example.sjakkwebapp.util.LoginUtil;


@Controller
public class AutentiseringController {
	
	@Autowired 
    private BrukerService s;
	
	@GetMapping("/")
    public String index() {
    	return "indexView";
    }
	
    @GetMapping("/index")
    public String hjem() {
    	return "indexView";
    }

    @GetMapping("/login")
    public String innlogget(HttpSession session) {
    	if (!LoginUtil.erBrukerInnlogget(session))
    		return "indexView";
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
        return "registrerView";
    }

    @PostMapping("/registrer")
    public String registrer(@RequestParam String epost, String passord, String passordRepetert,
    		HttpServletRequest request, RedirectAttributes ra)
					throws UnsupportedEncodingException, NoSuchAlgorithmException {
    	
		if (!ValidateUtil.validering(epost, passord, passordRepetert, ra, s))
			return "redirect:registrer";
    	
    	s.leggTilBruker(epost, passord);
    	LoginUtil.loggInnBruker(request, epost);
    	
        return "redirect:login";
    }
   

}