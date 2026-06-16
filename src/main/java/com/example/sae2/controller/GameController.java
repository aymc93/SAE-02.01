package com.example.sae2.controller;

import com.example.sae2.modele.carte.ModeleTerrain;
import com.example.sae2.modele.deck.ModeleDeck;
import com.example.sae2.modele.deck.TypeCarte;
import com.example.sae2.modele.ennemis.BFS;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import com.example.sae2.modele.ennemis.ModeleVague;
import com.example.sae2.modele.joueur.ModeleJoueur;
import com.example.sae2.modele.tours.ModeleTour;
import com.example.sae2.modele.tours.ModeleProjectile;
import com.example.sae2.vue.VueGameOver;
import com.example.sae2.vue.carte.VueHover;
import com.example.sae2.vue.carte.VueObstacles;
import com.example.sae2.vue.carte.VueTerrain;
import com.example.sae2.vue.deck.VueAnimPerteCoeur;
import com.example.sae2.vue.deck.VueArgent;
import com.example.sae2.vue.deck.VueCoeurs;
import com.example.sae2.vue.deck.VueDeck;
import com.example.sae2.vue.ennemis.VueEnnemi;
import com.example.sae2.vue.tours.VueTour;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Contrôleur principal du jeu.
 * Fusionne GameController, ControllerTour et ControllerEnnemis.
 *
 * Chaque ennemi et chaque tour sont représentés par une instance interne
 * (EtatEnnemi / EtatTour) gérée directement ici, ce qui évite la prolifération
 * de fichiers contrôleurs séparés tout en conservant une séparation claire des
 * données par objet de jeu.
 */
public class GameController {

    // -------------------------------------------------------------------------
    // Constantes partagées
    // -------------------------------------------------------------------------

    private static final int    TAILLE_ENNEMI = 64;
    private static final long   DELAI_TIR     = 800_000_000L; // 0.8 s en nanosecondes

    // -------------------------------------------------------------------------
    // FXML – vues injectées
    // -------------------------------------------------------------------------

    @FXML private Pane      paneJeu;
    @FXML private TilePane  gridTerrain;
    @FXML private Pane      tilePaneEntites;
    @FXML private Pane      paneTours;
    @FXML private Pane      paneEnnemis;
    @FXML private ImageView hoverCase;
    @FXML private HBox      groupePouvoirs;
    @FXML private HBox      groupeTours;
    @FXML private HBox      paneArgent;
    @FXML private HBox      paneCoeurs;

    // -------------------------------------------------------------------------
    // Modèles
    // -------------------------------------------------------------------------

    private ModeleTerrain modele;
    private ModeleDeck    modeleDeck;
    private ModeleJoueur  modeleJoueur;
    private int           T; // taille d'une tuile en pixels

    // -------------------------------------------------------------------------
    // Vues globales
    // -------------------------------------------------------------------------

    private VueDeck           vueDeck;
    private VueHover          vueHover;
    private VueArgent         vueArgent;
    private VueCoeurs         vueCoeurs;
    private VueAnimPerteCoeur vueAnimPerteCoeur;
    private VueGameOver       vueGameOver;

    // -------------------------------------------------------------------------
    // Vagues
    // -------------------------------------------------------------------------

    private static final List<ModeleVague> VAGUES = List.of(
            new ModeleVague(5, "Fatty", 500, 48, 2.0)
    );
    private int indexVague              = 0;
    private int ennemisVivantsDansVague = 0;

    // -------------------------------------------------------------------------
    // État du jeu
    // -------------------------------------------------------------------------

    /** État interne d'un ennemi actif (remplace ControllerEnnemis). */
    private class EtatEnnemi {
        final ModeleEnnemi modele;
        final VueEnnemi    vue;
        final List<int[]>  chemin;
        int           indexChemin    = 0;
        long          tempsPrecedent = -1;
        AnimationTimer timer;

        EtatEnnemi(ModeleEnnemi modele, VueEnnemi vue, List<int[]> chemin) {
            this.modele = modele;
            this.vue    = vue;
            this.chemin = chemin;
        }

        void stopper() { if (timer != null) timer.stop(); }
    }

