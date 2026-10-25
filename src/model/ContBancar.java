package model;

public class ContBancar {
    private String iban;
    private String numeUtilizator;
    private String parola;
    private double sold;

    public ContBancar(String iban, String numeUtilizator, String parola, double sold) {
        this.iban = iban;
        this.numeUtilizator = numeUtilizator;
        this.parola = parola;
        this.sold = sold;
    }

    public String getIban() {
        return iban;
    }
    public String getNumeUtilizator() {
        return numeUtilizator;
    }
    public String getParola() {
        return parola;
    }
    public double getSold() {
        return sold;
    }

    public void adaugaBani(double suma) {
        this.sold += suma;
    }

    public void scadeBani(double suma) {
        this.sold -= suma;
    }
}
