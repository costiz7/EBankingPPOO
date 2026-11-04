package model;

public class ContBancar {
    private String iban;
    private String numeUtilizator;
    private String parola;
    private double sold;
    private int tipCont;

    public ContBancar(String iban, String numeUtilizator, String parola, double sold, int tipCont) {
        this.iban = iban;
        this.numeUtilizator = numeUtilizator;
        this.parola = parola;
        this.sold = sold;
        this.tipCont = tipCont;
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
    public int getTipCont() { return tipCont; }

    public void adaugaBani(double suma) {
        this.sold += suma;
    }

    public void scadeBani(double suma) {
        this.sold -= suma;
    }
}
