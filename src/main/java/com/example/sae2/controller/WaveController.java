package com.example.sae2.controller;

import com.example.sae2.modele.ennemis.*;
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

    private final int ligneEntreeFixe;
    private final int colonneEntreeFixe;


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
        java.util.Random random = new java.util.Random();

        for (int i = 1; i <= 50; i++) {
            int nbEnnemis = 3 + (i * 2);
            int pvBase = 350 + (i * 150);
            int vitesse = 48 + (i / 10) * 4;
            double intervalle = Math.max(0.4, 2.0 - (i * 0.03));

            if (i % 10 == 0) {
                // Vague de Boss (10, 20, 30, 40, 50) : 1 seul ennemi massif
                vagues.add(new ModeleVague(1, "Gish", pvBase, vitesse, intervalle));
            }
            else if (i < 4) {
                // Vagues 1 à 3 : Que des Fatty pour laisser le joueur s'installer
                vagues.add(new ModeleVague(nbEnnemis, "Fatty", pvBase, vitesse, intervalle));
            }
            else {
                // À partir de la vague 4, le jeu choisit le type de monstre au hasard
                int choix = random.nextInt(3); // Tire un nombre entre 0, 1 et 2
                String typeVague;

                if (choix == 0) {
                    typeVague = "Fatty";
                } else if (choix == 1) {
                    typeVague = "Spider"; // Vague d'araignées (très rapides !)
                } else {
                    typeVague = "Gasper";
                    // On réduit un peu le nombre d'ennemis pour Gasper,
                    // car chaque mort génère 3 Spiders, ça peut vite surcharger la map !
                    nbEnnemis = Math.max(3, nbEnnemis / 2);
                }

                vagues.add(new ModeleVague(nbEnnemis, typeVague, pvBase, vitesse, intervalle));
            }
        }
    }

    private int[] getTuileAleatoireLibre() {
        boolean[][] grille = main.modele.getGrilleBloquee();
        java.util.Random rand = new java.util.Random();
        int l, c;
        do {
            l = rand.nextInt(grille.length);
            c = rand.nextInt(grille[0].length);
        } while (grille[l][c]); // Recommence si la case est un obstacle
        return new int[]{l, c};
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
            // Si c'est une araignée, elles spawnent toutes d'un coup (0.1s). Sinon, délai normal.
            double delai = vague.getTypeEnnemi().equals("Spider") ? 0.1 : 0.1 + (i * vague.getIntervalleSpawn());

            final int numeroEnnemi = i + 1;
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(delai), e -> {
                System.out.println("[Spawn] Ennemi " + numeroEnnemi + " / " + vague.getNombreEnnemis() + " est entré.");
                spawnerEnnemi(vague);
            }));
            timelinesActives.add(t);
            t.play();
        }
    }

    private void spawnerEnnemi(ModeleVague vague) {
        // Tout le monde part de la porte d'entrée
        double startX = colonneEntreeFixe * main.T;
        double startY = ligneEntreeFixe * main.T;

        // On garde juste un petit décalage visuel pour les Spiders à l'entrée
        // pour éviter qu'elles soient parfaitement superposées en sortant
        if (vague.getTypeEnnemi().equals("Spider")) {
            java.util.Random rand = new java.util.Random();
            startX += (rand.nextDouble() - 0.5) * 40;
            startY += (rand.nextDouble() - 0.5) * 40;
        }

        ModeleEnnemi modEnnemi;
        switch (vague.getTypeEnnemi()) {
            case "Gish":   modEnnemi = new ModeleGish(startX, startY, vague.getPvBase()); break;
            case "Gasper": modEnnemi = new ModeleGasper(startX, startY, vague.getPvBase()); break;
            case "Spider": modEnnemi = new ModeleSpider(startX, startY, vague.getPvBase()); break;
            default:       modEnnemi = new ModeleFatty(startX, startY, vague.getPvBase()); break;
        }

        VueEnnemi vueEnnemi = new VueEnnemi(modEnnemi, main.paneEnnemis, main.modele.getDossierSprites());

        List<int[]> chemin;

        // La mécanique de recherche (Errance)
        if (modEnnemi instanceof com.example.sae2.modele.ennemis.ModeleSpider && !com.example.sae2.modele.ennemis.ModeleSpider.sortieTrouvee) {
            int[] dest = getTuileAleatoireLibre();
            // Le calcul du chemin part bien de la porte d'entrée !
            chemin = BFS.calculerChemin(ligneEntreeFixe, colonneEntreeFixe, dest[0], dest[1], main.modele.getGrilleBloquee());
        } else {
            // Chemin direct vers la sortie (Fatty, Gasper, Gish)
            chemin = BFS.calculerChemin(ligneEntreeFixe, colonneEntreeFixe, main.modele.getLigneSortie(), main.modele.getColonneSortie(), main.modele.getGrilleBloquee());
        }

        if (chemin.isEmpty()) {
            ennemisVivantsDansVague--;
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

                // 1. L'ennemi a été tué par tes tours
                if (etat.modele.estMort()) {
                    etat.vue.supprimer();
                    stop();
                    gererFinEnnemi(etat.modele);
                    return;
                }

                // 2. L'ennemi est arrivé à la fin de son chemin actuel
                if (etat.chemin == null || etat.chemin.isEmpty() || etat.indexChemin >= etat.chemin.size() - 1) {

                    // Cas spécial : L'araignée est en mode errance et vient de finir son trajet aléatoire
                    if (etat.modele instanceof com.example.sae2.modele.ennemis.ModeleSpider && !com.example.sae2.modele.ennemis.ModeleSpider.sortieTrouvee) {
                        int[] dest = getTuileAleatoireLibre();
                        etat.chemin = BFS.calculerChemin(
                                (int) (etat.modele.getY() / main.T),
                                (int) (etat.modele.getX() / main.T),
                                dest[0], dest[1],
                                main.modele.getGrilleBloquee()
                        );
                        etat.indexChemin = 0; // On réinitialise l'index pour son nouveau chemin
                        return; // On arrête là pour cette frame, elle avancera au prochain tick !
                    }
                    // Cas normal : C'est la vraie sortie (pour un Fatty, Gasper, Gish ou une Araignée alertée)
                    else {
                        etat.vue.supprimer();
                        stop();
                        gererFinEnnemi(etat.modele);
                        return;
                    }
                }

                // 3. Déplacement continu vers la prochaine tuile
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

            // --- NOUVEAUTÉ : SI GASPER MEURT, SPAWN DES SPIDERS ---
            if (modEnnemi instanceof ModeleGasper) {
                ModeleVague vagueActuelle = vagues.get(indexVague);

                ennemisVivantsDansVague += 3;
                ModeleSpider.sortieTrouvee = false;

                java.util.Random rand = new java.util.Random();

                for (int i = 0; i < 3; i++) {
                    // ÉPARPILLEMENT MASSIF : Elles explosent beaucoup plus loin (jusqu'à 1.5 cases d'écart)
                    double ecartMax = main.T * 1.5;
                    double offsetX = (rand.nextDouble() - 0.5) * ecartMax;
                    double offsetY = (rand.nextDouble() - 0.5) * ecartMax;

                    double startX = modEnnemi.getX() + offsetX;
                    double startY = modEnnemi.getY() + offsetY;

                    ModeleSpider spider = new ModeleSpider(startX, startY, vagueActuelle.getPvBase());
                    VueEnnemi vueSpider = new VueEnnemi(spider, main.paneEnnemis, main.modele.getDossierSprites());

                    int ligneDepart = Math.max(0, Math.min(main.modele.getNbLignes() - 1, (int) (startY / main.T)));
                    int colonneDepart = Math.max(0, Math.min(main.modele.getNbColonnes() - 1, (int) (startX / main.T)));

                    List<int[]> cheminSpider = BFS.calculerChemin(
                            ligneDepart, colonneDepart,
                            main.modele.getLigneSortie(), main.modele.getColonneSortie(),
                            main.modele.getGrilleBloquee()
                    );

                    EtatEnnemi etatSpider = new EtatEnnemi(spider, vueSpider, cheminSpider);
                    ennemisActifs.put(spider, etatSpider);
                    demarrerEnnemi(etatSpider);
                }
            }


        } else {
            System.out.println("[Fuite] Un ennemi a atteint la sortie ! Vie perdue.");

            // --- DÉCLENCHEMENT DE L'ESSAIM ---
            if (modEnnemi instanceof ModeleSpider && !ModeleSpider.sortieTrouvee) {
                ModeleSpider.sortieTrouvee = true;
                System.out.println("⚠️ UNE ARAIGNÉE A TROUVÉ LA SORTIE ! TOUTES LES AUTRES ATTAQUENT !");

                // On recalcule le chemin de TOUTES les araignées actuellement sur la map
                for (EtatEnnemi e : ennemisActifs.values()) {
                    if (e.modele instanceof ModeleSpider && !e.modele.estMort()) {
                        e.chemin = BFS.calculerChemin(
                                (int) (e.modele.getY() / main.T),
                                (int) (e.modele.getX() / main.T),
                                main.modele.getLigneSortie(), main.modele.getColonneSortie(),
                                main.modele.getGrilleBloquee()
                        );
                        e.indexChemin = 0; // Elles repartent sur leur nouveau chemin
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