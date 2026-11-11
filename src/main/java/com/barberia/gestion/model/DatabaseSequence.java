package com.barberia.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Coleccion auxiliar que guarda el ultimo id usado por cada coleccion,
 * para poder seguir usando ids numericos (Long) como en MySQL.
 */
@Document(collection = "database_sequences")
public class DatabaseSequence {

    @Id
    private String id;
    private long seq;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getSeq() { return seq; }
    public void setSeq(long seq) { this.seq = seq; }
}
