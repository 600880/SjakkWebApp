package com.example.sjakkwebapp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
	
@Entity
@Table(name = "parti")
public class Parti {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String hvit;
	private String svart;
	@Column(columnDefinition = "TEXT")
	private String pgn;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getHvit() {
		return hvit;
	}
	public void setHvit(String hvit) {
		this.hvit = hvit;
	}
	public String getSvart() {
		return svart;
	}
	public void setSvart(String svart) {
		this.svart = svart;
	}
	public String getPgn() {
		return pgn;
	}
	public void setPgn(String png) {
		this.pgn = png;
	}
	
}