import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu {
    public static void main(String[] args) {
        // Cadre principal pour notre menu
        JFrame frame = new JFrame("Menu Principal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        // Panneau pour contenir nos boutons
        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);

        // Paramétrage de la fenêtre
        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Création du bouton pour envoyer un mail
        JButton sendMailButton = new JButton("Envoyer un Mail");
        sendMailButton.setBounds(50, 20, 300, 50);
        sendMailButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ferme le menu principal (optionnel)
                // frame.dispose();

                // Ouvre l'interface d'envoi de mail
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new MailClientGUI();
                    }
                });
            }
        });

        panel.add(sendMailButton);

        // Création du bouton pour accéder à la boîte de réception
        JButton inboxButton = new JButton("Boîte de Réception");
        inboxButton.setBounds(50, 100, 300, 50);
        inboxButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new InboxGUI();
                    }
                });
            }
        });
        panel.add(inboxButton);
    }
}

