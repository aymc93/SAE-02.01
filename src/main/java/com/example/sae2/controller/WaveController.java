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

    private final List<ModeleVague> vagues = new ArrayList<>();

    private int indexVague = 0;
    private int ennemisVivantsDansVague = 0;

    // ─── SAUVEGARDE FIXE DES COORDONNÉES DE LA PORTE ───
    private final int ligneEntreeFixe;
    private final int colonneEntreeFixe;
    // ───────────────────────────────────────────────────

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

        // 1. SAUVEGARDE : On mémorise la porte définie au chargement initial du jeu
        this.ligneEntreeFixe = main.modele.getLigneEntree();
        this.colonneEntreeFixe = main.modele.getColonneEntree();

        generer50Vagues();
        System.out.println("[Débug] Porte sauvegardée en Ligne : " + ligneEntreeFixe + " | Col : " + colonneEntreeFixe);
    }

    private void generer50Vagues() {
        for (int i = 1; i <= 50; i++) {
            int nbEnnemis = 3 + (i * 2);
            int pvBase = 350 + (i * 150);
            int vitesse = 48 + (i / 10) * 4;
            double intervalle = Math.max(0.4, 2.0 - (i * 0.03));

            vagues.add(new ModeleVague(nbEnnemis, "Fatty", pvBase, vitesse, intervalle));
        }
    }

    public void demarrerVague() {
        if (indexVague >= vagues.size()) {
            System.out.println("====== VICTOIRE ULTIME : Les 50 vagues ont été repoussées ! ======");
            main.arreterTout();
            return;
        }

        ModeleVague vague = vagues.get(indexVague);
        ennemisVivantsDansVague = vague.getNombreEnnemis();

        System.out.println("\n=========================================");
        System.out.println("====== DÉBUT DE LA VAGUE " + (indexVague + 1) + " / 50 ======");
        System.out.println("[Infos] Nombre d'ennemis attendus : " + ennemisVivantsDansVague);
        System.out.println("=========================================");

        for (int i = 0; i < vague.getNombreEnnemis(); i++) {
            double delai = 0.1 + (i * vague.getIntervalleSpawn());
            final int numeroEnnemi = i + 1;
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(delai), e -> {
                System.out.println("[Spawn] Ennemi " + numeroEnnemi + " / " + vague.getNombreEnnemis() + " est entré sur la map.");
                spawnerEnnemi(vague);
            }));
            timelinesActives.add(t);
            t.play();
        }
    }

    private void spawnerEnnemi(ModeleVague vague) {
        // CORRECTION : On utilise nos variables sauvegardées (ligneEntreeFixe / colonneEntreeFixe)
        ModeleEnnemi modEnnemi = new ModeleEnnemi(
                colonneEntreeFixe * main.T,
                ligneEntreeFixe * main.T,
                vague.getVitesse(),
                vague.getTypeEnnemi(),
                vague.getPvBase()
        );

        VueEnnemi vueEnnemi = new VueEnnemi(modEnnemi, main.paneEnnemis, main.modele.getDossierSprites());

        // Calcul du chemin à partir de notre porte fixe vers la sortie de la map
        List<int[]> chemin = BFS.calculerChemin(
                ligneEntreeFixe, colonneEntreeFixe,
                main.modele.getLigneSortie(), main.modele.getColonneSortie(),
                main.modele.getGrilleBloquee()
        );

        if (chemin.isEmpty()) {
            System.out.println("[ERREUR BFS] Aucun chemin trouvé ! L'ennemi est annulé pour ne pas bloquer la vague.");
            ennemisVivantsDansVague--;
            if (ennemisVivantsDansVague == 0 && main.modeleJoueur.estEnVie()) {
                avancerVague();
            }
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
            System.out.println("[Mort] Un ennemi a été éliminé. +100 pièces.");
            main.modeleJoueur.gagnerArgentKill();
            main.vueArgent.actualiser(main.modeleJoueur.getArgent());
        } else {
            System.out.println("[Fuite] Un ennemi a atteint la sortie ! Vie perdue.");
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

        ennemisVivantsDansVague--;
        System.out.println("[Suivi Vague] Ennemis restants à traiter dans cette vague : " + ennemisVivantsDansVague);

        if (ennemisVivantsDansVague == 0 && main.modeleJoueur.estEnVie()) {
            System.out.println("\n-----------------------------------------");
            System.out.println("vague " + (indexVague + 1) + " finie !");
            System.out.println("-----------------------------------------");

            avancerVague();
        }
    }

    private void avancerVague() {
        // 1. On calcule le niveau correspondant à la vague actuelle (Vagues 1-10 = Niveau 1)
        int niveauDeLaVagueActuelle = (indexVague / 10) + 1;

        indexVague++; // Passage à l'index de la vague suivante

        // 2. On calcule le niveau de la future vague qui va commencer
        int nouveauNiveauMap = (indexVague / 10) + 1;
        if (nouveauNiveauMap > 5) nouveauNiveauMap = 5;

        // 3. SECURITÉ CRITIQUE : On ne change le niveau QUE si on change de tranche de 10 vagues !
        if (nouveauNiveauMap != niveauDeLaVagueActuelle) {
            System.out.println("[Changement Niveau] Palier franchi ! Passage au niveau de carte : " + nouveauNiveauMap);

            // On met à jour le modèle
            main.modele.setNiveau(nouveauNiveauMap);

            // On met à jour l'affichage graphique du terrain
            main.getVueTerrain().actualiserNiveau(main.modele, main.getGridTerrain());
        } else {
            // Le joueur reste dans le même monde (ex: entre la vague 1 et la vague 2), on ne touche à rien !
            System.out.println("[Suivi Map] Toujours au même niveau de carte. Conservation de la grille actuelle.");
        }

        // 4. Lancement du compte à rebours habituel avant le départ des monstres
        Timeline compteARebours = new Timeline();
        for (int sec = 0; sec <= 4; sec++) {
            final int tempsRestant = 4 - sec;
            KeyFrame frame = new KeyFrame(Duration.seconds(sec), e -> {
                if (tempsRestant > 0) {
                    System.out.println("[Compte à rebours] Prochaine vague dans " + tempsRestant + " seconde(s)...");
                } else {
                    demarrerVague();
                }
            });
            compteARebours.getKeyFrames().add(frame);
        }
        timelinesActives.add(compteARebours);
        compteARebours.play();
    }

    public void arreter() {
        timelinesActives.forEach(Timeline::stop);
        timelinesActives.clear();
        ennemisActifs.values().forEach(EtatEnnemi::stopper);
    }
}