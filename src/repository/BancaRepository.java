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

/**
 * Clasa responsabilă cu persistența datelor și gestionarea memoriei RAM (Data Access Object / Repository).
 *
 * Utilizează structuri de date dinamice (HashMap pentru acces O(1) la conturi prin IBAN,
 * ArrayList pentru stocarea secvențială a istoricului), precum și masive fundamentale:
 * un vector unidimensional pentru limitele de transfer și o matrice bidimensională pentru statistici.
 *
 * Implementează operațiuni de citire/scriere (I/O) locale pentru a asigura
 * salvarea și restaurarea stării sistemului între rulările aplicației.
 */
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

    /**
     * Înregistrează un cont bancar nou în memoria aplicației (în interiorul structurii HashMap).
     *
     * @param cont Instanța de tip ContBancar ce trebuie salvată.
     */
    public void adaugaCont(ContBancar cont) {
        conturi.put(cont.getIban(), cont);
    }

    /**
     * Caută și returnează un cont bancar activ pe baza identificatorului unic.
     *
     * @param iban Șirul de caractere reprezentând IBAN-ul căutat.
     * @return Obiectul ContBancar dacă a fost găsit, sau null dacă nu există în memorie.
     */
    public ContBancar gasesteCont(String iban) {
        return conturi.get(iban);
    }

    public Map<String, ContBancar> getToateConturile() {
        return conturi;
    }

    /**
     * Stochează o înregistrare imuabilă a unui transfer în lista de tranzacții.
     *
     * @param tranzactie Instanța validă a tranzacției efectuate.
     */
    public void adaugaTranzactie(Tranzactie tranzactie) {
        tranzactii.add(tranzactie);
    }

    public List<Tranzactie> getToateTranzactiile() {
        return tranzactii;
    }

    /**
     * Extrage limita maximă de transfer per operațiune dintr-un vector unidimensional de tip double.
     *
     * @param tipCont Gradul clientului (0 = Standard, 1 = Premium, 2 = VIP).
     * @return Limita financiară maximă permisă (în RON).
     */
    public double getLimita(int tipCont) {
        return limiteTranzactionare[tipCont];
    }

    /**
     * Contorizează numărul de transferuri reușite sau eșuate pentru fiecare zi a lunii,
     * actualizând valorile direct în matricea bidimensională in-memory.
     *
     * @param ziuaDinLuna Ziua calendaristică curentă (valori între 1 și 31).
     * @param tranzactieReusita true dacă transferul s-a finalizat cu succes, false dacă a fost oprit de o validare.
     */
    public void inregistreazaStatistica(int ziuaDinLuna, boolean tranzactieReusita) {
        int index = ziuaDinLuna - 1;
        if (tranzactieReusita) {
            statisticiLuna[index][0]++;
        } else {
            statisticiLuna[index][1]++;
        }
    }

    /**
     * Accesează linia matricei aferentă unei zile și extrage situația de audit zilnică.
     *
     * @param ziuaDinLuna Ziua calendaristică interogată (1-31).
     * @return Un vector primitiv de 2 elemente: [număr tranzacții reușite, număr tranzacții eșuate].
     */
    public int[] getStatisticiZilnice(int ziuaDinLuna) {
        return statisticiLuna[ziuaDinLuna - 1];
    }

    /**
     * Metodă de persistență ce parsează fișierele text (conturi.txt, tranzactii.txt, statistici.txt)
     * și restaurează datele în structurile dinamice (Map, List, masive).
     * Este apelată la inițializarea aplicației pentru a asigura continuitatea datelor.
     * Folosește blocuri try-with-resources pentru închiderea automată a fluxurilor.
     */
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

    /**
     * Suprascrie fișierele text locale cu datele curente din memoria RAM.
     * Iterează prin toate colecțiile și transformă obiectele în linii text structurate (CSV-like).
     * Este apelată automat (prin ShutdownHook) la închiderea aplicației.
     */
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