package com.example.sjakkwebapp.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;

import com.example.sjakkwebapp.model.Parti;
import com.example.sjakkwebapp.service.PartiService;
import com.example.sjakkwebapp.util.LoginUtil;

@RestController
public class PartiController {

    @Autowired 
    private PartiService s;

    @GetMapping("/minePartier")
    public ResponseEntity<List<Parti>> minePartier(HttpSession session) {
        
        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        String bruker = (String) session.getAttribute("bruker");
        List<Parti> partier = s.finnPartierForBruker(bruker);
        
        return ResponseEntity.ok(partier);
    }
    
    @GetMapping("/partier/{id}")
    public ResponseEntity<Parti> getParti(@PathVariable int id, HttpSession session) {
        
        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        return s.finnPartiById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PostMapping("/saveGame")
    public ResponseEntity<String> saveGame(@RequestParam String hvit, @RequestParam String svart, @RequestParam String pgn, HttpSession session) {
        
        if (!LoginUtil.erBrukerInnlogget(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
        
        try {
            s.leggTilParti(hvit, svart, pgn);
            return ResponseEntity.ok("Game saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save game: " + e.getMessage());
        }
    }

}
