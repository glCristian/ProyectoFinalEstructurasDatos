package com.techpark.model;

public class Usuario {
    private String id;
    private String nombre;
    private String email;
    private Rol rol;
    private String passwordHash; // Para autenticación (no se almacena en texto plano)

    public Usuario(String id, String nombre, String email, Rol rol, String passwordHash) {
        this.id           = id;
        this.nombre       = nombre;
        this.email        = email;
        this.rol          = rol;
        this.passwordHash = passwordHash;
    }


    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    
    
}
