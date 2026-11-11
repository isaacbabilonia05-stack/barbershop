package com.barberia.gestion.Repository;

import com.barberia.gestion.model.Barbero;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface BarberoRepository extends MongoRepository<Barbero, Long> {
    List<Barbero> findByActivoTrue();
    Optional<Barbero> findByEmail(String email);
    boolean existsByEmail(String email);
}
