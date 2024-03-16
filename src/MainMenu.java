import javax.swing.*;
import java.awt.*;

public class MainMenu {
    private String userEmail;
    private String userPassword;

    public MainMenu(String email, String password) {
        this.userEmail = email;
        this.userPassword = password;
        initializeMainMenu();
    }

    private void initializeMainMenu() {
        // Création et configuration de la fenêtre principale
        JFrame frame = new JFrame("Menu Principal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        // Panel pour contenir les boutons
        JPanel panel = new JPanel();
        frame.add(panel);

        // Placement des composants (boutons) dans le panel
        placeComponents(panel, frame);

        // Rendre la fenêtre visible
        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel, JFrame frame) {
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Bouton pour envoyer un mail
        JButton sendMailButton = new JButton("Envoyer un Mail");
        sendMailButton.addActionListener(e -> {
            frame.dispose(); // Fermer le menu principal
            // Ouvre l'interface d'envoi de mail avec l'email et le mot de passe de l'utilisateur
            new MailClientGUI(userEmail, userPassword);
        });
        panel.add(sendMailButton);

        // Bouton pour accéder à la boîte de réception
        JButton inboxButton = new JButton("Boîte de Réception");
        inboxButton.addActionListener(e -> {
            frame.dispose(); // Fermer le menu principal
            // Ouvre l'interface de la boîte de réception avec l'email et le mot de passe de l'utilisateur
            new InboxGUI(userEmail, userPassword);
        });
        panel.add(inboxButton);
    }
}
