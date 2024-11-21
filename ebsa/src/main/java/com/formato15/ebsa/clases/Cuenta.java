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
    private Number matricula;

    @Column(name = "departamento")
    private Number departamento;

    @Column(name = "municipio")
    private Number ciudad;

    public Number getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Number departamento) {
        this.departamento = departamento;
    }

    public Number getMunicipio() {
        return ciudad;
    }

    public void setMunicipio(Number ciudad) {
        this.ciudad = ciudad;
    }

    

}
