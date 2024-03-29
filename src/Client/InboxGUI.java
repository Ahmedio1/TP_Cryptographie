package Client;

import Cryptography.IBE.IBEBasicIdentScheme;
import it.unisa.dia.gas.jpbc.Element;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import Mail.SendReceiveMail;


public class InboxGUI {
    private JFrame frame; // La fenêtre principale de l'application.
    private JList<String> listMails; // La liste affichant les e-mails reçus.
    private JTextArea mailContent; // La zone de texte affichant le contenu de l'e-mail sélectionné.
    private DefaultListModel<String> listModel; // Le modèle de données pour la `listMails`.
    private Store store; // L'objet Store utilisé pour la connexion à la boîte de réception e-mail.
    private Folder emailFolder; // Le dossier e-mail (par exemple, INBOX) auquel on accède.
    private JPanel attachmentPanel; // Le panneau utilisé pour afficher les pièces jointes des e-mails.

    private String userEmail; // L'adresse e-mail de l'utilisateur.
    private String userPassword; // Le mot de passe de l'adresse e-mail de l'utilisateur.
    private Element sk; // La clé privée de l'utilisateur, utilisée dans les opérations de cryptographie.

    private SendReceiveMail receiveMail; // L'objet responsable de la réception des e-mails.
    private JLabel senderLabel; // Un label pour afficher l'expéditeur de l'e-mail sélectionné.

    private Map<File, File> fichiersChiffresMap = new HashMap<>(); // Une carte associant les fichiers chiffrés à leurs fichiers de clés correspondants.

    public InboxGUI(String email, String password, Element sk) {
        // Assignation des paramètres du constructeur aux attributs de la classe pour l'email, le mot de passe, et la clé privée.
        this.userEmail = email;
        this.userPassword = password;
        this.sk = sk;

        // Initialisation de l'interface utilisateur.
        // Cette méthode configure et affiche la fenêtre principale, la liste des e-mails, le contenu de l'email sélectionné et le panneau des pièces jointes.
        initializeUI();

        // Récupération des e-mails à partir du serveur.
        // Cette méthode se connecte au serveur de messagerie, récupère les e-mails du dossier INBOX, et remplit la liste des e-mails avec les sujets de ces derniers.
        fetchMails();

        // Création d'une instance de SendReceiveMail.
        // Cette instance sera utilisée pour des opérations supplémentaires liées à l'envoi et à la réception des e-mails.
        receiveMail = new SendReceiveMail();
    }



    private void initializeUI() {
        // Création et configuration de la fenêtre principale.
        frame = new JFrame("Boîte de Réception");
        frame.setBounds(100, 100, 800, 600); // Définition de la position et de la taille de la fenêtre.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Action à exécuter lorsque la fenêtre est fermée.

        // Obtention du conteneur principal de la fenêtre.
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout()); // Utilisation d'un BorderLayout pour organiser les composants.

        // Initialisation et ajout de la liste des mails à gauche de la fenêtre.
        listModel = new DefaultListModel<>();
        listMails = new JList<>(listModel); // JList utilisant listModel pour afficher les e-mails.
        pane.add(new JScrollPane(listMails), BorderLayout.WEST); // Ajout de la liste dans un JScrollPane pour la défilement.

        // Initialisation de la zone de texte pour afficher le contenu de l'e-mail sélectionné.
        mailContent = new JTextArea();
        mailContent.setEditable(false); // Rendre la zone de texte non éditable.
        pane.add(new JScrollPane(mailContent), BorderLayout.CENTER); // Ajout au centre de la fenêtre.

        // Création d'un JLabel pour afficher l'expéditeur de l'e-mail sélectionné.
        senderLabel = new JLabel("Expéditeur: ");
        senderLabel.setHorizontalAlignment(JLabel.CENTER); // Centrage du texte dans le label.

        // Création d'un JPanel pour le bouton de retour.
        JPanel backButtonPanel = new JPanel();
        backButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Alignement du bouton à gauche.
        JButton backButton = new JButton("Retour"); // Création du bouton "Retour".
        backButton.addActionListener(e -> {
            frame.dispose(); // Fermeture de la fenêtre courante.
            new MainMenu(userEmail, userPassword, sk); // Retour au menu principal.
        });
        backButtonPanel.add(backButton); // Ajout du bouton au panel.

