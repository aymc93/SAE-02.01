package com.example.sae2.modele.tours;

import com.example.sae2.modele.deck.TypeCarte;
import com.example.sae2.modele.ennemis.ModeleEnnemi;

public class ModeleTour {

    public static final int DIR_BAS    = 0;
    public static final int DIR_DROITE = 1;
    public static final int DIR_HAUT   = 2;
    public static final int DIR_GAUCHE = 3;

    private final int colonne;
    private final int ligne;
    private TypeCarte type;
    private int    degats;
    private double vitesseProjectile;

    private long dernierTir = 0;
    private static final long DELAI_TIR = 800000000L;

    public ModeleTour(int colonne, int ligne) {
        this.colonne           = colonne;
        this.ligne             = ligne;
        this.type              = TypeCarte.SIMPLE_TOWER;
        this.degats            = TypeCarte.SIMPLE_TOWER.getDegats();
        this.vitesseProjectile = TypeCarte.SIMPLE_TOWER.getVitesseProjectile();
    }

    public void appliquerPouvoir(TypeCarte pouvoir) {
        this.type              = pouvoir;
        this.degats            = pouvoir.getDegats();
        this.vitesseProjectile = pouvoir.getVitesseProjectile();
    }

    /**
     * Détermine la direction à afficher en fonction du vecteur tour → ennemi.
     * @param dx différence en X (ennemiX - tourX)
     * @param dy différence en Y (ennemiY - tourY)
     */
    public static int calculerDirection(double dx, double dy) {
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (angle >= -45  && angle <  45)  return DIR_DROITE;
        if (angle >=  45  && angle < 135)  return DIR_BAS;
        if (angle >= -135 && angle < -45)  return DIR_HAUT;
        return DIR_GAUCHE;
    }

    public int       getColonne()           { return colonne; }
    public int       getLigne()             { return ligne; }
    public TypeCarte getType()              { return type; }
    public int       getDegats()            { return degats; }
    public double    getVitesseProjectile() { return vitesseProjectile; }

    public ModeleEnnemi trouverCible(java.util.Set<ModeleEnnemi> ennemis, int tailleTuile) {
        ModeleEnnemi cible = null;
        double distMin = Double.MAX_VALUE;

        // Calcul du centre de la tour
        double cx = this.colonne * tailleTuile + tailleTuile / 2.0;
        double cy = this.ligne * tailleTuile + tailleTuile / 2.0;
        double portee = 3.0 * tailleTuile; // Portée de 3 cases

        for (ModeleEnnemi e : ennemis) {
            if (e.estMort()) continue;

            // Le centre de l'ennemi (tailleTuile / 2.0 correspond à 32 pixels)
            double dx = (e.getX() + tailleTuile / 2.0) - cx;
            double dy = (e.getY() + tailleTuile / 2.0) - cy;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist <= portee && dist < distMin) {
                distMin = dist;
                cible = e;
            }
        }
        return cible;
    }

    /**
     * La tour calcule la direction vers laquelle elle doit regarder
     */
    public int calculerDirectionVers(ModeleEnnemi cible, int tailleTuile) {
        double cx = this.colonne * tailleTuile + tailleTuile / 2.0;
        double cy = this.ligne * tailleTuile + tailleTuile / 2.0;
        double dx = (cible.getX() + tailleTuile / 2.0) - cx;
        double dy = (cible.getY() + tailleTuile / 2.0) - cy;

        return calculerDirection(dx, dy); // Appel à ta méthode statique existante
    }

    /** La tour vérifie si son cooldown de tir est terminé */
    public boolean peutTirer(long now) {
        if (now - dernierTir >= DELAI_TIR) {
            dernierTir = now;
            return true;
        }
        return false;
    }

}
