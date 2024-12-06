package com.formato15.ebsa.clases;

import jakarta.persistence.*;

@Entity
@Table(name = "FORMATO_15_USER")
public class Usuario {

    @Id
    @Column(name = "id")
    private String id; // Cambiado de long a String

    @Column(name = "usuario", nullable = false, unique = true)
    private String usuario;

    @Column(name = "contrasena", nullable = false)
    private String contrasena;

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
