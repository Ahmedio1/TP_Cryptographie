import javax.mail.util.ByteArrayDataSource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

public class MailClientGUI {
    private JFrame frame;
    private JTextField textFieldTo;
    private JTextField textFieldSubject;
    private JTextArea textAreaMessage;
    private JButton sendButton;
    private JButton buttonFile;
    private JFileChooser fileChooser;
    private JLabel attachmentsLabel;
    private ArrayList<File> attachedFiles;

    public MailClientGUI() {
        attachedFiles = new ArrayList<>();
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Client Mail");

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel("À:"));
        textFieldTo = new JTextField(20);
        panel.add(textFieldTo);

        panel.add(new JLabel("Sujet:"));
        textFieldSubject = new JTextField(20);
        panel.add(textFieldSubject);

        buttonFile = new JButton("Ajouter Pièces Jointes");
        buttonFile.addActionListener(this::showOpenFileDialog);
        panel.add(buttonFile);

        attachmentsLabel = new JLabel();
        panel.add(attachmentsLabel);

        pane.add(panel, BorderLayout.NORTH);

        textAreaMessage = new JTextArea();
        pane.add(new JScrollPane(textAreaMessage), BorderLayout.CENTER);

        sendButton = new JButton("Envoyer");
        sendButton.addActionListener(e -> sendEmailWithAttachments());
        pane.add(sendButton, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    private void showOpenFileDialog(ActionEvent event) {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
        }

        int option = fileChooser.showOpenDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                attachedFiles.add(file);
            }
            updateAttachmentsLabel();
        }
    }

    private void updateAttachmentsLabel() {
        StringBuilder sb = new StringBuilder("<html>");
        for (File file : attachedFiles) {
            sb.append(file.getName()).append("<br>");
        }
        sb.append("</html>");
        attachmentsLabel.setText(sb.toString());
    }

    private void sendEmailWithAttachments() {
        final String username = "tp_crypto_2024@outlook.com"; // update with your email address
        final String password = "******"; // update with your password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.office365.com"); // update with your SMTP host
        props.put("mail.smtp.port", "587"); // or 465

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(textFieldTo.getText()));
            message.setSubject(textFieldSubject.getText());

            // Create a multipart message for attachment
            Multipart multipart = new MimeMultipart();

            // Partie texte du message
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(textAreaMessage.getText());
            multipart.addBodyPart(textPart);

            // Ajout des pièces jointes
            for (File file : attachedFiles) {
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.setDataHandler(new DataHandler(new ByteArrayDataSource(new FileInputStream(file), "application/octet-stream")));
                attachPart.setFileName(file.getName());
                multipart.addBodyPart(attachPart);
            }


            // configuration du message (expéditeur, destinataire, sujet)
            message.setContent(multipart);

            // Envoi du message
            Transport.send(message);

            JOptionPane.showMessageDialog(frame, "Email sent successfully!");

        } catch (MessagingException | IOException e) {
            JOptionPane.showMessageDialog(frame, "Error sending email: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MailClientGUI::new);
    }
}
