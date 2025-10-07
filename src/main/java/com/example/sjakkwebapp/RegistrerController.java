package com.example.sjakkwebapp;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import brikke.Farge;
import spill.Spiller;

import com.example.util.ValidateUtil;
import com.example.util.LoginUtil;


@Controller
public class RegistrerController {
	
	@Autowired 
    private BrukerService s;
	
	@Autowired 
    private PartiService p;
	
    @GetMapping("/index")
    public String index() {
    	return "redirect:/index.html";
    }
    
    @GetMapping("/registrer")
    public String registrer() {
        return "redirect:/registrer.html";
    }

    @GetMapping("/login")
    public String innlogget() {
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

    @PostMapping("/registrer")
    public String innlogging(@RequestParam String epost, String passord, String passordRepetert, RedirectAttributes ra)
					throws UnsupportedEncodingException, NoSuchAlgorithmException {
    	
		if (!ValidateUtil.validering(epost, passord, passordRepetert, ra))
			return "redirect:registrer";
    	
    	s.leggTilNyBruker(epost, passord);
    	
        return "redirect:login";
    }
    
    @ResponseBody
    @GetMapping("/spill")
    public String sjakk(HttpSession session) throws UnsupportedEncodingException, NoSuchAlgorithmException {
    	
    	String spillerHvit = (String) session.getAttribute("bruker");
    	String spillerSvart = "admin@admin.no";

    	spill.Parti parti = new spill.Parti();
        Spiller hvit = new Spiller(spillerHvit, Farge.HVIT, 3, true, parti);
        Spiller svart = new Spiller(spillerSvart, Farge.SVART, 3, true, parti);
        parti.spill(hvit, svart);

        p.leggTilParti(spillerHvit, spillerSvart, parti.getPNG());

        return parti.getPNG();
    }
    
    @GetMapping("/minePartier")
    @ResponseBody
    public List<Parti> minePartier(HttpSession session) {
        String bruker = (String) session.getAttribute("bruker");
        if (bruker == null) return List.of(); // empty list if not logged in
        List<Parti> partier = p.finnPartierForBruker(bruker);
        return partier;
    }

}