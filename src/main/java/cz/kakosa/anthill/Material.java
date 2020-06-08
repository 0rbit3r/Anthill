package cz.kakosa.anthill;

/**
 * Material is used as building block of the map (voxels is an array of this Material)
 */
public enum Material {
    AIR(0xff000000,false, false),
    SOIL(0xff995600, true, true),
    GRASS(0xff00aa00, true, true),
    STONE(0xff8888a0, true, false),
    WOOD(0xff6d4800, true, true),
    LEAVES(0xff4ca11e,true, true),
    FOOD(0xffb7d870, true, true),

    EGG(0xff8ef2ed, true, false),
    RED_ANT(0xffff0000, true, false)
    ;

    private final int color;
    private final boolean solid;
    private final boolean digable;

    /**
     * Constructor setting all the parameters
     * @param color base color of the pixel (should be in format 0xff######)
     * @param solid solid blocks can't be seen through
     * @param digable digable materials can't be mined by ants
     */
    Material(int color, boolean solid, boolean digable){
        this.color = color;
        this.solid = solid;
        this.digable = digable;
    }

    /**
     * base color of the pixel
     * @return returns color of the pixel
     */
    public int getColor() {
        return color;
    }

    /**
     * solid blocks can't be seen through
     * @return returns true if material is solid
     */
    public boolean isSolid() {
        return solid;
    }

    /**
     * digable materials can't be mined by ants
     * @return returns true if material is digable
     */
    public boolean isDigable() {
        return digable;
    }

}
