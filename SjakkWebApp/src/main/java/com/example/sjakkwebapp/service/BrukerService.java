package com.example.sjakkwebapp.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.sjakkwebapp.repository.BrukerRepo;
import com.example.sjakkwebapp.model.Bruker;
import com.example.sjakkwebapp.util.PasswordUtil;

@Service
public class BrukerService {
	
	@Autowired
	private BrukerRepo brukerRepo;
	
	public void leggTilBruker (String bruker, String passord)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {

		Bruker b = new Bruker();
		String salt = PasswordUtil.lagSalt();
		String hashetPassord = PasswordUtil.passordHashing(passord, salt);
		
		b.setBruker(bruker);
		b.setPassord(hashetPassord);
		b.setPassordsalt(salt);
		brukerRepo.save(b);

	}
	
	public Bruker finnBruker(String bruker) {
	    return brukerRepo.findByBruker(bruker);
	}

}
