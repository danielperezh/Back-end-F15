package com.formato15.ebsa.clases;

import javax.persistence.*;

@Entity
@Table(name = "Formato_15") // Nombre de la tabla en la base de datos
public class Formato15 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String departamentoDANE;
    private String ciudadDANE;
    private String asentamiento;
    private String radicadoRecibido;
    //@Temporal(TemporalType.TIMESTAMP)
    private String fechaHoraRadicacion;
    private String tipoTramite;
    private String grupoCausal;
    private String detalleCausal;
    private String accountNumber;
    private String numeroFactura;
    private String tipoRespuesta;
    //@Temporal(TemporalType.TIMESTAMP)
    private String fechaRespuesta;
    private String radicadoRespuesta;
    //@Temporal(TemporalType.TIMESTAMP)
    private String fechaNotificacion;
    private String tipoNotificacion;
    //@Temporal(TemporalType.TIMESTAMP)
    private String fechaTransferenciaSSPD;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDepartamentoDANE() {
        return departamentoDANE;
    }
    public void setDepartamentoDANE(String departamentoDANE) {
        this.departamentoDANE = departamentoDANE;
    }
    public String getCiudadDANE() {
        return ciudadDANE;
    }
    public void setCiudadDANE(String ciudadDANE) {
        this.ciudadDANE = ciudadDANE;
    }
    public String getAsentamiento() {
        return asentamiento;
    }
    public void setAsentamiento(String asentamiento) {
        this.asentamiento = asentamiento;
    }
    public String getRadicadoRecibido() {
        return radicadoRecibido;
    }
    public void setRadicadoRecibido(String radicadoRecibido) {
        this.radicadoRecibido = radicadoRecibido;
    }
    public String getFechaHoraRadicacion() {
        return fechaHoraRadicacion;
    }
    public void setFechaHoraRadicacion(String fechaHoraRadicacion) {
        this.fechaHoraRadicacion = fechaHoraRadicacion;
    }
    public String getTipoTramite() {
        return tipoTramite;
    }
    public void setTipoTramite(String tipoTramite) {
        this.tipoTramite = tipoTramite;
    }
    public String getGrupoCausal() {
        return grupoCausal;
    }
    public void setGrupoCausal(String grupoCausal) {
        this.grupoCausal = grupoCausal;
    }
    public String getDetalleCausal() {
        return detalleCausal;
    }
    public void setDetalleCausal(String detalleCausal) {
        this.detalleCausal = detalleCausal;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public String getNumeroFactura() {
        return numeroFactura;
    }
    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }
    public String getTipoRespuesta() {
        return tipoRespuesta;
    }
    public void setTipoRespuesta(String tipoRespuesta) {
        this.tipoRespuesta = tipoRespuesta;
    }
    public String getFechaRespuesta() {
        return fechaRespuesta;
    }
    public void setFechaRespuesta(String fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
    public String getRadicadoRespuesta() {
        return radicadoRespuesta;
    }
    public void setRadicadoRespuesta(String radicadoRespuesta) {
        this.radicadoRespuesta = radicadoRespuesta;
    }
    public String getFechaNotificacion() {
        return fechaNotificacion;
    }
    public void setFechaNotificacion(String fechaNotificacion) {
        this.fechaNotificacion = fechaNotificacion;
    }
    public String getTipoNotificacion() {
        return tipoNotificacion;
    }
    public void setTipoNotificacion(String tipoNotificacion) {
        this.tipoNotificacion = tipoNotificacion;
    }
    public String getFechaTransferenciaSSPD() {
        return fechaTransferenciaSSPD;
    }
    public void setFechaTransferenciaSSPD(String fechaTransferenciaSSPD) {
        this.fechaTransferenciaSSPD = fechaTransferenciaSSPD;
    }

    // Getters y Setters
    
}