    /** État interne d'une tour active (remplace ControllerTour). */
    private class EtatTour {
        final ModeleTour modele;
        final VueTour    vue;
        final double     cx, cy, porteePixels;

        boolean        enApparition  = true;
        boolean        ennemiDetecte = false;
        long           dernierTir    = 0;
        AnimationTimer timerTracking;

        EtatTour(ModeleTour modele, VueTour vue, double cx, double cy, double porteePixels) {
            this.modele       = modele;
            this.vue          = vue;
            this.cx           = cx;
            this.cy           = cy;
            this.porteePixels = porteePixels;
        }

        void stopper() { if (timerTracking != null) timerTracking.stop(); }
    }

    private final Map<String, EtatTour>          tours        = new HashMap<>();
    private final Map<ModeleEnnemi, EtatEnnemi>  ennemisActifs = new HashMap<>();
    private final List<Timeline>                 timelinesActives = new ArrayList<>();

    // Sélection de carte en cours
    private TypeCarte carteSelectionnee    = null;
    private ImageView carteSelectionneeView = null;

    // -------------------------------------------------------------------------
    // Initialisation FXML
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        modele = new ModeleTerrain();
        modele.setNiveau(5);

        T = ModeleTerrain.TAILLE_TUILE;
        int cols = modele.getNbColonnes();
        int rows = modele.getNbLignes();

        paneJeu.setPrefSize(cols * T, rows * T);
        paneTours.setPrefSize(cols * T, rows * T);

        new VueTerrain(modele, gridTerrain);
        new VueObstacles(modele, tilePaneEntites);

        vueHover = new VueHover(hoverCase, T);

        modeleDeck = new ModeleDeck();
        vueDeck    = new VueDeck(modeleDeck, groupePouvoirs, groupeTours, this::selectionnerCarte);

        modeleJoueur      = new ModeleJoueur();
        vueArgent         = new VueArgent(paneArgent);
        vueCoeurs         = new VueCoeurs(paneCoeurs);
        vueAnimPerteCoeur = new VueAnimPerteCoeur(paneJeu);
        vueGameOver       = new VueGameOver();

