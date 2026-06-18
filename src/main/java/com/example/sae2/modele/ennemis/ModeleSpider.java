package com.example.sae2.modele.ennemis;

import com.example.sae2.modele.carte.ModeleTerrain;
import java.util.List;

public class ModeleSpider extends ModeleEnnemi {
    public static boolean sortieTrouvee = false;
    public ModeleSpider(double x, double y, int pvBase) {
        super(x + (Math.random() - 0.5) * 40, y + (Math.random() - 0.5) * 40, 250.0, "Spider", pvBase / 10);
    }

    @Override
    public List<int[]> calculerSonChemin(ModeleTerrain terrain, int tailleTuile) {
        int ligneDepart = Math.max(0, (int) (getY() / tailleTuile));
        int colonneDepart = Math.max(0, (int) (getX() / tailleTuile));

        if (!sortieTrouvee) {
            int[] dest = terrain.getTuileAleatoireLibre();
            return BFS.calculerChemin(ligneDepart, colonneDepart, dest[0], dest[1], terrain.getGrilleBloquee());
        } else {
            return super.calculerSonChemin(terrain, tailleTuile);
        }
    }
}