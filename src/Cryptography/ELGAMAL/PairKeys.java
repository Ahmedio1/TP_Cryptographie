package Cryptography.ELGAMAL;

import it.unisa.dia.gas.jpbc.Element;

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
