package com.techpark.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Sesion {
    
    private String token;
    private String usuarioId;
    private String rol;
    private LocalDateTime fechaExpiracion;

    public Sesion() {
        this.token = UUID.randomUUID().toString();
    }

    public Sesion(String usuarioId, String rol, LocalDateTime fechaExpiracion) {
        this.token = UUID.randomUUID().toString();
        this.usuarioId = usuarioId;
        this.rol = rol;
        this.fechaExpiracion = fechaExpiracion;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

}
