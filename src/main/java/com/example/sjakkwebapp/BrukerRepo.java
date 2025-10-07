package com.example.sjakkwebapp;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BrukerRepo extends JpaRepository<Bruker, String> {
	Bruker findByBruker(String bruker);
}