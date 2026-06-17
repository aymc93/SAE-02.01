package com.example.sae2.modele.ennemis;

public class ModeleGasper extends ModeleEnnemi {
    // Vitesse moyenne, PV moyens
    public ModeleGasper(double x, double y, int pvBase) {
        super(x, y, 70.0, "Gasper", pvBase / 3);
    }
}