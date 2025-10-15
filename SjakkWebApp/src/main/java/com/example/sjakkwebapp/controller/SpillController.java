package com.example.sjakkwebapp.controller;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import com.example.sjakkwebapp.model.Parti;
import com.example.sjakkwebapp.service.PartiService;
import com.example.sjakkwebapp.util.LoginUtil;

import brikke.Farge;
import jakarta.servlet.http.HttpSession;
import spill.Spiller;

@RestController
public class SpillController {
	
	@Autowired 
    private PartiService s;
	
    @GetMapping("/spill")
    public ResponseEntity<String> sjakk(HttpSession session) throws UnsupportedEncodingException, NoSuchAlgorithmException {

    	
        if (!LoginUtil.erBrukerInnlogget(session)) {
            // Return 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
    	
    	String spillerHvit = (String) session.getAttribute("bruker");
    	String spillerSvart = "cpu@cpu.no";

    	spill.Parti parti = new spill.Parti();
        Spiller hvit = new Spiller(spillerHvit, Farge.HVIT, 5, true, parti);
        Spiller svart = new Spiller(spillerSvart, Farge.SVART, 5, true, parti);
        parti.spill(hvit, svart);

        s.leggTilParti(spillerHvit, spillerSvart, parti.getPNG());

        return ResponseEntity.ok(parti.getPNG());
    }
    
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
    
    // Keep track of all clients
    private static final List<SseEmitter> clients = new CopyOnWriteArrayList<>();

    // Endpoint for clients to connect
    @GetMapping("/moves/stream")
    public SseEmitter streamMoves() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // no timeout
        clients.add(emitter);

        // Remove emitter if connection is closed
        emitter.onCompletion(() -> clients.remove(emitter));
        emitter.onTimeout(() -> clients.remove(emitter));
        emitter.onError((e) -> clients.remove(emitter));

        return emitter;
    }

    // Push a move to all connected clients
    /*@PostMapping("/moves/push")
    public ResponseEntity<String> pushMove(@RequestBody String move) {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(SseEmitter.event().name("move").data(move));
            } catch (IOException e) {
                clients.remove(emitter);
            }
        }
        return ResponseEntity.ok("Move pushed: " + move);
    }*/
    
    public static void makeAIMove(String move) {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(SseEmitter.event().name("move").data(move));
            } catch (IOException e) {
                clients.remove(emitter);
            }
        }
    }
    
}