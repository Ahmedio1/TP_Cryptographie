package AutoriteDeConfiance;

import Cryptography.ELGAMAL.ElgamalCipher;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;


import static Cryptography.ELGAMAL.ElGamal.elGamalEnc;


public class HTTPServer {
    private static final HashMap<String, Boolean> verifiedEmails = new HashMap<>();
    private static final HashMap<String, String> emailToTokenMap = new HashMap<>();
    private static final HashMap<String, String> tokenToEmailMap = new HashMap<>();
    private static final HashMap<String, Element> EmailToPubKeyMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        try {
            HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(8000), 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // On initialise la keystore
            char[] password = "Crypto2024".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("mykeystore.jks");
            ks.load(fis, password);

            // On met en place le manager Keystore
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // Set up the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // On initialise le SSL context
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // On initialise le SSL context
                        SSLContext c = getSSLContext();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // Mettre par defaut les paramètres default SSL
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                    } catch (Exception ex) {
                        System.err.println("Failed to create HTTPS port");
                    }
                }
            });
            IBEBasicIdentScheme ibeScheme = new IBEBasicIdentScheme();

            httpsServer.createContext("/requestEmailVerification", exchange -> {

                // Lire les données envoyées par le client
                String clientData = new String(exchange.getRequestBody().readAllBytes());
                // Diviser les données en deux parties : l'adresse email et la clé publique ElGamal
                String[] parts = clientData.split("\n", 2);

                if (parts.length >= 2) {
                    String email = parts[0];
                    // Charger les paramètres de la courbe utilisée pour ElGamal
                    PairingParameters pairingParams = PairingFactory.getPairingParameters("src/Parameters/curves/a.properties");
                    Pairing pairing = PairingFactory.getPairing(pairingParams);

                    // Décoder la clé publique ElGamal envoyée par le client
                    String base64PublicKey = parts[1].trim();
                    byte[] publicKeyBytes = Base64.decode(base64PublicKey);
                    Element elGamalPublicKey = pairing.getG1().newElement();
                    elGamalPublicKey.setFromBytes(publicKeyBytes);

                    // Stocker la clé publique ElGamal associée à l'adresse email
                    EmailToPubKeyMap.put(email, elGamalPublicKey);


                    // Vérifier si l'adresse email n'est pas vide et n'a pas déjà été vérifiée
                    if (!email.isEmpty() && !verifiedEmails.containsKey(email)) {
                        String token = generateVerificationToken(email);

                        try {
                            // Envoyer l'email de vérification
                            sendVerificationEmail(email, token);
                            sendResponse(exchange, 200, "Verification email sent to: " + email);
                        } catch (MessagingException e) {
                            sendResponse(exchange, 500, "Failed to send verification email.");
                        }
                    } else {
                        sendResponse(exchange, 400, "Invalid email or already verified.");
                    }
                } else {
                    sendResponse(exchange, 400, "Invalid request format.");
                }

            });

            httpsServer.createContext("/verifyEmail", exchange -> {
                // Récupérer le jeton de vérification de l'URL de la requête
                String query = exchange.getRequestURI().getQuery();
                String token = query != null && query.contains("=") ? query.split("=")[1] : "";

                // Vérifier si le jeton est correct et correspond à un utilisateur
                if (verifyToken(token)) {
                    String email = getEmailFromTokenMail(token);

                    // Marquer l'adresse email comme vérifiée
                    verifiedEmails.put(email, true);


                    String response = "Votre email à bien été verifié.";
                    sendResponse(exchange, 200, response);
                } else {
                    sendResponse(exchange, 400, "Error: Token invalide.");
                }
            });


            httpsServer.createContext("/requestPrivateKey", exchange -> {
                // Lire les données envoyées par le client
                String clientData = new String(exchange.getRequestBody().readAllBytes());
                String[] parts = clientData.split("\n", 2);

                String clientEmail = parts[0];

                // Vérifier si l'adresse e-mail a été vérifiée
                if (verifiedEmails.getOrDefault(clientEmail, true)) {
                    // Charger les paramètres de la courbe utilisée pour ElGamal et IBE
                    PairingParameters pairingParams = PairingFactory.getPairingParameters("src/Parameters/curves/a.properties");
                    Pairing pairing = PairingFactory.getPairing(pairingParams);

                    // Décoder la clé publique ElGamal envoyée par le client
                    Element generator = pairing.getG1().newElement();
                    generator.setFromBytes(Base64.decode(parts[1]));

                    // Récupérer la clé publique ElGamal de l'utilisateur
                    Element elGamalPublicKey = EmailToPubKeyMap.get(clientEmail);

                    // Générer la clé privée IBE pour l'identité (adresse e-mail) de l'utilisateur
                    Element privateKeyIBE = ibeScheme.genererClePriveePourID(clientEmail);

                    // Chiffrer la clé privée IBE avec la clé publique ElGamal de l'utilisateur
                    ElgamalCipher encryptedPrivateKey = elGamalEnc(pairing, generator, privateKeyIBE, elGamalPublicKey);

                    // Récupérer les paramètres publics du système IBE
                    Element[] PP = ibeScheme.Public_Parameters();

                    // Préparer la réponse incluant la clé privée IBE chiffrée et les paramètres publics
                    String responseStr = Base64.encodeBytes(encryptedPrivateKey.getU().toBytes()) + "\n" +
                            Base64.encodeBytes(encryptedPrivateKey.getV().toBytes()) + "\n" +
                            Base64.encodeBytes(PP[0].toBytes()) + "\n" +
                            Base64.encodeBytes(PP[1].toBytes());
                    byte[] responseBytes = responseStr.getBytes();

                    // Envoyer la réponse au client
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.write(responseBytes);
                    responseBody.close();

                } else {
                    // Si l'adresse e-mail n'a pas été vérifiée, renvoyer une erreur
                    String response = "Error: Email not verified.";
                    exchange.sendResponseHeaders(403, response.getBytes().length);
                }
            });

            httpsServer.start();
            System.out.println("Server is listening on port ");
        } catch (UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
    // Méthode pour envoyer une réponse HTTP au client
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    // Méthode pour vérifier si un jeton est valide
    private static boolean verifierToken(String token) {
        return verifiedEmails.containsKey(token) && verifiedEmails.get(token);
    }

    // Méthode pour récupérer l'adresse e-mail associée à un jeton
    private static String getEmailFromToken(String token) {
        return token;
    }

    // Méthode pour envoyer un e-mail de vérification à un utilisateur
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
        String verificationLink = "https://localhost:8000/verifyEmail?token=" + token;
        message.setContent("<h1>V&eacuterification de l'email </h1><p>Veuillez cliquez pour confirmer votre mail : <a href=\"" + verificationLink + "\">Verify</a></p>", "text/html");

        Transport.send(message);


        System.out.println("Verification email sent successfully...");
    }

    // Méthode pour générer un jeton de vérification unique
    public static String generateVerificationToken(String recipientEmail) {
        // Génère un token unique
        String token = UUID.randomUUID().toString();

        // Associe le token à l'email dans les deux sens pour une vérification facile plus tard
        emailToTokenMap.put(recipientEmail, token);
        tokenToEmailMap.put(token, recipientEmail);

        return token;
    }

    // Méthode pour vérifier si un jeton est valide
    public static boolean verifyToken(String token) {
        // Vérifie si le token existe et retourne true si c'est le cas
        return tokenToEmailMap.containsKey(token);
    }

    // Méthode pour récupérer l'adresse e-mail associée à un jeton
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