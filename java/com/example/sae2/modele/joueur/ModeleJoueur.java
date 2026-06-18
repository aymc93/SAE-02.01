package com.example.sae2.modele.joueur;

public class ModeleJoueur {

    private static final int RECOMPENSE_PAR_KILL = 100;
    public  static final int VIES_MAX            = 3;

    private int argent = 150;
    private int vies   = VIES_MAX;

    public void gagnerArgentKill() {
        argent += RECOMPENSE_PAR_KILL;
    }

    public void perdreVie() {
        if (vies > 0) vies--;
    }

    public int  getArgent()   { return argent; }
    public int  getVies()     { return vies; }
    public boolean estEnVie() { return vies > 0; }
    public void depenserArgent(int montant) {
        if (montant <= argent) {
            argent -= montant;
        }
    }

    public void ajouterArgent(int montant) {
        this.argent += montant;
    }
}
