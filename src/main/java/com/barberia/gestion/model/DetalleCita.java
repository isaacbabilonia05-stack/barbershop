package com.barberia.gestion.model;

import org.springframework.data.mongodb.core.mapping.DBRef;

/**
 * En MongoDB este ya NO es una coleccion aparte:
 * va EMBEBIDO dentro del documento de la cita (array "detalles").
 * Por eso no lleva @Document ni @Id.
 */
public class DetalleCita {

    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;

    @DBRef
    private Servicio servicio;

    public DetalleCita() {}

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }
}
