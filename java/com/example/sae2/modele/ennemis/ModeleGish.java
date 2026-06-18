package com.example.sae2.modele.ennemis;

public class ModeleGish extends ModeleEnnemi {
    // Lent comme Fatty, mais des PV colossaux
    public ModeleGish(double x, double y, int pvBase) {
        super(x, y, 30.0, "Gish", pvBase * 10);
    }
}