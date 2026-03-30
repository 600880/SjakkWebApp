package com.example.sjakkwebapp.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.sjakkwebapp.repository.PartiRepo;
import com.example.sjakkwebapp.model.Parti;

@Service
public class PartiService {
	
	@Autowired
	private PartiRepo partiRepo;
	
	public void leggTilParti (String hvit, String svart, String pgn)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {

		Parti p = new Parti();
		
		p.setHvit(hvit);
		p.setSvart(svart);
		p.setPgn(pgn);
		partiRepo.save(p);
		
	}
	
    public List<Parti> finnPartierForBruker(String bruker) {
        return partiRepo.findByHvitOrSvart(bruker, bruker);
    }
    
    public Optional<Parti> finnPartiById(int id) {
        return partiRepo.findById(id);
    }

}
