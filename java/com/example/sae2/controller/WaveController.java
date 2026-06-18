package com.example.sae2.controller;

import com.example.sae2.modele.ennemis.ModeleVague;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class WaveController {

    private final GameController main;
    private EnnemiController ennemiController; // Lien vers le contrôleur des ennemis

    private final List<ModeleVague> vagues = new ArrayList<>();
    private final List<Timeline> timelinesActives = new ArrayList<>();

    private int indexVague = 0;
    private int ennemisVivantsDansVague = 0;

    private final int ligneEntreeFixe;
    private final int colonneEntreeFixe;

    public WaveController(GameController main) {
        this.main = main;
        this.ligneEntreeFixe = main.modele.getLigneEntree();
        this.colonneEntreeFixe = main.modele.getColonneEntree();
        generer50Vagues();
    }

    // On lie les deux contrôleurs
    public void setEnnemiController(EnnemiController ennemiController) {
        this.ennemiController = ennemiController;
    }

    private void generer50Vagues() {
        java.util.Random random = new java.util.Random();
        for (int i = 1; i <= 50; i++) {
            int nbEnnemis = 3 + (i * 2);
            int pvBase = 350 + (i * 150);
            int vitesse = 48 + (i / 10) * 4;
            double intervalle = Math.max(0.4, 2.0 - (i * 0.03));

            if (i % 10 == 0) {
                vagues.add(new ModeleVague(1, "Gish", pvBase, vitesse, intervalle));
            } else if (i < 4) {
                vagues.add(new ModeleVague(nbEnnemis, "Fatty", pvBase, vitesse, intervalle));
            } else {
                int choix = random.nextInt(3);
                String typeVague = (choix == 0) ? "Fatty" : (choix == 1) ? "Spider" : "Gasper";
                if (typeVague.equals("Gasper")) nbEnnemis = Math.max(3, nbEnnemis / 2);
                vagues.add(new ModeleVague(nbEnnemis, typeVague, pvBase, vitesse, intervalle));
            }
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

        // On informe l'Environnement du PV de référence de la vague courante
        // (utilisé pour générer les ennemis-enfants à la mort d'un ennemi).
        main.environnement.setPvBaseVague(vague.getPvBase());

        System.out.println("\n=========================================");
        System.out.println("====== DÉBUT DE LA VAGUE " + (indexVague + 1) + " / 50 ======");
        System.out.println("[Infos] Nombre d'ennemis attendus : " + ennemisVivantsDansVague);

        for (int i = 0; i < vague.getNombreEnnemis(); i++) {
            double delai = vague.getTypeEnnemi().equals("Spider") ? 0.1 : 0.1 + (i * vague.getIntervalleSpawn());
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(delai), e -> {
                // Le WaveController donne simplement l'ordre au EnnemiController de faire spawner le monstre
                ennemiController.spawnerEnnemi(vague.getTypeEnnemi(), vague.getPvBase(), ligneEntreeFixe, colonneEntreeFixe);
            }));
            timelinesActives.add(t);
            t.play();
        }
    }

    // --- Méthodes appelées par le EnnemiController pour synchroniser la vague ---

    public int getPvBaseVagueActuelle() {
        return vagues.get(indexVague).getPvBase();
    }

    public void ajouterEnnemisVivants(int quantite) {
        ennemisVivantsDansVague += quantite;
    }

    public void notifierEnnemiTermine() {
        ennemisVivantsDansVague--;
        System.out.println("[Suivi Vague] Ennemis restants : " + ennemisVivantsDansVague);

        if (ennemisVivantsDansVague <= 0 && main.modeleJoueur.estEnVie()) {
            System.out.println("Vague " + (indexVague + 1) + " finie !");
            avancerVague();
        }
    }

    private void avancerVague() {
        int niveauDeLaVagueActuelle = (indexVague / 10) + 1;
        indexVague++;
        int nouveauNiveauMap = Math.min((indexVague / 10) + 1, 5);

        if (nouveauNiveauMap != niveauDeLaVagueActuelle) {
            main.changerNiveauMap(nouveauNiveauMap);
        }

        Timeline compteARebours = new Timeline();
        for (int sec = 0; sec <= 4; sec++) {
            final int tempsRestant = 4 - sec;
            compteARebours.getKeyFrames().add(new KeyFrame(Duration.seconds(sec), e -> {
                if (tempsRestant <= 0) demarrerVague();
            }));
        }
        timelinesActives.add(compteARebours);
        compteARebours.play();
    }

    public void arreter() {
        timelinesActives.forEach(Timeline::stop);
        timelinesActives.clear();
    }
}