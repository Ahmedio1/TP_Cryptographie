package Cryptography.ELGAMAL;
import it.unisa.dia.gas.jpbc.Element;



public class CipherText  {
    private Element u;
    private Element v;

    public CipherText(Element u, Element v) {
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
