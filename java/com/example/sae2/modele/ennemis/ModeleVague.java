package com.example.sae2.modele.ennemis;

public class ModeleVague {

    private final int    nombreEnnemis;
    private final String typeEnnemi;
    private final int    pvBase;
    private final double vitesse;
    private final double intervalleSpawn; // secondes entre chaque ennemi

    public ModeleVague(int nombreEnnemis, String typeEnnemi, int pvBase, double vitesse, double intervalleSpawn) {
        this.nombreEnnemis   = nombreEnnemis;
        this.typeEnnemi      = typeEnnemi;
        this.pvBase          = pvBase;
        this.vitesse         = vitesse;
        this.intervalleSpawn = intervalleSpawn;
    }

    public int    getNombreEnnemis()   { return nombreEnnemis; }
    public String getTypeEnnemi()      { return typeEnnemi; }
    public int    getPvBase()          { return pvBase; }
    public double getVitesse()         { return vitesse; }
    public double getIntervalleSpawn() { return intervalleSpawn; }
}
