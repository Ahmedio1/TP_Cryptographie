// Importation des bibliothèques nécessaires
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import Cryptography.AES.AES;
import Mail.SendReceiveMail;

public class MailClientGUI {
    // Déclaration des composants de l'interface utilisateur et des variables pour l'authentification
    private JFrame frame;
    private JTextField textFieldTo, textFieldSubject;
    private JTextArea textAreaMessage;
    private JButton sendButton, buttonFile, deleteAttachmentButton;
    private JFileChooser fileChooser;
    private JList<File> attachedFilesList;
    private DefaultListModel<File> attachedFilesModel;
    private String userEmail, userPassword,sk;

    private SendReceiveMail sendMail;

    // Constructeur qui initialise l'interface utilisateur
    public MailClientGUI(String email, String password,String sk) {
        this.userEmail = email;
        this.userPassword = password;
        this.sk=sk;
        attachedFilesModel = new DefaultListModel<>();
        initializeUI();
        sendMail = new SendReceiveMail();
    }

    // Initialisation de l'interface utilisateur
    private void initializeUI() {
        frame = new JFrame();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximise la fenêtre
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Client Mail");

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Configuration et ajout des champs pour l'adresse, le sujet et les pièces jointes
        panel.add(new JLabel("À:"));
        textFieldTo = new JTextField(20);
        panel.add(textFieldTo);
        panel.add(new JLabel("Sujet:"));
        textFieldSubject = new JTextField(20);
        panel.add(textFieldSubject);

        // Bouton pour ajouter des pièces jointes
        buttonFile = new JButton("Ajouter Pièces Jointes");
        buttonFile.addActionListener(this::showOpenFileDialog);
        panel.add(buttonFile);

        // Liste pour afficher les fichiers joints
        attachedFilesList = new JList<>(attachedFilesModel);
        attachedFilesList.setPreferredSize(new Dimension(200, 100));
        JScrollPane listScrollPane = new JScrollPane(attachedFilesList);
        panel.add(listScrollPane);

        // Bouton pour supprimer une pièce jointe sélectionnée
        deleteAttachmentButton = new JButton("Supprimer Pièce Jointe");
        deleteAttachmentButton.addActionListener(e -> deleteSelectedAttachment());
        panel.add(deleteAttachmentButton);

        // Bouton de retour au menu principal
        JButton backButton = new JButton("Retour");
        backButton.addActionListener(e -> {
            frame.dispose(); // Ferme cette fenêtre
            new MainMenu(userEmail, userPassword,sk); // Ouvre le menu principal
        });
        panel.add(backButton);

        pane.add(panel, BorderLayout.NORTH);

        textAreaMessage = new JTextArea(); // Zone de texte pour le corps de l'email
        pane.add(new JScrollPane(textAreaMessage), BorderLayout.CENTER);

        // Bouton pour envoyer l'email
        sendButton = new JButton("Envoyer");
        sendButton.addActionListener(e -> sendEmailWithAttachments());
        pane.add(sendButton, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    // Affiche un dialogue pour choisir des fichiers à joindre
    private void showOpenFileDialog(ActionEvent event) {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
        }

        int option = fileChooser.showOpenDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                attachedFilesModel.addElement(file); // Ajoute le fichier à la liste des pièces jointes
            }
        }
    }

    // Supprime la pièce jointe sélectionnée de la liste
    private void deleteSelectedAttachment() {
        int selectedIndex = attachedFilesList.getSelectedIndex();
        if (selectedIndex != -1) {
            attachedFilesModel.remove(selectedIndex);
        }
    }

    // Configure et envoie l'email avec les pièces jointes
    private void sendEmailWithAttachments() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.office365.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userEmail, userPassword);
            }
        });

        try {
            String AESkey = AES.randomString();

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(userEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(textFieldTo.getText()));
            message.setSubject(textFieldSubject.getText());

            Multipart multipart = new MimeMultipart();

            // Partie texte de l'email
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(textAreaMessage.getText());
            multipart.addBodyPart(textPart);

            // Ajoute chaque fichier sélectionné en tant que pièce jointe
            for (int i = 0; i < attachedFilesModel.getSize(); i++) {
                File file = attachedFilesModel.getElementAt(i);
                file = sendMail.preparerInfosDechiffrementAES(file.getName(),AESkey,textFieldTo.getText());
                MimeBodyPart attachPart = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                attachPart.setDataHandler(new DataHandler(source));
                attachPart.setFileName(file.getName());
                multipart.addBodyPart(attachPart);
            }

            message.setContent(multipart);
            Transport.send(message);

            JOptionPane.showMessageDialog(frame, "Email sent successfully!");
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(frame, "Error sending email: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
