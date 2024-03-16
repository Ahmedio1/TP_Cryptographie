import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuConnexion {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JPanel controlPanel;
    private JPanel buttonPanel; // Nouveau panneau pour les boutons
    private JTextField mailText;
    private JPasswordField passwordText;
    private JLabel emailLabel;
    private JLabel passwordLabel;
    private JLabel statusLabel;

    public MenuConnexion() {
        prepareGUI();
    }

    public static void main(String[] args) {
        MenuConnexion menuConnexion = new MenuConnexion();
    }

    private void prepareGUI() {
        mainFrame = new JFrame("Menu Connexion");
        mainFrame.setSize(400, 400);

        // Utilisation d'un GridBagLayout pour le centrage vertical
        mainFrame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0; // Poids pour centrer verticalement
        mainFrame.add(new JPanel(), gbc); // Espace vide en haut

        headerLabel = new JLabel("Page de connexion à la boite mail", JLabel.CENTER);
        statusLabel = new JLabel("", JLabel.CENTER);

        gbc.gridy = 1;
        mainFrame.add(headerLabel, gbc);

        emailLabel = new JLabel("Adresse email :");
        passwordLabel = new JLabel("Mot de passe :");

        controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 5, 5, 5); // Marge entre les composants

        mailText = new JTextField(20);
        passwordText = new JPasswordField(20);

        JButton connectionButton = new JButton("Se Connecter");
        JButton pwdButton = new JButton("Mot de passe oublié ?");

        Dimension buttonSize = new Dimension(160, 30);
        connectionButton.setPreferredSize(buttonSize);
        pwdButton.setPreferredSize(buttonSize);

        connectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ferme le menu principal (optionnel)
                mainFrame.dispose();

                // Ouvre l'interface d'envoi de mail
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new MainMenu();
                    }
                });
            }
        });

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

        gbc.gridy = 2;
        mainFrame.add(controlPanel, gbc);

        buttonPanel = new JPanel();
        buttonPanel.add(connectionButton);
        buttonPanel.add(pwdButton);

        gbc.gridy = 3;
        mainFrame.add(buttonPanel, gbc);

        gbc.gridy = 4;
        mainFrame.add(statusLabel, gbc);

        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


}
