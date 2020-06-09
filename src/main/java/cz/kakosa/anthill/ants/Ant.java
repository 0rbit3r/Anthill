package cz.kakosa.anthill.ants;

import cz.kakosa.anthill.Material;
import cz.kakosa.anthill.Terrain;

import java.util.Random;

/**
 * Ant is displayed as a single moving pixel on the map.
 */
public abstract class Ant {

    Random r;
    int[] lastPos;
    public int foodEaten;

    int internalClock = 0;

    AntManager antManager;
    Terrain t;
    Ant[][][] antMap;

    int posX, posY, posZ;

    int[] queenLoc;
    final int EGG_SPACE_NEEDED = 150;

    int visibleRadius = 10;
    int communicationRad = 5;

    int dirDeltas[];

    int[][] direction2D = {{1,0}, {1,-1},{0,-1},{-1,-1}, {-1, 0}, {-1,1}, {0,1}, {1,1}};

    Material carrying;

    /**
     * Sets all parameters and adds itself to the map by accessing arrays at Terrain
     * @param am AntManager to add itself in
     * @param r global Random object
     * @param t Terrain to set up its location
     * @param x x position
     * @param y y position
     * @param z y position
     * @param queenLoc queenLoc is important  for orientation inside the nest
     */
    public Ant (AntManager am, Random r, Terrain t, int x, int y, int z, int[] queenLoc){
        lastPos = new int[] {x, y, z-1};
        antManager = am;
        antMap = am.getAntMap();
        posX = x;
        posY = y;
        posZ = z;
        this.r = r;
        this.t = t;
        this.queenLoc = queenLoc;
        t.setVoxelAt(posX, posY, posZ, Material.RED_ANT);
        t.setColorMapAt(posX, posY, posZ);
    }

    /**
     * Abstract method that holds the behaviour algorithms of the ant
     */
    public abstract void behaveLikeAnAnt();

    /**
     * Changes position of an ant to new coordinates. Should be called after deciding where the ant should go. Handles Material map, Antmap as well as color map.
     * @param x Nes X position
     * @param y New Y position
     * @param z New Z position
     */
    void moveTo(int x, int y, int z){
        int oldX = posX, oldY = posY, oldZ = posZ;

        lastPos = new int[] {oldX, oldY, oldZ};

        posX = x;
        posY = y;
        posZ = z;

        antMap[oldX][oldY][oldZ] = null;
        antMap[posX][posY][posZ] = this;

        t.setVoxelAt(oldX, oldY, oldZ, Material.AIR);
        t.setVoxelAt(x,y,z,Material.RED_ANT);

        t.setColorMapAt(posX, posY, posZ);

        if ( ! t.getVoxelAt(oldX,oldY,oldZ-1).isSolid()){
            for (int i = 0; i <= t.getVisDepth(); i++) { //cleaning up the column above
                if (t.checkBoundaries(oldX, oldY, oldZ + i)) {
                    if (t.getVoxelAt(oldX, oldY, oldZ + i) == null || t.getVoxelAt(oldX, oldY, oldZ + i) != Material.AIR)
                        break;
                    t.setBlackAt(oldX, oldY, oldZ + i);
                }
            }
            int i;
            for (i = 0; ! t.getVoxelAt(oldX,oldY,oldZ - i).isSolid() && i < t.getVisDepth(); i++){ }
            t.setColorMapAt(oldX, oldY, oldZ - i);
        }
        else{
            //t.setBlackAt(oldX, oldY, oldZ + t.getVisDepth());
            t.setColorMapAt(oldX, oldY, oldZ - 1);
        }
    }

    /**
     * Returns true if said position is next to a block in any direction - making it possible for the ant to climb there
     * @param x x position of tested place
     * @param y y position of tested place
     * @param z z position of tested place
     * @return Returns true if position has a block somewhere next to it (excluding diagonal directions), false if the position is surrounded by nonsolid Materials
     */
    Material getSupport(int x, int y, int z){
        boolean crowded = false;
        if (queenLoc != null) {
            crowded = distTo(queenLoc[0], queenLoc[1], queenLoc[2]) < 5;
        }
        if (t.checkBoundaries(x, y, z - 1) && t.getVoxelAt(x, y, z - 1).isSolid() && (t.getVoxelAt(x, y, z - 1) != Material.RED_ANT || crowded))
            return t.getVoxelAt(x, y, z - 1);

        if (t.checkBoundaries(x + 1, y, z) && t.getVoxelAt(x + 1, y, z).isSolid() && (t.getVoxelAt(x + 1, y, z) != Material.RED_ANT || crowded))
            return t.getVoxelAt(x + 1, y, z);

        if (t.checkBoundaries(x, y + 1, z) && t.getVoxelAt(x, y + 1, z).isSolid() && (t.getVoxelAt(x, y + 1, z) != Material.RED_ANT || crowded))
            return t.getVoxelAt(x, y + 1, z);

        if (t.checkBoundaries(x - 1, y, z) && t.getVoxelAt(x - 1, y, z).isSolid() && (t.getVoxelAt(x - 1, y, z) != Material.RED_ANT || crowded))
            return t.getVoxelAt(x - 1, y, z);

        if (t.checkBoundaries(x, y - 1, z) && t.getVoxelAt(x, y - 1, z).isSolid() && (t.getVoxelAt(x, y - 1, z) != Material.RED_ANT || crowded))
            return t.getVoxelAt(x, y - 1, z);

        if (t.checkBoundaries(x, y, z + 1) && t.getVoxelAt(x, y, z + 1).isSolid() && (t.getVoxelAt(x, y, z + 1) != Material.RED_ANT || crowded))
            return t.getVoxelAt(x, y, z + 1);

        return null;

    }

