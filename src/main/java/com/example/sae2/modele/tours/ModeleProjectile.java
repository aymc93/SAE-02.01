package com.example.sae2.modele.tours;

import com.example.sae2.modele.ennemis.ModeleEnnemi;

public class ModeleProjectile {

    private double x;
    private double y;
    private final double vitesse;
    private final int degats;
    private final ModeleEnnemi cible;

    public ModeleProjectile(double x, double y, ModeleEnnemi cible,
                            int degats, double vitesse) {
        this.x = x;
        this.y = y;
        this.cible = cible;
        this.degats = degats;
        this.vitesse = vitesse;
    }

    public boolean avancer(double delta) {
        double cibleX = cible.getX() + 32;
        double cibleY = cible.getY() + 32;
        double dx = cibleX - x;
        double dy = cibleY - y;
        double distance   = Math.sqrt(dx * dx + dy * dy);
        double deplacement = vitesse * delta;

        if (distance <= deplacement) {
            x = cibleX;
            y = cibleY;
            cible.subirDegats(degats);
            return true;
        }

        x += dx / distance * deplacement;
        y += dy / distance * deplacement;
        return false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
