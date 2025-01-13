package com.prosegur.spinventory.repository;

import com.prosegur.spinventory.data.ServicePrincipal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicePrincipalRepository extends JpaRepository<ServicePrincipal, String> {
}