import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class MailClientGUI {
    private JFrame frame;
    private JTextField textFieldTo;
    private JTextField textFieldSubject;
    private JTextArea textAreaMessage;
    private JButton sendButton;
    private JButton buttonFile;
    private JFileChooser areaFileChoose;
    private JLabel attachmentsLabel;
    private ArrayList<String> namefile;

    public MailClientGUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setTitle("Client Mail");

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));

        panel.add(new JLabel("À:"));
        textFieldTo = new JTextField();
        textFieldTo.setPreferredSize(new Dimension(200,1));
        panel.add(textFieldTo);


        panel.add(new JLabel("Sujet:"));
        textFieldSubject = new JTextField();
        textFieldSubject.setSize(200,10);
        panel.add(textFieldSubject);

        panel.add(new JLabel("Pièces Jointes:"));
        attachmentsLabel = new JLabel(); // Initialisez le JLabel ici
        panel.add(attachmentsLabel); // Ajoutez le JLabel au panneau

        // bouton pour accèder a la fenêtre modal pour mettre une piece jointe
        buttonFile = new JButton("Pièces Jointes");
        buttonFile.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               showOpenFileDialog();
           }
         });
        panel.add(buttonFile);

        pane.add(panel, BorderLayout.NORTH);

        textAreaMessage = new JTextArea();
        pane.add(new JScrollPane(textAreaMessage), BorderLayout.CENTER);

        sendButton = new JButton("Envoyer");

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(sendButton);

        pane.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void showOpenFileDialog() {
        // Créez une instance de JFileChooser.
        JFileChooser fileChooser = new JFileChooser();

        // Permet de sélectionner plusieurs fichiers si nécessaire.
        fileChooser.setMultiSelectionEnabled(true);

        // Définit le titre de la boîte de dialogue.
        fileChooser.setDialogTitle("Sélectionnez les fichiers à joindre");

        // Affiche la boîte de dialogue et attend que l'utilisateur sélectionne un fichier.
        int result = fileChooser.showOpenDialog(frame);

        // Vérifie si l'utilisateur a appuyé sur le bouton OK ou Ouvrir.
        if (result == JFileChooser.APPROVE_OPTION) {
            // Obtient les fichiers sélectionnés.
            File[] selectedFiles = fileChooser.getSelectedFiles();

            StringBuilder sb = new StringBuilder();
            for (File file : selectedFiles) {
                if (sb.length() > 0) {
                    sb.append(", "); // Séparateur entre les noms de fichiers
                }
                sb.append(file.getName());
                if (namefile == null) {
                    namefile = new ArrayList<>();
                }
                namefile.add(file.getAbsolutePath());
            }

            // Mettre à jour le JLabel avec les noms des fichiers sélectionnés
            attachmentsLabel.setText(sb.toString());
        }
    }

    // Méthode main pour tester l'interface
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MailClientGUI window = new MailClientGUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

