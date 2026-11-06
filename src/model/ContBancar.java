package model;

/**
 * Reprezintă entitatea unui cont bancar în sistemul E-Banking.
 * Gestionează datele de identificare ale clientului, soldul curent și tipul contului.
 *
 * Atribute principale:
 * - iban: Identificatorul unic al contului bancar.
 * - numeUtilizator: Numele complet al titularului.
 * - parola: Parola utilizată pentru autentificarea în sistem.
 * - sold: Cantitatea de fonduri disponibile în cont (în RON).
 * - tipCont: Gradul contului folosit pentru determinarea limitelor de transfer (0 = Standard, 1 = Premium, 2 = VIP).
 */
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

    /**
     * Suplimentează soldul curent al contului bancar cu o sumă specificată.
     * Metoda este utilizată atunci când contul este destinația unui transfer valid.
     *
     * @param suma Valoarea (în RON) care se adaugă la soldul curent.
     */
    public void adaugaBani(double suma) {
        this.sold += suma;
    }

    /**
     * Diminuează soldul curent al contului bancar cu o sumă specificată.
     * Metoda este utilizată atunci când din cont se inițiază un transfer către o altă destinație.
     *
     * @param suma Valoarea (în RON) care se extrage din soldul curent.
     */
    public void scadeBani(double suma) {
        this.sold -= suma;
    }
}
