import javax.imageio.ImageIO;

import Cryptography.IBE.IBEBasicIdentScheme;
import it.unisa.dia.gas.jpbc.Element;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
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
    private JFrame frame;
    private JList<String> listMails;
    private JTextArea mailContent;
    private DefaultListModel<String> listModel;
    private Store store;
    private Folder emailFolder;
    private JPanel attachmentPanel; // Panel pour afficher les pièces jointes

    private String userEmail;
    private String userPassword;
    private Element sk;

    private SendReceiveMail receiveMail;
    private JLabel senderLabel; // Label pour afficher l'adresse de l'expéditeur


    private Map<File, File> fichiersChiffresMap = new HashMap<>();

    public InboxGUI(String email, String password,Element sk) {
        this.userEmail = email;
        this.userPassword = password;
        this.sk = sk;
        initializeUI();
        fetchMails();
        receiveMail = new SendReceiveMail();
    }


    private void initializeUI() {
        frame = new JFrame("Boîte de Réception");
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        listMails = new JList<>(listModel);
        pane.add(new JScrollPane(listMails), BorderLayout.WEST);

        mailContent = new JTextArea();
        mailContent.setEditable(false);
        pane.add(new JScrollPane(mailContent), BorderLayout.CENTER);

        senderLabel = new JLabel("Expéditeur: "); // Initialisé avec un texte par défaut
        senderLabel.setHorizontalAlignment(JLabel.CENTER); // Centrer le texte du label

        // Panel pour le bouton de retour
        JPanel backButtonPanel = new JPanel();
        backButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Retour");
        backButton.addActionListener(e -> {
            frame.dispose(); // Fermer la fenêtre actuelle
            new MainMenu(userEmail, userPassword, sk); // Réouvrir le MainMenu
        });
        backButtonPanel.add(backButton);

        // Panel pour regrouper backButtonPanel et senderLabel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(backButtonPanel, BorderLayout.WEST);
        topPanel.add(senderLabel, BorderLayout.CENTER);

        pane.add(topPanel, BorderLayout.NORTH); // Ajouter le panel combiné en haut

        // Panel pour afficher les pièces jointes
        attachmentPanel = new JPanel(new FlowLayout());
        pane.add(attachmentPanel, BorderLayout.SOUTH);

        listMails.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList<?> list = (JList<?>) evt.getSource();
                if (evt.getClickCount() == 1) {
                    int index = list.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        displaySelectedMessage(index);
                    }
                }
            }
        });

        frame.setVisible(true);
    }


    private void fetchMails() {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "outlook.office365.com");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.starttls.enable", "true");

        try {
            Session emailSession = Session.getInstance(props);
            store = emailSession.getStore("imaps");
            store.connect("outlook.office365.com", this.userEmail, this.userPassword);

            emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            Message[] messages = emailFolder.getMessages();
            for (int i = 0; i < messages.length; i++) {
                listModel.addElement(messages[i].getSubject());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displaySelectedMessage(int index) {
        try {
            Message message = emailFolder.getMessage(index + 1);
            // Récupération et affichage de l'adresse de l'expéditeur
            Address[] fromAddress = message.getFrom();
            String emailFrom = fromAddress != null ? fromAddress[0].toString() : "Inconnu";
            senderLabel.setText("Expéditeur: " + emailFrom); // Met à jour le label avec l'adresse de l'expéditeur

            mailContent.setText(getTextFromMessage(message));


            mailContent.setText(getTextFromMessage(message));

            attachmentPanel.removeAll(); // Nettoyez le panel pour les nouvelles pièces jointes

            if (message.getContent() instanceof Multipart) {

                Multipart multipart = (Multipart) message.getContent();
                Map<File, File> fichiersChiffresMap = new HashMap<>();

                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);

                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        String attachFileName = bodyPart.getFileName();
                        if (attachFileName != null) {
                            attachFileName = MimeUtility.decodeText(attachFileName);
                        }
                        File downloadedFile = saveAttachment(bodyPart, "./attachments/" + attachFileName);

                        if (attachFileName != null && (attachFileName.endsWith(".properties"))) {
                            System.out.println("oui");
                            String aesFileName = attachFileName.replace(".properties", ".chiffre");
                            for (int j = 0; j < multipart.getCount(); j++) {
                                BodyPart aesBodyPart = multipart.getBodyPart(j);
                                String aesFileNameEncoded = aesBodyPart.getFileName();
                                System.out.println(aesFileNameEncoded);
                                if (aesFileNameEncoded != null) {
                                    String aesFileNameDecoded = MimeUtility.decodeText(aesFileNameEncoded);
                                    if (aesFileName.equals(aesFileNameDecoded)) {
                                        File aesFile = saveAttachment(aesBodyPart, "./attachments/" + aesFileName);
                                        fichiersChiffresMap.put(downloadedFile, aesFile); // Associe le fichier chiffré avec son fichier d'infos AES
                                        break;
                                    }
                                }
                            }
                        }
                        else if (attachFileName != null && (attachFileName.endsWith(".chiffre"))){
                            System.out.println("oui");
                            String aesFileName = attachFileName.replace(".chiffre", ".properties");
                            for (int j = 0; j < multipart.getCount(); j++) {
                                BodyPart aesBodyPart = multipart.getBodyPart(j);
                                String aesFileNameEncoded = aesBodyPart.getFileName();
                                System.out.println(aesFileNameEncoded);
                                if (aesFileNameEncoded != null) {
                                    String aesFileNameDecoded = MimeUtility.decodeText(aesFileNameEncoded);
                                    if (aesFileName.equals(aesFileNameDecoded)) {
                                        File aesFile = saveAttachment(aesBodyPart, "./attachments/" + aesFileName);
                                        fichiersChiffresMap.put(aesFile, downloadedFile); // Associe le fichier chiffré avec son fichier d'infos AES
                                        break;
                                    }
                                }
                            }
                        }
                        else {
                            // Pièce jointe non chiffrée
                            System.out.println("Pièce jointe non chiffrée : " + attachFileName);

                            // Ajouter un JLabel pour afficher le nom de la pièce jointe non chiffrée
                            JLabel nonEncryptedAttachmentLabel = new JLabel(attachFileName);
                            attachmentPanel.add(nonEncryptedAttachmentLabel);

                            // Ajouter un JButton pour ouvrir la pièce jointe non chiffrée
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

                            // Ajouter un JButton pour enregistrer la pièce jointe non chiffrée
                            JButton saveNonEncryptedAttachmentButton = new JButton("Enregistrer");
                            saveNonEncryptedAttachmentButton.addActionListener(e -> {
                                JFileChooser fileChooser = new JFileChooser();
                                int result = fileChooser.showSaveDialog(frame);
                                if (result == JFileChooser.APPROVE_OPTION) {
                                    File selectedFile = fileChooser.getSelectedFile();
                                    try {
                                        Files.copy(downloadedFile.toPath(), selectedFile.toPath());
                                        JOptionPane.showMessageDialog(frame, "Fichier enregistré avec succès.", "Enregistrement réussi", JOptionPane.INFORMATION_MESSAGE);
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                        JOptionPane.showMessageDialog(frame, "Impossible d'enregistrer le fichier.", "Erreur", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });
                            attachmentPanel.add(saveNonEncryptedAttachmentButton);
                        }
                    }
                }

                // Déchiffrement de toutes les pièces jointes
                ArrayList<File> decryptedFiles = new ArrayList<>();
                for (Map.Entry<File, File> entry : fichiersChiffresMap.entrySet()) {
                    File fichierChiffre = entry.getValue();
                    File fichierInfosAES = entry.getKey();
                    IBEBasicIdentScheme ibe = new IBEBasicIdentScheme();
                    File decryptedFile = receiveMail.dechiffrerPieceJointe(fichierChiffre, fichierInfosAES, sk);
                    decryptedFiles.add(decryptedFile);
                    System.out.println(decryptedFile.getName());
                }

                // Ajout des fichiers déchiffrés en téléchargement en bas de la page
                JPanel downloadPanel = new JPanel();
                downloadPanel.setLayout(new BoxLayout(downloadPanel, BoxLayout.Y_AXIS));
                for (File decryptedFile : decryptedFiles) {
                    JPanel filePanel = new JPanel();
                    filePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

                    JLabel fileNameLabel = new JLabel(decryptedFile.getName());
                    filePanel.add(fileNameLabel);

                    JButton openButton = new JButton("Ouvrir");
                    openButton.addActionListener(e -> {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().open(decryptedFile);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(frame, "Impossible d'ouvrir le fichier.", "Erreur", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });

                    JButton downloadButton = new JButton("Télécharger");
                    downloadButton.addActionListener(e -> {
                        // Implémentez la logique de sauvegarde à un emplacement spécifique si nécessaire
                        JOptionPane.showMessageDialog(frame, "Fichier sauvegardé à : " + decryptedFile.getAbsolutePath(), "Téléchargement Réussi", JOptionPane.INFORMATION_MESSAGE);
                    });

                    filePanel.add(openButton);
                    filePanel.add(downloadButton);

                    downloadPanel.add(filePanel);
                }
                attachmentPanel.add(downloadPanel,BorderLayout.SOUTH);

                attachmentPanel.revalidate();
                attachmentPanel.repaint();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private File saveAttachment(BodyPart part, String savePath) throws MessagingException, IOException {
        InputStream input = part.getInputStream();
        File file = new File(savePath);
        file.getParentFile().mkdirs(); // Assurez-vous que le répertoire existe
        try (FileOutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
        return file; // Retourne le fichier sauvegardé
    }



    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    result += bodyPart.getContent().toString();
                }
            }
        }
        return result;
    }


}
