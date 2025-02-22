package com.padellevel.services;

import com.padellevel.data.Equipo;
import com.padellevel.repository.EquipoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;

    @Autowired
    public EquipoService(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    @Transactional(readOnly = true)
    public List<Equipo> findEquiposByPozo(Long pozoId) {
        return equipoRepository.findByPozoId(pozoId);
    }

    @Transactional
    public Equipo save(Equipo equipo) {
        return equipoRepository.save(equipo);
    }
}
