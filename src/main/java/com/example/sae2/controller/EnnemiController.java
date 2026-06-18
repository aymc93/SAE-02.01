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

public class EnnemiController {

    private final GameController main;
    private final Environnement env;
    private final WaveController waveController;
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

    public void spawnerEnnemi(String typeEnnemi, int pvBase, int ligneEntree, int colonneEntree) {
        EtatEnnemi etat = env.creerEtatEnnemi(typeEnnemi, pvBase, ligneEntree, colonneEntree);
        if (etat == null) {
            waveController.notifierEnnemiTermine();
            return;
        }
        VueEnnemi vueEnnemi = new VueEnnemi(etat.modele, main.paneEnnemis, main.modele.getDossierSprites());
        vues.put(etat.modele, vueEnnemi);
    }

    private void demarrerBoucle() {
        boucle = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (tempsPrecedent < 0) { tempsPrecedent = now; return; }
                double delta = (now - tempsPrecedent) / 100000000000.0;
                tempsPrecedent = now;
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

    private void traiterFin(EtatEnnemi etat) {
        Environnement.ResultatFinEnnemi res = env.gererFinEnnemi(etat);
        if (res.estTue) {
            main.vueArgent.actualiser(main.modeleJoueur.getArgent());
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

    public void arreterTout() {
        if (boucle != null) boucle.stop();
    }
}
