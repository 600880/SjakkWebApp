package com.example.sjakkwebapp;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
