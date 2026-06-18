package com.example.sae2.controller;

import com.example.sae2.modele.Environnement;
import com.example.sae2.modele.Environnement.EtatEnnemi;
import com.example.sae2.modele.ennemis.*;
import com.example.sae2.vue.VueGameOver;
import com.example.sae2.vue.deck.VueAnimPerteCoeur;
import com.example.sae2.vue.ennemis.VueEnnemi;
import javafx.animation.AnimationTimer;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur des ennemis.
 *
 * Ne contient PLUS de logique de jeu : une seule boucle ({@link #boucle})
 * dont le {@code handle} délègue toute la mécanique à l'Environnement, puis
 * synchronise les Vues. Le mapping modèle -> vue est conservé ici car c'est un
 * détail de présentation (le "V"/"C"), pas de la logique métier.
 */
public class EnnemiController {

    private final GameController main;
    private final Environnement env;
    private final WaveController waveController;

    /** Lien entre un modèle d'ennemi et sa vue (présentation uniquement). */
    private final Map<ModeleEnnemi, VueEnnemi> vues = new HashMap<>();

    private final VueAnimPerteCoeur vueAnimPerteCoeur;
    private final VueGameOver vueGameOver;

    private AnimationTimer boucle;
    private long tempsPrecedent = -1;

    public EnnemiController(GameController main, WaveController waveController) {
        this.main = main;
        this.env = main.environnement;
        this.waveController = waveController;
        this.vueAnimPerteCoeur = new VueAnimPerteCoeur(main.paneJeu);
        this.vueGameOver = new VueGameOver();
        demarrerBoucle();
    }

    /** Crée un ennemi (modèle + vue) et l'enregistre dans l'Environnement. */
    public void spawnerEnnemi(String typeEnnemi, int pvBase, int ligneEntree, int colonneEntree) {
        double startX = colonneEntree * main.T;
        double startY = ligneEntree * main.T;

        ModeleEnnemi modEnnemi;
        switch (typeEnnemi) {
            case "Gish":   modEnnemi = new ModeleGish(startX, startY, pvBase); break;
            case "Gasper": modEnnemi = new ModeleGasper(startX, startY, pvBase); break;
            case "Spider": modEnnemi = new ModeleSpider(startX, startY, pvBase); break;
            default:       modEnnemi = new ModeleFatty(startX, startY, pvBase); break;
        }

        List<int[]> chemin = modEnnemi.calculerSonChemin(main.modele, main.T);
        if (chemin.isEmpty()) {
            waveController.notifierEnnemiTermine();
            return;
        }

        VueEnnemi vueEnnemi = new VueEnnemi(modEnnemi, main.paneEnnemis, main.modele.getDossierSprites());
        EtatEnnemi etat = new EtatEnnemi(modEnnemi, chemin);

        env.ajouterEnnemi(modEnnemi, etat);
        vues.put(modEnnemi, vueEnnemi);
    }

    // -------------------------------------------------------------------------
    // LA BOUCLE UNIQUE DU CONTRÔLEUR
    // -------------------------------------------------------------------------
    private void demarrerBoucle() {
        boucle = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (tempsPrecedent < 0) { tempsPrecedent = now; return; }
                double delta = (now - tempsPrecedent) / 100000000000.0;
                tempsPrecedent = now;

                // On itère sur une copie : la liste peut être modifiée pendant le tour.
                for (EtatEnnemi etat : new java.util.ArrayList<>(env.getEnnemisActifs().values())) {
                    boolean fini = env.fairePasserUnTourEnnemi(etat, delta);

                    VueEnnemi vue = vues.get(etat.modele);
                    if (fini) {
                        if (vue != null) { vue.supprimer(); vues.remove(etat.modele); }
                        traiterFin(etat);
                    } else if (vue != null) {
                        vue.actualiser();
                    }
                }
            }
        };
        boucle.start();
    }

    /** Délègue la fin d'un ennemi à l'Environnement, puis met à jour les Vues. */
    private void traiterFin(EtatEnnemi etat) {
        Environnement.ResultatFinEnnemi res = env.gererFinEnnemi(etat);

        if (res.estTue) {
            main.vueArgent.actualiser(main.modeleJoueur.getArgent());
            // Matérialise les ennemis enfants côté Vue.
            for (EtatEnnemi enfant : res.nouveauxEtats) {
                waveController.ajouterEnnemisVivants(1);
                VueEnnemi vueEnfant = new VueEnnemi(enfant.modele, main.paneEnnemis, main.modele.getDossierSprites());
                vues.put(enfant.modele, vueEnfant);
            }
        }

        if (res.joueurAPerduVie) {
            main.vueCoeurs.actualiser(main.modeleJoueur.getVies());
            vueAnimPerteCoeur.jouer();
            if (res.gameOver) {
                main.arreterTout();
                vueGameOver.afficher((Stage) main.paneJeu.getScene().getWindow());
                return;
            }
        }

        waveController.notifierEnnemiTermine();
    }

    /** Force le recalcul des chemins (appelé quand un obstacle bouge). */
    public void recalculerChemins() {
        env.recalculerCheminsEnnemis();
    }

    public void arreterTout() {
        if (boucle != null) boucle.stop();
    }
}
