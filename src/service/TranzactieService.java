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

/**
 * Componenta de Business Logic a aplicației (Service Layer).
 *
 * Intermediază operațiunile dintre interfața utilizatorului și depozitul de date.
 * Gestionează validarea strictă a datelor, aplică regulile de domeniu (ex. verificarea
 * limitelor din vectorii de tranzacționare) și orchestrează logic transferurile de fonduri.
 */
public class TranzactieService {
    private BancaRepository repository;

    public TranzactieService(BancaRepository repository) {
        this.repository = repository;
    }

    /**
     * Validează și înregistrează un cont nou în sistem.
     * Previne crearea conturilor cu date goale sau cu un identificator (IBAN) deja existent.
     *
     * @param iban           Identificatorul unic propus pentru noul cont.
     * @param numeUtilizator Numele titularului.
     * @param parola         Parola asociată contului.
     * @param soldInitial    Suma de bani depusă la deschidere.
     * @param tipCont        Clasa contului (0 = Standard, 1 = Premium, 2 = VIP).
     * @throws IllegalArgumentException Dacă câmpurile text sunt goale sau IBAN-ul este duplicat.
     */
    public void  inregistreazaCont(String iban, String numeUtilizator, String parola, double soldInitial, int tipCont) {
        if (iban.trim().isEmpty() || numeUtilizator.trim().isEmpty() || parola.trim().isEmpty()) {
            throw new IllegalArgumentException("Datele de inregistrare nu pot fi goale!");
        }

        if (repository.gasesteCont(iban) != null) {
            throw new IllegalArgumentException("Acest IBAN exista deja în sistem!");
        }

        ContBancar contNou = new ContBancar(iban, numeUtilizator, parola, soldInitial, tipCont);
        repository.adaugaCont(contNou);
    }

    /**
     * Autentifică un utilizator pe baza credențialelor.
     *
     * @param iban   Identificatorul unic al contului.
     * @param parola Parola introdusă de utilizator.
     * @return Instanța obiectului ContBancar dacă datele sunt corecte.
     * @throws ContInexistentException Dacă IBAN-ul nu există sau parola este incorectă.
     */
    public ContBancar autentificare(String iban, String parola) throws ContInexistentException {
        ContBancar cont = repository.gasesteCont(iban);
        if (cont == null || !cont.getParola().equals(parola)) {
            throw new ContInexistentException("Autentificare esuata: IBAN sau parola incorecte.");
        }
        return cont;
    }

    /**
     * Efectuează un transfer securizat de fonduri între două conturi in-memory.
     * Interoghează matricea de limite, actualizează statisticile și gestionează istoricul.
     *
     * Procesul de validare:
     * 1. Verifică dacă suma este strict pozitivă și conturile diferă.
     * 2. Verifică prezența în sistem a ambelor conturi.
     * 3. Validează suma în raport cu limita impusă de tipul contului sursă.
     * 4. Validează acoperirea sumei din soldul curent.
     * În caz de eșec la oricare pas, înregistrează eroarea în matrice și oprește execuția.
     *
     * @param ibanSursa      IBAN-ul contului din care se retrag banii.
     * @param ibanDestinatie IBAN-ul contului în care se depun banii.
     * @param suma           Suma de bani dorită pentru transfer.
     * @throws ContInexistentException      Dacă oricare dintre conturi nu este găsit.
     * @throws FonduriInsuficienteException Dacă soldul sursei nu acoperă transferul.
     * @throws IllegalArgumentException     Dacă suma este negativă, IBAN-urile coincid sau limita este depășită.
     */
    public void efectueazaTransfer(String ibanSursa, String ibanDestinatie, double suma)
            throws ContInexistentException, FonduriInsuficienteException {
        int ziuaCurenta = LocalDateTime.now().getDayOfMonth();
        if (suma <= 0) {
            repository.inregistreazaStatistica(ziuaCurenta, false);
            throw new IllegalArgumentException("Suma transferata trebuie sa fie mai mare decat zero!");
        }
        if (ibanSursa.equals(ibanDestinatie)) {
            repository.inregistreazaStatistica(ziuaCurenta, false);
            throw new IllegalArgumentException("Nu poti transfera bani catre propriul cont!");
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

    /**
     * Filtrează registrul general de tranzacții pentru a extrage exclusiv istoricul unui utilizator.
     *
     * @param iban Identificatorul unic al clientului pentru care se generează raportul.
     * @return O listă ce conține tranzacțiile în care clientul a fost fie sursă, fie destinație.
     */
    public List<Tranzactie> getIstoricCont(String iban) {
        List<Tranzactie> istoricPersonal = new ArrayList<>();
        for (Tranzactie t : repository.getToateTranzactiile()) {
            if (t.getIbanSursa().equals(iban) || t.getIbanDestinatie().equals(iban)) {
                istoricPersonal.add(t);
            }
        }
        return istoricPersonal;
    }

    /**
     * Construiește un text informativ analizând matricea bidimensională pentru ziua curentă.
     *
     * @return Raportul de sistem formatat (afișând eșecurile și succesele).
     */
    public String genereazaRaportZiuaCurenta() {
        int ziuaCurenta = LocalDateTime.now().getDayOfMonth();
        int[] statistici = repository.getStatisticiZilnice(ziuaCurenta);

        return "Raport de sistem pentru ziua " + ziuaCurenta + ":\n" +
                " -> Tranzactii Reusite: " + statistici[0] + "\n" +
                " -> Tranzactii Esuate (limite/fonduri): " + statistici[1];
    }
}
