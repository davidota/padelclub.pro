package com.padellevel.services;

import com.padellevel.data.Pozo;
import com.padellevel.repository.PozoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PozoService {

    private final PozoRepository pozoRepository;

    @Autowired
    public PozoService(PozoRepository pozoRepository) {
        this.pozoRepository = pozoRepository;
    }

    @Transactional
    public Pozo save(Pozo pozo) {
        return pozoRepository.save(pozo);
    }

    @Transactional
    public void delete(Pozo pozo) {
        pozoRepository.delete(pozo);
    }

    @Transactional(readOnly = true)
    public List<Pozo> findAll() {
        return pozoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Pozo> findById(Long id) {
        return pozoRepository.findById(id);
    }
    
    // New method to fetch Pozo with enfrentamientos eagerly loaded
    @Transactional(readOnly = true)
    public Optional<Pozo> findByIdWithEnfrentamientos(Long id) {
        return pozoRepository.findByIdWithEnfrentamientos(id);
    }
}
