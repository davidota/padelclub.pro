package com.padellevel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.padellevel.data.ServicePrincipal;

@Repository
public interface ServicePrincipalRepository extends JpaRepository<ServicePrincipal, String> {
}