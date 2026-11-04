package service;

import exception.ContInexistentException;
import exception.FonduriInsuficienteException;
import model.ContBancar;
import model.Tranzactie;
import repository.BancaRepository;

import java.util.UUID;

public class TranzactieService {
    private BancaRepository repository;

    public TranzactieService(BancaRepository repository) {
        this.repository = repository;
    }

    public void  inregistreazaCont(String iban, String numeUtilizator, String parola, double soldInitial, int tipCont) {
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

        ContBancar sursa = repository.gasesteCont(ibanSursa);
        ContBancar destinatie = repository.gasesteCont(ibanDestinatie);

        if (sursa == null) {
            throw new ContInexistentException("Contul sursa nu a fost gasit in memorie.");
        }
        if (destinatie == null) {
            throw new ContInexistentException("Contul destinatie nu a fost gasit in memorie.");
        }
        if (sursa.getSold() < suma) {
            throw new FonduriInsuficienteException("Sold insuficient. Sold curent: " + sursa.getSold());
        }

        // Efectuarea modificărilor strict in-memory
        sursa.scadeBani(suma);
        destinatie.adaugaBani(suma);

        // Generarea istoricului
        String idUnic = UUID.randomUUID().toString().substring(0, 8);
        Tranzactie tranzactieNoua = new Tranzactie(idUnic, suma, ibanSursa, ibanDestinatie);
        repository.adaugaTranzactie(tranzactieNoua);
    }
}
