package com.prosegur.spinventory.services;

import com.prosegur.spinventory.data.ServicePrincipal;
import com.prosegur.spinventory.repository.ServicePrincipalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicePrincipalService {

    @Autowired
    private ServicePrincipalRepository repository;

    public List<ServicePrincipal> findAll() {
        return repository.findAll();
    }

    public Optional<ServicePrincipal> findById(String appId) {
        return repository.findById(appId);
    }

    public ServicePrincipal save(ServicePrincipal servicePrincipal) {
        return repository.save(servicePrincipal);
    }

    public void deleteById(String appId) {
        repository.deleteById(appId);
    }
}