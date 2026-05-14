package com.example.sae2.modele.carte;

/**
 * Représente le type d'une tuile dans la tilemap, incluant son angle de rotation.
 * 0°   = Haut (ou image par défaut)
 * 90°  = Droite
 * 180° = Bas
 * 270° = Gauche
 */
public enum ModeleTypeTuile {

    SOL_SIMPLE(0, 0),
    MUR_SIMPLE(30, 0),
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

    ModeleTypeTuile(int valeur, int angle) {
        this.valeur = valeur;
        this.angle = angle;
    }

    public int getAngle() {
        return angle;
    }

    // Convertit un entier (venant du tableau CARTE) en TypeTuile.
    public static ModeleTypeTuile fromInt(int valeur) {
        for (ModeleTypeTuile t : values()) {
            if (t.valeur == valeur) return t;
        }
        return SOL_SIMPLE; // sécurité
    }

    // pour savoir quelle image charger dans Vue
    public boolean isSolSimple() { return this == SOL_SIMPLE; }
    public boolean isMurBord()   { return this.name().startsWith("MUR_BORD"); }
    public boolean isMurCoin()   { return this.name().startsWith("MUR_COIN"); }
}