package Client;

import Cryptography.ELGAMAL.ElGamal;
import Cryptography.ELGAMAL.KeyPair;
import it.unisa.dia.gas.jpbc.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import java.util.Base64;


public class ClientConnexion {
    protected String emailAddress;
    protected boolean connected;
    protected KeyPair elGamalKeyPair;
    public ClientConnexion () {
        emailAddress = "";
        connected = false;
    }

    public ClientConnexion(String emailAddress) {
        this.emailAddress = emailAddress;
        connected = false;
    }

    public void generateElGamalKeyPair() {
        PairingParameters pairingParams = PairingFactory.getPairingParameters("params/curves/a.properties");
        Pairing pairing = PairingFactory.getPairing(pairingParams);
        Element generator = pairing.getZr().newRandomElement();
        elGamalKeyPair = ElGamal.generateKeyPair(pairing, generator);
    }
    public void privateKeyRequest() {
        try {
            URL url = new URL("http://192.168.1.50:8080/privateKeyRequest");
            // URL url = new URL("https://www.google.com");
            URLConnection urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);

            OutputStream out = urlConn.getOutputStream();

            generateElGamalKeyPair();
            Element publicKey = elGamalKeyPair.publicKey();

            // Encoder la clé publique en Base64 avant de l'envoyer
            byte[] publicKeyBytes = publicKey.toBytes();
            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);
            out.write(publicKeyBase64.getBytes());


            InputStream dis = urlConn.getInputStream();
            System.out.println(Integer.parseInt(urlConn.getHeaderField("Content-length")));
            byte[] b=new byte[Integer.parseInt(urlConn.getHeaderField("Content-length"))];
            dis.read(b);

            String response=new String(b);
            System.out.println("message reçu du serveur:"+response);

        } catch (MalformedURLException ex) {
            Logger.getLogger(ClientConnexion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClientConnexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
