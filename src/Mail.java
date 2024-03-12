import java.io.File;

public class Mail {
    String description,destinataire,expediteur,objet;
    File piecejointe;

    public Mail(String description, File piecejointe, String destinataire, String expediteur, String objet) {
        this.description = description;
        this.piecejointe = piecejointe;
        this.destinataire = destinataire;
        this.expediteur = expediteur;
        this.objet = objet;
    }
}
