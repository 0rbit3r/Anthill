package cz.kakosa.anthill.ants;

import cz.kakosa.anthill.Material;
import cz.kakosa.anthill.Terrain;

import java.util.Random;

/**
 * Explorer ant finds food and brings it back to the nest.
 */
public class ExplorerAnt extends Ant {

    public final int GETTING_OUT = 0, FINDING_FOOD = 1, GOING_HOME = 2, STORING = 3, GOING_FOR_FOOD = 4, RANDOM_WALK = 5;
    int mode = 0;
    int[] foodLoc;
    int[] homeLoc;

    int timeInMode;

    /**
     * Calls super constructor
     * @param am AntManager to add itself in
     * @param r global Random object
     * @param t Terrain to set up its location
     * @param x x position
     * @param y y position
     * @param z y position
     * @param queenLoc queenLoc is important  for orientation inside the nest
     */
    public ExplorerAnt(AntManager am, Random r, Terrain t, int x, int y, int z, int[] queenLoc){
        super(am, r, t, x, y, z, queenLoc);
    }


    /**
     * Behaviour of the ant, called each cycle.
     * Differs based on mode variable. Mode can be:
     * Getting out:
     *      Ant finds its way out from the nest
     * Finding food:
     *      Ant is searching for food around it while scouting randomly around the map
     * Going home:
     *      Ant has found food and now it carries it back to the nest to store it.
     * Storing:
     *      Ant finds suitable spot inside the nest where it stores the food
     * Going for food:
     *      Ant knows where the food source is and is making it's way towards it.
     * Random walk:
     *      Ant decides to walk some amount of steps in a random direction.
     */
    @Override
    public void behaveLikeAnAnt() {

        switch (mode){
            case GETTING_OUT: {
                tellOthers();
                if (internalClock == 0) {
                    dirToAirBubble(true);
                }
                if (r.nextInt(10) == 0) {
                    stepRandomly();
                } else {
                    solveMovementFromDir();
                }
                internalClock = (internalClock + 1) % 10;

                if (t.getVoxelAt(posX, posY, posZ - 1) == Material.GRASS || posZ == t.getDepth() * 3 / 2) {
                    mode = FINDING_FOOD;
                    dirDeltas = randomDir();
                    homeLoc = new int[] {posX, posY, posZ};
                }
                break;
            }
            case FINDING_FOOD:{
                {
                    if (posX < 20)
                        dirDeltas[0] = 1;
                    if (posX > t.getWidth() - 20)
                        dirDeltas[0] = -1;
                    if (posY < 20)
                        dirDeltas[1] = 1;
                    if (posY > t.getHeight() - 20)
                        dirDeltas[1] = -1;
                    if (posZ < 10)
                        dirDeltas[2] = 1;
                    if (posZ > t.getDepth() - 10)
                        dirDeltas[2] = -1;
                } //Checking boundries

                if (posZ <= queenLoc[2]){
                    mode = GETTING_OUT;
                }

                if (foodLoc == null) {
                    solveMovementFromDir();
                    if (r.nextInt(40) == 0){
                        dirDeltas = randomDir();
                    }
                    foodLoc = foodInVicinity(visibleRadius);
                }
                else{
                    dirDeltas = getDirFromMap(foodLoc[0], foodLoc[1], foodLoc[2] + 1);

                    if (r.nextInt(40) == 0 && distTo(foodLoc[0], foodLoc[1], foodLoc[2]) < 7){
                        foodLoc = foodInVicinity(visibleRadius);
                    }

                    if(r.nextInt(10)==0){
                        stepRandomly();
                    }
                    else {
                        solveMovementFromDir();
                    }

                }

                if (t.getVoxelAt(posX, posY, posZ - 1) == Material.FOOD || t.getVoxelAt(posX, posY, posZ - 1) == Material.LEAVES){
                    pickUpFood();
                    mode = GOING_HOME;
                }
                break;
            }
            case GOING_HOME:{
                tellOthers();

                if (r.nextInt(10) != 0)
                    dirDeltas = getDirFromMap(homeLoc[0], homeLoc[1], homeLoc[2]);
                else
                    dirDeltas = randomDir();

                if(r.nextInt(10)==0){
                    stepRandomly();
                }
                else {
                    solveMovementFromDir();
                }

                if(distTo(homeLoc[0], homeLoc[1], homeLoc[2]) < 10){
                    mode = STORING;
                }
                break;
            }
            case STORING:{
                tellOthers();
                if (internalClock == 0) {
                    if (r.nextInt(10) != 0){
                        dirToAirBubble(false);
                    }
                    else{
                        dirDeltas = randomDir();
                    }

                }


                solveMovementFromDir();

                internalClock = (internalClock + 1) % 12;

                if ( r.nextInt(4) == 0 && sizeOfChamber(5) > EGG_SPACE_NEEDED && t.getVoxelAt(posX, posY, posZ - 1).isSolid() && t.getVoxelAt(posX, posY, posZ - 1)!= Material.EGG && posZ < queenLoc[2] - 6){
                    moveTo(posX, posY, posZ + 1);
                    t.setVoxelAt(posX, posY, posZ-1, carrying);
                    carrying = null;
                    mode = GETTING_OUT;
                }

                if (t.getVoxelAt(posX, posY, posZ - 1) == Material.GRASS){
                    mode = GOING_HOME;
                }
                break;
            }
            case GOING_FOR_FOOD:{
                tellOthers();
                dirDeltas = getDirFromMap(foodLoc[0], foodLoc[1], foodLoc[2]);

                if (r.nextInt(100) == 0) {
                    timeInMode = 40;
                    mode = RANDOM_WALK;
                    dirDeltas = randomDir();
                }
                solveMovementFromDir();

                if(distTo(foodLoc[0], foodLoc[1], foodLoc[2]) < 6){
                    mode = FINDING_FOOD;
                }
                break;
            }
            case RANDOM_WALK: {
                tellOthers();
                solveMovementFromDir();
                timeInMode--;
                if (timeInMode == 0)
                    mode = GOING_HOME;
                break;
            }

        }
    }

    /**
     * Picks up food that is supposed to be directly bellow the ant.
     */
    private void pickUpFood(){
        carrying = t.getVoxelAt(posX,posY,posZ-1);
        t.setVoxelAt(posX, posY, posZ - 1, Material.AIR);
        moveTo(posX, posY, posZ - 1);
    }

    /**
     * Informs nearby ants coordinates of the source of the food it found.
     */
    private void tellOthers(){
        if (foodLoc != null)
            for (int z = -communicationRad; z < communicationRad; z++) {
                for (int x = -communicationRad; x < communicationRad; x++) {
                    for (int y = -communicationRad; y < communicationRad; y++) {
                        if (t.checkBoundaries(posX + x, posY + y, posZ + z) && antMap[posX][posY][posZ] != null && antMap[posX][posY][posZ] instanceof ExplorerAnt){
                            ((ExplorerAnt)antMap[posX][posY][posZ]).haveAChat(foodLoc);
                        }
                    }
                }
            }
    }

    /**
     * Returns one of 6 behavioural mods the ant is following now.
     * @return integer 0 - 5 depending on the mode.
     */
    public int getMode() {
        return mode;
    }

    /**
     * Is calle dby other ants. Sets the food location to value the other ants provide.
     * @param loc
     */
    public void haveAChat(int[] loc){
        if (mode == FINDING_FOOD){
            foodLoc = loc;
            mode = GOING_FOR_FOOD;
        }
    }
}
