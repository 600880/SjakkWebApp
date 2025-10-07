package com.example.sjakkapp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PartiRepo extends JpaRepository<Parti, String> {
	List<Parti> findByHvitOrSvart(String hvit, String svart);
}