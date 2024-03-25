package AutoriteDeConfiance;

import Cryptography.ELGAMAL.CipherText;
import Cryptography.IBE.IBEBasicIdentScheme;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static Cryptography.ELGAMAL.ElGamal.encrypt;

public class HTTPServer {
    private static final HashMap<String, Boolean> verifiedEmails = new HashMap<>();
    private static final HashMap<String, String> emailToTokenMap = new HashMap<>();
    private static final HashMap<String, String> tokenToEmailMap = new HashMap<>();

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            IBEBasicIdentScheme ibeScheme = new IBEBasicIdentScheme();
            verifiedEmails.put("tp_crypto_2024@outlook.com",true);
            verifiedEmails.put("tp_crypto_2_2024@outlook.com",true);

            server.createContext("/requestEmailVerification", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String email = query != null && query.contains("=") ? query.split("=")[1] : "";
                System.out.println(email);
                if (email != null && !email.isEmpty() && !verifiedEmails.containsKey(email)) {
                    String token = generateVerificationToken(email);
                    // Envoie l'email de vérification
                    try {
                        sendVerificationEmail(email, token);
                        sendResponse(exchange, 200, "Verification email sent to: " + email);
                    } catch (MessagingException e) {
                        sendResponse(exchange, 500, "Failed to send verification email.");
                    }
                } else {
                    sendResponse(exchange, 400, "Invalid email or already verified.");
                }
            });

            server.createContext("/verifyEmail", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String token = query != null && query.contains("=") ? query.split("=")[1] : "";
                System.out.println(token);

                // On vérifie si le token est correct et correspond à un utilisateur
                if (verifyToken(token)) {
                    String email = getEmailFromTokenMail(token);
                    verifiedEmails.put(email, true);
                    System.out.println("verify "+verifiedEmails.get(email).toString());

                    String response = "Your email has now been verified.";
                    sendResponse(exchange, 200, response);
                } else {
                    sendResponse(exchange, 400, "Error: Invalid Token.");
                }
            });


            server.createContext("/requestPrivateKey", exchange -> {
                String clientData = new String(exchange.getRequestBody().readAllBytes());
                String[] parts = clientData.split("\n", 2);
                String clientEmail = parts[0];

                if (verifiedEmails.getOrDefault(clientEmail, true)) {
                    PairingParameters pairingParams = PairingFactory.getPairingParameters("src/Parameters/curves/a.properties");
                    Pairing pairing = PairingFactory.getPairing(pairingParams);

                    String base64PublicKey = parts[1].trim();
                    byte[] publicKeyBytes = Base64.decode(base64PublicKey);
                    Element elGamalPublicKey = pairing.getG1().newElement();
                    Element generator = pairing.getG1().newRandomElement();
                    elGamalPublicKey.setFromBytes(publicKeyBytes);

                    System.out.println(clientEmail);
                    System.out.println("cle public elgamal "+elGamalPublicKey);

                    Element privateKeyIBE = ibeScheme.genererClePriveePourID(clientEmail);
                    System.out.println("clepv IBE "+ privateKeyIBE.toString());
                    //Chiffrement de la clé privée IBE avec la clé publique ElGamal de l'utilisateur
                    CipherText encryptedPrivateKey = encrypt(privateKeyIBE, elGamalPublicKey, pairing, generator);

                    // Récupération des paramètres publics du système IBE
                    Element[] PP = ibeScheme.Public_Parameters();

                    // Préparation de la réponse incluant la clé privée IBE chiffrée et les paramètres publics
                    System.out.println(encryptedPrivateKey.u() + " " + encryptedPrivateKey.v());
                    String responseStr = Base64.encodeBytes(encryptedPrivateKey.u().toBytes()) + "\n" +
                            Base64.encodeBytes(encryptedPrivateKey.v().toBytes()) + "\n" +
                            Base64.encodeBytes(PP[0].toBytes()) + "\n" +
                            Base64.encodeBytes(PP[1].toBytes());
                    byte[] responseBytes = responseStr.getBytes();
                    System.out.println(responseBytes.length);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.write(responseBytes); // Écrire les bytes de la réponse
                    responseBody.close(); // Fermer le OutputStream pour terminer l'envoi de la réponse


                } else {
                    String response = "Error: Email not verified.";
                    exchange.sendResponseHeaders(403, response.getBytes().length);
                }
            });

            server.start();
            System.out.println("Server is listening on port " );
        } catch (IOException e) {
            Logger.getLogger(HTTPServer.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    private static boolean verifierToken(String token) {
        return verifiedEmails.containsKey(token) && verifiedEmails.get(token);
    }
    private static String getEmailFromToken(String token) {
        return token;
    }

    public static void sendVerificationEmail(String recipientEmail, String token) throws MessagingException {
        String host = "smtp.office365.com"; // À remplacer par votre serveur SMTP
        final String senderEmail = "tp_crypto_2024@outlook.com"; // À remplacer par votre adresse email
        final String senderPassword = "Crypto2024"; // À remplacer par votre mot de passe

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "587"); // Port pour TLS/STARTTLS
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject("Email Verification");
        String verificationLink = "http://localhost:8000/verifyEmail?token=" + token;
        message.setContent("<h1>Email Verification</h1><p>Please click the link to verify your email: <a href=\"" + verificationLink + "\">Verify</a></p>", "text/html");

        Transport.send(message);
        System.out.println("Verification email sent successfully...");
    }

    public static String generateVerificationToken(String recipientEmail) {
        // Génère un token unique
        String token = UUID.randomUUID().toString();

        // Associe le token à l'email dans les deux sens pour une vérification facile plus tard
        emailToTokenMap.put(recipientEmail, token);
        tokenToEmailMap.put(token, recipientEmail);

        return token;
    }

    public static boolean verifyToken(String token) {
        // Vérifie si le token existe et retourne true si c'est le cas
        return tokenToEmailMap.containsKey(token);
    }

    public static String getEmailFromTokenMail(String token) {
        // Récupère l'email associé au token
        return tokenToEmailMap.get(token);
    }

    // Méthode pour marquer l'email comme vérifié, à appeler après la vérification réussie
    public static void markEmailAsVerified(String token) {
        if (verifyToken(token)) {
            String email = getEmailFromToken(token);

        }

    }
}