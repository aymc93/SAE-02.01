package com.example.sae2.vue.deck;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.net.URL;

public class VueArgent {

    private static final double LARGEUR_CHIFFRE = 20;
    private static final double HAUTEUR_CHIFFRE = 28;
    private static final double TAILLE_PIECE    = 32;
    private static final int    NB_CHIFFRES_MIN = 3;

    private final HBox    conteneur;
    private final Image[] spritesChiffres = new Image[10];
    private       Image   spritePiece;

    public VueArgent(HBox conteneur) {
        this.conteneur = conteneur;
        chargerSprites();
        afficherInitial();
    }

    private void chargerSprites() {
        for (int i = 0; i <= 9; i++) {
            URL url = getClass().getResource("/com/example/sae2/images/Chiffres/" + i + ".png");
            if (url != null) spritesChiffres[i] = new Image(url.toExternalForm());
        }
        URL urlPiece = getClass().getResource("/com/example/sae2/images/UI/piece.png");
        if (urlPiece != null) spritePiece = new Image(urlPiece.toExternalForm());
    }

    // Affiche "000" + icône pièce au démarrage (sans vider/recréer lors de chaque gain)
    private void afficherInitial() {
        String valeur = String.format("%0" + NB_CHIFFRES_MIN + "d", 0);
        for (char c : valeur.toCharArray()) {
            conteneur.getChildren().add(creerChiffre(c - '0'));
        }
        conteneur.getChildren().add(creerPiece());
    }

    // Met à jour uniquement les chiffres existants (ou les recrée si le nombre de digits change)
    public void actualiser(int montant) {
        String valeur = String.format("%0" + NB_CHIFFRES_MIN + "d", montant);
        int nbChiffres = valeur.length();
        int nbActuels  = conteneur.getChildren().size() - 1; // -1 pour l'icône pièce

        if (nbChiffres != nbActuels) {
            // Le nombre de digits a changé (ex : 999 → 1000) : on recrée tout
            conteneur.getChildren().clear();
            for (char c : valeur.toCharArray()) {
                conteneur.getChildren().add(creerChiffre(c - '0'));
            }
            conteneur.getChildren().add(creerPiece());
        } else {
            // Même nombre de digits : on met juste à jour les images
            for (int i = 0; i < nbChiffres; i++) {
                int chiffre = valeur.charAt(i) - '0';
                ImageView iv = (ImageView) conteneur.getChildren().get(i);
                if (spritesChiffres[chiffre] != null) iv.setImage(spritesChiffres[chiffre]);
            }
        }
    }

    private ImageView creerChiffre(int chiffre) {
        ImageView iv = new ImageView();
        iv.setFitWidth(LARGEUR_CHIFFRE);
        iv.setFitHeight(HAUTEUR_CHIFFRE);
        iv.setPreserveRatio(false);
        if (spritesChiffres[chiffre] != null) iv.setImage(spritesChiffres[chiffre]);
        return iv;
    }

    private ImageView creerPiece() {
        ImageView iv = new ImageView();
        iv.setFitWidth(TAILLE_PIECE);
        iv.setFitHeight(TAILLE_PIECE);
        iv.setPreserveRatio(true);
        if (spritePiece != null) iv.setImage(spritePiece);
        return iv;
    }
}
