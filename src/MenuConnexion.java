import AutoriteDeConfiance.HTTPServer;

import Cryptography.ELGAMAL.ElGamal;

import Cryptography.ELGAMAL.PairKeys;
import Cryptography.IBE.IBEBasicIdentScheme;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Properties;


import static Cryptography.ELGAMAL.ElGamal.elGamalDec_2;


public class MenuConnexion {
    // Déclaration des composants de l'interface graphique
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JPanel controlPanel;
    private JPanel buttonPanel; // Panneau pour les boutons
    private JTextField mailText; // Champ de texte pour l'email
    private JPasswordField passwordText; // Champ de mot de passe pour le mot de passe
    private JLabel emailLabel;
    private JLabel passwordLabel;
    private Store store;
    private JLabel statusLabel; // Label pour afficher des messages d'état

    private Element sk;
    private IBEBasicIdentScheme ibeParams = new IBEBasicIdentScheme();
    String email,password;

    public MenuConnexion() {
        prepareGUI(); // Appel de la méthode qui prépare l'interface graphique
    }

    public static void main(String[] args) {
        // Création de l'instance de MenuConnexion, ce qui lance l'interface de connexion
        MenuConnexion menuConnexion = new MenuConnexion();
    }

    // Configuration de l'interface graphique
    private void prepareGUI() {
        mainFrame = new JFrame("Menu Connexion");
        mainFrame.setSize(400, 400);

        // Utilisation d'un GridBagLayout pour un placement précis des composants
        mainFrame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0; // Permet de pousser les composants vers le centre vertical

        // Ajout d'un espace vide pour centrer verticalement les composants
        mainFrame.add(new JPanel(), gbc);

        // Configuration et ajout des labels et panels
        headerLabel = new JLabel("Page de connexion à la boite mail", JLabel.CENTER);
        gbc.gridy = 1;
        mainFrame.add(headerLabel, gbc);

        emailLabel = new JLabel("Adresse email :");
        passwordLabel = new JLabel("Mot de passe :");

        controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 5, 5, 5);

        mailText = new JTextField(20);
        passwordText = new JPasswordField(20);

        JButton connectionButton = new JButton("Se Connecter");

        // Configuration et action du bouton de connexion
        connectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Récupération de l'email et du mot de passe saisis
                email = mailText.getText();
                password = new String(passwordText.getPassword());

                Properties props = new Properties();
                props.put("mail.store.protocol", "imaps");
                props.put("mail.imaps.host", "outlook.office365.com");
                props.put("mail.imaps.port", "993");
                props.put("mail.imaps.starttls.enable", "true");

