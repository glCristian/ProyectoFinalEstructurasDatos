package main.java.com.techparck.model;

//── Atributos, Constructor, Getters y Setters ────────────────────────────────────────────
public class Visitante {
    private String id;
    private String nombre; 
    private String documento; 
    private int edad; 
    private double altura; 
    private String fotoPaseUrl;
    private double saldoVirtual; 
    private Ticket ticketActivo; 
    //Falta crear ListaEnlazada<>
    //Falta crear SetPropio<>

    public Visitante(String id, String nombre, String documento, int edad, double altura) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.edad = edad;
        this.altura = altura;
        this.saldoVirtual = 0.0; 
        //Falta historial y favoritos
    }

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

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public double getAltura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public String getFotoPaseUrl() {
        return fotoPaseUrl;
    }

    public void setFotoPaseUrl(String fotoPaseUrl) {
        this.fotoPaseUrl = fotoPaseUrl;
    }

    public double getSaldoVirtual() {
        return saldoVirtual;
    }

    public void setSaldoVirtual(double saldoVirtual) {
        this.saldoVirtual = saldoVirtual;
    }

    public Ticket getTicketActivo() {
        return ticketActivo;
    }

    public void setTicketActivo(Ticket ticketActivo) {
        this.ticketActivo = ticketActivo;
    }

    
}
