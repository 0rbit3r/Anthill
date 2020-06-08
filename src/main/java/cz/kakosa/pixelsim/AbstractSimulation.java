package cz.kakosa.pixelsim;

/**
 *Abstract class for making an external simulation
 */
public abstract class AbstractSimulation {
    /**
     * Updates the simulation
     * @param sc SimContainer that called the method
     * @param dt Update cap - amount of sim time passing between each update
     */
    public abstract void update(SimContainer sc, float dt);

    /**
     * Renders the simulation
     * @param sc SimContainer that called the method.
     * @param r Renderer used to render the scene
     */
    public abstract void render(SimContainer sc, Renderer r);


}
