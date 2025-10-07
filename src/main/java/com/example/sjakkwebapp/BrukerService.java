package com.example.sjakkwebapp;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.util.PasswordUtil;

@Service
public class BrukerService {
	
	@Autowired
	private BrukerRepo brukerRepo;
	
	public void leggTilNyBruker (String bruker, String passord)
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
	    return brukerRepo.findById(bruker).orElse(null);
	}

}
