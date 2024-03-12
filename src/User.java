import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class User {
    String nom,prenom,mail,password;
    HashMap<String, ArrayList<Mail>> mails;
    private String pk;


    public User(String nom, String prenom, String mail,String password){
        this.nom=nom;
        this.prenom=prenom;
        this.mail=mail;
        this.password=password;
        this.mails = new HashMap<>();
    }

    public void ajouterMailDestinataire(Mail mail) {
        // Utiliser l'adresse mail du destinataire comme clé pour regrouper les mails.
        ArrayList<Mail> listeMails = this.mails.getOrDefault(mail.destinataire, new ArrayList<>());
        listeMails.add(mail);
        this.mails.put(mail.destinataire, listeMails);
    }

    public void ajouterMailExpediteur(Mail mail) {
        // Utiliser l'adresse mail du destinataire comme clé pour regrouper les mails.
        ArrayList<Mail> listeMails = this.mails.getOrDefault(mail.expediteur, new ArrayList<>());
        listeMails.add(mail);
        this.mails.put(mail.expediteur, listeMails);
    }
    public ArrayList<Mail> getMails(String adresseMail) {
        return this.mails.getOrDefault(adresseMail, new ArrayList<>());
    }


}
