package com.example.sjakkwebapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sjakkwebapp.model.Bruker;

public interface BrukerRepo extends JpaRepository<Bruker, String> {
	Bruker findByBruker(String bruker);
}