                try {
                    Session emailSession = Session.getInstance(props);
                    Store store = emailSession.getStore("imaps");
                    store.connect("outlook.office365.com", email, password);

                    // Connection successful, proceed with sending the token
                    sendToken(email);
                } catch (NoSuchProviderException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Provider error. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } catch (MessagingException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        // Ajout des composants au panel de contrôle
        constraints.gridx = 0;
        constraints.gridy = 0;
        controlPanel.add(emailLabel, constraints);

        constraints.gridx = 1;
        controlPanel.add(mailText, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        controlPanel.add(passwordLabel, constraints);

        constraints.gridx = 1;
        controlPanel.add(passwordText, constraints);

        // Ajout du panel de contrôle à la fenêtre principale
        gbc.gridy = 2;
        mainFrame.add(controlPanel, gbc);

        // Ajout des boutons au panel des boutons
        buttonPanel = new JPanel();
        buttonPanel.add(connectionButton);


        // Ajout du panel des boutons à la fenêtre principale
        gbc.gridy = 3;
        mainFrame.add(buttonPanel, gbc);

        // Configuration finale de la fenêtre principale
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void sendToken(String email) {
        String urlStringVerify = "https://127.0.0.1:8000/verifyEmail?token=" + email;
        String urlStringRequest = "https://127.0.0.1:8000/requestPrivateKey";
        String urlStringRegister ="https://127.0.0.1:8000/requestEmailVerification?token=" + email;

//        if (!sendHttpPostRequest(urlStringRequest,email)) {
//            // Si la vérification échoue, tentez de demander la clé privée
//            sendHttpRequest(urlStringRegister);
//        }
        PairingParameters params = PairingFactory.getPairingParameters("src/Parameters/curves/a.properties");
        Pairing pairing = PairingFactory.getPairing(params);
        Element generator = pairing.getG1().newRandomElement().getImmutable();
        PairKeys elGamalkey = ElGamal.keygen(pairing,generator);
        System.out.println(elGamalkey.getPubkey().toString());
        String elGamalPublicKeyEncoded = Base64.encodeBytes(elGamalkey.getPubkey().toBytes());
        Element elGamalPrivateKeyEncoded = elGamalkey.getSecretkey();// Supposons que vous avez déjà la clé publique ElGamal encodée en Base64 ou autre format.
        String generatorEncoded = Base64.encodeBytes(generator.toBytes());
        String postData = email + "\n" + elGamalPublicKeyEncoded + "\n" + generatorEncoded;
        System.out.println("Cle public el :"+ elGamalPublicKeyEncoded.toString() + "clé pv : "+ elGamalPrivateKeyEncoded.toString());

        boolean success = sendHttpRequest(urlStringRegister);
        if (success) {
            // Affichez à nouveau la fenêtre de connexion
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new MenuConnexion();
                }
            });

            // Fermez la fenêtre actuelle
            mainFrame.dispose();
            JOptionPane.showMessageDialog(mainFrame, "Un email de confirmation vous à été envoyé", "Encore un petit pas", JOptionPane.INFORMATION_MESSAGE);

            System.out.println("La requête a été envoyée avec succès.");
        } else {
            sendHttpPostRequest(urlStringRequest, postData,elGamalPrivateKeyEncoded);
            // Fermez la fenêtre de connexion
            mainFrame.dispose();

            // Ouverture du menu principal avec l'email et le mot de passe
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new MainMenu(email,password, sk);
                }
            });



        }
    }

    private boolean sendHttpRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setHostnameVerifier((hostname, session) -> true);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                @Override
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            } }, new SecureRandom());
            conn.setSSLSocketFactory(context.getSocketFactory());
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            System.out.println("Réponse HTTP : " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    System.out.println(response.toString());
                }
                return true;
            } else {
                System.out.println("La requête n'a pas réussi.");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la connexion à l'URL: " + urlString);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean sendHttpPostRequest(String urlString, String postData,Element elGamalPrivateKey) {
        try {
            URL url = new URL(urlString);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setHostnameVerifier((hostname, session) -> true);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                @Override
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            } }, new SecureRandom());
            conn.setSSLSocketFactory(context.getSocketFactory());

            // Configurer la requête pour POST
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/plain"); // Définir le type de contenu attendu
            conn.setDoOutput(true); // Activer l'envoi de corps de requête

            // Écrire les données (postData) dans le corps de la requête
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            System.out.println("naaaan");
            int responseCode = conn.getResponseCode();
            System.out.println("oueee");
            System.out.println("Réponse HTTP : " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {

                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine + "\n"); // Ajoutez un retour à la ligne pour conserver la structure
                    }

                    // Convertir la réponse complète en chaîne de caractères
                    String responseStr = response.toString();

                    // Séparer la réponse en ses différentes parties
                    String[] parts = responseStr.split("\n");

                    if (parts.length >= 4) {
                        // Décoder chaque partie de la réponse
                        byte[] uBytes = Base64.decode(parts[0]);
                        byte[] vBytes = Base64.decode(parts[1]);
                        byte[] pp0Bytes = Base64.decode(parts[2]);
                        byte[] pp1Bytes = Base64.decode(parts[3]);

                        // Vérifier si la longueur de la réponse est suffisante
                        if (uBytes.length == 0 || vBytes.length == 0 || pp0Bytes.length == 0 || pp1Bytes.length == 0) {
                            System.out.println("Erreur : la réponse du serveur est invalide");
                            return false;
                        }

                        // Déchiffrer la clé privée IBE à l'aide de la clé privée ElGamal
                        Element decryptedPrivateKeyIBEBytes;
                        try {
                            PairingParameters params = PairingFactory.getPairingParameters("src/Parameters/curves/a.properties");
                            Pairing pairing = PairingFactory.getPairing(params);
                            decryptedPrivateKeyIBEBytes = elGamalDec_2(pairing,uBytes, vBytes, elGamalPrivateKey);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Erreur lors du déchiffrement ElGamal");
                            return false;
                        }





                        // Stocker la clé privée IBE dans une variable
                        sk = decryptedPrivateKeyIBEBytes;
                        System.out.println("Clé privée IBE : " + sk);


                        return true;
                    } else {
                        System.out.println("Erreur : la réponse du serveur est invalide");
                        return false;
                    }


                }
            catch (IOException e) {
                    System.err.println("Erreur lors de la connexion à l'URL: " + urlString);
                    e.printStackTrace();
                }
                }
                return false;
            } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public static SSLContext createTrustAllSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        return sc;
    }


}

