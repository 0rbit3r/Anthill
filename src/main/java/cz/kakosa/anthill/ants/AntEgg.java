package cz.kakosa.anthill.ants;

import cz.kakosa.anthill.Material;
import cz.kakosa.anthill.Terrain;

import java.util.Random;

/**
 * Ant Egg is a blue pixel that is stored by Maintainers. When fed enough and let sit for set amount of time, it hatches a new Ant
 */
public class AntEgg extends Ant{

    final public int foodToHatchThresh = 10;
    int timeToHatch = 500;
    int antClass;
    final int EXPLORER = 1, MAINTENANCE = 2;

    /**
     *  calls super constructor and sets class of the ant waiting to hatch inside
     * @param am AntManager to add itself in
     * @param r global Random object
     * @param t Terrain to set up its location
     * @param x x position
     * @param y y position
     * @param z y position
     * @param queenLoc queenLoc is important  for orientation inside the nest
     * @param antClass 1 if Explorer, 2 if Maintainer
     */
    public AntEgg(AntManager am, Random r, Terrain t, int x, int y, int z, int antClass, int[] queenLoc){
        super(am, r, t, x, y, z, queenLoc);
        t.setVoxelAt(posX, posY, posZ, Material.EGG);
        t.setColorMapAt(posX, posY, posZ);
        this.antClass = antClass;
    }

    /**
     * If the egg is not hungry and it's time has come (to zero), it hatches. Otherwise it decrements set time.
     */
    @Override
    public void behaveLikeAnAnt() {
        timeToHatch--;
        if (timeToHatch < 0){
            timeToHatch = 0;
        }
        if (timeToHatch == 0 && foodEaten >= foodToHatchThresh){
            switch(antClass){
                case MAINTENANCE:
                    antManager.hatchEgg(this, new MaintainingAnt(antManager, r, t, posX, posY, posZ, queenLoc));
                    break;
                case EXPLORER:
                    antManager.hatchEgg(this, new ExplorerAnt(antManager, r, t, posX, posY, posZ, queenLoc));
                    break;
            }
        }
    }

    /**
     * Returns time required to hatch which is at least 0 and timeToHatch at the beggining
     * @return 0 if the ant is ready to hatch time-wise
     */
    public int getTimeToHatch() {
        return timeToHatch;
    }

    /**
     * Used to determine wheter to feed the ant or not.
     * @return Returns true if the ant requires feeding.
     */
    public boolean isHungry(){
        return foodToHatchThresh - foodEaten > 0;
    }
}
