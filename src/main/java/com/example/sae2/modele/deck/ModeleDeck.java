package com.example.sae2.modele.deck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deck du joueur : liste ordonnée de cartes.
 * Chaque utilisation retire la première occurrence du type joué.
 */
public class ModeleDeck {

    private final List<TypeCarte> cartes = new ArrayList<>();

    public ModeleDeck() {
        cartes.add(TypeCarte.BLOODBAG);
        cartes.add(TypeCarte.BRIMSTONE);
        cartes.add(TypeCarte.SOYMILK);
        cartes.add(TypeCarte.SIMPLE_TOWER);
        cartes.add(TypeCarte.SIMPLE_TOWER);
    }

    /** Vue non modifiable de la liste (pour la vue). */
    public List<TypeCarte> getCartes() {
        return Collections.unmodifiableList(cartes);
    }

    /**
     * Retire la première occurrence de ce type du deck.
     * @return true si la carte existait et a été retirée.
     */
    public boolean utiliserCarte(TypeCarte carte) {
        return cartes.remove(carte);
    }

    public boolean contient(TypeCarte carte) {
        return cartes.contains(carte);
    }

    public boolean estVide() {
        return cartes.isEmpty();
    }
}
