package com.formato15.ebsa.clases;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;

@Entity
@Table(name = "Formato_15") // Nombre de la tabla en la base de datos
public class Formato15 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "departamento_dane")
    private Integer departamentoDane;

    @Column(name = "ciudad_dane")
    private String ciudadDane;

    @Column(name = "asentamiento")
    private String asentamiento;

    @Column(name="codigo_dane")
    private String codigoDane;

    @Column(name="radicado_recibido")
    private String radicadoRecibido;
    //@Temporal(TemporalType.TIMESTAMP)
    @Column(name="fecha_hora_radicacion")
    private String fechaHoraRadicacion;

    @Column(name="tipo_tramite")
    private String tipoTramite;

    @Column(name="grupo_causal")
    private String grupoCausal;

    @Column(name="detalle_causal")
    private String detalleCausal;

    @Column(name="numero_cuenta")
    private String numeroCuenta;

    @Column(name="numero_factura")
    private String numeroFactura;

    @Column(name="tipo_respuesta")
    private String tipoRespuesta;
    //@Temporal(TemporalType.TIMESTAMP)
    @Column(name="fecha_respuesta")
    private String fechaRespuesta;

    @Column(name="radicado_respuesta")
    private String radicadoRespuesta;
    //@Temporal(TemporalType.TIMESTAMP)
    @Column(name="fecha_notificacion")
    private String fechaNotificacion;

    @Column(name="tipo_notificacion")
    private String tipoNotificacion;
    //@Temporal(TemporalType.TIMESTAMP)

    @Column(name="fecha_transferencia_sspd")
    private String fechaTransferenciaSspd;

    // Getters y Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Integer getDepartamentoDane() {
        return departamentoDane;
    }
    public void setDepartamentoDane(Integer departamentoDane) {
        this.departamentoDane = departamentoDane;
    }
    public String getCiudadDane() {
        return ciudadDane;
    }
    public void setCiudadDane(String ciudadDane) {
        this.ciudadDane = ciudadDane;
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
    public String getFechaTransferenciaSspd() {
        return fechaTransferenciaSspd;
    }
    public void setFechaTransferenciaSspd(String fechaTransferenciaSspd) {
        this.fechaTransferenciaSspd = fechaTransferenciaSspd;
    }
    public String getNumeroCuenta() {
        return numeroCuenta;
    }
    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }
    public String getCodigoDane() {
        return codigoDane;
    }
    public void setCodigoDane(String codigoDane) {
        this.codigoDane = codigoDane;
    }

    
}
