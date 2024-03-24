import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuConnexion {
    // Déclaration des composants de l'interface graphique
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JPanel controlPanel;
    private JPanel buttonPanel; // Panneau pour les boutons
    private JTextField mailText; // Champ de texte pour l'email
    private JPasswordField passwordText; // Champ de mot de passe pour le mot de passe
    private JLabel emailLabel;
    private JLabel passwordLabel;
    private JLabel statusLabel; // Label pour afficher des messages d'état

    public MenuConnexion() {
        prepareGUI(); // Appel de la méthode qui prépare l'interface graphique
    }

    public static void main(String[] args) {
        // Création de l'instance de MenuConnexion, ce qui lance l'interface de connexion
        MenuConnexion menuConnexion = new MenuConnexion();
    }

    // Configuration de l'interface graphique
    private void prepareGUI() {
        mainFrame = new JFrame("Menu Connexion");
        mainFrame.setSize(400, 400);

        // Utilisation d'un GridBagLayout pour un placement précis des composants
        mainFrame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0; // Permet de pousser les composants vers le centre vertical

        // Ajout d'un espace vide pour centrer verticalement les composants
        mainFrame.add(new JPanel(), gbc);

        // Configuration et ajout des labels et panels
        headerLabel = new JLabel("Page de connexion à la boite mail", JLabel.CENTER);
        gbc.gridy = 1;
        mainFrame.add(headerLabel, gbc);

        emailLabel = new JLabel("Adresse email :");
        passwordLabel = new JLabel("Mot de passe :");

        controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 5, 5, 5);

        mailText = new JTextField(20);
        passwordText = new JPasswordField(20);

        JButton connectionButton = new JButton("Se Connecter");
        JButton pwdButton = new JButton("Mot de passe oublié ?");

        // Configuration et action du bouton de connexion
        connectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Récupération de l'email et du mot de passe saisis
                String email = mailText.getText();
                String password = new String(passwordText.getPassword());
                

                // Fermeture de la fenêtre de connexion
                mainFrame.dispose();

                // Ouverture du menu principal avec l'email et le mot de passe
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new MainMenu(email, password);
                    }
                });
            }
        });

        // Ajout des composants au panel de contrôle
        constraints.gridx = 0;
        constraints.gridy = 0;
        controlPanel.add(emailLabel, constraints);

        constraints.gridx = 1;
        controlPanel.add(mailText, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        controlPanel.add(passwordLabel, constraints);

        constraints.gridx = 1;
        controlPanel.add(passwordText, constraints);

        // Ajout du panel de contrôle à la fenêtre principale
        gbc.gridy = 2;
        mainFrame.add(controlPanel, gbc);

        // Ajout des boutons au panel des boutons
        buttonPanel = new JPanel();
        buttonPanel.add(connectionButton);
        buttonPanel.add(pwdButton);

        // Ajout du panel des boutons à la fenêtre principale
        gbc.gridy = 3;
        mainFrame.add(buttonPanel, gbc);

        // Configuration finale de la fenêtre principale
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
