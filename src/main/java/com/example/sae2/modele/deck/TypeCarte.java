package com.example.sae2.modele.deck;

/*
 * Type de carte jouable depuis le deck.
 * Contient le dossier d'images, les dégâts et la vitesse du projectile.
 * Vitesses projectile : Lent = 150 px/s · Moyen = 300 px/s · Rapide = 500 px/s
 */
public enum TypeCarte {
    //                   dossier         dégâts  vitesse proj (px/s)
    SIMPLE_TOWER("SimpleTower",  20,  300.0),
    BLOODBAG    ("BloodBag",     50,  300.0),   // moyen
    BRIMSTONE   ("Brimstone",   100,  150.0),   // lent
    SOYMILK     ("SoyMilk",      30,  1500.0);   // rapide

    private final String dossier;
    private final int    degats;
    private final double vitesseProjectile;

    TypeCarte(String dossier, int degats, double vitesseProjectile) {
        this.dossier            = dossier;
        this.degats             = degats;
        this.vitesseProjectile  = vitesseProjectile;
    }

    /* Dossier d'images sous resources/com/example/sae2/images/ */
    public String getDossier()            { return dossier; }

    /* Dégâts infligés à l'ennemi par projectile. */
    public int    getDegats()             { return degats; }

    /* Vitesse du projectile en pixels par seconde. */
    public double getVitesseProjectile()  { return vitesseProjectile; }

    /* Vrai pour BloodBag, Brimstone, SoyMilk. */
    public boolean estPouvoir()           { return this != SIMPLE_TOWER; }
}
