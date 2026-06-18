package com.example.sae2.modele.deck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModeleDeck {

    private final List<TypeCarte> cartes = new ArrayList<>();

    public ModeleDeck() {
        cartes.add(TypeCarte.BLOODBAG);
        cartes.add(TypeCarte.BRIMSTONE);
        cartes.add(TypeCarte.SOYMILK);
        cartes.add(TypeCarte.SIMPLE_TOWER);
        cartes.add(TypeCarte.SIMPLE_TOWER);
    }

    public List<TypeCarte> getCartes() {
        return Collections.unmodifiableList(cartes);
    }

    public boolean utiliserCarte(TypeCarte carte) {
        return cartes.remove(carte);
    }

    public boolean contient(TypeCarte carte) {
        return cartes.contains(carte);
    }

    public boolean estVide() {
        return cartes.isEmpty();
    }

    public void ajouterCarte(TypeCarte carte) {
        cartes.add(carte);
    }

    public int getNombreTours() {
        int count = 0;
        for (TypeCarte c : cartes) {
            if (!c.estPouvoir()) {
                count++;
            }
        }
        return count;
    }

    public int getNombrePouvoirs() {
        int count = 0;
        for (TypeCarte c : cartes) {
            if (c.estPouvoir()) {
                count++;
            }
        }
        return count;
    }

    public boolean estPlein() {
        return getNombreTours() >= 2 && getNombrePouvoirs() >= 3;
    }
}
