package com.example.sae2.modele.ennemis;

import java.util.ArrayList;
import java.util.List;

public class ModeleGasper extends ModeleEnnemi {

    public ModeleGasper(double x, double y, int pvBase) {
        super(x, y, 80.0, "Gasper", pvBase / 3);
    }

    @Override
    public List<ModeleEnnemi> genererEnnemisApresMort(int pvVague, double tailleTuile) {
        List<ModeleEnnemi> bebesSpiders = new ArrayList<>();
        java.util.Random rand = new java.util.Random();

        for (int i = 0; i < 5; i++) {
            // Le Gasper calcule lui-même l'éparpillement de ses enfants
            double ecartMax = tailleTuile * 1.5;
            double offsetX = (rand.nextDouble() - 0.5) * ecartMax;
            double offsetY = (rand.nextDouble() - 0.5) * ecartMax;

            bebesSpiders.add(new ModeleSpider(this.getX() + offsetX, this.getY() + offsetY, pvVague));
        }

        return bebesSpiders;
    }
}