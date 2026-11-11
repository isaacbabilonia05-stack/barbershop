package com.barberia.gestion.Repository;

import com.barberia.gestion.model.RegistroAcceso;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RegistroAccesoRepository extends MongoRepository<RegistroAcceso, Long> {
}
