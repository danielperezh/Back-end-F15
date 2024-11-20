package com.formato15.ebsa.clases;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;

@Entity
@Table(name = "Formato_15") 
public class Formato15 {

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_formato_15")
    private Long id;

    @Column(name = "ano")
    private Integer ano;

    @Column(name = "mes")
    private Integer mes;

    @Column(name="codigo_dane") //codigo_dane
    private Integer codigoDane;

    @Column(name="radicado_recibido") //radicado_recibido
    private String radicadoRecibido;
    
    @Column(name="fecha_reclamacion") // fecha_reclamacion
    private Timestamp  fechaHoraRadicacion;

    @Column(name="tipo_tramite") // tipo_tramite
    private String tipoTramite;

    @Column(name="grupo_causal") // grupo_causal
    private String grupoCausal;

    @Column(name="detalle_causal") //detalle_causal
    private String detalleCausal;

    @Column(name="niu") //niu
    private String numeroCuenta;

    @Column(name="id_factura") // id_factura
    private Integer numeroFactura;

    @Column(name="tipo_respuesta") // tipo_respuesta
    private Integer tipoRespuesta;
    
    @Column(name="fecha_respuesta") // fecha_respuesta
    private String fechaRespuesta;
 
    @Column(name="radicado_respuesta") // radicado_respuesta 
    private String radicadoRespuesta;
    
    @Column(name="fecha_notificacion") // fecha_notificacion
    private String fechaNotificacion;
 
    @Column(name="tipo_notificacion") // tipo_notificacion
    private String tipoNotificacion;

    @Column(name="fecha_traslado_sspd") // fecha_traslado_sspd
    private String fechaTransferenciaSspd;

    // Getters y Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getRadicadoRecibido() {
        return radicadoRecibido;
    }
    public void setRadicadoRecibido(String radicadoRecibido) {
        this.radicadoRecibido = radicadoRecibido;
    }
    public Timestamp  getFechaHoraRadicacion() {
        return fechaHoraRadicacion;
    }
    public void setFechaHoraRadicacion(Timestamp  fechaHoraRadicacion) {
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
    
    public Integer getNumeroFactura() {
        return numeroFactura;
    }
    public void setNumeroFactura(Integer numeroFactura) {
        this.numeroFactura = numeroFactura;
    }
    public Integer getTipoRespuesta() {
        return tipoRespuesta;
    }
    public void setTipoRespuesta(Integer tipoRespuesta) {
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
    public Integer getCodigoDane() {
        return codigoDane;
    }
    public void setCodigoDane(Integer codigoDane) {
        this.codigoDane = codigoDane;
    }
    public Integer getAno() {
        return ano;
    }
    public void setAno(Integer ano) {
        this.ano = ano;
    }
    public Integer getMes() {
        return mes;
    }
    public void setMes(Integer mes) {
        this.mes = mes;
    }

    
}
