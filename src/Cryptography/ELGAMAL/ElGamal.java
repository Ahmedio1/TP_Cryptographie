package Cryptography.ELGAMAL;

import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.util.Arrays;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/*
 * Classe permettant le chiffrement avec ElGamal basé sur les courbes elliptiques
 */
public class ElGamal {
    /*
     * Génère une paire de clés publique et privée à partir d'un générateur de la
     * courbe elliptique
     */
    public static PairKeys keygen(Pairing pairing, Element generator) {
        Element secretKey = pairing.getZr().newRandomElement();
        Element publicKey = generator.powZn(secretKey);
        return new PairKeys(publicKey, secretKey);
    }

    /*
     * Chiffre un message en utilisant la clé publique du destinataire
     */
    public static ElgamalCipher elGamalEnc(Pairing pairing, Element generator, Element message, Element publicKey) {
        List<ElgamalCipher> cipherParts = new ArrayList<>();
        byte[] messageBytes = ((Point) message).toBytesX();

        Element messagePartPoint = messageToPoint(pairing, messageBytes);
        Element randomElement = pairing.getZr().newRandomElement();
        Element U = generator.powZn(randomElement);
        Element V = messagePartPoint.mul(publicKey.powZn(randomElement));
        ElgamalCipher cipher = new ElgamalCipher(U, V);

        return cipher;
    }

    /*
     * Déchiffre un message chiffré en utilisant la clé privée du destinataire
     */
    public static Element elGamalDec_2(Pairing pairing, byte[] uBytes, byte[] vBytes, Element privateKey) {
        StringBuilder decryptedMessageBuilder = new StringBuilder();
        byte[] decryptedPointByte = new byte[128];
        // initialisation de la variable real à une valeur quelconque
        Element real = pairing.getG1().newRandomElement();
        Element U = pairing.getG1().newElementFromBytes(uBytes);
        Element V = pairing.getG1().newElementFromBytes(vBytes);

        System.out.println(V);

        Element sharedSecret = U.powZn(privateKey).invert();
        Element decryptedPoint = V.mul(sharedSecret);
        decryptedPointByte = ((Point) decryptedPoint).toBytesX();

        real.setFromBytes(decryptedPointByte);
     

        decryptedMessageBuilder.append(pointToMessage(decryptedPoint));

        return real;
    }

    /*
     * Permet d'attribuer un point sur le courbe elliptique à un message
     * initialement sous forme de tableau
     * de byte
     */
    private static Element messageToPoint(Pairing pairing, byte[] message) {
        Field G1 = pairing.getG1();
        Element point = G1.newElement();

        // Utilise seulement la partie nécessaire des bytes pour créer un point
        byte[] xBytes = new byte[((Point) point).getLengthInBytesX()];
        System.arraycopy(message, 0, xBytes, 0, Math.min(message.length, xBytes.length));
        ((Point) point).setFromBytesX(xBytes);
        return point;
    }

    /*
     * Réalise l'opération inverse de la méthode précédente
     */
    private static byte[] pointToMessage(Element point) {
        byte[] xBytes = ((Point) point).getX().toBytes();
        return xBytes;
    }

}
