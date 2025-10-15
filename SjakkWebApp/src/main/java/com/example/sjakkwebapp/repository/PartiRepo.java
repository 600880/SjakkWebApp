package com.example.sjakkwebapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sjakkwebapp.model.Parti;

public interface PartiRepo extends JpaRepository<Parti, String> {
	List<Parti> findByHvitOrSvart(String hvit, String svart);
}