package Mail;

import Cryptography.AES.AES;
import Cryptography.AES.AESEncryptingFile;
import Cryptography.IBE.IBECipherText;
import Cryptography.IBE.IBEBasicIdentScheme;
import it.unisa.dia.gas.jpbc.Element;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;


public class SendReceiveMail {


    protected IBEBasicIdentScheme parametresPublics = new IBEBasicIdentScheme();
    protected File dossierFichiersChiffres;
    protected File dossierFichiersDechiffres;

    public SendReceiveMail(){}

    //Create file that contains information about AES key
    public File preparerInfosDechiffrementAES(String nomFichier, String cleAES, String adresseDestinataire) throws IOException {
        byte[] cleAESBytes = cleAES.getBytes(StandardCharsets.UTF_8);
        IBECipherText cipher = this.parametresPublics.chiffrement(this.parametresPublics.getP(), this.parametresPublics.getclePublique(),adresseDestinataire, cleAES);
        Element u = cipher.getU();
        byte[] v = cipher.getV();
        File AESInfos = new File(this.dossierFichiersChiffres,  nomFichier + ".properties" /*nomFichier.replaceFirst("[.][^.]+$", ".properties")*/);
        AESInfos.createNewFile();
        Properties props = new Properties();
        props.load(new FileInputStream(AESInfos));
        byte[] inputBytesU = u.toBytes();
        String encodedU = Base64.getEncoder().encodeToString(inputBytesU);
        String encodeV = Base64.getEncoder().encodeToString(v);
        props.put("u", encodedU);
        props.put("v", encodeV);
        FileOutputStream outputStream = new FileOutputStream(AESInfos);
        props.store(outputStream, "AES secret key");

        return AESInfos;
    }

    public File dechiffrerPieceJointe(File fichierJoint, File fichierInfosAES, Element clePrivee) throws Exception {
        Properties proprietiesAES = new Properties();
        try (FileInputStream fis = new FileInputStream(fichierInfosAES)) {
            proprietiesAES.load(fis);
        }

        // Extraire et décoder les éléments U et V
        byte[] octetsU = Base64.getDecoder().decode(proprietiesAES.getProperty("u"));
        byte[] octetsV = Base64.getDecoder().decode(proprietiesAES.getProperty("v"));

        // Reconstruire l'élément U
        Element elementU = parametresPublics.getG().newElement();
        elementU.setFromBytes(octetsU);

        // Déchiffrer la clé AES avec IBE private key
        IBECipherText cipherText = new IBECipherText(elementU, octetsV);

        byte[] cleAESOctets = parametresPublics.dechiffrement(this.parametresPublics.getP(),this.parametresPublics.getP(),clePrivee, cipherText);
        String cleAES = new String(cleAESOctets);

        // Préparer le fichier déchiffré
        String nomOriginalFichier = fichierJoint.getName().replaceAll("\\.chiffre$", "");
        File fichierDechiffre = new File(dossierFichiersDechiffres, nomOriginalFichier);

        // Déchiffrer le fichier
        AESEncryptingFile.fileDecrypt(fichierJoint, fichierDechiffre, cleAES);

        return fichierDechiffre;
    }
    public File[] chiffrePieceJointe(String adresseDestinataire, File pieceJointe) {
        try {

            String cleAES = AES.randomString();
            File FichierChiffree = new File(this.dossierFichiersChiffres, pieceJointe.getName() + ".chiffre");
            FichierChiffree.createNewFile();
            AESEncryptingFile.fileEncrypt(pieceJointe,FichierChiffree,cleAES );

            File AESInfos = preparerInfosDechiffrementAES(pieceJointe.getName(), cleAES, adresseDestinataire );
            return new File[]{FichierChiffree, AESInfos};

        } catch (NoSuchPaddingException | IOException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

}






