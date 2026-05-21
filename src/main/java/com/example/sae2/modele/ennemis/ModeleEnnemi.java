package com.example.sae2.modele.ennemis;

public class ModeleEnnemi {

    private double x;
    private double y;
    private final double cibleX;
    private final double cibleY;
    private final double vitesse; // pixels par seconde

    public ModeleEnnemi(double x, double y, double cibleX, double cibleY, double vitesse) {
        this.x      = x;
        this.y      = y;
        this.cibleX = cibleX;
        this.cibleY = cibleY;
        this.vitesse = vitesse;
    }

    public void deplacer(double deltaSecondes) {
        double dx       = cibleX - x;
        double dy       = cibleY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double deplacement = vitesse * deltaSecondes;

        if (distance <= deplacement) {
            x = cibleX;
            y = cibleY;
        } else {
            x += (dx / distance) * deplacement;
            y += (dy / distance) * deplacement;
        }
    }

    public boolean estArrive() {
        return x == cibleX && y == cibleY;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
