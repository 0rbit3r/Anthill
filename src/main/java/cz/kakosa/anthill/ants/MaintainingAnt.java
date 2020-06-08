package cz.kakosa.anthill.ants;

import cz.kakosa.anthill.Material;
import cz.kakosa.anthill.Terrain;

import java.util.Random;

/**
 * Maintaining ant builds tunnels and feeds queen and eggs. Lives only inside of the nest.
 */
public class MaintainingAnt extends Ant {

    final int GO_UP = 0, GO_DOWN = 1, DIG = 2, PREPARING_DIGING= 3;
    int mode = 0;

    int dirChangeFrequency = 10;

    int tunnelLength;

    AntEgg carriedEgg;


    boolean carryingFood;

    int timeInMode;

    int inMode = 0;

    /**
     * Calles super constructor, lowers visible Radius (Area around the ant in which it can "see").
     * Randomly initializes timeInMode, which is the time that is considered too long for the ant to be in single behaviour mode.
     */
    public MaintainingAnt(AntManager am, Random r, Terrain t, int x, int y, int z, int[] queenLoc){
        super(am, r, t, x, y, z, queenLoc);
        visibleRadius = 7;
        timeInMode = r.nextInt(200) + 400;
    }

    /**
     * Behaviour of the ant, called each cycle.
     * Differs based on mode variable. Mode can be:
     * Preparing digging:
     *      Picking tunnel length and random direction and following it while possible, then switching to dig.
     * Dig:
     *      Digging downwards in certain direction which may change during the digging. At the end of the tunnel a chamber is made.
     *Going up:
     *      Ant gets up to the queens chamber where it can feed and queen take the egg. After doing either or waiting for long enough it gets into going down mode.
     *Going down:
     *      Ant gets down in the nest, where it looks for food which it either feeds to the eggs or it gets it to the queen by changing mode to going up.
     */
    @Override
    public void behaveLikeAnAnt() {

        switch (mode){
            case PREPARING_DIGING:{
                solveMovementFromDir();
                if (t.getVoxelAt(posX + dirDeltas[0], posY + dirDeltas[1], posZ + dirDeltas[2]).isDigable()){
                    mode = DIG;
                    tunnelLength = r.nextInt(10) + 20;
                }
                if (r.nextInt(20) == 0){
                    dirDeltas = randomDir();
                    dirDeltas[2] = -1;
                }
                break;
            }
            case DIG: {
                if ( getSupport(posX, posY, posZ) == null){
                    moveTo(posX, posY, posZ - 1);
                }
                if (tunnelLength > 0 || posZ > queenLoc[2] - 10) {
                    solveMovementFromDir();
                    if (lastPos[2] < posZ){
                        moveTo(lastPos[0], lastPos[1], lastPos[2]);
                    }
                    if (r.nextInt(25) == 0) {
                        strafe();
                    }
                    int goStraight = r.nextInt(2);
                    t.makeSphere(posX, posY, posZ + goStraight, 2, 1, 1, 1, Material.AIR, true);
                    tunnelLength--;
                    if (sizeOfChamber(6) > EGG_SPACE_NEEDED){
                        dirFromAirBubble();
                    }
                }
                else{ //Create chamber
                    t.makeSphere(posX, posY, posZ - 4, 6, 1, 1, 1, Material.AIR, true);
                    mode = GO_UP;
                    while (getSupport(posX, posY, posZ) == null){
                        moveTo(posX, posY, posZ - 1);
                    }
                }
                break;
            }
            case GO_UP:{
                if (internalClock == 0) {
                    if (r.nextInt(16) != 0){
                        if (posZ > queenLoc[2]){
                            dirToAirBubble(false);
                        }
                        else{
                            dirToAirBubble(true);
                        }
                    }
                    else{
                        dirDeltas = randomDir();
                    }

                    dirChangeFrequency = (dirChangeFrequency + 1) % 20;

                }
                if (distTo(queenLoc[0], queenLoc[1], queenLoc[2]) < 6){
                    dirDeltas = getDirFromMap(queenLoc[0], queenLoc[1] + r.nextInt(2), queenLoc[2]+1);
                    if (carriedEgg != null){
                        internalClock = 1;
                        dirDeltas = randomDir();
                        mode = GO_DOWN;
                    }
                }

                solveMovementFromDir();

                internalClock = (internalClock + 1) % (dirChangeFrequency + 10);

                if (t.getVoxelAt(posX, posY, posZ - 1) == Material.EGG && carriedEgg == null && antMap[posX][posY - 1][posZ - 1] != null && antMap[posX][posY - 1][posZ - 1] instanceof QueenAnt){ //Pick up egg
                    //System.out.println("Picking up egg!");
                    carriedEgg = (AntEgg)antMap[posX][posY][posZ - 1];
                    antMap[posX][posY][posZ - 1] = null;
                    t.setVoxelAt(posX, posY, posZ - 1, Material.AIR);
                    moveTo(posX, posY, posZ - 1);
                    mode = GO_DOWN;
                }
                if (antMap[posX][posY - 1][posZ] != null && antMap[posX][posY - 1][posZ] instanceof QueenAnt ){ //Feed Queen
                    if (carryingFood && r.nextBoolean()) {
                        carryingFood = false;
                        ((QueenAnt) antMap[posX][posY - 1][posZ]).feed();
                        mode = GO_DOWN;
                    }
                    else
                        mode = GO_DOWN;
                }

                if (inMode >= timeInMode){
                    mode = GO_DOWN;
                    inMode = 0;
                }
                inMode++;
                break;
            }

            case GO_DOWN: {
                if (internalClock == 0) {
                    if (r.nextInt(10) != 0){
                        dirToAirBubble(r.nextBoolean() && r.nextBoolean());

                    }
                    else{
                        dirDeltas = randomDir();
                    }

                    dirChangeFrequency = (dirChangeFrequency + 1) % 20;

                }

                int[] eggPos = eggInVicinity(4);
                if (carryingFood && posZ < queenLoc[2] - 5 && eggPos != null && (r.nextBoolean() || r.nextBoolean())){
                    dirDeltas = getDirFromMap(eggPos[0], eggPos[1], eggPos[2] + 1);
                }
                else {
                    int[] foodPos = foodInVicinity(4);
                    if(! carryingFood && foodPos != null && (r.nextBoolean() || r.nextBoolean())){
                        dirDeltas = getDirFromMap(foodPos[0], foodPos[1], foodPos[2] + 1);
                    }
                }

                solveMovementFromDir();

                internalClock = (internalClock + 1) % (dirChangeFrequency + 10);


                if (t.getVoxelAt(posX, posY, posZ - 1) == Material.EGG && carryingFood && ((AntEgg)antMap[posX][posY][posZ - 1]).isHungry()){
                    antMap[posX][posY][posZ - 1].foodEaten++;
                    carryingFood = false;
                }

                if (r.nextInt(3) == 0 && carriedEgg != null && sizeOfChamber(5) > EGG_SPACE_NEEDED && t.getVoxelAt(posX, posY, posZ - 1).isSolid() &&
                        t.getVoxelAt(posX, posY, posZ - 1) != Material.EGG && posZ < queenLoc[2] - 12){
                    moveTo(posX, posY, posZ + 1);
                    antMap[posX][posY][posZ - 1] = carriedEgg;
                    carriedEgg.posX = posX;
                    carriedEgg.posY = posY;
                    carriedEgg.posZ = posZ-1;
                    carriedEgg = null;
                    t.setVoxelAt(posX, posY, posZ-1, Material.EGG);
                    mode = GO_UP;
                }

                if (! carryingFood &&  (t.getVoxelAt(posX, posY, posZ - 1) == Material.LEAVES || t.getVoxelAt(posX, posY, posZ - 1) == Material.FOOD)){
                    carryingFood = true;
                    t.setVoxelAt(posX, posY, posZ - 1, Material.AIR);
                    moveTo(posX, posY, posZ - 1);
                    if (r.nextBoolean())
                        mode = GO_UP;
                }

                if (r.nextInt(30) == 0 && shouldIDig(5, 20)){
                    forceDig();
                }

                if (inMode >= timeInMode){
                    mode = GO_UP;
                    inMode = 0;
                }
                inMode++;
                break;
            }

        }
        if (t.getVoxelAt(posX, posY, posZ - 1) == Material.GRASS ){
            moveTo(queenLoc[0], queenLoc[1] - 1, queenLoc[2] + 2);
        }

    }

