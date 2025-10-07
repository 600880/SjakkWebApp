package com.example.sjakkwebapp;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;

@Entity
@Table(name = "bruker")
public class Bruker {
	
	@Id
	private String bruker;
	private String passord;
	private String passordsalt;
	
	public String getBruker() {
		return bruker;
	}
	public void setBruker(String bruker) {
		this.bruker = bruker;
	}
	public String getPassord() {
		return passord;
	}
	public void setPassord(String passord) {
		this.passord = passord;
	}
	
	public String getPassordsalt() {
		return passordsalt;
	}
	public void setPassordsalt(String passordsalt) {
		this.passordsalt = passordsalt;
	}

}
