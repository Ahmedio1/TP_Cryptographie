package Cryptography.IBE;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;

import java.util.HashMap;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;
public class IBEBasicIdentScheme {

    // Variables de classe pour les champs cryptographiques.
    static protected Pairing pairing = PairingFactory.getPairing("src/Parameters/curves/a.properties");
    static protected Field Zr = pairing.getZr();
    static protected Field G = pairing.getG1();
    static protected Field GT = pairing.getGT();

    // Variables pour le générateur, la clé publique et la clé privée maîtresse.
    protected Element P;  //generateur
    protected Element clePublique;
    protected Element cle_Maitresse_privee;

    // Dictionnaire pour stocker les paires de clés utilisateurs.
    protected HashMap<String, KeyPair> pairesCles = new HashMap<>();

    // Liste pour stocker les identités (IDs) des utilisateurs.
    protected ArrayList<String> identites = new ArrayList<>();

    public IBEBasicIdentScheme() {
        genererPMKetP();
        this.clePublique = P.duplicate().mulZn(cle_Maitresse_privee);
    }

    public static Pairing getPairing() {
        return pairing;
    }

    public static Field getZr() {
        return Zr;
    }

    public static Field getG() {
        return G;
    }

    public static Field getGT() {
        return GT;
    }

    public Element getP() {
        return P;
    }

    public Element getclePublique() {
        return clePublique;
    }

    public Element getcle_Maitresse_privee() {
        return cle_Maitresse_privee;
    }

    public HashMap<String, KeyPair> getpairesCles() {
        return pairesCles;
    }

    public ArrayList<String> getidentites() {
        return identites;
    }

    protected void New_Set_Up_IBE() {
        genererPMKetP();
        this.clePublique = (this.P).duplicate().mulZn(this.cle_Maitresse_privee);
        //On reconstruit les clés privés et utilisateurs
        pairesCles.clear();
        build_HashMap();
    }


    //gere la lecture dans un fichier
    private void genererPMKetP() {
        // Logique pour générer cleMaitressePrivee et generateur
        // Fichier de configuration pour stocker la clé secrète
        String configFilePath = "src/Cryptography/IBE/PKM.properties";
        Properties proprietes = new Properties();
        try (InputStream in = new FileInputStream(configFilePath)) {
            proprietes.load(in);
            String cleMaitressePriveeEncodee = proprietes.getProperty("PKM");
            String generateurEncode = proprietes.getProperty("P");
            if (cleMaitressePriveeEncodee != null && !cleMaitressePriveeEncodee.isEmpty() && generateurEncode != null && !generateurEncode.isEmpty()) {
                this.cle_Maitresse_privee = Zr.newElementFromBytes(Base64.decode(cleMaitressePriveeEncodee));
                this.P = G.newElementFromBytes(Base64.decode(generateurEncode));
            } else {
                this.cle_Maitresse_privee = Zr.newRandomElement();
                this.P = G.newRandomElement();
                proprietes.setProperty("PKM", Base64.encodeBytes(this.cle_Maitresse_privee.toBytes()));
                proprietes.setProperty("P", Base64.encodeBytes(this.P.toBytes()));

                // Enregistrer les nouvelles clés dans le fichier
                FileOutputStream sortie = new FileOutputStream(configFilePath);
                proprietes.store(sortie, "Mise à jour des clés maîtresses et du générateur");
                sortie.close(); // Fermer le flux après utilisation
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Générer une clé privée pour un identifiant (ID) spécifique.
    public KeyPair genererClePriveePourID(String ID) {
        if (!pairesCles.containsKey(ID)) {
            byte[] bytesID = ID.getBytes();
            Element Qid = G.newElementFromHash(bytesID, 0, bytesID.length);
            Element skID = Qid.duplicate().mulZn(this.cle_Maitresse_privee);
            KeyPair paireCle = new KeyPair(ID, skID);
            this.pairesCles.put(ID, paireCle);
            return paireCle;
        } else {
            return this.pairesCles.get(ID);
        }
    }

    protected void build_HashMap() {
        for (String adresse : identites) {
            genererClePriveePourID(adresse);
        }
    }

    // La fonction XOR pour l'opération de chiffrement et déchiffrement
    protected byte[] XOR(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    public IBECipherText chiffrement(String ID, byte[] message) {
        IBECipherText C = new IBECipherText();
        Element r = pairing.getZr().newRandomElement(); // Générer un élément aléatoire r dans Zr
        Element U = P.duplicate().mulZn(r); // U = r * G

        // Convertir l'ID en bytes et calculer Qid via une fonction de hachage
        byte[] IDbytes = ID.getBytes(StandardCharsets.UTF_8);
        Element Qid = pairing.getG1().newElementFromHash(IDbytes, 0, IDbytes.length);

        // Appliquer le couplage bilinéaire e(Qid, Ppub) et élever au pouvoir r
        Element V = pairing.pairing(Qid, clePublique).powZn(r);

        // Effectuer un XOR du résultat avec le message en clair pour obtenir le message chiffré
        byte[] messageChiffre = XOR(message, V.toBytes());

        // Définir U et le message chiffré dans C
        C.setU(U);
        C.setV(messageChiffre);

        return C;
    }

    public byte[] dechiffrement(String ID, IBECipherText cipherText) {
        // Récupérer la paire de clés pour l'ID donné
        KeyPair paireCle = pairesCles.get(ID);
        if (paireCle == null) {
            throw new IllegalArgumentException("Aucune clé trouvée pour l'ID spécifié: " + ID);
        }
        // Utiliser la clé privée pour effectuer le déchiffrement
        Element skID = paireCle.getSk();
        // Calculer e(skID, U), où U est une partie du texte chiffré
        Element eSkIDU = pairing.pairing(skID, cipherText.getU());
        // Convertir le résultat du couplage en bytes et l'appliquer au texte chiffré avec XOR
        byte[] messageClairBytes = XOR(cipherText.getV(), eSkIDU.toBytes());
        // Retourner le message clair
        return messageClairBytes;
    }

    public class Main {

        public static void main(String[] args) {
            // Créer une instance du schéma IBE.
            IBEBasicIdentScheme ibeSchema = new IBEBasicIdentScheme();
            // Définir l'identifiant de l'utilisateur.
            String utilisateurID = "jalidyamina1@gmail.com";
            // Générer la clé privée pour l'identifiant donné.
            KeyPair paireCle = ibeSchema.genererClePriveePourID(utilisateurID);
            // Créer un message à chiffrer.
            String messageClair = "Bonjour, ceci est un message confidentiel.";
            // Chiffrer le message en utilisant l'identifiant et le message.
            IBECipherText texteChiffre = ibeSchema.chiffrement(utilisateurID, messageClair.getBytes(StandardCharsets.UTF_8));
            // Déchiffrer le message.
            byte[] messageDechiffre = ibeSchema.dechiffrement(utilisateurID, texteChiffre);
            // Afficher le message déchiffré.
            System.out.println("Message déchiffré: " + new String(messageDechiffre, StandardCharsets.UTF_8));
        }

    }
}
