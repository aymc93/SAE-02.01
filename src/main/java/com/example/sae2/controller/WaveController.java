package com.example.sae2.controller;

import com.example.sae2.modele.ennemis.BFS;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.modele.ennemis.ModeleVague;
import com.example.sae2.vue.VueGameOver;
import com.example.sae2.vue.deck.VueAnimPerteCoeur;
import com.example.sae2.vue.ennemis.VueEnnemi;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaveController {

    private final GameController main;
    private static final int TAILLE_ENNEMI = 64;
    private static final List<ModeleVague> VAGUES = List.of(new ModeleVague(5, "Fatty", 500, 48, 2.0));

    private int indexVague = 0;
    private int ennemisVivantsDansVague = 0;

    public class EtatEnnemi {
        public final ModeleEnnemi modele;
        final VueEnnemi vue;
        final List<int[]> chemin;
        int indexChemin = 0;
        long tempsPrecedent = -1;
        AnimationTimer timer;

        EtatEnnemi(ModeleEnnemi modele, VueEnnemi vue, List<int[]> chemin) {
            this.modele = modele;
            this.vue = vue;
            this.chemin = chemin;
        }
        void stopper() { if (timer != null) timer.stop(); }
    }

    public final Map<ModeleEnnemi, EtatEnnemi> ennemisActifs = new HashMap<>();
    private final List<Timeline> timelinesActives = new ArrayList<>();
    private final VueAnimPerteCoeur vueAnimPerteCoeur;
    private final VueGameOver vueGameOver;

    public WaveController(GameController main) {
        this.main = main;
        this.vueAnimPerteCoeur = new VueAnimPerteCoeur(main.paneJeu);
        this.vueGameOver = new VueGameOver();
    }

    public void demarrerVague() {
        if (indexVague >= VAGUES.size()) return;
        ModeleVague vague = VAGUES.get(indexVague);
        ennemisVivantsDansVague = vague.getNombreEnnemis();

        for (int i = 0; i < vague.getNombreEnnemis(); i++) {
            double delai = i * vague.getIntervalleSpawn();
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(delai), e -> spawnerEnnemi(vague)));
            timelinesActives.add(t);
            t.play();
        }
    }

    private void spawnerEnnemi(ModeleVague vague) {
        ModeleEnnemi modEnnemi = new ModeleEnnemi(main.modele.getColonneEntree() * main.T, main.modele.getLigneEntree() * main.T, vague.getVitesse(), vague.getTypeEnnemi(), vague.getPvBase());
        VueEnnemi vueEnnemi = new VueEnnemi(modEnnemi, main.paneEnnemis, main.modele.getDossierSprites());

        List<int[]> chemin = BFS.calculerChemin(main.modele.getLigneEntree(), main.modele.getColonneEntree(), main.modele.getLigneSortie(), main.modele.getColonneSortie(), main.modele.getGrilleBloquee());

        EtatEnnemi etat = new EtatEnnemi(modEnnemi, vueEnnemi, chemin);
        ennemisActifs.put(modEnnemi, etat);
        demarrerEnnemi(etat);
    }

    private void demarrerEnnemi(EtatEnnemi etat) {
        int offset = (main.T - TAILLE_ENNEMI) / 2;
        etat.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (etat.tempsPrecedent < 0) { etat.tempsPrecedent = now; return; }
                double delta = (now - etat.tempsPrecedent) / 100000000000.0;
                etat.tempsPrecedent = now;

                if (etat.modele.estMort() || etat.chemin.isEmpty() || etat.indexChemin >= etat.chemin.size() - 1) {
                    etat.vue.supprimer();
                    stop();
                    gererFinEnnemi(etat.modele);
                    return;
                }

                int[] prochaine = etat.chemin.get(etat.indexChemin + 1);
                if (etat.modele.deplacerVers(prochaine[1] * main.T + offset, prochaine[0] * main.T + offset, delta)) {
                    etat.indexChemin++;
                }
                etat.vue.actualiser();
            }
        };
        etat.timer.start();
    }

    private void gererFinEnnemi(ModeleEnnemi modEnnemi) {
        boolean estTue = modEnnemi.estMort();
        ennemisActifs.remove(modEnnemi);

        if (estTue) {
            main.modeleJoueur.gagnerArgentKill();
            main.vueArgent.actualiser(main.modeleJoueur.getArgent());
        } else {
            main.modeleJoueur.perdreVie();
            main.vueCoeurs.actualiser(main.modeleJoueur.getVies());
            vueAnimPerteCoeur.jouer();
            if (!main.modeleJoueur.estEnVie()) {
                main.arreterTout();
                vueGameOver.afficher((Stage) main.paneJeu.getScene().getWindow());
                return;
            }
        }

        if (--ennemisVivantsDansVague == 0 && main.modeleJoueur.estEnVie()) {
            indexVague++;
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(4), e -> demarrerVague()));
            timelinesActives.add(t);
            t.play();
        }
    }

    public void arreter() {
        timelinesActives.forEach(Timeline::stop);
        timelinesActives.clear();
        ennemisActifs.values().forEach(EtatEnnemi::stopper);
    }
}