        // Création d'un panel pour regrouper backButtonPanel et senderLabel en haut de la fenêtre.
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(backButtonPanel, BorderLayout.WEST);
        topPanel.add(senderLabel, BorderLayout.CENTER);

        pane.add(topPanel, BorderLayout.NORTH); // Ajout du panel combiné en haut de la fenêtre.

        // Panel pour afficher les pièces jointes en bas de la fenêtre.
        attachmentPanel = new JPanel(new FlowLayout());
        pane.add(attachmentPanel, BorderLayout.SOUTH);

        // Ajout d'un écouteur d'événements pour détecter les clics sur la liste des e-mails.
        listMails.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList<?> list = (JList<?>) evt.getSource();
                if (evt.getClickCount() == 1) { // Vérification d'un simple clic.
                    int index = list.locationToIndex(evt.getPoint()); // Obtenir l'index de l'élément cliqué.
                    if (index >= 0) {
                        displaySelectedMessage(index); // Afficher le message sélectionné.
                    }
                }
            }
        });

        frame.setVisible(true); // Rendre la fenêtre visible.
    }



    private void fetchMails() {
        // Configuration de la connexion au serveur de messagerie à l'aide d'un objet Properties.
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps"); // Utilisation du protocole IMAPS pour une connexion sécurisée.
        props.put("mail.imaps.host", "outlook.office365.com"); // Adresse du serveur IMAP d'Outlook.
        props.put("mail.imaps.port", "993"); // Port utilisé par le protocole IMAPS.
        props.put("mail.imaps.starttls.enable", "true"); // Activation de STARTTLS pour sécuriser la connexion.

        try {
            // Création d'une session de messagerie avec les propriétés définies ci-dessus.
            Session emailSession = Session.getInstance(props);
            // Obtention d'un objet Store qui représente la connexion au serveur de messagerie.
            store = emailSession.getStore("imaps");
            // Connexion au serveur de messagerie avec l'adresse e-mail et le mot de passe de l'utilisateur.
            store.connect("outlook.office365.com", this.userEmail, this.userPassword);

            // Accès au dossier "INBOX" (Boîte de réception) en lecture seule.
            emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            // Récupération de tous les messages présents dans la boîte de réception.
            Message[] messages = emailFolder.getMessages();
            // Pour chaque message, ajout de son sujet dans le modèle de la liste `listMails`.
            for (int i = 0; i < messages.length; i++) {
                listModel.addElement(messages[i].getSubject());
            }
        } catch (Exception e) {
            // En cas d'erreur, affichage de la trace de la pile d'exécution pour le débogage.
            e.printStackTrace();
        }
    }


    private void displaySelectedMessage(int index) {
        try {
            // Récupération du message sélectionné basé sur l'index.
            Message message = emailFolder.getMessage(index + 1);

            // Récupération de l'adresse de l'expéditeur et mise à jour de l'interface utilisateur.
            Address[] fromAddress = message.getFrom();
            String emailFrom = fromAddress != null ? fromAddress[0].toString() : "Inconnu";
            senderLabel.setText("Expéditeur: " + emailFrom);

            // Mise à jour du contenu du mail dans l'interface utilisateur.
            mailContent.setText(getTextFromMessage(message));

            // Nettoyage du panneau des pièces jointes avant l'affichage des nouvelles pièces jointes.
            attachmentPanel.removeAll();

            // Vérification si le message contient des pièces jointes.
            if (message.getContent() instanceof Multipart) {
                Multipart multipart = (Multipart) message.getContent();

                // Pour chaque pièce jointe trouvée...
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);

                    // Vérification si la partie est une pièce jointe.
                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        // Récupération et décodage du nom de la pièce jointe.
                        String attachFileName = MimeUtility.decodeText(bodyPart.getFileName());
                        // Sauvegarde de la pièce jointe sur le système de fichiers local.
                        File downloadedFile = saveAttachment(bodyPart, "./attachments/" + attachFileName);

                        // Vérification si le nom de fichier se termine par ".properties", indiquant un fichier de propriétés pour une pièce jointe chiffrée.
                        if (attachFileName != null && attachFileName.endsWith(".properties")) {
                            // Remplacement de l'extension ".properties" par ".chiffre" pour obtenir le nom du fichier chiffré correspondant.
                            String aesFileName = attachFileName.replace(".properties", ".chiffre");
                            // Recherche du fichier chiffré correspondant dans les pièces jointes.
                            for (int j = 0; j < multipart.getCount(); j++) {
                                BodyPart aesBodyPart = multipart.getBodyPart(j);
                                String aesFileNameEncoded = aesBodyPart.getFileName();
                                if (aesFileNameEncoded != null) {
                                    String aesFileNameDecoded = MimeUtility.decodeText(aesFileNameEncoded);
                                    // Si le fichier chiffré correspondant est trouvé, sauvegarde le fichier AES.
                                    if (aesFileName.equals(aesFileNameDecoded)) {
                                        File aesFile = saveAttachment(aesBodyPart, "./attachments/" + aesFileName);
                                        // Association du fichier de propriétés avec le fichier chiffré dans une map pour traitement ultérieur.
                                        fichiersChiffresMap.put(downloadedFile, aesFile);
                                        break;
                                    }
                                }
                            }
                        }
// Traitement similaire pour les fichiers avec l'extension ".chiffre", en cherchant cette fois le fichier de propriétés associé.
                        else if (attachFileName != null && attachFileName.endsWith(".chiffre")) {
                            String aesFileName = attachFileName.replace(".chiffre", ".properties");
                            for (int j = 0; j < multipart.getCount(); j++) {
                                BodyPart aesBodyPart = multipart.getBodyPart(j);
                                String aesFileNameEncoded = aesBodyPart.getFileName();
                                if (aesFileNameEncoded != null) {
                                    String aesFileNameDecoded = MimeUtility.decodeText(aesFileNameEncoded);
                                    if (aesFileName.equals(aesFileNameDecoded)) {
                                        File aesFile = saveAttachment(aesBodyPart, "./attachments/" + aesFileName);
                                        // Inversion de la map par rapport au cas précédent, ici le fichier AES est la clé et le fichier chiffré la valeur.
                                        fichiersChiffresMap.put(aesFile, downloadedFile);
                                        break;
                                    }
                                }
                            }
                        }
// Gestion des pièces jointes non chiffrées.
                        else {

                            // Affichage d'un label pour le nom de la pièce jointe non chiffrée.
                            JLabel nonEncryptedAttachmentLabel = new JLabel(attachFileName);
                            attachmentPanel.add(nonEncryptedAttachmentLabel);

                            // Bouton pour ouvrir la pièce jointe non chiffrée avec l'application par défaut.
                            JButton openNonEncryptedAttachmentButton = new JButton("Ouvrir");
                            openNonEncryptedAttachmentButton.addActionListener(e -> {
                                if (Desktop.isDesktopSupported()) {
                                    try {
                                        Desktop.getDesktop().open(downloadedFile);
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                        JOptionPane.showMessageDialog(frame, "Impossible d'ouvrir le fichier.", "Erreur", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });
                            attachmentPanel.add(openNonEncryptedAttachmentButton);




// Ajout d'un bouton pour enregistrer la pièce jointe non chiffrée sur le disque de l'utilisateur.
                            JButton saveNonEncryptedAttachmentButton = new JButton("Enregistrer");
                            saveNonEncryptedAttachmentButton.addActionListener(e -> {
                                JFileChooser fileChooser = new JFileChooser(); // Ouvre une fenêtre de dialogue pour choisir l'emplacement de sauvegarde.
                                int result = fileChooser.showSaveDialog(frame); // Affiche la boîte de dialogue et attend la réponse de l'utilisateur.
                                if (result == JFileChooser.APPROVE_OPTION) { // Si l'utilisateur confirme la sauvegarde,
                                    File selectedFile = fileChooser.getSelectedFile(); // Obtient le fichier sélectionné par l'utilisateur.
                                    try {
                                        Files.copy(downloadedFile.toPath(), selectedFile.toPath()); // Copie le fichier téléchargé vers l'emplacement choisi.
                                        // Affiche une notification de succès.
                                        JOptionPane.showMessageDialog(frame, "Fichier enregistré avec succès.", "Enregistrement réussi", JOptionPane.INFORMATION_MESSAGE);
                                    } catch (IOException ex) { // En cas d'erreur lors de la copie,
                                        ex.printStackTrace(); // Imprime la trace de l'exception.
                                        // Affiche un message d'erreur.
                                        JOptionPane.showMessageDialog(frame, "Impossible d'enregistrer le fichier.", "Erreur", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });
                            attachmentPanel.add(saveNonEncryptedAttachmentButton); // Ajoute le bouton au panneau des pièces jointes.

                        }
                    }
                }

// Déchiffrement de toutes les pièces jointes identifiées comme étant chiffrées.
                ArrayList<File> decryptedFiles = new ArrayList<>();
                for (Map.Entry<File, File> entry : fichiersChiffresMap.entrySet()) {
                    File fichierChiffre = entry.getValue(); // Récupère le fichier chiffré.
                    File fichierInfosAES = entry.getKey(); // Récupère le fichier de propriétés associé.
                    IBEBasicIdentScheme ibe = new IBEBasicIdentScheme(); // Instance du schéma de cryptographie.
                    // Utilise la clé privée et les informations du fichier pour déchiffrer la pièce jointe.
                    File decryptedFile = receiveMail.dechiffrerPieceJointe(fichierChiffre, fichierInfosAES, sk);
                    decryptedFiles.add(decryptedFile); // Ajoute le fichier déchiffré à la liste pour affichage.

                }

// Préparation de l'affichage des fichiers déchiffrés pour téléchargement ou ouverture.
                JPanel downloadPanel = new JPanel();
                downloadPanel.setLayout(new BoxLayout(downloadPanel, BoxLayout.Y_AXIS));
                for (File decryptedFile : decryptedFiles) {
                    JPanel filePanel = new JPanel();
                    filePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

                    JLabel fileNameLabel = new JLabel(decryptedFile.getName()); // Affiche le nom du fichier déchiffré.
                    filePanel.add(fileNameLabel);

                    JButton openButton = new JButton("Ouvrir"); // Bouton pour ouvrir le fichier avec l'application par défaut.
                    openButton.addActionListener(e -> {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().open(decryptedFile); // Tente d'ouvrir le fichier.
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(frame, "Impossible d'ouvrir le fichier.", "Erreur", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });

                    JButton downloadButton = new JButton("Télécharger"); // Bouton pour télécharger le fichier.
                    downloadButton.addActionListener(e -> {
                        // La logique de sauvegarde spécifique n'est pas implémentée ici, mais vous pourriez ajouter une fonctionnalité pour enregistrer le fichier à un emplacement choisi par l'utilisateur.
                        JOptionPane.showMessageDialog(frame, "Fichier sauvegardé à : " + decryptedFile.getAbsolutePath(), "Téléchargement Réussi", JOptionPane.INFORMATION_MESSAGE);
                    });

                    filePanel.add(openButton);
                    filePanel.add(downloadButton);

                    downloadPanel.add(filePanel); // Ajoute le panel du fichier à celui des téléchargements.
                }
                attachmentPanel.add(downloadPanel, BorderLayout.SOUTH); // Ajoute le panel de téléchargement au panneau des pièces jointes.

                attachmentPanel.revalidate();
                attachmentPanel.repaint();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private File saveAttachment(BodyPart part, String savePath) throws MessagingException, IOException {
        // Obtenir le flux d'entrée de la pièce jointe
        InputStream input = part.getInputStream();
        // Créer un fichier au chemin spécifié
        File file = new File(savePath);
        // S'assurer que le répertoire parent du fichier existe, sinon le créer
        file.getParentFile().mkdirs();
        // Écrire le contenu du flux d'entrée dans le fichier
        try (FileOutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096]; // Tampon pour la lecture
            int bytesRead; // Nombre de bytes lus
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead); // Écriture dans le fichier
            }
        }
        return file; // Retourne le fichier sauvegardé
    }




    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = ""; // Résultat final contenant le texte de l'e-mail
        // Vérifier si le message est de type texte simple
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString(); // Obtenir directement le contenu
        }
        // Sinon, si le message est multipart (contient éventuellement des pièces jointes)
        else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent(); // Obtenir le multipart
            // Parcourir chaque partie du multipart
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i); // Obtenir une partie spécifique
                // Si cette partie est de type texte simple, ajouter son contenu au résultat
                if (bodyPart.isMimeType("text/plain")) {
                    result += bodyPart.getContent().toString();
                }
            }
        }
        return result; // Retourne le texte de l'e-mail
    }



}
