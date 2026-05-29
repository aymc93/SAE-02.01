package com.example.sae2.modele.ennemis;

public class ModeleEnnemi {

    private double x;
    private double y;
    private final double vitesse;    // pixels par seconde
    private final String typeEnnemi; // ex : "Fatty" -> nom du fichier .gif

    public ModeleEnnemi(double x, double y, double vitesse, String typeEnnemi) {
        this.x = x;
        this.y = y;
        this.vitesse = vitesse;
        this.typeEnnemi = typeEnnemi;
    }

    public boolean deplacerVers(double targetX, double targetY, double delta) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double deplacement = vitesse * delta;

        if (distance <= deplacement) {
            x = targetX;
            y = targetY;
            return true;
        }
        x += (dx / distance) * deplacement;
        y += (dy / distance) * deplacement;
        return false;
    }

    public double getX()          { return x; }
    public double getY()          { return y; }
    public double getVitesse()    { return vitesse; }
    public String getTypeEnnemi() { return typeEnnemi; }
}
