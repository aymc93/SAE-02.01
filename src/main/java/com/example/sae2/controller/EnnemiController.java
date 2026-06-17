package com.example.sae2.controller;

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
    private final WaveController waveController; // Lien vers le gestionnaire de vagues
    private static final int TAILLE_ENNEMI = 64;

    public class EtatEnnemi {
        public final ModeleEnnemi modele;
        final VueEnnemi vue;
        List<int[]> chemin;
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
    private final VueAnimPerteCoeur vueAnimPerteCoeur;
    private final VueGameOver vueGameOver;

    public EnnemiController(GameController main, WaveController waveController) {
        this.main = main;
        this.waveController = waveController;
        this.vueAnimPerteCoeur = new VueAnimPerteCoeur(main.paneJeu);
        this.vueGameOver = new VueGameOver();
    }

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

        VueEnnemi vueEnnemi = new VueEnnemi(modEnnemi, main.paneEnnemis, main.modele.getDossierSprites());
        List<int[]> chemin = modEnnemi.calculerSonChemin(main.modele, main.T);

        if (chemin.isEmpty()) {
            waveController.notifierEnnemiTermine(); // On prévient que l'ennemi est annulé
            return;
        }

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

                if (etat.modele.estMort()) {
                    etat.vue.supprimer();
                    stop();
                    gererFinEnnemi(etat.modele);
                    return;
                }

                if (etat.chemin == null || etat.chemin.isEmpty() || etat.indexChemin >= etat.chemin.size() - 1) {
                    if (etat.modele.aAtteintSortie(main.modele, main.T)) {
                        etat.vue.supprimer();
                        stop();
                        gererFinEnnemi(etat.modele);
                    } else {
                        etat.chemin = etat.modele.calculerSonChemin(main.modele, main.T);
                        etat.indexChemin = 0;
                    }
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
            System.out.println("[Mort] Un ennemi a été éliminé. +100 pièces.");
            main.modeleJoueur.gagnerArgentKill();
            main.vueArgent.actualiser(main.modeleJoueur.getArgent());

            // L'ennemi génère ses enfants (ex: Gasper -> Spiders)
            int pvVagueActuelle = waveController.getPvBaseVagueActuelle();
            List<ModeleEnnemi> nouveauxEnnemis = modEnnemi.genererEnnemisApresMort(pvVagueActuelle, main.T);

            if (!nouveauxEnnemis.isEmpty()) {
                waveController.ajouterEnnemisVivants(nouveauxEnnemis.size()); // On met à jour le compteur de la vague

                for (ModeleEnnemi nouvelEnnemi : nouveauxEnnemis) {
                    if (nouvelEnnemi instanceof ModeleSpider) ModeleSpider.sortieTrouvee = false;

                    VueEnnemi vueEnfant = new VueEnnemi(nouvelEnnemi, main.paneEnnemis, main.modele.getDossierSprites());
                    List<int[]> cheminEnfant = nouvelEnnemi.calculerSonChemin(main.modele, main.T);

                    EtatEnnemi etatEnfant = new EtatEnnemi(nouvelEnnemi, vueEnfant, cheminEnfant);
                    ennemisActifs.put(nouvelEnnemi, etatEnfant);
                    demarrerEnnemi(etatEnfant);
                }
            }
        } else {
            System.out.println("[Fuite] Un ennemi a atteint la sortie ! Vie perdue.");

            if (modEnnemi instanceof ModeleSpider && !ModeleSpider.sortieTrouvee) {
                ModeleSpider.sortieTrouvee = true;
                System.out.println("⚠️ UNE ARAIGNÉE A TROUVÉ LA SORTIE ! TOUTES LES AUTRES ATTAQUENT !");
                for (EtatEnnemi e : ennemisActifs.values()) {
                    if (e.modele instanceof ModeleSpider && !e.modele.estMort()) {
                        e.chemin = e.modele.calculerSonChemin(main.modele, main.T);
                        e.indexChemin = 0;
                    }
                }
            }

            main.modeleJoueur.perdreVie();
            main.vueCoeurs.actualiser(main.modeleJoueur.getVies());
            vueAnimPerteCoeur.jouer();
            if (!main.modeleJoueur.estEnVie()) {
                System.out.println("[GAME OVER] Plus de vies disponibles.");
                main.arreterTout();
                vueGameOver.afficher((Stage) main.paneJeu.getScene().getWindow());
                return;
            }
        }

        // On prévient le WaveController qu'un ennemi a fini son parcours (mort ou fuite)
        waveController.notifierEnnemiTermine();
    }

    public void arreterTout() {
        ennemisActifs.values().forEach(EtatEnnemi::stopper);
    }

    // Force tous les ennemis vivants à recalculer leur chemin (pour éviter le nouvel obstacle)
    public void recalculerChemins() {
        for (EtatEnnemi e : ennemisActifs.values()) {
            if (!e.modele.estMort()) {
                e.chemin = e.modele.calculerSonChemin(main.modele, main.T);
                e.indexChemin = 0; // Ils repartent sur ce nouveau chemin
            }
        }
    }
}