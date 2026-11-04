package service;

import exception.ContInexistentException;
import exception.FonduriInsuficienteException;
import model.ContBancar;
import model.Tranzactie;
import repository.BancaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TranzactieService {
    private BancaRepository repository;

    public TranzactieService(BancaRepository repository) {
        this.repository = repository;
    }

    public void  inregistreazaCont(String iban, String numeUtilizator, String parola, double soldInitial, int tipCont) {
        if (iban.trim().isEmpty() || numeUtilizator.trim().isEmpty() || parola.trim().isEmpty()) {
            throw new IllegalArgumentException("Eroare: Datele de inregistrare nu pot fi goale!");
        }

        if (repository.gasesteCont(iban) != null) {
            throw new IllegalArgumentException("Eroare: Acest IBAN exista deja în sistem!");
        }

        ContBancar contNou = new ContBancar(iban, numeUtilizator, parola, soldInitial, tipCont);
        repository.adaugaCont(contNou);
    }

    public ContBancar autentificare(String iban, String parola) throws ContInexistentException {
        ContBancar cont = repository.gasesteCont(iban);
        if (cont == null || !cont.getParola().equals(parola)) {
            throw new ContInexistentException("Autentificare esuata: IBAN sau parola incorecte.");
        }
        return cont;
    }

    public void efectueazaTransfer(String ibanSursa, String ibanDestinatie, double suma)
            throws ContInexistentException, FonduriInsuficienteException {
        int ziuaCurenta = LocalDateTime.now().getDayOfMonth();
        if (suma <= 0) {
            repository.inregistreazaStatistica(ziuaCurenta, false);
            throw new IllegalArgumentException("Eroare: Suma transferata trebuie sa fie mai mare decat zero!");
        }
        if (ibanSursa.equals(ibanDestinatie)) {
            repository.inregistreazaStatistica(ziuaCurenta, false);
            throw new IllegalArgumentException("Eroare: Nu poti transfera bani catre propriul cont!");
        }

        ContBancar sursa = repository.gasesteCont(ibanSursa);
        ContBancar destinatie = repository.gasesteCont(ibanDestinatie);

        if (sursa == null || destinatie == null) {
            repository.inregistreazaStatistica(ziuaCurenta, false);
            throw new ContInexistentException("Contul sursa/destinatie nu a fost gasit in memorie.");
        }

        double limitaMax = repository.getLimita(sursa.getTipCont());
        if (suma > limitaMax) {
            repository.inregistreazaStatistica(ziuaCurenta, false);
            throw new IllegalArgumentException("Suma depaseste limita maxima admisa pentru contul tau: " + limitaMax + " RON.");
        }
        if (sursa.getSold() < suma) {
            repository.inregistreazaStatistica(ziuaCurenta, false);
            throw new FonduriInsuficienteException("Sold insuficient. Sold curent: " + sursa.getSold());
        }

        // Efectuarea modificarilor strict in-memory
        sursa.scadeBani(suma);
        destinatie.adaugaBani(suma);

        // Actualizare Matrice (Succes)
        repository.inregistreazaStatistica(ziuaCurenta, true);

        // Generarea istoricului
        String idUnic = UUID.randomUUID().toString().substring(0, 8);
        Tranzactie tranzactieNoua = new Tranzactie(idUnic, suma, ibanSursa, ibanDestinatie);
        repository.adaugaTranzactie(tranzactieNoua);
    }

    public List<Tranzactie> getIstoricCont(String iban) {
        List<Tranzactie> istoricPersonal = new ArrayList<>();
        for (Tranzactie t : repository.getToateTranzactiile()) {
            if (t.getIbanSursa().equals(iban) || t.getIbanDestinatie().equals(iban)) {
                istoricPersonal.add(t);
            }
        }
        return istoricPersonal;
    }

    public String genereazaRaportZiuaCurenta() {
        int ziuaCurenta = LocalDateTime.now().getDayOfMonth();
        int[] statistici = repository.getStatisticiZilnice(ziuaCurenta);

        return "Raport de sistem pentru ziua " + ziuaCurenta + ":\n" +
                " -> Tranzactii Reusite: " + statistici[0] + "\n" +
                " -> Tranzactii Esuate (limite/fonduri): " + statistici[1];
    }
}
