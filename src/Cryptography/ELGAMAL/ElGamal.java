package Cryptography.ELGAMAL;

import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.util.Arrays;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ElGamal {

    public static PairKeys keygen(Pairing pairing, Element generator) {
        Element secretKey = pairing.getZr().newRandomElement();
        Element publicKey = generator.powZn(secretKey);
        return new PairKeys(publicKey, secretKey);
    }

    public static ElgamalCipher elGamalEnc(Pairing pairing, Element generator, Element message, Element publicKey) {
        List<ElgamalCipher> cipherParts = new ArrayList<>();
        byte[] messageBytes = ((Point)message).toBytesX();


        Element messagePartPoint = messageToPoint(pairing, messageBytes);
        Element randomElement = pairing.getZr().newRandomElement();
        Element U = generator.powZn(randomElement);
        Element V = messagePartPoint.mul(publicKey.powZn(randomElement));
        ElgamalCipher cipher =  new ElgamalCipher(U, V);

        return cipher;
    }
    public static Element elGamalDec(Pairing pairing, ElgamalCipher cipher, Element privateKey) {
        StringBuilder decryptedMessageBuilder = new StringBuilder();
        byte[] decryptedPointByte =new byte[128];
        Element real = pairing.getG1().newRandomElement();
        // Déchiffrer chaque partie et les concaténer pour reconstituer le message

        Element U = cipher.getU();
        Element V = cipher.getV();



        Element sharedSecret = U.powZn(privateKey).invert();
        Element decryptedPoint = V.mul(sharedSecret);
        decryptedPointByte = ((Point) decryptedPoint).toBytesX();

        real.setFromBytes(decryptedPointByte);
        // System.out.println("Real : " + real);

        decryptedMessageBuilder.append(pointToMessage(decryptedPoint));

        return real;
    }

    public static Element elGamalDec_2(Pairing pairing, byte[] uBytes, byte[] vBytes, Element privateKey) {
        StringBuilder decryptedMessageBuilder = new StringBuilder();
        byte[] decryptedPointByte =new byte[128];
        Element real = pairing.getG1().newRandomElement();
        // Déchiffrer chaque partie et les concaténer pour reconstituer le message

        Element U = pairing.getG1().newElementFromBytes(uBytes);
        Element V = pairing.getG1().newElementFromBytes(vBytes);

        System.out.println(V);

        Element sharedSecret = U.powZn(privateKey).invert();
        Element decryptedPoint = V.mul(sharedSecret);
        decryptedPointByte = ((Point) decryptedPoint).toBytesX();

        real.setFromBytes(decryptedPointByte);
        // System.out.println("Real : " + real);

        decryptedMessageBuilder.append(pointToMessage(decryptedPoint));

        return real;
    }
    private static Element messageToPoint(Pairing pairing, byte[] message) {
        // byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        Field G1 = pairing.getG1();
        Element point = G1.newElement();

        // Utilisez seulement la partie nécessaire des bytes pour créer un point
        byte[] xBytes = new byte[((Point) point).getLengthInBytesX()];
        System.arraycopy(message, 0, xBytes, 0, Math.min(message.length, xBytes.length));

        ((Point) point).setFromBytesX(xBytes);

        return point;
    }


    private static byte[] pointToMessage(Element point) {
        // Element X = ((Point) point).getX();
        // System.out.println("X: " + X);
        byte[] xBytes = ((Point) point).getX().toBytes();


        // Supposons que le message est une chaîne de caractères ASCII, pas de zeros non significatifs
        return xBytes;
    }


}
