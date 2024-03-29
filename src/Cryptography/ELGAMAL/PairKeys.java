package Cryptography.ELGAMAL;

import it.unisa.dia.gas.jpbc.Element;

/*
 * Ce fichier définit une classe simple pour gérer une paire de 
 * clés (publique et secrète) dans l'algorithme ElGamal. Elle stocke ces clés comme des éléments (Element) de la bibliothèque JPBC,
 * avec des méthodes pour récupérer chacune d'elles
 */

public class PairKeys {

    private Element pubkey;
    private Element secretkey;

    public PairKeys(Element pubkey, Element secretkey) {
        this.pubkey = pubkey;
        this.secretkey = secretkey;
    }

    public Element getPubkey() {
        return pubkey;
    }

    public Element getSecretkey() {
        return secretkey;
    }

}
