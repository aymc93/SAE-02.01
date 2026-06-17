package com.example.sae2.modele.ennemis;

public class ModeleSpider extends ModeleEnnemi {
    // Variable statique : dès qu'une araignée passe cette variable à "true", toutes les autres le savent
    public static boolean sortieTrouvee = false;

    // Très rapide, très peu de PV
    public ModeleSpider(double x, double y, int pvBase) {
        super(x, y, 150.0, "Spider", pvBase / 10);
    }
}