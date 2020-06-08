package cz.kakosa.anthill.ants;

import cz.kakosa.anthill.Material;
import cz.kakosa.anthill.Terrain;

import java.util.LinkedList;
import java.util.Random;

/**
 * Used to store all the Ant objects and call their behaviour methods each update
 */
public class AntManager {

    private Random r;
    private LinkedList<Ant> ants;
    private LinkedList<Ant> antsToAdd;
    private LinkedList<Ant> antsToRemove;

    private Ant[][][] antMap;

    /**
     * Calls createAnts
     * @param terrain Terrain to put the ants in
     * @param r global Random object
     */
    public AntManager(Terrain terrain, Random r){
        this.r = r;
        createAnts(terrain);
    }

    /**
     * Creates lists and arrays of ants, adds Queen
     * @param t Terrain to put the ants in
     */
    public void createAnts(Terrain t){
        antMap = new Ant[t.getWidth()][t.getHeight()][t.getDepth()];
        ants = new LinkedList<>();
        antsToAdd = new LinkedList<>();
        antsToRemove = new LinkedList<>();
        int x;
        int y;
        do {
            x = r.nextInt(t.getWidth() - 80) + 40;
            y = r.nextInt(t.getHeight() - 80) + 40;
        } while (t.getVoxelAt(x, y, 149) == Material.WOOD);

        int z;
        for (z = t.getDepth()*2 /3 ; t.getVoxelAt(x, y, z-1) == Material.AIR ; z--) { }


        Ant q = new QueenAnt(this, r, t, x, y, z);
        ants.add(q);
        antMap[q.posX][q.posY][q.posZ] = q;
    }

    /**
     * Adds ants, removes ants, calls behaviour method for each ant
     */
    public void manageAnts(){
        ants.addAll(antsToAdd);
        ants.removeAll(antsToRemove);
        antsToRemove.clear();
        antsToAdd.clear();
        for (Ant ant : ants){
            ant.behaveLikeAnAnt();
        }
    }

    /**
     * Returns antMap
     * @return 3D array containing either (and mostly) null or a pointer to Ant. Used fo communication between ants.
     */
    public Ant[][][] getAntMap() {
        return antMap;
    }

    /**
     * Adds ant to other ants. It is achieved through separate list antsToAdd to avoid adding while iterating over the list.
     * @param ant Ant extending object to add
     */
    public void addAnt(Ant ant){
        antsToAdd.add(ant);
        antMap[ant.posX][ant.posY][ant.posZ] = ant;
    }

    /**
     * Adds new ant and removes given egg.
     * @param egg Egg to remove
     * @param ant Ant to add
     */
    public void hatchEgg(AntEgg egg, Ant ant){
        addAnt(ant);
        antsToRemove.add(egg);
    }

    /**
     * Returns number of Ants including Queen and eggs
     * @return Number of ants
     */
    public int getNumofAnts(){
        return ants.size();
    }
}
