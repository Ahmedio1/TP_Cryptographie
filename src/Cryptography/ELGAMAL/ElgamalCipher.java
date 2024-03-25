package Cryptography.ELGAMAL;

import it.unisa.dia.gas.jpbc.Element;

public class ElgamalCipher {
    private Element u;
    private Element v;

    public ElgamalCipher(Element u, Element v) {
        this.u = u;
        this.v = v;
    }

    public Element getU() {
        return u;
    }

    public Element getV() {
        return v;
    }
}
