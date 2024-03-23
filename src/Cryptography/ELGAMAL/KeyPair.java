package Cryptography.ELGAMAL;

import it.unisa.dia.gas.jpbc.Element;

public class KeyPair {

    Element publicKey;
    Element privatekey;

    public KeyPair(Element publicKey, Element privatekey ) {
        this.publicKey = publicKey;
        this.privatekey = privatekey;
    }



    public Element privatekey() {
        return privatekey;
    }


    public Element publicKey() {
        return publicKey;
    }
}
