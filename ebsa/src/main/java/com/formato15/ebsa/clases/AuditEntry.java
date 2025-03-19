package com.formato15.ebsa.clases;

public class AuditEntry {
    private int rowIndex;
    private String usuario;
    private String accion;
    private String campo;
    private String valorOriginal;
    private String valorNuevo;

    public AuditEntry(int rowIndex, String usuario, String accion, String campo, String valorOriginal,
            String valorNuevo) {
        this.rowIndex = rowIndex;
        this.usuario = usuario;
        this.accion = accion;
        this.campo = campo;
        this.valorOriginal = valorOriginal;
        this.valorNuevo = valorNuevo;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorOriginal() {
        return valorOriginal;
    }

    public void setValorOriginal(String valorOriginal) {
        this.valorOriginal = valorOriginal;
    }

    public String getValorNuevo() {
        return valorNuevo;
    }

    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    

}