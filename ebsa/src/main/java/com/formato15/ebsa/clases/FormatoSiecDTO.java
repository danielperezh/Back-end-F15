package com.formato15.ebsa.clases;

import java.sql.Timestamp;

public class FormatoSiecDTO {

        private String daneDpto;
        private String daneMpio;
        private String daneAsentamiento;
        private String radicadoRecibido;
        private String fechaReclamacion;
        private String tipoTramite;
        private String grupoCausal;
        private String detalleCausal;
        private String niu;
        private String idFactura;
        private String tipoRespuesta;
        private String fechaRespuesta;
        private String radicadoRespuesta;
        private String fechaNotificacion;
        private String tipoNotificacion;
        private String fechaTrasladoSspd;

        // Constructor sin parámetros (por defecto)
        public FormatoSiecDTO() {
        }

        public FormatoSiecDTO(String daneDpto, String daneMpio, String daneAsentamiento, String radicadoRecibido, 
        String fechaReclamacion, String tipoTramite, String grupoCausal, String detalleCausal, 
                      String niu, String idFactura, String tipoRespuesta, String fechaRespuesta, 
                      String radicadoRespuesta, String fechaNotificacion, String tipoNotificacion, 
                      String fechaTrasladoSspd) {
            this.daneDpto = daneDpto;
            this.daneMpio = daneMpio;
            this.daneAsentamiento = daneAsentamiento;
            this.radicadoRecibido = radicadoRecibido;
            this.fechaReclamacion = fechaReclamacion;
            this.tipoTramite = tipoTramite;
            this.grupoCausal = grupoCausal;
            this.detalleCausal = detalleCausal;
            this.niu = niu;
            this.idFactura = idFactura;
            this.tipoRespuesta = tipoRespuesta;
            this.fechaRespuesta = fechaRespuesta;
            this.radicadoRespuesta = radicadoRespuesta;
            this.fechaNotificacion = fechaNotificacion;
            this.tipoNotificacion = tipoNotificacion;
            this.fechaTrasladoSspd = fechaTrasladoSspd;
        }

        
        public String getDaneDpto() {
            return daneDpto;
        }
        public void setDaneDpto(String daneDpto) {
            this.daneDpto = daneDpto;
        }
        public String getDaneMpio() {
            return daneMpio;
        }
        public void setDaneMpio(String daneMpio) {
            this.daneMpio = daneMpio;
        }
        public String getDaneAsentamiento() {
            return daneAsentamiento;
        }
        public void setDaneAsentamiento(String daneAsentamiento) {
            this.daneAsentamiento = daneAsentamiento;
        }
        public String getRadicadoRecibido() {
            return radicadoRecibido;
        }
        public void setRadicadoRecibido(String radicadoRecibido) {
            this.radicadoRecibido = radicadoRecibido;
        }
        public String getFechaReclamacion() {
            return fechaReclamacion;
        }
        public void setFechaReclamacion(String fechaReclamacion) {
            this.fechaReclamacion = fechaReclamacion;
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
        public String getNiu() {
            return niu;
        }
        public void setNiu(String niu) {
            this.niu = niu;
        }
        public String getIdFactura() {
            return idFactura;
        }
        public void setIdFactura(String idFactura) {
            this.idFactura = idFactura;
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
        public String getFechaTrasladoSspd() {
            return fechaTrasladoSspd;
        }
        public void setFechaTrasladoSspd(String fechaTrasladoSspd) {
            this.fechaTrasladoSspd = fechaTrasladoSspd;
        }
    
        // Getters y setters

        
    
}
