package com.example.sae2.modele.carte;

public enum ModeleTypeObstacle {
    BARIL,
    CAILLOU;

    public static ModeleTypeObstacle fromInt(int valeur) {
        if (valeur == 1) return BARIL;
        if (valeur == 2) return CAILLOU;
        return null;
    }
}