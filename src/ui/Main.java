package ui;

import exception.ContInexistentException;
import exception.FonduriInsuficienteException;
import model.ContBancar;
import repository.BancaRepository;
import service.TranzactieService;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        BancaRepository repository = new BancaRepository();
        repository.incarcaDate();

        TranzactieService service = new TranzactieService(repository);
        Scanner scanner = new Scanner(System.in);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Sistem] Aplicatia se inchide. Se salveaza datele in fișiere...");
            repository.salveazaDate();
        }));

        //Starea aplicatiei
        boolean ruleaza = true;
        ContBancar contLogat = null;

        System.out.println("=== Sistem E-Banking ASE ===");

        // 4. Bucla Principală (Meniul)
        while (ruleaza) {
            try {
                if (contLogat == null) {
                    // Meniul Public (Neautentificat)
                    System.out.println("\n--- Meniu Principal ---");
                    System.out.println("1. Logare");
                    System.out.println("2. Inregistrare cont nou");
                    System.out.println("0. Iesire");
                    System.out.print("Alege o opțiune: ");

                    String optiune = scanner.nextLine();

                    switch (optiune) {
                        case "1":
                            System.out.print("Introdu IBAN: ");
                            String iban = scanner.nextLine();
                            System.out.print("Introdu Parola: ");
                            String parola = scanner.nextLine();

                            // Prindem excepția custom definită de noi
                            contLogat = service.autentificare(iban, parola);
                            System.out.println("Autentificare reusita! Bun venit, " + contLogat.getNumeUtilizator() + "!");
                            break;

                        case "2":
                            System.out.print("Introdu un IBAN nou: ");
                            String ibanNou = scanner.nextLine();
                            System.out.print("Introdu Numele tau: ");
                            String nume = scanner.nextLine();
                            System.out.print("Introdu o parola: ");
                            String parolaNoua = scanner.nextLine();
                            System.out.print("Introdu suma de deschidere cont (RON): ");
                            double soldInitial = scanner.nextDouble();
                            scanner.nextLine(); //Curatam buffer-ul dupa citirea unui numar

                            service.inregistreazaCont(ibanNou, nume, parolaNoua, soldInitial);
                            System.out.println("Cont creat cu succes! Te poti loga acum.");
                            break;

                        case "0":
                            ruleaza = false;
                            break;

                        default:
                            System.out.println("Optiune invalida!");
                    }
                } else {
                    // Meniul Privat (Autentificat)
                    System.out.println("\n--- Meniu Cont: " + contLogat.getIban() + " ---");
                    System.out.println("1. Verifica soldul");
                    System.out.println("2. Efectueaza un transfer");
                    System.out.println("3. Delogare");
                    System.out.println("0. Iesire din aplicatie");
                    System.out.print("Alege o optiune: ");

                    String optiune = scanner.nextLine();

                    switch (optiune) {
                        case "1":
                            System.out.println("Soldul curent este: " + contLogat.getSold() + " RON");
                            break;

                        case "2":
                            System.out.print("Introdu IBAN-ul destinatarului: ");
                            String ibanDestinatie = scanner.nextLine();
                            System.out.print("Introdu suma de transferat: ");
                            double suma = scanner.nextDouble();
                            scanner.nextLine(); // Curățăm buffer-ul

                            // Apelăm serviciul și tratăm ambele excepții posibile
                            service.efectueazaTransfer(contLogat.getIban(), ibanDestinatie, suma);
                            System.out.println("Transfer realizat cu succes!");
                            break;

                        case "3":
                            contLogat = null;
                            System.out.println("Te-ai delogat cu succes.");
                            break;

                        case "0":
                            ruleaza = false;
                            break;

                        default:
                            System.out.println("Optiune invalida!");
                    }
                }
            } catch (ContInexistentException | FonduriInsuficienteException e) {
                System.out.println("[Eroare Bancara] " + e.getMessage());
            } catch (InputMismatchException e) {
                System.out.println("[Eroare de Input] Te rog sa introduci o valoare numerica valida!");
                scanner.nextLine(); // Curatam inputul gresit pentru a preveni blocarea buclei
            } catch (Exception e) {
                System.out.println("[Eroare Necunoscuta] A aparut o problema: " + e.getMessage());
            }
        }

        scanner.close();
        System.exit(0);
    }
}