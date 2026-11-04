package repository;

import model.ContBancar;
import model.Tranzactie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

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

        // Matrice cu 31 de randuri (zile ale lunii) si 2 coloane (0 = tranzactii reusite, 1 = esuate)
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

    public double getLimita(int tipCont) {
        return limiteTranzactionare[tipCont];
    }

    public void inregistreazaStatistica(int ziuaDinLuna, boolean tranzactieReusita) {
        int index = ziuaDinLuna - 1;
        if (tranzactieReusita) {
            statisticiLuna[index][0]++;
        } else {
            statisticiLuna[index][1]++;
        }
    }

    public int[] getStatisticiZilnice(int ziuaDinLuna) {
        return statisticiLuna[ziuaDinLuna - 1];
    }

    //Metode pentru persistenta datelor
    public void incarcaDate() {
        try (BufferedReader br = new BufferedReader(new FileReader("data/conturi.txt"))){
            String linie;
            while((linie = br.readLine()) != null) {
                String[] date = linie.split(",");
                if(date.length == 5) {
                    ContBancar cont = new ContBancar(date[0], date[1], date[2], Double.parseDouble(date[3]), Integer.parseInt(date[4]));
                    conturi.put(cont.getIban(), cont);
                }
            }
        } catch (IOException e) {
            System.out.println("Fisierul conturi.txt nu a fost gasit sau este gol. Se va crea la prima salvare.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader("data/tranzactii.txt"))){
            String linie;
            while ((linie = br.readLine()) != null) {
                String[] date = linie.split(",");
                if (date.length == 5) {
                    Tranzactie t = new Tranzactie(date[0], Double.parseDouble(date[1]), date[2], date[3], LocalDateTime.parse(date[4]));
                    tranzactii.add(t);
                }
            }
        } catch (IOException e) {
            System.out.println("Fișierul tranzactii.txt nu a fost gasit sau este gol. Se va crea la prima salvare.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader("data/statistici.txt"))) {
            String linie;
            while ((linie = br.readLine()) != null) {
                String[] date = linie.split(",");
                if (date.length == 3) {
                    int indexZi = Integer.parseInt(date[0]) - 1;
                    statisticiLuna[indexZi][0] = Integer.parseInt(date[1]);
                    statisticiLuna[indexZi][1] = Integer.parseInt(date[2]);
                }
            }
        } catch (IOException e) {
            System.out.println("Fisierul statistici.txt nu a fost gasit. Matricea va porni de la zero.");
        }
    }

    public void salveazaDate() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("data/conturi.txt"))) {
            for (ContBancar cont : conturi.values()) {
                bw.write(cont.getIban() + "," + cont.getNumeUtilizator() + "," + cont.getParola() + "," + cont.getSold() + "," + cont.getTipCont());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Eroare severa la salvarea conturilor: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("data/tranzactii.txt"))) {
            for (Tranzactie t : tranzactii) {
                bw.write(t.getIdTranzactie() + "," + t.getSuma() + "," + t.getIbanSursa() + "," + t.getIbanDestinatie() + "," + t.getDataOra().toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Eroare severa la salvarea tranzactiilor: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("data/statistici.txt"))) {
            for (int i = 0; i < statisticiLuna.length; i++) {
                int ziua = i + 1;
                bw.write(ziua + "," + statisticiLuna[i][0] + "," + statisticiLuna[i][1]);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Eroare severa la salvarea statisticilor: " + e.getMessage());
        }
    }
}