package Cryptography.AES;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class AES {


    private static final String CHAR_LIST =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int RANDOM_STRING_LENGTH = 10;
    private static int getRandomNumber() {
        int randomInt = 0;
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(CHAR_LIST.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }
    public static String randomString(){
        StringBuffer randomString = new StringBuffer();
        for(int i=0; i<RANDOM_STRING_LENGTH; i++){
            int number = getRandomNumber();
            char ch = CHAR_LIST.charAt(number);
            randomString.append(ch);
        }
        return randomString.toString();
    }

    public static byte[] encrypt(String m, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        //méthode de chiffrement AES d'un message m en utilisant la clef key

        Cipher cipher= Cipher.getInstance("AES/ECB/PKCS5Padding");
        MessageDigest digest=MessageDigest.getInstance("SHA1");
        digest.update(key);
        byte[] AESkey= Arrays.copyOf(digest.digest(),16);
        SecretKeySpec keyspec=new SecretKeySpec(AESkey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keyspec);
        byte[] ciphertext= Base64.getEncoder().encode(cipher.doFinal(m.getBytes("UTF-8")));

        return ciphertext;
    }


    public static String decrypt(byte[] ciphertext, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException{
        //méthode de déchiffrement AES d'un ciphertext en utilisant la clef key

        Cipher cipher= Cipher.getInstance("AES/ECB/PKCS5Padding");
        MessageDigest digest=MessageDigest.getInstance("SHA1");
        digest.update(key);
        byte[] AESkey=Arrays.copyOf(digest.digest(),16);
        SecretKeySpec keyspec=new SecretKeySpec(AESkey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keyspec);
        String plaintext=new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)));

        return plaintext;

    }

    /****pour les Files****/
    public static byte[] encryptV2(byte[] m, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException{

        Cipher cipher= Cipher.getInstance("AES/ECB/PKCS5Padding");
        MessageDigest digest=MessageDigest.getInstance("SHA1");
        digest.update(key);
        byte[] AESkey=Arrays.copyOf(digest.digest(),16);
        SecretKeySpec keyspec=new SecretKeySpec(AESkey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keyspec);
        byte[] ciphertext=Base64.getEncoder().encode(cipher.doFinal(m));

        return ciphertext;
    }

    public static byte[] decryptV2(byte[]ciphertext, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher= Cipher.getInstance("AES/ECB/PKCS5Padding");
        MessageDigest digest=MessageDigest.getInstance("SHA1");
        digest.update(key);
        byte[] AESkey=Arrays.copyOf(digest.digest(),16);
        SecretKeySpec keyspec=new SecretKeySpec(AESkey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keyspec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));

        return decryptedBytes;
    }
}
