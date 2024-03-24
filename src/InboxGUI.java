import javax.imageio.ImageIO;
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

        // Panel pour afficher les pièces jointes
        attachmentPanel = new JPanel(new FlowLayout());
        pane.add(attachmentPanel, BorderLayout.SOUTH);

        // Panel pour le bouton de retour
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Retour");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Fermer la fenêtre actuelle
                new MainMenu(userEmail, userPassword,sk); // Réouvrir le MainMenu
            }
        });
        backButtonPanel.add(backButton);

        pane.add(backButtonPanel, BorderLayout.NORTH);

        listMails.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
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
                        File downloadedFile = saveAttachment(bodyPart, "./attachmentss/" + attachFileName);

                        if (attachFileName != null && attachFileName.endsWith(".properties")) {
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
                    }
                }

                // Déchiffrement de toutes les pièces jointes
                ArrayList<File> decryptedFiles = new ArrayList<>();
                for (Map.Entry<File, File> entry : fichiersChiffresMap.entrySet()) {
                    File fichierChiffre = entry.getValue();
                    File fichierInfosAES = entry.getKey();
                    File decryptedFile = receiveMail.dechiffrerPieceJointe(fichierChiffre, fichierInfosAES, sk);
                    decryptedFiles.add(decryptedFile);
                    System.out.println(decryptedFile.getName());
                }

                // Ajout des fichiers déchiffrés en téléchargement en bas de la page
                JPanel downloadPanel = new JPanel();
                downloadPanel.setLayout(new BoxLayout(downloadPanel, BoxLayout.Y_AXIS));
                for (File decryptedFile : decryptedFiles) {
                    JLabel downloadLabel = new JLabel("<html><a href=\"file:" + decryptedFile.getAbsolutePath() + "\">Télécharger " + decryptedFile.getName() + "</a></html>");
                    downloadLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    downloadLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                                try {
                                    Desktop.getDesktop().open(decryptedFile);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
                    downloadPanel.add(downloadLabel);
                }
                attachmentPanel.add(downloadPanel, BorderLayout.SOUTH);

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
