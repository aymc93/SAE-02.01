package com.example.sae2;

import com.example.sae2.modele.tours.ModeleTour;
import com.example.sae2.modele.ennemis.ModeleEnnemi;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

public class TestJeu {

    @Test
    public void testDeplacementEnnemiVersCible() {
        ModeleEnnemi ennemi = new ModeleEnnemi(0, 0, 10, "Fatty");

        boolean arrive = ennemi.deplacerVers(100, 0, 1);

        assertFalse(arrive);
        assertEquals(10.0, ennemi.getX(), 0.01);
        assertEquals(0.0, ennemi.getY(), 0.01);
    }

    @Test
    public void testEnnemiAtteintCible() {
        ModeleEnnemi ennemi = new ModeleEnnemi(0, 0, 10, "Fatty");

        boolean arrive = ennemi.deplacerVers(5, 0, 1);

        assertTrue(arrive);
        assertEquals(5.0, ennemi.getX(), 0.01);
        assertEquals(0.0, ennemi.getY(), 0.01);
    }

    @Test
    public void testSubirDegats() {
        ModeleEnnemi ennemi = new ModeleEnnemi(0, 0, 10, "Fatty");

        ennemi.subirDegats(100);

        assertEquals(400, ennemi.getPv());
        assertFalse(ennemi.estMort());
    }

    @Test
    public void testMortEnnemi() {
        ModeleEnnemi ennemi = new ModeleEnnemi(0, 0, 10, "Fatty");

        ennemi.subirDegats(500);

        assertEquals(0, ennemi.getPv());
        assertTrue(ennemi.estMort());
    }

    @Test
    public void testGettersEnnemi() {
        ModeleEnnemi ennemi = new ModeleEnnemi(15, 20, 30, "Fatty");

        assertEquals(15.0, ennemi.getX(), 0.01);
        assertEquals(20.0, ennemi.getY(), 0.01);
        assertEquals(30.0, ennemi.getVitesse(), 0.01);
        assertEquals("Fatty", ennemi.getTypeEnnemi());
    }
    @Test
    public void testCreationTour() {
        ModeleTour tour = new ModeleTour(5, 3);

        assertEquals(5, tour.getColonne());
        assertEquals(3, tour.getLigne());
    }

    @Test
    public void testPositionTour() {
        ModeleTour tour = new ModeleTour(2, 4);

        assertTrue(tour.getColonne() >= 0);
        assertTrue(tour.getLigne() >= 0);
    }
}