    /**
     * Returns map location of an egg within given radius
     * @param rad radius of searching box around the ant
     * @return position of first found egg or null if there are none
     */
    int[] eggInVicinity(int rad){
        for (int z = rad; z > - rad; z--) {
            for (int x = - rad; x <=  + rad; x++) {
                for (int y =  - rad; y <= + rad; y++) {
                    if (t.checkBoundaries(posX + x, posY + y, posZ + z) && t.getVoxelAt(posX + x, posY + y, posZ + z) == Material.EGG){
                        return new int[] {posX + x, posY + y, posZ + z};
                    }
                }
            }
        }
        return null;
    }

    /**
     * Makes the ant strafe in the direction from the biggest amount of pixels above him.
     * Used when digging to make sure the ant strafes from other chambers
     */
    void dirFromAirBubble(){

        int sectorLength = (visibleRadius * 2 + 1) / 3;
        int highest = 0;
        int current;


        for (int i = 0; i < direction2D.length; i++) {
            current = 0;
            for (int z = 1; z <= visibleRadius; z++)
                for (int y = - visibleRadius + (direction2D[i][1] + 1) * (sectorLength); y <= - visibleRadius + (direction2D[i][1] + 1) * sectorLength + sectorLength; y++) {
                    for (int x = - visibleRadius + (direction2D[i][0] + 1) * (sectorLength); x <= - visibleRadius + (direction2D[i][0] + 1) * sectorLength + sectorLength; x++) {
                        if ( t.checkBoundaries(posX + x, posY + y, posZ + z) && ! t.getVoxelAt(posX + x, posY + y, posZ + z).isSolid()) {
                            current++;
                        }

                    }
                }
            if (current > highest) {
                highest = current;
                dirDeltas = new int[] {direction2D[(i + 4) % 8 ][0], direction2D[(i + 4) % 8][1], -1};
            }
        }
    }

