package com.example.sae2.modele.ennemis;

public class ModeleFatty extends ModeleEnnemi {
    // Vitesse lente et beaucoup de PV
    public ModeleFatty(double x, double y, int pvBase) {
        super(x, y, 50.0, "Fatty", pvBase);
    }
}