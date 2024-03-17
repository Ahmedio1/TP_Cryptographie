package Cryptography.AES;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class AESEncryptingFile {


    public static void fileEncrypt(File inputFile, File outputFile, String secretKey) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] inputBytes = readFile(inputFile);
        byte[] encryptedBytes = AES.encryptV2(inputBytes, secretKey.getBytes());
        writeFile(outputFile, encryptedBytes);
    }

    public static void fileDecrypt(File inputFile, File outputFile, String secretKey) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] encryptedBytes = readFile(inputFile);
        byte[] decryptedBytes = AES.decryptV2(encryptedBytes, secretKey.getBytes());
        writeFile(outputFile, decryptedBytes);
    }

    private static byte[] readFile(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] fileBytes = new byte[(int) file.length()];
            int bytesRead = inputStream.read(fileBytes);
            if (bytesRead != fileBytes.length) {
                throw new IOException("File not completely read.");
            }
            return fileBytes;
        }
    }
    private static void writeFile(File file, byte[] bytes) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(bytes);
        }
    }

    public static void main(String[] args)
        {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your secret key: ");
            String secretKey = scanner.nextLine();
            File dir = new File("/Users/jalidyamina/Desktop/TP_Cryptographie/encryptedFiles");
            dir.mkdirs();

            System.out.print("Enter path of file to encrypt: ");
            File inputFile = new File(scanner.nextLine());

            System.out.print("Enter path of encrypted file: ");
            File encryptedFile = new File(dir, "encrypted_" + inputFile.getName());

            System.out.print("Enter path of decrypted file: ");
            File decryptedFile = new File(scanner.nextLine());
            try {
                fileEncrypt(inputFile, encryptedFile, secretKey);
                // fileDecrypt(encryptedFile, decryptedFile, secretKey);

                System.out.println("File encrypted and decrypted successfully.");
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (NoSuchPaddingException e) {
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