    /**
     * Used to check digging conditions - too much food/eggs around and low amount of air pixels below
     * @param rad radius in which to check food/egg number and air pixels
     * @param foodEggThresh Number of food that is considered too much (To much food results in digging)
     * @return true when the conditions for digging are right
     */
    private boolean shouldIDig(int rad, int foodEggThresh){
        int foodEggNum = 0;
        for (int z = rad; z >= -1; z--) {
            for (int x = - rad; x <=  + rad; x++) {
                for (int y =  - rad; y <= + rad; y++) {
                    if (t.checkBoundaries(posX + x, posY + y, posZ + z) &&
                            (t.getVoxelAt(posX + x, posY + y, posZ + z) == Material.EGG || t.getVoxelAt(posX + x, posY + y, posZ + z) == Material.FOOD ) ){
                        foodEggNum++;
                    }
                }
            }
        }
        if ( foodEggNum > foodEggThresh){
            int air = 0;
            for (int z = -1; z >= -rad; z--) {
                for (int x = - rad; x <=  + rad; x++) {
                    for (int y =  - rad; y <= + rad; y++) {
                        if (t.checkBoundaries(posX + x, posY + y, posZ + z) &&
                                (! t.getVoxelAt(posX + x, posY + y, posZ + z).isSolid() ) ){
                            air++;
                        }
                    }
                }
            }
            if (air < 10)
                return true;
        }

        return false;
    }

    /**
     * Used externaly when Queen wants more tunnels. Sets mode to Preparing digging.
     */
    public void forceDig(){
        dirDeltas = randomDir();
        dirDeltas[2] = 0;
        mode = PREPARING_DIGING;
    }

    /**
     * Turns 45° in random direction
     */
    private void strafe(){
        boolean lr = r.nextBoolean();
        int[][] matrixX;
        int[][] matrixY;

        if (lr) {
            matrixX = new int[][]
                    {{-1,-1, 0 },
                    {-1, 0, 1},
                    {0, 1, 1}};
            matrixY = new int[][]
                            {{0,-1, -1 },
                            {1, 0, -1},
                            {1, 1, 0}};
        }else{
            matrixX = new int[][]
                            {{0, 1, 1 },
                            {-1, 0, 1},
                            {-1, -1, 0}};
            matrixY = new int[][]
                            {{-1,-1, 0},
                            {-1, 0, 1},
                            {0, 1, 1}};
        }

        dirDeltas[0] = matrixX[dirDeltas[0] + 1][dirDeltas[1] + 1];
        dirDeltas[1] = matrixY[dirDeltas[0] + 1][dirDeltas[1] + 1];

    }

    /**
     * Returns true if the ant carries food
     * @return carryingFood is variable that indicates the Ant can feed queen or an egg.
     */
    public boolean isCarryingFood() {
        return carryingFood;
    }

    /**
     * If the ant carries egg, returns pointer to that egg.
     * @return pointer to carried egg or null if the ant doesn't carry any.
     */
    public AntEgg getCarriedEgg() {
        return carriedEgg;
    }

    /**
     * Returns one of four behavioural modes the ant is in.
     * @return 0 if the mode is "go up", 1 if "go down", 2 if "dig" and 3 if "prepare digging".
     */
    public int getMode(){
        return mode;
    }
}

