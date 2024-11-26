package com.formato15.ebsa.clases;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "FORMATO_15") 
public class FormatoSiec {

    @Column(name = "ano")
    private Integer ano;

    @Column(name = "mes")
    private Integer mes;

    @Column(name = "niu")
    private Number niu;

    @Column(name = "codigo_dane")
    private Number codigoDane;

    @Column(name = "radicado_recibido")
    private String radicadoRecibido;

    @Column(name = "fecha_reclamacion")
    private Date fechaReclamacion;

    @Column(name = "tipo_tramite")
    private Number tipoTramite;

    @Column(name = "detalle_causal")
    private Number detalle_causal;

    @Column(name = "id_factura")
    private Number idFactura;

    @Column(name = "tipo_respuesta")
    private Number tipoRespuesta;

    @Column(name = "fecha_respuesta")
    private String fechaRespuesta;

    @Column(name = "radicado_respuesta")
    private String radicadoRespuesta;

    @Column(name = "fecha_notificacion")
    private String fechaNotificacion;

    @Column(name = "tipo_notificacion")
    private String tipoNotificacion;

    @Column(name = "fecha_traslado_sspd")
    private String fechaTrasladoSspd;

    @Column(name = "grupo_causal")
    private String grupoCausal;

    @Id
    @Column(name = "id_formato_15")
    private Long idFormato15;

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

    public Number getNiu() {
        return niu;
    }

    public void setNiu(Number niu) {
        this.niu = niu;
    }

    public Number getCodigoDane() {
        return codigoDane;
    }

    public void setCodigoDane(Number codigoDane) {
        this.codigoDane = codigoDane;
    }

    public String getRadicadoRecibido() {
        return radicadoRecibido;
    }

    public void setRadicadoRecibido(String radicadoRecibido) {
        this.radicadoRecibido = radicadoRecibido;
    }

    public Date getFechaReclamacion() {
        return fechaReclamacion;
    }

    public void setFechaReclamacion(Date fechaReclamacion) {
        this.fechaReclamacion = fechaReclamacion;
    }

    public Number getTipoTramite() {
        return tipoTramite;
    }

    public void setTipoTramite(Number tipoTramite) {
        this.tipoTramite = tipoTramite;
    }

    public Number getDetalle_causal() {
        return detalle_causal;
    }

    public void setDetalle_causal(Number detalle_causal) {
        this.detalle_causal = detalle_causal;
    }

    public Number getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(Number idFactura) {
        this.idFactura = idFactura;
    }

    public Number getTipoRespuesta() {
        return tipoRespuesta;
    }

    public void setTipoRespuesta(Number tipoRespuesta) {
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

    public String getFechaTrasladoSspd() {
        return fechaTrasladoSspd;
    }

    public void setFechaTrasladoSspd(String fechaTrasladoSspd) {
        this.fechaTrasladoSspd = fechaTrasladoSspd;
    }

    public String getGrupoCausal() {
        return grupoCausal;
    }

    public void setGrupoCausal(String grupoCausal) {
        this.grupoCausal = grupoCausal;
    }
                
}
