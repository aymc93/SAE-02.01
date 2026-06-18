package com.example.sae2.modele.deck;


public enum TypeCarte {
    //                   dossier         dégâts  vitesse proj (px/s)
    SIMPLE_TOWER("SimpleTower",  20,  300.0),
    BLOODBAG    ("BloodBag",     50,  300.0),   // moyen
    BRIMSTONE   ("Brimstone",   100,  150.0),   // lent
    SOYMILK     ("SoyMilk",      30,  1500.0);   // rapide

    private final String dossier;
    private final int degats;
    private final double vitesseProjectile;

    TypeCarte(String dossier, int degats, double vitesseProjectile) {
        this.dossier = dossier;
        this.degats = degats;
        this.vitesseProjectile = vitesseProjectile;
    }

    public String getDossier() { return dossier; }
    public int getDegats() { return degats; }
    public double getVitesseProjectile() { return vitesseProjectile; }
    public boolean estPouvoir() { return this != SIMPLE_TOWER; }
}
