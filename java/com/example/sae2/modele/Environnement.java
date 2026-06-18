package com.example.sae2.modele;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.deck.ModeleDeck;
import com.example.sae2.modele.joueur.ModeleJoueur;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.modele.ennemis.ModeleSpider;
import com.example.sae2.modele.tours.ModeleTour;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Coeur logique du jeu (le "M" du MVC).
 *
 * L'Environnement contient l'état complet de la partie (terrain, joueur, deck,
 * ennemis actifs, tours posées) ET la logique de progression du jeu.
 *
 * Les contrôleurs ne font plus tourner la mécanique eux-mêmes : ils possèdent
 * juste une boucle (AnimationTimer.handle) qui appelle {@link #fairePasserUnTour}
 * à chaque frame, puis demandent aux vues de se redessiner.
 */
public class Environnement {

    private final ModeleTerrain terrain;
    private final ModeleJoueur joueur;
    private final ModeleDeck deck;
    private final int tailleTuile;

    // -- État dynamique de la partie : pure logique (aucune Vue ici) --
    private final ObservableMap<ModeleEnnemi, EtatEnnemi> ennemisActifs;
    private final ObservableMap<String, ModeleTour> toursPosees;

    /**
     * État de déplacement d'un ennemi sur le terrain (données de boucle).
     * Ne contient AUCUNE Vue : c'est de la logique pure.
     */
    public static class EtatEnnemi {
        public final ModeleEnnemi modele;
        public List<int[]> chemin;
        public int indexChemin = 0;
        public boolean termine = false;   // a fini son parcours (mort ou sortie)
        public boolean estMortParTir = false;

        public EtatEnnemi(ModeleEnnemi modele, List<int[]> chemin) {
            this.modele = modele;
            this.chemin = chemin;
        }
    }

    public Environnement(ModeleTerrain terrain, ModeleJoueur joueur, ModeleDeck deck, int tailleTuile) {
        this.terrain = terrain;
        this.joueur = joueur;
        this.deck = deck;
        this.tailleTuile = tailleTuile;
        this.ennemisActifs = FXCollections.observableHashMap();
        this.toursPosees = FXCollections.observableHashMap();
    }

    // -------------------------------------------------------------------------
    // Getters des composants principaux
    // -------------------------------------------------------------------------

    public ModeleTerrain getTerrain()  { return terrain; }
    public ModeleJoueur  getJoueur()   { return joueur; }
    public ModeleDeck    getDeck()     { return deck; }
    public int           getTailleTuile() { return tailleTuile; }

    public ObservableMap<ModeleEnnemi, EtatEnnemi> getEnnemisActifs() { return ennemisActifs; }
    public ObservableMap<String, ModeleTour>       getToursPosees()   { return toursPosees; }

    // -------------------------------------------------------------------------
    // Gestion des Ennemis
    // -------------------------------------------------------------------------

    public void ajouterEnnemi(ModeleEnnemi modele, EtatEnnemi etat) {
        this.ennemisActifs.put(modele, etat);
    }

    public void supprimerEnnemi(ModeleEnnemi modele) {
        this.ennemisActifs.remove(modele);
    }

    // -------------------------------------------------------------------------
    // Gestion des Tours
    // -------------------------------------------------------------------------

    public void ajouterTour(int col, int ligne, ModeleTour tour) {
        this.toursPosees.put(ligne + "," + col, tour);
    }

    public void supprimerTour(int col, int ligne) {
        this.toursPosees.remove(ligne + "," + col);
    }

    public ModeleTour getTourAt(int col, int ligne) {
        return this.toursPosees.get(ligne + "," + col);
    }

    public boolean aUneTour(int col, int ligne) {
        return this.toursPosees.containsKey(ligne + "," + col);
    }

    // -------------------------------------------------------------------------
    // BOUCLE DE JEU : faire passer un tour
    // -------------------------------------------------------------------------

    /**
     * Fait avancer la logique d'UN ennemi d'un tick.
     * Met à jour la position du modèle et son avancée sur le chemin BFS.
     *
     * @return true si l'ennemi a terminé son parcours pendant ce tick
     *         (mort par tir, ou arrivé à la sortie). Le contrôleur devra alors
     *         supprimer la vue et appeler {@link #gererFinEnnemi}.
     */
    public boolean fairePasserUnTourEnnemi(EtatEnnemi etat, double delta) {
        int offset = (tailleTuile - ModeleEnnemi.TAILLE_AFFICHAGE) / 2;

        if (etat.modele.estMort()) {
            etat.estMortParTir = true;
            etat.termine = true;
            return true;
        }

        if (etat.chemin == null || etat.chemin.isEmpty()
                || etat.indexChemin >= etat.chemin.size() - 1) {
            if (etat.modele.aAtteintSortie(terrain, tailleTuile)) {
                etat.estMortParTir = false;
                etat.termine = true;
                return true;
            } else {
                etat.chemin = etat.modele.calculerSonChemin(terrain, tailleTuile);
                etat.indexChemin = 0;
                return false;
            }
        }

        int[] prochaine = etat.chemin.get(etat.indexChemin + 1);
        if (etat.modele.deplacerVers(prochaine[1] * tailleTuile + offset,
                                     prochaine[0] * tailleTuile + offset, delta)) {
            etat.indexChemin++;
        }
        return false;
    }

    /**
     * Traite la fin de parcours d'un ennemi (mort ou fuite) côté logique pure.
     * Met à jour l'argent / les vies du joueur et génère les ennemis enfants.
     *
     * @return la liste des nouveaux ennemis (déjà ajoutés à l'environnement,
     *         avec leur chemin calculé) que le contrôleur doit faire apparaître
     *         côté Vue. Liste vide si rien à faire apparaître.
     */
    public ResultatFinEnnemi gererFinEnnemi(EtatEnnemi etat) {
        ModeleEnnemi modEnnemi = etat.modele;
        boolean estTue = modEnnemi.estMort();
        ennemisActifs.remove(modEnnemi);

        ResultatFinEnnemi resultat = new ResultatFinEnnemi();
        resultat.estTue = estTue;

        if (estTue) {
            joueur.gagnerArgentKill();

            List<ModeleEnnemi> nouveaux =
                    modEnnemi.genererEnnemisApresMort(pvBaseVagueCourante, tailleTuile);

            for (ModeleEnnemi nouvel : nouveaux) {
                if (nouvel instanceof ModeleSpider) ModeleSpider.sortieTrouvee = false;
                List<int[]> cheminEnfant = nouvel.calculerSonChemin(terrain, tailleTuile);
                EtatEnnemi etatEnfant = new EtatEnnemi(nouvel, cheminEnfant);
                ennemisActifs.put(nouvel, etatEnfant);
                resultat.nouveauxEtats.add(etatEnfant);
            }
        } else {
            // L'ennemi a atteint la sortie : le joueur perd une vie.
            if (modEnnemi instanceof ModeleSpider && !ModeleSpider.sortieTrouvee) {
                ModeleSpider.sortieTrouvee = true;
                for (EtatEnnemi e : ennemisActifs.values()) {
                    if (e.modele instanceof ModeleSpider && !e.modele.estMort()) {
                        e.chemin = e.modele.calculerSonChemin(terrain, tailleTuile);
                        e.indexChemin = 0;
                    }
                }
            }
            joueur.perdreVie();
            resultat.joueurAPerduVie = true;
            resultat.gameOver = !joueur.estEnVie();
        }
        return resultat;
    }

    /** Petit conteneur de résultat pour informer le contrôleur sans lui donner la logique. */
    public static class ResultatFinEnnemi {
        public boolean estTue = false;
        public boolean joueurAPerduVie = false;
        public boolean gameOver = false;
        public final List<EtatEnnemi> nouveauxEtats = new ArrayList<>();
    }

    /** Force tous les ennemis vivants à recalculer leur chemin (nouvel obstacle). */
    public void recalculerCheminsEnnemis() {
        for (EtatEnnemi e : ennemisActifs.values()) {
            if (!e.modele.estMort()) {
                e.chemin = e.modele.calculerSonChemin(terrain, tailleTuile);
                e.indexChemin = 0;
            }
        }
    }

    // PV de base de la vague courante, fourni par le WaveController via setter.
    private int pvBaseVagueCourante = ModeleEnnemi.PV_MAX;
    public void setPvBaseVague(int pv) { this.pvBaseVagueCourante = pv; }

    /**
     * Fait passer un tour à UNE tour : recherche de cible, orientation, tir.
     *
     * @return un ordre de tir si la tour doit tirer ce tick, sinon null.
     *         Le contrôleur se contente de matérialiser le projectile côté Vue.
     */
    public OrdreTir fairePasserUnTourTour(ModeleTour tour, long now) {
        ModeleEnnemi cible = tour.trouverCible(ennemisActifs.keySet(), tailleTuile);
        if (cible == null) {
            return null;
        }
        int direction = tour.calculerDirectionVers(cible, tailleTuile);
        boolean doitTirer = tour.peutTirer(now);
        return new OrdreTir(cible, direction, doitTirer);
    }

    /** Décision logique d'une tour pour un tick (cap, tir éventuel). */
    public static class OrdreTir {
        public final ModeleEnnemi cible;
        public final int direction;
        public final boolean doitTirer;
        public OrdreTir(ModeleEnnemi cible, int direction, boolean doitTirer) {
            this.cible = cible;
            this.direction = direction;
            this.doitTirer = doitTirer;
        }
    }
}
