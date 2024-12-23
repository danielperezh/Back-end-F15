package com.formato15.ebsa.clases;

import jakarta.persistence.*;

@Entity
@Table(name = "dba_users")
public class Usuario {

    @Id
    @Column(name = "user_id")
    private String id; // Cambiado de long a String

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String contrasena;

    public String getUsuario() {
        return username;
    }

    public void setUsuario(String username) {
        this.username = username;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
