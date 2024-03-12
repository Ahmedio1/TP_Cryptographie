import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InboxGUI {
    private JFrame frame;
    private JList<String> listMails;
    private JTextArea mailContent;

    public InboxGUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Boîte de Réception");
        frame.setBounds(100, 100, 500, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // Liste factice des mails
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("Mail 1");
        listModel.addElement("Mail 2");
        listModel.addElement("Mail 3");
        // Ajouter plus si nécessaire

        listMails = new JList<>(listModel);
        pane.add(new JScrollPane(listMails), BorderLayout.WEST);

        mailContent = new JTextArea();
        pane.add(new JScrollPane(mailContent), BorderLayout.CENTER);

        // Gestionnaire d'événements pour la sélection d'un mail
        listMails.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) { // Double-clic pour ouvrir le mail
                    int index = list.locationToIndex(evt.getPoint());
                    openMail(index); // Méthode pour afficher le contenu du mail
                }
            }
        });

        frame.setVisible(true);
    }

    private void openMail(int index) {
        mailContent.setText("Contenu du Mail " + (index + 1));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new InboxGUI();
            }
        });
    }
}
