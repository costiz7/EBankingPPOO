package repository;

import model.ContBancar;
import model.Tranzactie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BancaRepository {
    private Map<String, ContBancar> conturi;
    private List<Tranzactie> tranzactii;
    private double[] limiteTranzactionare;
    private int[][] statisticiLuna;

    public BancaRepository() {
        this.conturi = new HashMap<>();
        this.tranzactii = new ArrayList<>();

        // Index 0: limita Standard, Index 1: limita Premium, Index 2: limita VIP
        this.limiteTranzactionare = new double[]{2000.0, 5000.0, 10000.0};

        // Matrice cu 31 de randuri (zilele ale lunii) si 2 coloane (0 = tranzactii reusite, 1 = esuate)
        this.statisticiLuna = new int[31][2];
    }

    public void adaugaCont(ContBancar cont) {
        conturi.put(cont.getIban(), cont);
    }

    public ContBancar gasesteCont(String iban) {
        return conturi.get(iban);
    }

    public Map<String, ContBancar> getToateConturile() {
        return conturi;
    }

    public void adaugaTranzactie(Tranzactie tranzactie) {
        tranzactii.add(tranzactie);
    }

    public List<Tranzactie> getToateTranzactiile() {
        return tranzactii;
    }
}