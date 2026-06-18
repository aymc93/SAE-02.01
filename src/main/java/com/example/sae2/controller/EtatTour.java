package com.example.sae2.controller;

import com.example.sae2.modele.tours.ModeleTour;
import com.example.sae2.vue.tours.VueTour;

public class EtatTour {
    final ModeleTour modele;
    final VueTour vue;
    boolean enApparition = true;
    boolean ennemiDetecte = false;

    EtatTour(ModeleTour modele, VueTour vue) {
        this.modele = modele;
        this.vue = vue;
    }
}
