import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class User {
    String nom,prenom,mail;
    HashMap<String, ArrayList<Mail>> mails;
    private String pk;

    public User(String nom, String prenom, String mail){
        this.nom=nom;
        this.prenom=prenom;
        this.mail=mail;
        this.mails = new HashMap<>();
    }

    public void ajouterMail(Mail mail) {
        // Utiliser l'adresse mail du destinataire comme clé pour regrouper les mails.
        ArrayList<Mail> listeMails = this.mails.getOrDefault(mail.destinataire, new ArrayList<>());
        listeMails.add(mail);
        this.mails.put(mail.destinataire, listeMails);
    }

    public ArrayList<Mail> getMails(String adresseMail) {
        return this.mails.getOrDefault(adresseMail, new ArrayList<>());
    }


}
