package com.example.sae2.modele;

import com.example.sae2.modele.boutique.ModeleBoutique;
import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.deck.ModeleDeck;
import com.example.sae2.modele.deck.TypeCarte;
import com.example.sae2.modele.joueur.ModeleJoueur;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.modele.ennemis.ModeleFatty;
import com.example.sae2.modele.ennemis.ModeleGasper;
import com.example.sae2.modele.ennemis.ModeleGish;
import com.example.sae2.modele.ennemis.ModeleSpider;
import com.example.sae2.modele.ennemis.ModeleVague;
import com.example.sae2.modele.tours.ModeleProjectile;
import com.example.sae2.modele.tours.ModeleTour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Environnement {

    private final ModeleTerrain terrain;
    private final ModeleJoueur joueur;
    private final ModeleDeck deck;
    private final ModeleBoutique boutique;
    private final int tailleTuile;
    private final Map<ModeleEnnemi, EtatEnnemi> ennemisActifs;
    private final Map<String, ModeleTour> toursPosees;
    private int ennemisVivantsVague = 0;

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
        this.boutique = new ModeleBoutique();
        this.tailleTuile = tailleTuile;
        this.ennemisActifs = new HashMap<>();
        this.toursPosees = new HashMap<>();
    }

    public ModeleDeck getDeck() { return deck; }
    public Map<ModeleEnnemi, EtatEnnemi> getEnnemisActifs() { return ennemisActifs; }
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

    public static class ResultatFinEnnemi {
        public boolean estTue = false;
        public boolean joueurAPerduVie = false;
        public boolean gameOver = false;
        public final List<EtatEnnemi> nouveauxEtats = new ArrayList<>();
    }

    public void recalculerCheminsEnnemis() {
        for (EtatEnnemi e : ennemisActifs.values()) {
            if (!e.modele.estMort()) {
                e.chemin = e.modele.calculerSonChemin(terrain, tailleTuile);
                e.indexChemin = 0;
            }
        }
    }

    private int pvBaseVagueCourante = ModeleEnnemi.PV_MAX;
    public void setPvBaseVague(int pv) { this.pvBaseVagueCourante = pv; }

    public OrdreTir fairePasserUnTourTour(ModeleTour tour, long now) {
        ModeleEnnemi cible = tour.trouverCible(ennemisActifs.keySet(), tailleTuile);
        if (cible == null) {
            return null;
        }
        int direction = tour.calculerDirectionVers(cible, tailleTuile);
        boolean doitTirer = tour.peutTirer(now);
        return new OrdreTir(cible, direction, doitTirer);
    }

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

    public ModeleProjectile creerProjectile(ModeleTour tour, ModeleEnnemi cible) {
        double cx = tour.getColonne() * tailleTuile + tailleTuile / 2.0;
        double cy = tour.getLigne()   * tailleTuile + tailleTuile / 2.0;
        return new ModeleProjectile(cx, cy, cible,
                tour.getDegats(), tour.getVitesseProjectile());
    }

    public EtatEnnemi creerEtatEnnemi(String typeEnnemi, int pvBase,
                                      int ligneEntree, int colonneEntree) {
        double startX = colonneEntree * tailleTuile;
        double startY = ligneEntree   * tailleTuile;

        ModeleEnnemi modEnnemi;
        switch (typeEnnemi) {
            case "Gish":   modEnnemi = new ModeleGish(startX, startY, pvBase);   break;
            case "Gasper": modEnnemi = new ModeleGasper(startX, startY, pvBase); break;
            case "Spider": modEnnemi = new ModeleSpider(startX, startY, pvBase); break;
            default:       modEnnemi = new ModeleFatty(startX, startY, pvBase);  break;
        }

        List<int[]> chemin = modEnnemi.calculerSonChemin(terrain, tailleTuile);
        if (chemin.isEmpty()) return null;

        EtatEnnemi etat = new EtatEnnemi(modEnnemi, chemin);
        ennemisActifs.put(modEnnemi, etat);
        return etat;
    }

    public static final int GainVenteTour = 50;

    public boolean peutPoserTour(int col, int ligne) {
        return terrain.estConstructible(ligne, col) && !aUneTour(col, ligne);
    }

    public void vendreTour(int col, int ligne) {
        supprimerTour(col, ligne);
        joueur.ajouterArgent(GainVenteTour);
    }

    public boolean appliquerPouvoirTour(int col, int ligne, TypeCarte pouvoir) {
        ModeleTour tour = getTourAt(col, ligne);
        if (tour == null) return false;
        tour.appliquerPouvoir(pouvoir);
        return true;
    }

    public boolean tenterDeplacerObstacle(int oldLigne, int oldCol, int newLigne, int newCol) {
        if (newCol == oldCol && newLigne == oldLigne) return false;
        if (aUneTour(newCol, newLigne)) return false;
        boolean ok = terrain.deplacerObstacle(oldLigne, oldCol, newLigne, newCol);
        if (ok) recalculerCheminsEnnemis();
        return ok;
    }

    public int getPrixBooster() { return boutique.getPrixBooster(); }

    public TypeCarte[] acheterBooster() {
        if (deck.estPlein()) return null;
        return boutique.genererChoixBooster(joueur);
    }

    public boolean validerChoixCarte(TypeCarte carteChoisie) {
        if (!carteChoisie.estPouvoir() && deck.getNombreTours() >= 2) return false;
        if (carteChoisie.estPouvoir()  && deck.getNombrePouvoirs() >= 3) return false;
        deck.ajouterCarte(carteChoisie);
        return true;
    }

    public static String getNomImageCarte(TypeCarte carte) {
        return switch (carte) {
            case BLOODBAG -> "Card-BloodBag.png";
            case BRIMSTONE -> "Card-Brimstone.png";
            case SOYMILK -> "Card-SoyMilk.png";
            case SIMPLE_TOWER -> "Card-tower-simple.png";
        };
    }

    public void setEnnemisVivantsVague(int n) { this.ennemisVivantsVague = n; }
    public int  getEnnemisVivantsVague() { return ennemisVivantsVague; }
    public void ajouterEnnemisVivants(int quantite) { ennemisVivantsVague += quantite; }

    public boolean ennemiTermine() {
        ennemisVivantsVague--;
        return ennemisVivantsVague <= 0 && joueur.estEnVie();
    }

    public List<ModeleVague> genererVagues(int nombre) {
        List<ModeleVague> vagues = new ArrayList<>();
        Random random = new Random();
        for (int i = 1; i <= nombre; i++) {
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
        return vagues;
    }
}
