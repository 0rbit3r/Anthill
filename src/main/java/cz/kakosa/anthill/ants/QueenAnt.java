package cz.kakosa.anthill.ants;

import cz.kakosa.anthill.Material;
import cz.kakosa.anthill.Terrain;

import java.util.Random;

/**
 * Queen builds the main shaft of the nest and then lies in the center of it laying eggs.
 */
public class QueenAnt extends Ant {

    int foodEaten = 100;
    int notMoved = 0;
    boolean checkedBirthSpot = false;
    int instantAnts = 15;
    int contextDir;
    final int EXPLORER = 1, MAINTENANCE = 2;

    final public int foodToLayThresh = 5;


    /**
     * Constructor calls Ant constructor and sets random direction
     * @param am AntManager to add itself
     * @param am AntManager to add itself in
     * @param r global Random object
     * @param t Terrain to set up its location
     * @param x x position
     * @param y y position
     * @param z y position
     */
    public QueenAnt(AntManager am, Random r, Terrain t, int x, int y, int z){
        super(am, r, t, x, y, z, null);
        contextDir = r.nextInt(8);
    }

    /**
     * Queen starts above the ground where it makes its way down into the ground digging the main shaft of the nest.
     * When low enough (or when it hasn't movet for long enough time) it creates a pedistal of 9 stone blocks on which it creates 15 instant workers.
     * After that the queen only accepts food and lays eggs.
     */
    @Override
    public void behaveLikeAnAnt() {

        t.setVoxelAt(posX, posY, posZ, Material.RED_ANT);
        antMap[posX][posY][posZ] = this;

        if ( getSupport(posX, posY, posZ) == null){
            moveTo(posX, posY, posZ - 1);
        }

        else if(posZ > t.getDepth() * 2 / 5 - 5 && notMoved < 50){
            if (r.nextInt(14) == 0) {
                contextDir = (contextDir + 1) % 8;
            }
            int newX = posX  + direction2D[contextDir][0] * r.nextInt(2);
            int newY = posY  + direction2D[contextDir][1] * r.nextInt(2);
            if (newX != posX || newY != posY) {
                if (newX < t.getWidth() && newX >= 0 && newY >= 0 && newY < t.getHeight() && ! t.getVoxelAt(newX, newY, posZ).isSolid()) {
                    moveTo( newX, newY, posZ);
                    t.makeSphere(posX, posY, posZ + 2, 5, 1, 1, 1, Material.AIR, true);
                }
            }
            else{
                notMoved++;
            }
        }
        else{
            if (!checkedBirthSpot) {
                while ( getSupport(posX, posY, posZ) == null){
                    moveTo(posX, posY, posZ - 1);
                }
                t.generateDisc(posX, posY, posZ-1, 2, Material.STONE);
                for (int y = -2; y <=2 ; y++) {
                    for (int x = -2; x <=2 ; x++) {
                        t.setColorMapAt(posX + x, posY + y, posZ - 1);
                    }
                }
                t.setVoxelAt(posX, posY + 1, posZ, Material.AIR);
                checkedBirthSpot = true;
                queenLoc = new int[] {posX, posY, posZ};
            }
            if (foodEaten >= foodToLayThresh && internalClock == 0 && ! t.getVoxelAt(posX, posY + 1, posZ).isSolid()){
                foodEaten -= foodToLayThresh;
                if (instantAnts > 0){
                    if (instantAnts > 5){
                        antManager.addAnt(new ExplorerAnt(antManager, r, t, posX, posY + 1, posZ, queenLoc));
                    }
                    else{
                        MaintainingAnt ant = new MaintainingAnt(antManager, r, t, posX, posY + 1, posZ, queenLoc);
                        if (instantAnts < 4)
                            ant.forceDig();
                        antManager.addAnt(ant);
                    }
                    instantAnts--;
                }
                else{
                    if (r.nextInt(4) == 0){
                        antManager.addAnt(new AntEgg(antManager, r, t, posX, posY + 1, posZ, MAINTENANCE, queenLoc));
                    }
                    else{
                        antManager.addAnt(new AntEgg(antManager, r, t, posX, posY + 1, posZ, EXPLORER, queenLoc));
                    }
                }
            }
            internalClock = (internalClock + 1) % 10;
        }
    }

    /**
     * Called externally to feed the queen. Food makes it lay eggs.
     */
    public void feed(){
         foodEaten++;
    }

    /**
     * Shows current status of the queens hunger.
     * @return foodEaten can be integer from 0 to the value resulting in laying an egg (stored in foodToLayThresh)
     */
    public int getFoodEaten() {
        return foodEaten;
    }
}