    /**
     * Picks a random direction and tries to get one pixel in that direction
     */
    void stepRandomly(){
        int dx = r.nextInt(2) - 1;
        int dy = r.nextInt(2) - 1;
        int dz = r.nextInt(2) - 1;

        while (t.checkBoundaries(posX + dx, posY + dy, posZ + dz) && ! t.getVoxelAt(posX + dx, posY + dy, posZ + dz).isSolid() && getSupport(posX + dx, posY + dy, posZ + dz) != null) {
            moveTo(posX + dx, posY + dy, posZ + dz);
            dx = r.nextInt(2) - 1;
            dy = r.nextInt(2) - 1;
            dz = r.nextInt(2) - 1;
        }
    }

    /**
     * Tries to move the ant based on aimForPixel to nearest pixel with support.
     */
    void solveMovementFromDir(){

        boolean moved = false;

        if (getSupport(posX, posY, posZ) == null){
            moveTo(posX, posY, posZ - 1);
            moved = true;
        }

        if (! moved){
            moved = testAndMove(posX + dirDeltas[0], posY + dirDeltas[1], posZ + dirDeltas[2]);
        }

        if (! moved && dirDeltas[0] == 0) {
            moved = testAndMove(posX + 1, posY + dirDeltas[1], posZ + dirDeltas[2]);
            if ( ! moved)
                moved = testAndMove(posX - 1, posY + dirDeltas[1], posZ + dirDeltas[2]);
        }
        if (! moved && dirDeltas[1] == 0) {
            moved = testAndMove(posX + dirDeltas[0], posY + 1, posZ + dirDeltas[2]);
            if (! moved)
                moved = testAndMove( posX + dirDeltas[0], posY - 1, posZ + dirDeltas[2]);
        }
        if (! moved && dirDeltas[2] == 0) {
            moved = testAndMove(posX + dirDeltas[0], posY + dirDeltas[1], posZ + 1);
            if (! moved)
                moved = testAndMove(posX + dirDeltas[0], posY + dirDeltas[1], posZ - 1);
        }

        if (! moved && dirDeltas[2] != 0) {
            moved = testAndMove(posX + dirDeltas[0], posY + dirDeltas[1], posZ);
            if (!moved)
                moved = testAndMove(posX + dirDeltas[0], posY + dirDeltas[1], posZ - dirDeltas[2]);
        }
        if (! moved && dirDeltas[0] != 0) {
            moved = testAndMove(posX, posY + dirDeltas[1], posZ + dirDeltas[2]);
            if (!moved)
                moved = testAndMove(posX - dirDeltas[0], posY + dirDeltas[1], posZ + dirDeltas[2]);
        }
        if (! moved && dirDeltas[1] != 0) {
            moved = testAndMove(posX + dirDeltas[0], posY, posZ + dirDeltas[2]);
            if (!moved)
                moved = testAndMove(posX + dirDeltas[0], posY - dirDeltas[1], posZ + dirDeltas[2]);
        }

        if (!moved){
            stepRandomly();
        }

    }

    /**
     * Used in solveMovementFromAim as a submethod that checks boundaries, checks if given position is valid and if so, moves the Ant.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return true if movement was successful, false if not
     */
    boolean testAndMove(int x, int y, int z){
        if ( t.checkBoundaries(x,y,z) && ! t.getVoxelAt(x,y,z).isSolid() && getSupport(x,y,z) != null && ! (x == lastPos[0] && y == lastPos[1] && z == lastPos[2])) {
            moveTo(x,y,z);
            return true;
        }
        return false;
    }

    /**
     * gets three dimensional direction deltas (from -1 to 1) based on a point of interests location on map
     * @param x point of interests x location
     * @param y point of interests x location
     * @param z point of interests x location
     * @return returnd int[3] of x delta, y delta and z delta all ranging from -1 to 1. (1 vertically is up)
     */
    int[] getDirFromMap(int x, int y, int z){
        int[] toReturn = new int[3];
        if (x < posX){
            toReturn[0] = -1;
        }
        else if (x == posX){
            toReturn[0] = 0;
        }
        else{
            toReturn[0] = 1;
        }
        if (y < posY){
            toReturn[1] = -1;
        }
        else if (y == posY){
            toReturn[1] = 0;
        }
        else{
            toReturn[1] = 1;
        }
        if (z < posZ){
            toReturn[2] = -1;
        }
        else if (z == posZ){
            toReturn[2] = 0;
        }
        else{
            toReturn[2] = 1;
        }

        return toReturn;
    }

