package cz.kakosa.anthill;

import java.util.Random;

/**
 * Terrain is the map where the simulation happens.
 * Complete with random map generation and arrays that make rendering easier.
 */
public class Terrain implements Runnable{

    private SimManager sm;

    /**
     * Voxels store each pixel of the map as a block of material.
     */
    private Material[][][] voxels;

    /**
     * Colormap is used for rendering. The camera sees not only current slice but also past it up to a certain distance.
     * Hence it is faster to have all these layers preloaded instead of computing each air pixels value based on what's bellow it every time the camera moves.
     */
    private int[][][] colorMap;

    private int width, height, depth = 220;
    private int visDepth = 50;
    private Random r;

    /**
     * If x-ray is on. User can see better underground.
     * It is achived througn the "light" that is manifestes as a gradient of a materials color in colorMap being able to go through not only air but air and then solid blocks (until it encounters air for the second time).
     */
    private boolean xRay;

    /**
     *
     * @param sm
     * @param width
     * @param height
     */
    public Terrain(SimManager sm, int width, int height) {
        this.sm = sm;
        this.width = width;
        this.height = height;
        voxels = new Material[width][height][depth];
        colorMap = new int[width][height][depth];

        generateTerrain();

        setVisDepth(visDepth);

        updateColor(false);
    }

