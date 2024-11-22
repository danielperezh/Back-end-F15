package com.formato15.ebsa.clases;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuenta") 
public class Cuenta {

    @Id
    @Column(name = "matricula")
    private Long matricula;

    @Column(name = "departamento")
    private Integer departamento;

    @Column(name = "municipio")
    private Integer ciudad;

    public Integer getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Integer departamento) {
        this.departamento = departamento;
    }

    public Integer getMunicipio() {
        return ciudad;
    }

    public void setMunicipio(Integer ciudad) {
        this.ciudad = ciudad;
    }

    

}
