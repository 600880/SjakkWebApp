package com.example.sjakkwebapp.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
	
	public static String passordHashing(String passord, String salt)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		
		// Passord og salt slåes sammen.
		String passordOgSalt = passord+salt;
		
		// Passord blir lagret som bytes.
		byte[] passordSomBytes = passordOgSalt.getBytes("UTF-8");
		
		// Passord som bytes blir hashet.
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] digest = passordSomBytes;
		for (int i = 0; i < 10000; i++) {
			digest = md.digest(digest);
		}
		
		// Hashet passord blir lagret som string igjen og returneres.
		return Base64.getEncoder().encodeToString(digest);

	}
	
	public static String lagSalt() {
		
		// Tilfeldig genererte bytes opprettes og returneres.
		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);

	}

}
