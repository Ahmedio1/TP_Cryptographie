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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static Cryptography.ELGAMAL.ElGamal.encrypt;

public class HTTPServer {
    private static final HashMap<String, Boolean> verifiedEmails = new HashMap<>();

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            IBEBasicIdentScheme ibeScheme = new IBEBasicIdentScheme();

            server.createContext("/verifyEmail", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String token = query != null && query.contains("=") ? query.split("=")[1] : "";

                // On vérifie si le token est correct et correspond à un utilisateur
                if (verifierToken(token)) {
                    String email = getEmailFromToken(token);
                    verifiedEmails.put(email, true);

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

                if (verifiedEmails.getOrDefault(clientEmail, false)) {

                    PairingParameters pairingParams = PairingFactory.getPairingParameters("src/Parameters/curves/a.properties");
                    Pairing pairing = PairingFactory.getPairing(pairingParams);
                    Element elGamalPublicKey = pairing.getZr().newElement();
                    Element generator = pairing.getZr().newRandomElement();
                    elGamalPublicKey.setFromBytes(Base64.decode(parts[1]));

                    System.out.println(clientEmail);
                    System.out.println(elGamalPublicKey);

                    Element privateKeyIBE = ibeScheme.genererClePriveePourID(clientEmail);
                    //Chiffrement de la clé privée IBE avec la clé publique ElGamal de l'utilisateur
                    CipherText encryptedPrivateKey = encrypt(privateKeyIBE, elGamalPublicKey, pairing, generator);

                    // Récupération des paramètres publics du système IBE
                    Element[] PP = ibeScheme.Public_Parameters();

                    // Préparation de la réponse incluant la clé privée IBE chiffrée et les paramètres publics
                    String responseStr = Base64.encodeBytes(encryptedPrivateKey.u().toBytes()) + "\n" +
                            Base64.encodeBytes(encryptedPrivateKey.v().toBytes()) + "\n" +
                            Base64.encodeBytes(PP[0].toBytes()) + "\n" +
                            Base64.encodeBytes(PP[1].toBytes());
                    byte[] responseBytes = responseStr.getBytes();

                    exchange.sendResponseHeaders(200, responseBytes.length);

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
}
