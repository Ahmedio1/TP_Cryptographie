package Cryptography.ELGAMAL;

import it.unisa.dia.gas.jpbc.Element;

/*
 * Ce fichier définit une classe pour représenter un message chiffré dans l'algorithme 
 * ElGamal. Un message chiffré est composé de deux éléments (U et V) de la courbe 
 * elliptique, représentant respectivement le partage de la clé et le message chiffré
 */
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
