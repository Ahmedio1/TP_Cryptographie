import javax.imageio.ImageIO;
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
import java.util.Properties;

public class InboxGUI {
    private JFrame frame;
    private JList<String> listMails;
    private JTextArea mailContent;
    private DefaultListModel<String> listModel;
    private Store store;
    private Folder emailFolder;
    private JPanel attachmentPanel; // Panel pour afficher les pièces jointes

    public InboxGUI() {
        initializeUI();
        fetchMails();
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
        attachmentPanel = new JPanel();
        attachmentPanel.setLayout(new FlowLayout());
        pane.add(attachmentPanel, BorderLayout.SOUTH);

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
            store.connect("outlook.office365.com", "tp_crypto_2024@outlook.com", "******");

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

            // Traitement du contenu du message pour les pièces jointes
            if (message.getContent() instanceof Multipart) {
                Multipart multipart = (Multipart) message.getContent();

                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);

                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        // Ici, vous pouvez choisir de sauvegarder l'image ou de l'utiliser directement si elle est déjà sauvegardée
                        String contentType = bodyPart.getContentType();
                        System.out.println(contentType);
                        System.out.println(contentType.startsWith("image/"));
                        if (contentType.startsWith("image/")) {
                            // C'est une image
                            System.out.println("ahhaha");
                            InputStream is = bodyPart.getInputStream();
                            BufferedImage img = ImageIO.read(is);
                            ImageIcon icon = new ImageIcon(img);
                            // Redimensionner l'image si nécessaire
                            Image image = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Exemple de redimensionnement
                            icon = new ImageIcon(image);
                            String attachFileName = bodyPart.getFileName();
                            JButton downloadButton = new JButton(attachFileName);
                            downloadButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        saveAttachment(bodyPart, "./attachments/" + attachFileName);
                                        JOptionPane.showMessageDialog(frame, "Saved attachment to: ./attachments/" + attachFileName);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                            attachmentPanel.add(downloadButton);
                            JLabel label = new JLabel(icon);
                            attachmentPanel.add(label);
                        }
                    }
                }
            }

            attachmentPanel.revalidate();
            attachmentPanel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAttachment(BodyPart part, String savePath) throws MessagingException, IOException {
        InputStream input = part.getInputStream();
        File file = new File(savePath);
        // Assurer que le dossier de destination existe
        file.getParentFile().mkdirs();
        try (FileOutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InboxGUI::new);
    }
}
