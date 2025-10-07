package com.example.sjakkwebapp;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.util.LoginUtil;

import brikke.Farge;
import jakarta.servlet.http.HttpSession;
import spill.Spiller;

public class SpillController {
	
	@Autowired 
    private PartiService s;
	
    @ResponseBody
    @GetMapping("/spill")
    public ResponseEntity<String> sjakk(HttpSession session) throws UnsupportedEncodingException, NoSuchAlgorithmException {

    	
        if (!LoginUtil.erBrukerInnlogget(session)) {
            // Return 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
    	
    	String spillerHvit = (String) session.getAttribute("bruker");
    	String spillerSvart = "cpu@cpu.no";

    	spill.Parti parti = new spill.Parti();
        Spiller hvit = new Spiller(spillerHvit, Farge.HVIT, 3, true, parti);
        Spiller svart = new Spiller(spillerSvart, Farge.SVART, 3, true, parti);
        parti.spill(hvit, svart);

        s.leggTilParti(spillerHvit, spillerSvart, parti.getPNG());

        return ResponseEntity.ok(parti.getPNG());
    }
    
    @ResponseBody
    @GetMapping("/minePartier")
    public ResponseEntity<List<Parti>> minePartier(HttpSession session) {
        
        if (!LoginUtil.erBrukerInnlogget(session)) {
            // Return 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        String bruker = (String) session.getAttribute("bruker");
        List<Parti> partier = s.finnPartierForBruker(bruker);
        
        return ResponseEntity.ok(partier);
    }

}