    /**
     * Starts a new thread in which it generates the world. Stone board on the bottom first, valleys second, then dirt and stones, then trees and lastly food
     */
    private void generateTerrain() {
        r = sm.r;

        //Make floor
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                voxels[x][y][0] = Material.STONE;
            }
        }
        Runnable runnable = () -> {
            generateValleys(r);
            fillGround();
            generateStones(r);
            generateTrees(r);
            for (int i = 0; i < 7; i++) {
                generateFood(r);
            }
        };

        Thread t = new Thread(runnable);
        t.start();
    }

    /**
     * Updates the 3D array of pixels to be displayed. All slices are preloaded at once so that the array doesn't have to be updated every time user changes camera height.
     * @param partial if partial is true, rendering starts at half of the world since the bottom half is black anyway.
     */
    public synchronized void updateColor(boolean partial){
        for (int z = partial ? 55 : 0; z < depth; z++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (voxels[x][y][z] == null) voxels[x][y][z] = Material.AIR;
                    if (voxels[x][y][z].isSolid()){
                        if (! xRay){
                            colorMap[x][y][z] = 0xff000000;
                        }
                        visColumn(x,y,z);
                    }
                }
            }
        }
    }

    /**
     * Updates world color pixels on top of given coordinates. Base color at the (x,y,z) and dimming gradient as the color goes up. If x-ray is off, only pixels up to the last air pixels are updated.
     * If x-ray is on, pixels up to the second encounter with air are updated (Making user able to see tunnels when camera is obscured by solid blocks)
     * @param x x position 0 to width
     * @param y y position 0 to height
     * @param z z position 0 to depth
     */
    public void visColumn(int x, int y, int z){

        if (z < depth -1 && (voxels[x][y][z + 1] == null || ! voxels[x][y][z + 1].isSolid())){
            int origColor = voxels[x][y][z].getColor();
            int origR = (origColor - 0xff000000) / 0x10000;
            int origG = ((origColor - 0xff000000) % 0x10000) / 0x100;
            int origB = ((origColor - 0xff000000) % 0x100);
            int airOccurence = 0;

            colorMap[x][y][z + 1] = origColor;
            int z0 = z + 1;
            int d = 1;
            float changeRate = 0.92f;
            float dc = 1f; //Slouží k vytvoření gradientu
            while(z0 < depth && d < visDepth && ( voxels[x][y][z0] == null  || ! voxels[x][y][z0].isSolid())){
                colorMap[x][y][z0] = (int)(origR * dc) * 0x10000 + (int)(origG * dc) * 0x100 + (int)(origB * dc);
                dc *= changeRate;
                d++;
                z0++;
            }
            if (xRay){
                while(z0 + 1 < depth && d < visDepth && ( voxels[x][y][z0 + 1] != null  && voxels[x][y][z0 + 1].isSolid())){
                    colorMap[x][y][z0] = (int)(origR * dc) * 0x10000 + (int)(origG * dc) * 0x100 + (int)(origB * dc);
                    dc *= changeRate;
                    d++;
                    z0++;
                }
            }
        }
    }

    /**
     * Fills ground with soil from bottom to grass
     */
    private void fillGround(){
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 1; z < depth; z++) {
                    if (voxels[x][y][z] == null || ! voxels[x][y][z].isSolid())
                        voxels[x][y][z] = Material.SOIL;
                    else
                        break;
                }
            }
        }
    }

    /**
     * Generates grass hills and valleys by combining 3D normal curves with randomized parameters
     * @param r global Random object
     */
    private void generateValleys(Random r){
        int points_num = 10;
        int points[][] = new int[points_num][4];
        for (int i = 0; i < points_num; i++) {
            points[i][0] = r.nextInt(width);
            points[i][1] = r.nextInt(height);
            points[i][2] = r.nextInt(5);
            if(r.nextBoolean()) points[i][2] = -points[i][2];
            points[i][3] = 50 + r.nextInt(100);

        }
        float sum;
        double dist;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sum = 0;
                for (int i = 0; i < points_num; i++) {
                    dist = Math.sqrt( Math.pow(x - points[i][0], 2) + Math.pow(y - points[i][1], 2));
                    sum += points[i][2] *  Math.exp((double) (-1 * (Math.pow(dist  / points[i][3], 2))));
                }
                voxels[x][y][(int)(sum + depth/2)] = Material.GRASS;
            }
        }
    }

    /**
     * Generates stones inside ground with random placement, radius and scales in all 3 dimensions.
     * @param r global Random object
     */
    private void generateStones(Random r){
        int stonesNum = 65;
        int centX, centY, centZ, rad;
        int scaleY,scaleZ, scaleX;
        for (int i = 0; i < stonesNum; i++) {
            centX = r.nextInt(width);
            centY = r.nextInt(height);
            centZ = (r.nextInt(depth/2));
            if (centZ > depth/4) centZ -= r.nextInt(depth/4);
            rad = r.nextInt(20) + 20;
            scaleY = r.nextInt(3) + 1;
            scaleX = r.nextInt(3) + 1;
            scaleZ = r.nextInt(10) + 5;
            makeSphere(centX,centY,centZ,rad,scaleX,scaleY, scaleZ, Material.STONE, false);
        }
    }

    /**
     * Generates one or two trees by making a cylinderic trunk, random crawling roots and branches
     * @param r global Random object
     */
    private void generateTrees(Random r){
        int treesNum = 1 + r.nextInt(2);
        int locX, locY, rad;
        for (int i = 0; i < treesNum; i++) {
            locX = r.nextInt( width/treesNum ) + width/treesNum * i;
            locY = r.nextInt(height - 50) + 25;
            rad = r.nextInt(60) + 50;
            int rad0 = 0;
            for (int h = depth/2 - 10; h < depth ; h++) {
                rad0 = (int)(rad * (1-2*Math.sin(0.2* (h - depth/2)/(depth/2))*(1-0.5*(h - depth/2)/depth*2)));
                generateDisc(locX, locY, h, rad0, Material.WOOD);
                if (h > depth / 5 * 3 && r.nextInt(6) == 0){
                    double dir = r.nextDouble() * Math.PI * 2;
                    makeBranch(r, (locX + Math.cos(dir) * rad0), (locY + Math.sin(dir) * rad0), h, dir, r.nextInt(150) + 50);
                }
            }

            int rootX, rootY, rootRad;
            for (int root = 0; root < 7; root++) {
                rootX = locX + (int)(Math.cos(2*Math.PI / 7 * root) * 20 + r.nextInt(20) - 10);
                rootY = locY + (int)(Math.sin(2*Math.PI / 7 * root) * 20 + r.nextInt(20) - 10);
                rootRad = (int)(rad0 * (0.7 + (0.3 * r.nextDouble())));
                makeRoot(r, rootX, rootY, rootRad, 2*Math.PI / 7 * root);
            }
        }

    }

    /**
     * Generates random branch at the position of the given coordinates with given (but slightly randomized) size and in given direction
     * @param r global Random object
     * @param posX x position
     * @param posY y position
     * @param h z positon (or height)
     * @param dir direction in radians of growth
     * @param size approximate size of the branch
     */
    private void makeBranch(Random r, double posX, double posY, int h, double dir, int size){
        int rad = size / 8;
        double leavesDir = dir + Math.PI/2;
        double checkerX, checkerY;;
        int shift;
        int leavesRad;
        double dirShift;
        for (int i = 0; i < size && rad > 3 && h < depth; i++) {
            checkerX = posX + Math.cos(dir) * rad;
            checkerY = posY + Math.sin(dir) * rad;
            if(posX < width && posY < height && posX >= 0 && posY >=0 && h < depth && (checkerX) < width &&
                    (checkerY) < height && (checkerX) >= 0 && (checkerY) >= 0) {
                if (voxels[(int) (checkerX)][(int) (checkerY)][h] == null || ! voxels[(int) (checkerX)][(int) (checkerY)][h].isSolid())
                    makeSphere((int) posX, (int) posY, h, rad, 1, 1, 1, Material.WOOD, false);
            }
            shift = r.nextInt(2);
            posX += Math.cos(dir) * (shift + 2);
            posY += Math.sin(dir) * (shift + 2);
            if (r.nextBoolean() && r.nextBoolean()) rad -= 1;
            if (r.nextBoolean()) h += r.nextInt(3);
            leavesRad = rad - r.nextInt(3);
            if (i > 10)
                makeLeaf(r,(int)(posX + Math.cos(leavesDir) * (rad + leavesRad) * 0.8), (int)(posY  + Math.sin(leavesDir) * (rad + leavesRad) * 0.8), h, leavesDir,leavesRad);
            dirShift = Math.PI/16 * r.nextDouble() - Math.PI/32;
            dir += dirShift;
            leavesDir = ((leavesDir + Math.PI) + dirShift) % (Math.PI * 2);
        }
    }

    /**
     * Generates a leaf at given coordinates, similar to generateBranch
     * @param r global
     * @param posX x position
     * @param posY y position
     * @param posZ z position
     * @param dir direction in radians
     * @param rad starting thickness of the leaf
     */
    private void makeLeaf(Random r, int posX, int posY, int posZ, double dir, int rad){
        int size = r.nextInt(6) + 4;
        int shift;
        for (int i = 0; i < size && rad > 3; i++) {
            generateDisc(posX,posY,posZ, rad, Material.LEAVES);
            shift = r.nextInt(3);
            posX += Math.cos(dir) * (shift + 2);
            posY += Math.sin(dir) * (shift + 2);
            rad -= r.nextInt(3);
        }
    }

    /**
     * Crates root that sprawls downwards starting at given coordinates with given radius and direction
     * @param r global Random r
     * @param posX x position
     * @param posY y position
     * @param rad starting radius of the root
     * @param dir starting horizontal direction
     */
    private void makeRoot(Random r, int posX, int posY, int rad, double dir){
        int dirDelta = r.nextBoolean() ? 1 : -1;
        for (int h = (int)(depth/7f * 4); h >= 0 ; h--) {
            generateDisc(posX, posY, h, rad, Material.WOOD);
            posX += (int)(Math.cos(dir) * r.nextInt(8));
            posY += (int)(Math.sin(dir) * r.nextInt(8));
            rad -= r.nextInt(4);
            dir += dirDelta * r.nextDouble() * 0.1;
        }

    }

    /**
     *  Creates sphere with given parameters
     * @param x0 center x position
     * @param y0 center y position
     * @param z0 center z position
     * @param rad base radius of the sphere
     * @param scaleX x scaling of the sphere (making it ellipsoid if not 1)
     * @param scaleY y scaling of the sphere (making it ellipsoid if not 1)
     * @param scaleZ z scaling of the sphere (making it ellipsoid if not 1)
     * @param material Material with witch the sphere should be filled
     * @param digging if true, stones, eggs and ant are not replaced. Also redraws the region of visual pixels afterwards
     */
    public void makeSphere(int x0, int y0, int z0, int rad, float scaleX, float scaleY, float scaleZ, Material material, boolean digging){
        for (int x = Math.max(x0 - rad , 0); x < Math.min(width, x0 + rad); x++) {
            for (int y = Math.max(0, y0 - rad); y < Math.min(height, y0 + rad); y++) {
                for (int z = Math.max(z0 - rad, 0); z < Math.min(z0 + rad, depth); z++) {
                    if (Math.pow(x0 - x, 2) * scaleX + Math.pow(y0 - y, 2) * scaleY + Math.pow(z0 - z, 2) * scaleZ < Math.pow(rad,2)) {
                        if ( ! digging || (voxels[x][y][z] != Material.STONE && voxels[x][y][z] != Material.RED_ANT))
                            voxels[x][y][z] = material;
                    }
                }

            }
        }
        if (digging) // fuj
            for (int x = Math.max(x0 - rad , 0); x < Math.min(width, x0 + rad); x++) {
                for (int y = Math.max(0, y0 - rad); y < Math.min(height, y0 + rad); y++) {
                    for (int z = Math.min(z0 + rad, depth) - 1; z >= Math.max(z0 - rad, 0); z--) {
                            setColorMapAt(x, y, z);
                    }
                }
            }
    }

    /**
     * Generates horizontal disc of material at given position with thickness of 1 pixel
     * @param x0 x position of center
     * @param y0 y position of center
     * @param z0 z position of center
     * @param rad radius of the disc
     * @param mat material with which to fill the disc
     */
    public void generateDisc(int x0, int y0, int z0, int rad, Material mat){
        if (z0 > 0 && z0 < depth) {
            for (int x = Math.max(x0 - rad, 0); x < Math.min(width, x0 + rad); x++) {
                for (int y = Math.max(0, y0 - rad); y < Math.min(height, y0 + rad); y++) {
                    if (Math.sqrt((x - x0) * (x - x0) + (y - y0) * (y - y0)) < rad)
                        voxels[x][y][z0] = mat;
                }
            }
        }
    }

    /**
     * sets voxels array at given coordinates
     * @param x x position
     * @param y y position
     * @param z z position
     * @param m material with which to set the voxel
     */
    public void setVoxelAt(int x, int y, int z, Material m) {
        voxels[x][y][z] = m;
    }

    /**
     * Return voxel Material at given position
     * @param x x coordinates
     * @param y y coordinates
     * @param z z coordinates
     * @return Material enum that lies at the coordinates
     */
    public Material getVoxelAt(int x, int y, int z) {
        return voxels[x][y][z];
    }

    /**
     * Creates a single blob of food on the floor level at given coordinates. For perfect sphere, all scales must be 1.
     * @param x0 x position
     * @param y0 y position
     * @param rad radius of the blob
     * @param scaleX x scale
     * @param scaleY y scale
     * @param scaleZ z scale
     */
    private void dropFood(int x0, int y0, int rad, float scaleX, float scaleY, float scaleZ){
        int z0 = depth/2 + 50;
        while (voxels[x0][y0][z0] !=  Material.GRASS && voxels[x0][y0][z0] !=  Material.STONE){
            z0--;
        }
        for (int x = Math.max(x0 - rad , 0); x < Math.min(width, x0 + rad); x++) {
            for (int y = Math.max(0, y0 - rad); y < Math.min(height, y0 + rad); y++) {
                for (int z = Math.max(z0 - rad, 0); z < Math.min(z0 + rad, depth); z++) {
                    if (Math.pow(x0 - x, 2) * scaleX + Math.pow(y0 - y, 2) * scaleY + Math.pow(z0 - z, 2) * scaleZ < Math.pow(rad,2)) {
                        if ( voxels[x][y][z] == null || !voxels[x][y][z].isSolid())
                            voxels[x][y][z] = Material.FOOD;
                    }
                }

            }
        }

    }

    /**
     * Generates depositories of food over the map. On each position multiple blobs are created and dropped using dropFood randomly near that position
     * @param r global Random object
     */
    public void generateFood(Random r){
        int centX = r.nextInt(width - 40) + 20;
        int centY = r.nextInt(height - 40) + 20;
        int rad = r.nextInt(20) + 10;
        int cX;
        int cY;
        int cRad;
        int iMax = r.nextInt(4) + 4;
        int cScaleY, cScaleX;
        for (int i = 0; i < iMax; i++){
            cX = centX + r.nextInt(rad) + rad/2;
            cY = centY + r.nextInt(rad) + rad/2;
            cRad = r.nextInt(5) + 5;
            cScaleY = r.nextInt(3) + 1;
            cScaleX = r.nextInt(3) + 1;
            if (checkBoundaries(cX,cY,0))
                dropFood(cX,cY,cRad,cScaleX,cScaleY,1);
        }
    }

    /**
     * Sets color map at coordinates and sets visual pixels gradient above the pixel
     * @param x x position
     * @param y y position
     * @param z z position
     */
    public void setColorMapAt(int x, int y, int z) {
        if (x < width && x >= 0 && y<height && y>= 0 && z <depth && z>=0 && voxels[x][y][z].isSolid()) {
            colorMap[x][y][z] = getVoxelAt(x, y, z) == Material.RED_ANT ? 0xffff0000 : 0xff000000;
            visColumn(x, y, z);
        }
    }

    /**
     * Sets visual pixel black at given coordinates
     * @param x x position
     * @param y y position
     * @param z z position
     */
    public void setBlackAt(int x, int y, int z){
        colorMap[x][y][z] = 0xff000000;
    }

    /**
     * Returns current color map. Color map is used for rendering. For instance if you want to render map at level 50, you take colorMap[all][all][50] and you have all the pixels to render.
     * Without it you would have to go "visual depth" times down for every transparent pixel.
     * @return returns colorMap. in format colorMap[horizontal x][horizontal y][vertical z/camera depth]
     */
    public int[][][] getColorMap() {
        return colorMap;
    }

    /**
     *
     * @return width of the map
     */
    public int getWidth() {
        return width;
    }
    /**
     * height is horizontal. Maximum "height" of an object is in depth
     * @return height of the map
     */
    public int getHeight() {
        return height;
    }

    /**
     * depth is vertical
     * @return depth of the map
     */
    public int getDepth() {
        return depth;
    }

    /**
     * visual depth dictates how far the camera can see before it's cut off with black pixels
     * @return current set visual depth
     */
    public int getVisDepth() {
        return visDepth;
    }

    /**
     * Sets visual depth.
     * @param visDepth can be at least 30 (some ant rendering glitches may occur when bellow 30), max value is 50 since visible pixels further than 50 are too dim anyway
     */
    public void setVisDepth(int visDepth) {
        this.visDepth = visDepth;
        colorMap = new int[width][height][depth];
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Used by another thread when user changes visual depth during simulation to not halt the simulation itself.
     */
    @Override
    public void run() {
        updateColor(false);
        sm.setSimRunning();
    }

    /**
     * Return true if the pixels are inside the map to avoid out of array exceptions
     * @param x controlled x location
     * @param y controlled y location
     * @param z controlled z location
     * @return true if all the values are within map boundaries
     */
    public boolean checkBoundaries(int x, int y, int z){
        if( x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth)
            return true;
        return false;
    }

    /**
     * Toggles x-ray and redraws the colorMap array
     */
    public void setxRay() {
        this.xRay = ! xRay;
        colorMap = new int[width][height][depth];
        Thread t = new Thread(this);
        t.start();
    }

    /**
     *
     * @return true if x-ray is set on, false otherwise
     */
    public boolean isxRay() {
        return xRay;
    }
}
