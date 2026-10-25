package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Tranzactie {
    private String idTranzactie;
    private double suma;
    private String ibanSursa;
    private String ibanDestinatie;
    private LocalDateTime dataOra;

    public Tranzactie(String idTranzactie, double suma, String ibanSursa, String ibanDestinatie) {
        this.idTranzactie = idTranzactie;
        this.suma = suma;
        this.ibanSursa = ibanSursa;
        this.ibanDestinatie = ibanDestinatie;
        this.dataOra = LocalDateTime.now();
    }

    public String getIdTranzactie() {
        return idTranzactie;
    }
    public double getSuma() {
        return suma;
    }
    public String getIbanSursa() {
        return ibanSursa;
    }
    public String getIbanDestinatie() {
        return ibanDestinatie;
    }
    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public String afisareDetalii() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return "ID: " + idTranzactie + " | De la: " + ibanSursa + " | Către: " + ibanDestinatie +
                " | Sumă: " + suma + " RON | Data: " + dataOra.format(formatter);
    }
}