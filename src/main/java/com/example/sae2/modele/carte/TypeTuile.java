package com.example.sae2.modele.carte;

/**
 * Type d'une tuile dans la tilemap, avec son angle de rotation.
 * 0° = Haut, 90° = Droite, 180° = Bas, 270° = Gauche
 */
public enum TypeTuile {

    SOL_SIMPLE(0, 0),
    MUR_BORD_HAUT(40, 0),
    MUR_BORD_DROITE(41, 90),
    MUR_BORD_BAS(42, 180),
    MUR_BORD_GAUCHE(43, 270),
    MUR_COIN_HG(50, 0),
    MUR_COIN_HD(51, 90),
    MUR_COIN_BD(52, 180),
    MUR_COIN_BG(53, 270);

    private final int valeur;
    private final int angle;

    TypeTuile(int valeur, int angle) {
        this.valeur = valeur;
        this.angle = angle;
    }

    public int getAngle() {
        return angle;
    }

    public static TypeTuile fromInt(int valeur) {
        for (TypeTuile t : values()) {
            if (t.valeur == valeur) return t;
        }
        return SOL_SIMPLE;
    }

    public boolean isSolSimple() { return this == SOL_SIMPLE; }
    public boolean isMurBord()   { return this.name().startsWith("MUR_BORD"); }
}