        demarrerVague();
    }

    // -------------------------------------------------------------------------
    // Sélection de carte
    // -------------------------------------------------------------------------

    private void selectionnerCarte(TypeCarte type, ImageView vue) {
        if (carteSelectionneeView == vue) {
            deselectionner();
        } else {
            deselectionner();
            carteSelectionnee     = type;
            carteSelectionneeView = vue;
            vueDeck.selectionner(vue);
        }
    }

    private void deselectionner() {
        vueDeck.deselectionner();
        carteSelectionneeView = null;
        carteSelectionnee     = null;
        vueHover.cacher();
    }

    // -------------------------------------------------------------------------
    // Gestion du hover
    // -------------------------------------------------------------------------

    @FXML
    private void actualiserHover(MouseEvent event) {
        if (carteSelectionnee == null) { vueHover.cacher(); return; }
        vueHover.actualiser(event.getX(), event.getY());
    }

    @FXML
    private void cacherHover(MouseEvent event) {
        vueHover.cacher();
    }

    // -------------------------------------------------------------------------
    // Clic sur le terrain
    // -------------------------------------------------------------------------

    @FXML
    private void placerTour(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) return;
        if (carteSelectionnee == null) return;

        int col   = (int) (event.getX() / T);
        int ligne = (int) (event.getY() / T);

        if (ligne < 0 || ligne >= modele.getNbLignes())   return;
        if (col  < 0 || col  >= modele.getNbColonnes())   return;

        TypeCarte type   = carteSelectionnee;
        boolean   succes = type.estPouvoir()
                ? tenterAppliquerPouvoir(col, ligne, type)
                : tenterPoserTour(col, ligne);

        if (succes) {
            deselectionner();
            modeleDeck.utiliserCarte(type);
            vueDeck.masquerCarte(type);
        }
    }

    // -------------------------------------------------------------------------
    // Vagues d'ennemis
    // -------------------------------------------------------------------------

    private void demarrerVague() {
        if (indexVague >= VAGUES.size()) {
            System.out.println("[Vagues] Toutes les vagues terminées !");
            return;
        }
        ModeleVague vague = VAGUES.get(indexVague);
        System.out.println("[Vagues] ── Vague " + (indexVague + 1) + " / " + VAGUES.size()
                + "  (" + vague.getNombreEnnemis() + " ennemis, " + vague.getPvBase() + " PV) ──");

        ennemisVivantsDansVague = vague.getNombreEnnemis();

        for (int i = 0; i < vague.getNombreEnnemis(); i++) {
            double delai = i * vague.getIntervalleSpawn();
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(delai), e -> spawnerEnnemi(vague)));
            timelinesActives.add(t);
            t.play();
        }
    }

    private void spawnerEnnemi(ModeleVague vague) {
        double departX = modele.getColonneEntree() * T;
        double departY = modele.getLigneEntree()   * T;

        ModeleEnnemi modEnnemi = new ModeleEnnemi(
                departX, departY, vague.getVitesse(), vague.getTypeEnnemi(), vague.getPvBase());
        VueEnnemi vueEnnemi = new VueEnnemi(modEnnemi, paneEnnemis, modele.getDossierSprites());

        // Calcul du chemin BFS une seule fois à la création
        List<int[]> chemin = BFS.calculerChemin(
                modele.getLigneEntree(),   modele.getColonneEntree(),
                modele.getLigneSortie(),   modele.getColonneSortie(),
                modele.getGrilleBloquee()
        );
        if (chemin.isEmpty()) {
            System.err.println("[GameController] Aucun chemin trouvé pour l'ennemi !");
        }

        EtatEnnemi etat = new EtatEnnemi(modEnnemi, vueEnnemi, chemin);
        ennemisActifs.put(modEnnemi, etat);
        demarrerEnnemi(etat, modEnnemi);
    }

    // -------------------------------------------------------------------------
    // Déplacement d'un ennemi (ex-ControllerEnnemis.demarrer)
    // -------------------------------------------------------------------------

    private void demarrerEnnemi(EtatEnnemi etat, ModeleEnnemi modEnnemi) {
        int offset = (T - TAILLE_ENNEMI) / 2;

        etat.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (etat.tempsPrecedent < 0) { etat.tempsPrecedent = now; return; }

                double delta = (now - etat.tempsPrecedent) / 1_000_000_000.0;
                etat.tempsPrecedent = now;

                // Ennemi mort ou sorti
                if (etat.modele.estMort() || etat.chemin.isEmpty()
                        || etat.indexChemin >= etat.chemin.size() - 1) {
                    etat.vue.supprimer();
                    stop();
                    gererFinEnnemi(modEnnemi);
                    return;
                }

                int[]  prochaine = etat.chemin.get(etat.indexChemin + 1);
                double cibleX    = prochaine[1] * T + offset;
                double cibleY    = prochaine[0] * T + offset;

                if (etat.modele.deplacerVers(cibleX, cibleY, delta)) {
                    etat.indexChemin++;
                }
                etat.vue.actualiser();
            }
        };
        etat.timer.start();
    }

    // ennemi
    private void gererFinEnnemi(ModeleEnnemi modEnnemi) {
        boolean estTue = modEnnemi.estMort();
        ennemisActifs.remove(modEnnemi);

        if (estTue) {
            modeleJoueur.gagnerArgentKill();
            vueArgent.actualiser(modeleJoueur.getArgent());
        } else {
            modeleJoueur.perdreVie();
            vueCoeurs.actualiser(modeleJoueur.getVies());
            vueAnimPerteCoeur.jouer();
            if (!modeleJoueur.estEnVie()) {
                arreterJeu();
                Stage stage = (Stage) paneJeu.getScene().getWindow();
                vueGameOver.afficher(stage);
                return;
            }
        }

        if (--ennemisVivantsDansVague == 0 && modeleJoueur.estEnVie()) {
            indexVague++;
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(4), e -> demarrerVague()));
            timelinesActives.add(t);
            t.play();
        }
    }

    // -------------------------------------------------------------------------
    // Arrêt du jeu
    // -------------------------------------------------------------------------

    private void arreterJeu() {
        timelinesActives.forEach(Timeline::stop);
        timelinesActives.clear();
        ennemisActifs.values().forEach(EtatEnnemi::stopper);
        tours.values().forEach(EtatTour::stopper);
    }

    // -------------------------------------------------------------------------
    // Pose d'une tour
    // -------------------------------------------------------------------------

    private boolean tenterPoserTour(int col, int ligne) {
        if (!modele.estConstructible(ligne, col)) return false;
        String cle = ligne + "," + col;
        if (tours.containsKey(cle)) return false;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/sae2/tour.fxml"));
            Group tourNode = loader.load();

            tourNode.setLayoutX(col   * T);
            tourNode.setLayoutY(ligne * T);
            paneTours.getChildren().add(tourNode); // ← ajout AVANT le lookup

            // lookup() fonctionne uniquement après ajout au scene graph
            Circle    porteeShape = (Circle)    tourNode.lookup("#portee");
            ImageView spriteView  = (ImageView) tourNode.lookup("#spriteView");

            double cx     = col   * T + T / 2.0;
            double cy     = ligne * T + T / 2.0;
            double portee = 3.0   * T;

            ModeleTour modeleTour = new ModeleTour(col, ligne);
            VueTour    vueTour    = new VueTour(spriteView, porteeShape, paneTours);

            EtatTour etat = new EtatTour(modeleTour, vueTour, cx, cy, portee);
            vueTour.initialiser(TypeCarte.SIMPLE_TOWER.getDossier(), () -> etat.enApparition = false);

            tours.put(cle, etat);
            demarrerTracking(etat);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Tracking / tir d'une tour (ex-ControllerTour.demarrerTracking)
    // -------------------------------------------------------------------------

    private void demarrerTracking(EtatTour etat) {
        etat.timerTracking = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (etat.enApparition) return;

                // Chercher l'ennemi le plus proche dans la portée
                ModeleEnnemi cible   = null;
                double       distMin = Double.MAX_VALUE;
                for (ModeleEnnemi e : ennemisActifs.keySet()) {
                    if (e.estMort()) continue;
                    double dx   = (e.getX() + 32) - etat.cx;
                    double dy   = (e.getY() + 32) - etat.cy;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist <= etat.porteePixels && dist < distMin) {
                        distMin = dist;
                        cible   = e;
                    }
                }

                if (cible == null) {
                    if (etat.ennemiDetecte) { etat.ennemiDetecte = false; etat.vue.setAlerte(false); }
                    return;
                }

                if (!etat.ennemiDetecte) { etat.ennemiDetecte = true; etat.vue.setAlerte(true); }

                double dx = (cible.getX() + 32) - etat.cx;
                double dy = (cible.getY() + 32) - etat.cy;
                etat.vue.setDirection(ModeleTour.calculerDirection(dx, dy));

                if (now - etat.dernierTir >= DELAI_TIR) {
                    tirerProjectile(etat, cible);
                    etat.dernierTir = now;
                }
            }
        };
        etat.timerTracking.start();
    }

    // tour
    private void tirerProjectile(EtatTour etat, ModeleEnnemi cible) {
        System.out.println("[Tour] Tir  →  dégâts=" + etat.modele.getDegats()
                + "  vitesse=" + (int) etat.modele.getVitesseProjectile() + " px/s");
        ModeleProjectile projectile = new ModeleProjectile(
                etat.cx, etat.cy, cible,
                etat.modele.getDegats(), etat.modele.getVitesseProjectile());
        etat.vue.afficherProjectile(projectile);
    }

    // -------------------------------------------------------------------------
    // Application d'un pouvoir sur une tour existante
    // -------------------------------------------------------------------------


    // carte
    private boolean tenterAppliquerPouvoir(int col, int ligne, TypeCarte pouvoir) {
        EtatTour etat = tours.get(ligne + "," + col);
        if (etat == null) return false;
        etat.modele.appliquerPouvoir(pouvoir);
        etat.enApparition = true;
        etat.vue.changerDossier(pouvoir.getDossier(), () -> etat.enApparition = false);
        return true;
    }
}