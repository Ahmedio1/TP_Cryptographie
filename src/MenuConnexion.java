import AutoriteDeConfiance.HTTPServer;
import Cryptography.ELGAMAL.CipherText;
import Cryptography.ELGAMAL.ElGamal;
import Cryptography.ELGAMAL.KeyPair;
import Cryptography.IBE.IBEBasicIdentScheme;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

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

    private JLabel statusLabel; // Label pour afficher des messages d'état

    private Element sk;
    private IBEBasicIdentScheme ibeParams = new IBEBasicIdentScheme();


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
        JButton pwdButton = new JButton("Mot de passe oublié ?");

        // Configuration et action du bouton de connexion
        connectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Récupération de l'email et du mot de passe saisis
                String email = mailText.getText();
                String password = new String(passwordText.getPassword());
                sendToken(email);

                // Fermeture de la fenêtre de connexion
                mainFrame.dispose();

                // Ouverture du menu principal avec l'email et le mot de passe
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new MainMenu(email, password,sk);
                    }
                });
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
        buttonPanel.add(pwdButton);

        // Ajout du panel des boutons à la fenêtre principale
        gbc.gridy = 3;
        mainFrame.add(buttonPanel, gbc);

        // Configuration finale de la fenêtre principale
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void sendToken(String email) {
        String urlStringVerify = "http://127.0.0.1:8000/verifyEmail?token=" + email;
        String urlStringRequest = "http://127.0.0.1:8000/requestPrivateKey";
        String urlStringRegister ="http://127.0.0.1:8000/requestEmailVerification?token=" + email;

//        if (!sendHttpPostRequest(urlStringRequest,email)) {
//            // Si la vérification échoue, tentez de demander la clé privée
//            sendHttpRequest(urlStringRegister);
//        }
        PairingParameters params = PairingFactory.getPairingParameters("src/Parameters/curves/a.properties");
        Pairing pairing = PairingFactory.getPairing(params);
        Element gen = pairing.getZr().newRandomElement();
        KeyPair elGamalkey = ElGamal.generateKeyPair(pairing,gen);
        System.out.println(elGamalkey.publicKey().toString());
        String elGamalPublicKeyEncoded = it.unisa.dia.gas.plaf.jpbc.util.io.Base64.encodeBytes(elGamalkey.publicKey().toBytes());
        Element elGamalPrivateKeyEncoded = elGamalkey.privatekey();// Supposons que vous avez déjà la clé publique ElGamal encodée en Base64 ou autre format.
        String postData = email + "\n" + elGamalPublicKeyEncoded;
        System.out.println("Cle public el :"+ elGamalPublicKeyEncoded.toString() + "clé pv : "+ elGamalPrivateKeyEncoded.toString());

        boolean success = sendHttpPostRequest(urlStringRequest, postData,elGamalPrivateKeyEncoded);
        if (success) {
            // Traitez la réussite de l'envoi
            System.out.println("La requête a été envoyée avec succès.");
        } else {
            sendHttpRequest(urlStringRegister);
            System.out.println("La requête a échoué.");
        }
    }

    private boolean sendHttpRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
        }
        return false;
    }

    private boolean sendHttpPostRequest(String urlString, String postData,Element elGamalPrivateKey) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Configurer la requête pour POST
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/plain"); // Définir le type de contenu attendu
            conn.setDoOutput(true); // Activer l'envoi de corps de requête

            // Écrire les données (postData) dans le corps de la requête
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
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
                        byte[] uBytes = Base64.getDecoder().decode(parts[0]);
                        byte[] vBytes = Base64.getDecoder().decode(parts[1]);
                        byte[] pp0Bytes = Base64.getDecoder().decode(parts[2]);
                        byte[] pp1Bytes = Base64.getDecoder().decode(parts[3]);

                        // Vérifier si la longueur de la réponse est suffisante
                        if (uBytes.length == 0 || vBytes.length == 0 || pp0Bytes.length == 0 || pp1Bytes.length == 0) {
                            System.out.println("Erreur : la réponse du serveur est invalide");
                            return false;
                        }

                        // Déchiffrer la clé privée IBE à l'aide de la clé privée ElGamal
                        byte[] decryptedPrivateKeyIBEBytes;
                        try {
                            decryptedPrivateKeyIBEBytes = ElGamal.elGamalDecrypt(uBytes, vBytes, elGamalPrivateKey);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Erreur lors du déchiffrement ElGamal");
                            return false;
                        }

                        // Convertir les bytes déchiffrés en un élément de l'espace Zr du groupe bilinéaire
                        Pairing pairing = PairingFactory.getPairing("src/Parameters/curves/a.properties");
                        Element privateKeyIBE = pairing.getZr().newElementFromBytes(decryptedPrivateKeyIBEBytes);

                        // Vérifier si la clé privée IBE est valide
                        if (privateKeyIBE.isZero()) {
                            System.out.println("Erreur : la clé privée IBE est invalide");
                            return false;
                        }

                        // Stocker la clé privée IBE dans une variable
                        sk = privateKeyIBE;
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
        }
    }
    }