    /**
     *Sets direction based on where the biggest amount of air is above or below the ant
     * @param upMode if this is true the ant searches for air pockets above it, and below it if false.
     *               This results in the ant traveling up and outside the ant Nest if true or inside and down the nest if false.
     */
    void dirToAirBubble(boolean upMode){

        int sectorLength = (visibleRadius * 2 + 1) / 3;
        int highest = 0;
        int current;
        int  verticalDelta = upMode ? 1 : -1;

        int z0 = upMode ? 1 : -visibleRadius;
        int zn = upMode ? visibleRadius : -1;


        for (int i = 0; i < direction2D.length; i++) {
            current = 0;
            for (int z = z0; z <= zn ; z++)
                for (int y = - visibleRadius + (direction2D[i][1] + 1) * (sectorLength); y <= - visibleRadius + (direction2D[i][1] + 1) * sectorLength + sectorLength; y++) {
                    for (int x = - visibleRadius + (direction2D[i][0] + 1) * (sectorLength); x <= - visibleRadius + (direction2D[i][0] + 1) * sectorLength + sectorLength; x++) {
                        if ( t.checkBoundaries(posX + x, posY + y, posZ + z) && ! t.getVoxelAt(posX + x, posY + y, posZ + z).isSolid()) {
                            current++;
                        }

                    }
                }
            if (current > highest) {
                highest = current;
                dirDeltas = new int[] {direction2D[i][0], direction2D[i][1], verticalDelta};
            }
        }
    }

    /**
     * Counts air pixels around the ant (excluding parts bellow him)
     * @param rad radius of the cube around the ant in which to check
     * @return number of air pixels that are around the ant
     */
    int sizeOfChamber(int rad){
        int toReturn = 0;

        int start = - rad;
        int end = rad;

        for (int z = 0 ; z <= end; z++) {
            for (int y = start; y <= end; y++) {
                for (int x = start; x <= end; x++) {
                    if ( t.checkBoundaries(posX + x, posY + y, posZ + z) && ! t.getVoxelAt(posX + x, posY + y, posZ + z).isSolid()){
                        toReturn++;
                    }
                }
            }
        }
        return toReturn;
    }


    /**
     * Looks for the food in a vicinity of the ant
     * @param rad radius of the cube in which to search for the food
     * @return null if there is no food around, int[3] coordinates of the food if there is any.
     */
    int[] foodInVicinity(int rad){
        for (int z = rad; z >= -rad; z--) {
            for (int x = -rad; x <= rad; x++) {
                for (int y = -rad; y <= rad; y++) {
                    if ( t.checkBoundaries(posX + x, posY + y, posZ + z) &&
                            (t.getVoxelAt(posX + x, posY + y, posZ + z) == Material.LEAVES || t.getVoxelAt(posX + x, posY + y, posZ + z) == Material.FOOD)){
                        return new int[] {posX + x,posY + y,posZ + z};
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sets random direction for the ant to follow
     * @return int[3] of direction deltas from -1 to 1
     */
    int[] randomDir(){
        int randDir = r.nextInt(8);
        return new int[] {direction2D[randDir][0], direction2D[randDir][1], 0};
    }

    /**
     * Returns Euclidean distance from the ant to given coordinates (rounded to int)
     * @param x x position of the measured spot
     * @param y y position of the measured spot
     * @param z z position of the measured spot
     * @return returns non negative int that coresponds to the distance between the Ant and the coordinates
     */
    int distTo(int x, int y, int z){
        return (int)Math.sqrt(Math.pow(posX - x,2) + Math.pow(posY - y,2) + Math.pow(posZ - z,2));
    }

    /**
     * Debugging tool for better view of 3D matrix
     * @param matrix can be any matrix of Material with no nulls.
     * @return is text of letter codes when solid and "_" when non-solid. Top 2D matrix is top most layer of the array, other dimensions are same as if showed in the program
     */
    public static String get3DArrayPrint(Material[][][] matrix) {
        String output = new String();
        String letter = "_";
        for (int z = matrix[0][0].length - 1; z >=0; z--) {
            for (int y = 0; y < matrix[0].length; y++) {
                for (int x = 0; x < matrix.length;   x++) {
                    switch(matrix[x][y][z]){
                        case SOIL:
                        case WOOD:
                        case STONE:
                        case GRASS:
                            letter = "█";
                            break;
                        case RED_ANT:
                            letter = "A";
                            break;
                        case LEAVES:
                        case FOOD:
                            letter = "F";
                            break;
                        case EGG:
                            letter = "E";
                            break;
                        case AIR:
                            letter = "_";
                    }
                    output = output + letter;
                }
                output = output + "\n";
            }
            output = output + "\n";
        }
        return output;
    }

    /**
     * Returns null if ant doesn't carry anything, or carried Material
     * @return
     */
    public Material getCarrying() {
        return carrying;
    }
}
