package exception;

/**
 * Excepție personalizată (Checked Exception) aruncată în momentul în care
 * se încearcă efectuarea unui transfer cu o sumă care depășește soldul curent.
 * Asigură integritatea financiară a entităților de tip ContBancar,
 * prevenind tranzacțiile care ar genera o balanță negativă.
 */
public class FonduriInsuficienteException extends Exception {
    public FonduriInsuficienteException(String mesaj) {
        super(mesaj);
    }
}