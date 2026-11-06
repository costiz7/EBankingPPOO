package exception;

/**
 * Excepție personalizată (Checked Exception) aruncată atunci când o operațiune
 * interoghează un cont bancar care nu există în depozitul de date.
 * Este utilizată în validările de business din cadrul fluxurilor de autentificare
 * și la inițierea transferurilor, pentru a opri execuția dacă IBAN-ul este invalid.
 */
public class ContInexistentException extends Exception {
    public ContInexistentException(String mesaj) {
        super(mesaj);
    }
}