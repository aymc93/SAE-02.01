package com.example.sae2.vue.deck;

import com.example.sae2.modele.deck.ModeleDeck;
import com.example.sae2.modele.deck.TypeCarte;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Vue du deck : crée tous les slots une seule fois au départ.
 * Quand une carte est utilisée, son ImageView est rendue invisible
 * (setVisible=false) sans être retirée du HBox — les autres cartes
 * ne bougent donc jamais.
 */
public class VueDeck {

    private static final double LARGEUR_CARTE = 75;
    private static final double HAUTEUR_CARTE = 110;

    private final List<TypeCarte>  slotTypes = new ArrayList<>();
    private final List<ImageView>  slotViews = new ArrayList<>();

    private final BiConsumer<TypeCarte, ImageView> onCarteCliquee;
    private ImageView vueSelectionnee = null;

    public VueDeck(ModeleDeck modele,
                   HBox groupePouvoirs,
                   HBox groupeTours,
                   BiConsumer<TypeCarte, ImageView> onCarteCliquee) {
        this.onCarteCliquee = onCarteCliquee;

        // Créer tous les slots une seule fois depuis la liste initiale du modèle
        for (TypeCarte carte : modele.getCartes()) {
            ImageView iv = creerCarteView(carte);

            if (carte.estPouvoir()) {
                groupePouvoirs.getChildren().add(iv);
            } else {
                groupeTours.getChildren().add(iv);
            }

            slotTypes.add(carte);
            slotViews.add(iv);
        }
    }

    /** Marque visuellement une carte comme sélectionnée (retire l'ancienne sélection). */
    public void selectionner(ImageView vue) {
        if (vueSelectionnee != null)
            vueSelectionnee.getStyleClass().remove("carte-selectionnee");
        vueSelectionnee = vue;
        if (vue != null)
            vue.getStyleClass().add("carte-selectionnee");
    }

    /** Retire la mise en évidence de la carte actuellement sélectionnée. */
    public void deselectionner() {
        selectionner(null);
    }

    /** Cache le premier slot VISIBLE correspondant au type donné. */
    public void masquerCarte(TypeCarte type) {
        for (int i = 0; i < slotTypes.size(); i++) {
            if (slotTypes.get(i) == type && slotViews.get(i).isVisible()) {
                slotViews.get(i).setVisible(false);
                break;
            }
        }
    }

    // ── Construction d'une ImageView pour un slot ─────────────────────────────

    private ImageView creerCarteView(TypeCarte carte) {
        ImageView iv = new ImageView();
        iv.setFitWidth(LARGEUR_CARTE);
        iv.setFitHeight(HAUTEUR_CARTE);
        iv.setPreserveRatio(false);

        URL url = getClass().getResource("/com/example/sae2/images/Cartes/" + nomImage(carte));
        if (url != null) {
            iv.setImage(new Image(url.toExternalForm()));
        }

        iv.setOnMouseClicked(e -> {
            onCarteCliquee.accept(carte, iv);
            e.consume();
        });

        return iv;
    }

    private String nomImage(TypeCarte carte) {
        return switch (carte) {
            case BLOODBAG     -> "Card-BloodBag.png";
            case BRIMSTONE    -> "Card-Brimstone.png";
            case SOYMILK      -> "Card-SoyMilk.png";
            case SIMPLE_TOWER -> "Card-tower-simple.png";
        };
    }
}
