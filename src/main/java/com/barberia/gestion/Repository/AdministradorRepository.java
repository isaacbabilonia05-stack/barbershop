package com.barberia.gestion.Repository;

import com.barberia.gestion.model.Administrador;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface AdministradorRepository extends MongoRepository<Administrador, Long> {
    Optional<Administrador> findByEmail(String email);
}
