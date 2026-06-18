package com.example.sae2.modele.boutique;

import com.example.sae2.modele.deck.TypeCarte;
import com.example.sae2.modele.joueur.ModeleJoueur;
import java.util.Random;

public class ModeleBoutique {

    private static final int PRIX_BOOSTER = 150;
    private final Random random = new Random();

    public TypeCarte[] genererChoixBooster(ModeleJoueur joueur) {
        if (joueur.getArgent() >= PRIX_BOOSTER) {
            joueur.depenserArgent(PRIX_BOOSTER);
            TypeCarte[] choix = new TypeCarte[2];
            choix[0] = TypeCarte.SIMPLE_TOWER;
            TypeCarte[] pouvoirs = {TypeCarte.BLOODBAG, TypeCarte.BRIMSTONE, TypeCarte.SOYMILK};
            choix[1] = pouvoirs[random.nextInt(pouvoirs.length)];
            return choix;
        }
        return null;
    }

    public int getPrixBooster() { return PRIX_BOOSTER; }
}