package com.example.sae2.modele.ennemis;

public class ModeleEnnemi {

    public static final int PV_MAX = 500;

    private double x;
    private double y;
    private final double vitesse;
    private final String typeEnnemi;
    private final int    pvMax;
    private int          pv;

    public ModeleEnnemi(double x, double y, double vitesse, String typeEnnemi) {
        this(x, y, vitesse, typeEnnemi, PV_MAX);
    }

    public ModeleEnnemi(double x, double y, double vitesse, String typeEnnemi, int pvMax) {
        this.x          = x;
        this.y          = y;
        this.vitesse    = vitesse;
        this.typeEnnemi = typeEnnemi;
        this.pvMax      = pvMax;
        this.pv         = pvMax;
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

    /** Réduit les PV de l'ennemi (minimum 0). Ignoré si déjà mort. */
    public void subirDegats(int degats) {
        if (pv <= 0) return;                      // projectiles en vol ignorés après mort
        pv = Math.max(0, pv - degats);
        System.out.println("[Ennemi] -" + degats + " PV  →  " + pv + " / " + pvMax + " PV restants"
                + (pv == 0 ? "  (MORT)" : ""));
    }

    public boolean estMort()  { return pv <= 0; }
    public int     getPv()    { return pv; }
    public int     getPvMax() { return pvMax; }

    public double getX()          { return x; }
    public double getY()          { return y; }
    public double getVitesse()    { return vitesse; }
    public String getTypeEnnemi() { return typeEnnemi; }
}
