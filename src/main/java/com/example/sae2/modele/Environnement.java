package com.example.sae2.modele;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.deck.ModeleDeck;
import com.example.sae2.modele.joueur.ModeleJoueur;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.controller.WaveController.EtatEnnemi;
import com.example.sae2.controller.TowerController.EtatTour;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class Environnement {

    private final ModeleTerrain terrain;
    private final ModeleJoueur joueur;
    private final ModeleDeck deck;
    private final int tailleTuile;

    // Cartes associant le modèle métier à son état complet (Contrôleur + Vue)
    private final ObservableMap<ModeleEnnemi, EtatEnnemi> ennemisActifs;
    private final ObservableMap<String, EtatTour> toursPosees;

    public Environnement(ModeleTerrain terrain, ModeleJoueur joueur, ModeleDeck deck, int tailleTuile) {
        this.terrain = terrain;
        this.joueur = joueur;
        this.deck = deck;
        this.tailleTuile = tailleTuile;

        // Utilisation de maps observables pour notifier d'éventuels écouteurs si besoin
        this.ennemisActifs = FXCollections.observableHashMap();
        this.toursPosees = FXCollections.observableHashMap();
    }

    // -------------------------------------------------------------------------
    // Getters des composants principaux du jeu
    // -------------------------------------------------------------------------

    public ModeleTerrain getTerrain() {
        return terrain;
    }

    public ModeleJoueur getJoueur() {
        return joueur;
    }

    public ModeleDeck getDeck() {
        return deck;
    }

    public int getTailleTuile() {
        return tailleTuile;
    }

    // -------------------------------------------------------------------------
    // Gestion des Ennemis (Synchronisé avec WaveController.ennemisActifs)
    // -------------------------------------------------------------------------

    public ObservableMap<ModeleEnnemi, EtatEnnemi> getEnnemisActifs() {
        return ennemisActifs;
    }

    public void ajouterEnnemi(ModeleEnnemi modele, EtatEnnemi etat) {
        this.ennemisActifs.put(modele, etat);
    }

    public void supprimerEnnemi(ModeleEnnemi modele) {
        this.ennemisActifs.remove(modele);
    }

    // -------------------------------------------------------------------------
    // Gestion des Tours (Synchronisé avec TowerController.tours)
    // -------------------------------------------------------------------------

    public ObservableMap<String, EtatTour> getToursPosees() {
        return toursPosees;
    }

    public void ajouterTour(int col, int ligne, EtatTour etat) {
        String cle = ligne + "," + col;
        this.toursPosees.put(cle, etat);
    }

    public EtatTour getTourAt(int col, int ligne) {
        return this.toursPosees.get(ligne + "," + col);
    }

    public boolean aUneTour(int col, int ligne) {
        return this.toursPosees.containsKey(ligne + "," + col);
    }
}