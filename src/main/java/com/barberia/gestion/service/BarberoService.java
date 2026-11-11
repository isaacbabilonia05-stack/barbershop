package com.barberia.gestion.service;

import com.barberia.gestion.model.Barbero;
import com.barberia.gestion.Repository.BarberoRepository; // Importación corregida
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BarberoService {

    private final BarberoRepository barberoRepository;

    public BarberoService(BarberoRepository barberoRepository) {
        this.barberoRepository = barberoRepository;
    }

    public List<Barbero> listarTodos() {
        return barberoRepository.findAll();
    }
}