package com.example.sae2.vue.autres;

import com.example.sae2.modele.autres.ModeleTerrain;
import com.example.sae2.modele.autres.ModeleTerrain.TypeCase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.Objects;

/**
 * VUE — GridPane qui affiche le terrain case par case.
 *
 * Pour chaque case PIERRE, on lit les 4 voisins et on choisit
 * la bonne tuile + la bonne rotation :
 *
 *  ┌─────────────────────────────────────────────────────────┐
 *  │  Route1.png  – ligne droite (base = VERTICALE)          │
 *  │    vertical   (haut/bas, sans côtés)   →   0°           │
 *  │    horizontal (gauche/droite, sans h/b)→  90°           │
 *  │                                                         │
 *  │  Route2.png  – coin (base = HAUT + DROITE ouvert)       │
 *  │    haut  + droite   →   0°                              │
 *  │    droite + bas     →  90°                              │
 *  │    bas   + gauche   → 180°                              │
 *  │    gauche + haut    → 270°                              │
 *  │                                                         │
 *  │  Route3.png  – T (base = HAUT+BAS+GAUCHE, droite fermé) │
 *  │    haut+bas+gauche  (fermé droite)  →   0°              │
 *  │    haut+droite+gauche (fermé bas)   →  90°              │
 *  │    haut+bas+droite  (fermé gauche)  → 180°              │
 *  │    bas+droite+gauche (fermé haut)   → 270°              │
 *  │                                                         │
 *  │  Route4.png  – croix (4 voisins)    →   0° (symétrique) │
 *  └─────────────────────────────────────────────────────────┘
 */
public class VueTerrain extends GridPane {

    public static final int TAILLE = 64;

    private final Image imageHerbe;
    private final Image imageRouteDroite;
    private final Image imageRouteVirage;
    private final Image imageRouteT;
    private final Image imageRouteCroix;

    public VueTerrain(ModeleTerrain terrain) {
        imageHerbe = chargerImage("/com/example/sae2/images/Herbe.png");
        imageRouteDroite = chargerImage("/com/example/sae2/images/route/Route1.png");
        imageRouteVirage = chargerImage("/com/example/sae2/images/route/Route2.png");
        imageRouteT = chargerImage("/com/example/sae2/images/route/Route3.png");
        imageRouteCroix = chargerImage("/com/example/sae2/images/route/Route4.png");

        setHgap(0);
        setVgap(0);

        afficherTerrain(terrain);
    }

    private void afficherTerrain(ModeleTerrain terrain) {

        for (int ligne = 0; ligne < terrain.getNbLignes(); ligne++) {
            for (int colonne = 0; colonne < terrain.getNbCols(); colonne++) {

                ImageView imageCase;

                if (terrain.getCase(ligne, colonne) == TypeCase.HERBE) {
                    imageCase = creerImageView(imageHerbe, 0);
                } else {
                    imageCase = choisirImageRoute(terrain, ligne, colonne);
                }

                add(imageCase, colonne, ligne);
            }
        }
    }

    private ImageView choisirImageRoute(ModeleTerrain terrain, int ligne, int colonne) {

        boolean routeHaut = estRoute(terrain, ligne - 1, colonne);
        boolean routeBas = estRoute(terrain, ligne + 1, colonne);
        boolean routeGauche = estRoute(terrain, ligne, colonne - 1);
        boolean routeDroite = estRoute(terrain, ligne, colonne + 1);

        int nbRoutesAutour = 0;

        if (routeHaut) nbRoutesAutour++;
        if (routeBas) nbRoutesAutour++;
        if (routeGauche) nbRoutesAutour++;
        if (routeDroite) nbRoutesAutour++;

        if (nbRoutesAutour == 4) {
            return creerImageView(imageRouteCroix, 0);
        }
        if (nbRoutesAutour == 3) {
            return choisirRouteT(routeHaut, routeBas, routeGauche, routeDroite);
        }
        if (routeHaut && routeBas && !routeGauche && !routeDroite) {
            return creerImageView(imageRouteDroite, 0);
        }
        if (routeGauche && routeDroite && !routeHaut && !routeBas) {
            return creerImageView(imageRouteDroite, 90);
        }
        if (routeHaut && routeDroite) {
            return creerImageView(imageRouteVirage, 270);
        }
        if (routeDroite && routeBas) {
            return creerImageView(imageRouteVirage, 0);
        }
        if (routeBas && routeGauche) {
            return creerImageView(imageRouteVirage, 90);
        }
        if (routeGauche && routeHaut) {
            return creerImageView(imageRouteVirage, 180);
        }
        if (routeHaut || routeBas) {
            return creerImageView(imageRouteDroite, 0);
        }
        if (routeGauche || routeDroite) {
            return creerImageView(imageRouteDroite, 90);
        }
        return creerImageView(imageRouteDroite, 0);
    }

    private ImageView choisirRouteT(boolean haut, boolean bas, boolean gauche, boolean droite) {

        if (!droite) {
            return creerImageView(imageRouteT, 180);
        }
        if (!bas) {
            return creerImageView(imageRouteT, 90);
        }
        if (!gauche) {
            return creerImageView(imageRouteT, 0);
        }
        return creerImageView(imageRouteT, 270);
    }

    private boolean estRoute(ModeleTerrain terrain, int ligne, int colonne) {

        if (ligne < 0 || ligne >= terrain.getNbLignes()) {
            return false;
        }
        if (colonne < 0 || colonne >= terrain.getNbCols()) {
            return false;
        }
        return terrain.getCase(ligne, colonne) == TypeCase.PIERRE;
    }

    private ImageView creerImageView(Image image, double rotation) {

        ImageView imageView = new ImageView(image);

        imageView.setFitWidth(TAILLE);
        imageView.setFitHeight(TAILLE);
        imageView.setPreserveRatio(false);
        imageView.setRotate(rotation);

        return imageView;
    }

    private Image chargerImage(String cheminImage) {

        return new Image(Objects.requireNonNull(
                VueTerrain.class.getResourceAsStream(cheminImage),
                "Image introuvable : " + cheminImage
        ));
    }
}