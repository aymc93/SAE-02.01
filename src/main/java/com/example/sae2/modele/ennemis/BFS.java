package com.example.sae2.modele.ennemis;

import java.util.*;

public class BFS {

    public static List<int[]> calculerChemin(
            int ligneDepart, int colonneDepart,
            int ligneCible, int colonneCible,
            boolean[][] grilleBloquee) {

        int lignes = grilleBloquee.length;
        int colonnes = grilleBloquee[0].length;

        boolean[][] visite = new boolean[lignes][colonnes];

        int[][] parentLigne = new int[lignes][colonnes];
        int[][] parentColonne = new int[lignes][colonnes];

        for (int i = 0; i < lignes; i++) {
            Arrays.fill(parentLigne[i], -1);
            Arrays.fill(parentColonne[i], -1);
        }

        Queue<int[]> file = new LinkedList<>();

        file.add(new int[]{ligneDepart, colonneDepart});
        visite[ligneDepart][colonneDepart] = true;

        int[] depLigne = {-1, 1, 0, 0};
        int[] depColonne = {0, 0, -1, 1};

        while (!file.isEmpty()) {

            int[] position = file.poll();

            int ligne = position[0];
            int colonne = position[1];

            for (int i = 0; i < 4; i++) {

                int nouvelleLigne = ligne + depLigne[i];
                int nouvelleColonne = colonne + depColonne[i];

                if (nouvelleLigne >= 0 && nouvelleLigne < lignes && nouvelleColonne >= 0 && nouvelleColonne < colonnes
                        && !visite[nouvelleLigne][nouvelleColonne] && !grilleBloquee[nouvelleLigne][nouvelleColonne]) {

                    visite[nouvelleLigne][nouvelleColonne] = true;

                    parentLigne[nouvelleLigne][nouvelleColonne] = ligne;
                    parentColonne[nouvelleLigne][nouvelleColonne] = colonne;

                    file.add(new int[]{nouvelleLigne, nouvelleColonne});
                }
            }
        }

        if (!visite[ligneCible][colonneCible]) {
            return Collections.emptyList();
        }

        List<int[]> chemin = new ArrayList<>();

        int ligne = ligneCible;
        int colonne = colonneCible;

        while (ligne != ligneDepart || colonne != colonneDepart) {

            chemin.add(0, new int[]{ligne, colonne});

            int ancienneLigne = parentLigne[ligne][colonne];
            int ancienneColonne = parentColonne[ligne][colonne];

            ligne = ancienneLigne;
            colonne = ancienneColonne;
        }

        chemin.add(0, new int[]{ligneDepart, colonneDepart});

        return chemin;
    }
}