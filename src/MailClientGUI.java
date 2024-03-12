import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MailClientGUI {
    private JFrame frame;
    private JTextField textFieldTo;
    private JTextField textFieldSubject;
    private JTextArea textAreaMessage;
    private JButton sendButton;
    private JButton receiveButton;

    public MailClientGUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Client Mail");

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));

        panel.add(new JLabel("À:"));
        textFieldTo = new JTextField();
        panel.add(textFieldTo);

        panel.add(new JLabel("Sujet:"));
        textFieldSubject = new JTextField();
        panel.add(textFieldSubject);

        pane.add(panel, BorderLayout.NORTH);

        textAreaMessage = new JTextArea();
        pane.add(new JScrollPane(textAreaMessage), BorderLayout.CENTER);

        sendButton = new JButton("Envoyer");
        receiveButton = new JButton("Recevoir");

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(sendButton);
        bottomPanel.add(receiveButton);

        pane.add(bottomPanel, BorderLayout.SOUTH);

        // Ajouter des gestionnaires d'événements pour les boutons ici

        frame.setVisible(true);
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

