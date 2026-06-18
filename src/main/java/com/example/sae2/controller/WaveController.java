package com.example.sae2.controller;

import com.example.sae2.modele.ennemis.ModeleVague;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class WaveController {

    private final GameController main;
    private EnnemiController ennemiController;
    private final List<ModeleVague> vagues = new ArrayList<>();
    private final List<Timeline> timelinesActives = new ArrayList<>();
    private int indexVague = 0;
    private final int ligneEntreeFixe;
    private final int colonneEntreeFixe;

    public WaveController(GameController main) {
        this.main = main;
        this.ligneEntreeFixe = main.modele.getLigneEntree();
        this.colonneEntreeFixe = main.modele.getColonneEntree();
        vagues.addAll(main.environnement.genererVagues(50));
    }

    public void setEnnemiController(EnnemiController ennemiController) {
        this.ennemiController = ennemiController;
    }

    public void demarrerVague() {
        if (indexVague >= vagues.size()) {
            System.out.println("====== VICTOIRE ULTIME : Les 50 vagues ont été repoussées ! ======");
            main.arreterTout();
            return;
        }
        ModeleVague vague = vagues.get(indexVague);
        main.environnement.setEnnemisVivantsVague(vague.getNombreEnnemis());
        main.environnement.setPvBaseVague(vague.getPvBase());
        System.out.println("\n=========================================");
        System.out.println("====== DÉBUT DE LA VAGUE " + (indexVague + 1) + " / 50 ======");
        System.out.println("[Infos] Nombre d'ennemis attendus : " + vague.getNombreEnnemis());
        for (int i = 0; i < vague.getNombreEnnemis(); i++) {
            double delai = vague.getTypeEnnemi().equals("Spider") ? 0.1 : 0.1 + (i * vague.getIntervalleSpawn());
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(delai), e -> {
                ennemiController.spawnerEnnemi(vague.getTypeEnnemi(), vague.getPvBase(), ligneEntreeFixe, colonneEntreeFixe);
            }));
            timelinesActives.add(t);
            t.play();
        }
    }

    public void ajouterEnnemisVivants(int quantite) {
        main.environnement.ajouterEnnemisVivants(quantite);
    }

    public void notifierEnnemiTermine() {
        boolean vagueFinie = main.environnement.ennemiTermine();
        System.out.println("[Suivi Vague] Ennemis restants : " + main.environnement.getEnnemisVivantsVague());

        if (vagueFinie) {
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