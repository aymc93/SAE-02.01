package com.example.sae2.modele.ennemis;

import com.example.sae2.modele.carte.ModeleTerrain;

import java.util.ArrayList;
import java.util.List;

public class ModeleEnnemi {

    public static final int PV_MAX = 500;

    /** Taille en pixels du sprite de l'ennemi (sert au centrage sur la tuile). */
    public static final int TAILLE_AFFICHAGE = 64;

    private double x;
    private double y;
    private final double vitesse;
    private final String typeEnnemi;
    private final int    pvMax;
    private int          pv;

    // Attribut ajouté pour suivre la position sur le chemin BFS
    private int indexChemin;

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
        this.indexChemin = 0; // Initialisé à la première case
    }

    public boolean deplacerVers(double targetX, double targetY, double delta) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Multiplié par 64 pour adapter la vitesse aux pixels de la carte
        double deplacement = vitesse * delta * 64;

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


    public List<int[]> calculerSonChemin(ModeleTerrain terrain, int tailleTuile) {
        int ligneDepart = Math.max(0, (int) (this.y / tailleTuile));
        int colonneDepart = Math.max(0, (int) (this.x / tailleTuile));

        return BFS.calculerChemin(
                ligneDepart, colonneDepart,
                terrain.getLigneSortie(), terrain.getColonneSortie(),
                terrain.getGrilleBloquee()
        );
    }

    public boolean aAtteintSortie(ModeleTerrain terrain, int tailleTuile) {
        int ligne = (int) (this.y / tailleTuile);
        int colonne = (int) (this.x / tailleTuile);
        return ligne == terrain.getLigneSortie() && colonne == terrain.getColonneSortie();
    }

    public List<ModeleEnnemi> genererEnnemisApresMort(int pvVague, double tailleTuile) {
        return new ArrayList<>(); // Par défaut, un ennemi normal ne fait rien apparaître
    }


}