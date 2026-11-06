package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelează o tranzacție financiară efectuată între două conturi bancare.
 * Păstrează istoricul imuabil al operațiunii, incluzând data și ora exactă.
 *
 * Atribute principale:
 * - idTranzactie: Identificatorul unic al tranzacției (generat ca UUID).
 * - suma: Valoarea transferată între conturi (în RON).
 * - ibanSursa: Contul din care au fost retrași banii.
 * - ibanDestinatie: Contul în care au fost depuși banii.
 * - dataOra: Data și ora exactă la care s-a efectuat sau înregistrat tranzacția.
 *
 * Notă arhitecturală privind instanțierea:
 * Această clasă dispune de doi constructori distincți. Primul este utilizat pentru
 * tranzacțiile noi, generate în timpul execuției aplicației (folosind timpul curent).
 * Cel de-al doilea constructor (care primește și parametrul dataOra) este utilizat
 * exclusiv la operațiunea de citire/încărcare (upload) din fișierul text, pentru
 * a garanta că se folosește timpul istoric la care a avut loc tranzacția, și nu momentul curent.
 */
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

    public Tranzactie(String idTranzactie, double suma, String ibanSursa, String ibanDestinatie, LocalDateTime dataOra) {
        this.idTranzactie = idTranzactie;
        this.suma = suma;
        this.ibanSursa = ibanSursa;
        this.ibanDestinatie = ibanDestinatie;
        this.dataOra = dataOra;
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

    /**
     * Formatează datele tranzacției într-un șir de caractere lizibil pentru utilizator.
     * Utilizează un formatter specific pentru a afișa timpul într-un format standardizat (zi-lună-an oră:minut:secundă).
     *
     * @return Un String ce sumarizează detaliile operațiunii (ID, IBAN sursă, IBAN destinație, sumă și data).
     */
    public String afisareDetalii() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return "ID: " + idTranzactie + " | De la: " + ibanSursa + " | Către: " + ibanDestinatie +
                " | Sumă: " + suma + " RON | Data: " + dataOra.format(formatter);
    }
}