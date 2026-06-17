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

}
