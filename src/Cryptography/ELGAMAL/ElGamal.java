package Cryptography.ELGAMAL;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import java.util.Base64;

public class ElGamal {
    public static void main(String[] args) {

        PairingParameters params = PairingFactory.getPairingParameters("src/Parameters/curves/a.properties");
        Pairing pairing = PairingFactory.getPairing(params);
        Element gen = pairing.getZr().newRandomElement();

        KeyPair keys = generateKeyPair(pairing, gen);
        Element msg = pairing.getZr().newElement();

        CipherText cipher = encrypt(msg, keys.publicKey(), pairing, gen);

        Element decryptedMsg = decrypt(cipher, keys.privatekey());
        System.out.println("Message: " + Base64.getEncoder().encodeToString(msg.toBytes()));
        System.out.println("U: " + Base64.getEncoder().encodeToString(cipher.u().toBytes()));
        System.out.println("V: " + Base64.getEncoder().encodeToString(cipher.v().toBytes()));
        System.out.println("Decrypted Message: " + Base64.getEncoder().encodeToString(decryptedMsg.toBytes()));

        if (msg.isEqual(decryptedMsg)) {
            System.out.println("Decryption Successful!");
        } else {
            System.out.println("Decryption Failed.");
        }
    }

    public static KeyPair generateKeyPair(Pairing pairing, Element gen) {
        Element privKey = pairing.getZr().newRandomElement();
        Element pubKey = gen.duplicate().mulZn(privKey);

        return new KeyPair(privKey, pubKey);
    }

    public static CipherText encrypt(Element msg, Element pubKey, Pairing pairing, Element gen) {
        Element encKey = pairing.getZr().newRandomElement();

        Element u = gen.duplicate().mulZn(encKey);
        Element v = msg.duplicate().mulZn(pubKey.duplicate().mulZn(encKey).duplicate());

        return new CipherText(u, v);
    }

    public static Element decrypt(CipherText cipher, Element privKey) {
        Element decKey = cipher.u().duplicate().mulZn(privKey);
        Element invDecKey = decKey.duplicate().invert();
        Element msg = invDecKey.mulZn(cipher.v().duplicate());

        return msg;
    }

    public static byte[] elGamalDecrypt(byte[] uBytes, byte[] vBytes, Element privateKey) {
        try {
            // Charger les paramètres de couplage
            Pairing pairing = PairingFactory.getPairing("src/Parameters/curves/a.properties");
            // Convertir uBytes et vBytes en éléments du groupe
            Field G1 = pairing.getG1();
            Element u = G1.newElementFromBytes(uBytes);
            Element v = G1.newElementFromBytes(vBytes);

            CipherText cipher = new CipherText(u,v);
            System.out.println(u + " " + v);

            // Calculer l'inverse de u^privateKey
            Element uToSkInverse = u.powZn(privateKey).invert();

            // Multiplier v par l'inverse de u^privateKey pour obtenir le message déchiffré
            Element mDecrypted = v.mul(uToSkInverse);

            // Convertir l'élément déchiffré en bytes
            byte[] decryptedBytes = mDecrypted.toBytes();
            System.out.println(decryptedBytes.toString());

            return decryptedBytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}

