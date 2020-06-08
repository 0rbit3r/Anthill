package cz.kakosa.anthill;

import cz.kakosa.pixelsim.*;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Random;

import cz.kakosa.anthill.ants.*;

/**
 * Specific Simulation manager. In this case simulaties Anthill
 */
public class SimManager extends AbstractSimulation{

    public static final int WIDTH = 750, HEIGHT = 500, INFOBAR_WIDTH = 250,
                            MAX_SPEED = 256, MIN_SPEED = 1;// Dimensions of the Window and the information text bar

    public int cursorX = 0;
    public int cursorY = 0;

    public Random r;

    private boolean simRunning;

    private boolean paused;

    private float speed = 1;

    private float visibleLayer; //Topmost Layer on which is seen on the screen
    private int locX =  0; //locX and locY are position of the topleft corner of the zoomed view of the map
    private int locY = 0;
    private int zoom = 1;
    private int windowWidth; //Window dimensions of the current object
    private int windowHeight;

    float AntTimeKeeper = 0; //Keeps how much time passed since last update

    AntManager antManager;
    Terrain terrain;
    ScreenInfo screenInfo;

    private boolean isTitleScreen;
    private boolean isGenerating;
    private Image titleScreenImage = new Image("/TitleScreen.png");
    private Image titleGenerating = new Image("/Generating.png");

    /**
     * Creates new SimManager with set dimensions
     * @param width Width in pixels
     * @param height Height in pixels
     * @param infobar Width of the gray bar that holds all the info
     */
    public SimManager(int width, int height, int infobar) {
        this.windowWidth = width;
        this.windowHeight = height;
        isTitleScreen = true;
        isGenerating = false;
        r = new Random();

        screenInfo = new ScreenInfo(infobar);
    }

    @Override
    public void update(SimContainer sc, float dt) {

        if(!isTitleScreen && !isGenerating ){
            manageInputs(sc);

            if (!paused && simRunning){
                AntTimeKeeper += dt * speed;
                if (AntTimeKeeper > 1){
                    antManager.manageAnts();
                    //terrain.updateCurrentState();
                    AntTimeKeeper = 0;
                }

            }

        }
        else {
            if (isGenerating){

                terrain = new Terrain(this, windowWidth - INFOBAR_WIDTH, windowHeight);
                visibleLayer = terrain.getDepth()/2;

                antManager = new AntManager(terrain, r);
                //terrain.updateCurrentState();

                isGenerating = false;
                setSimRunning();
                paused = false;
            }
            if (sc.getInput().isKeyDown(KeyEvent.VK_ENTER)){
                isTitleScreen = false;
                isGenerating = true;
            }
        }
    }

    /**
     * Renders everything using a Renderer.
     * @param sc SimContainer that called the method.
     * @param r Renderer used to render the scene
     */
    @Override
    public void render(SimContainer sc, Renderer r) {
        if (!isTitleScreen && !isGenerating){
            int[][][] colors = terrain.getColorMap();
            for (int y = 0; y < windowHeight; y++) {
                for (int x = 0; x < windowWidth - screenInfo.getInfobarWidth(); x++) {
                    if (locX + (x/zoom) < terrain.getWidth() && locX + (x/zoom) >= 0 && locY + (y/zoom) < terrain.getHeight() && locY + (y/zoom)>= 0) {
                        r.setPixel(x, y, colors[locX + (x / zoom)][locY + (y / zoom)][(int)visibleLayer]);
                    }
                }
            }
            screenInfo.render(this, r);
        }
        else{
            if (isGenerating)
                r.drawImage(titleGenerating, 0, 0);
            else
            r.drawImage(titleScreenImage, 0, 0);
        }
    }


    /**
     * Takes care of all the inputs while the simulation is running
     * @param sc that has Input of the window stored inside
     */
    void manageInputs(SimContainer sc){
        if (visibleLayer < terrain.getDepth() - 1 && (sc.getInput().isKeyDown(KeyEvent.VK_Q) || sc.getInput().isKey(KeyEvent.VK_Q))) {
            visibleLayer += 0.4;
        }
        if (visibleLayer > 2 && (sc.getInput().isKeyDown(KeyEvent.VK_A) || sc.getInput().isKey(KeyEvent.VK_A))) {
            visibleLayer -= 0.4;
        }

        if (zoom > 1 && (sc.getInput().isKeyDown(KeyEvent.VK_S))) {
            zoom /= 2;
            locX -= terrain.getWidth() / zoom / 4;
            locY -= terrain.getHeight() / zoom / 4;
        }
        if (zoom <= 16 && (sc.getInput().isKeyDown(KeyEvent.VK_W))) {
            locX += terrain.getWidth() / zoom / 4;
            locY += terrain.getHeight() / zoom / 4;
            zoom *= 2;
        }

        if (speed > MIN_SPEED && (sc.getInput().isKeyDown(KeyEvent.VK_F))){
            speed /= 2;
        }
        if (speed < MAX_SPEED && (sc.getInput().isKeyDown(KeyEvent.VK_G))){
            speed *= 2;
        }

        if (sc.getInput().isKeyDown(KeyEvent.VK_E)){
            if (terrain.getVisDepth() < 50) {
                simRunning = false;
                terrain.setVisDepth(terrain.getVisDepth() + 10);
            }
        }
        if (sc.getInput().isKeyDown(KeyEvent.VK_D)){
            if (terrain.getVisDepth() > 30) {
                simRunning = false;
                terrain.setVisDepth(terrain.getVisDepth() - 10);
            }
        }

        if (sc.getInput().isKeyDown(KeyEvent.VK_SPACE)){
            paused = !paused;
        }

        if (locX > -50 && (sc.getInput().isKeyDown(KeyEvent.VK_LEFT) || sc.getInput().isKey(KeyEvent.VK_LEFT))) {
            locX -= 2;
        }
        if (locX <= terrain.getWidth() + 50 - terrain.getWidth() / zoom && (sc.getInput().isKeyDown(KeyEvent.VK_RIGHT) || sc.getInput().isKey(KeyEvent.VK_RIGHT))) {
            locX += 2;
        }
        if (locY > -50 && (sc.getInput().isKeyDown(KeyEvent.VK_UP) || sc.getInput().isKey(KeyEvent.VK_UP))) {
            locY -= 2;
        }
        if (locY <= terrain.getHeight() + 50 - terrain.getHeight() / zoom && (sc.getInput().isKeyDown(KeyEvent.VK_DOWN) || sc.getInput().isKey(KeyEvent.VK_DOWN))) {
            locY += 2;
        }

        if(sc.getInput().isButtonDown(MouseEvent.BUTTON1)){
            cursorX = sc.getInput().getMouseX()/zoom + locX;
            cursorY = sc.getInput().getMouseY()/zoom + locY;

            Ant selected = null;
            for (int z = (int)visibleLayer; z > 0; z--) {
                    if (antManager.getAntMap()[cursorX][cursorY][z] != null){
                        selected = antManager.getAntMap()[cursorX][cursorY][z];
                        break;
                    }
            }
            screenInfo.setAnt(selected);
        }

        if(sc.getInput().isKeyDown(KeyEvent.VK_X)){
            terrain.setxRay();
        }

    }

    /**
     * Starts the program
     * @param args Not used
     */
    public static void main(String args[]){
            SimContainer sc = new SimContainer(new SimManager(WIDTH, HEIGHT, INFOBAR_WIDTH), WIDTH, HEIGHT);
            sc.start();
    }

    /**
     * Returns the layer on which the camera is located
     * @return integer between 0 and height that can be understood as a camera Z location
     */
    float getVisibleLayer() {
        return visibleLayer;
    }

    /**
     * Returns Window width in pixels
     * @return
     */
    public int getWindowWidth() {
        return windowWidth;
    }

    /**
     * Returns Window height in pixels
     * @return
     */
    public int getWindowHeight() {
        return windowHeight;
    }

    /**
     * Used to continue simulation from outside (When other threads modify arrays)
     */
    public void setSimRunning() {
        this.simRunning = true;
    }

    /**
     * Returns whether the simulation is paused by User ort not
     * @return
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Returns speed of the simulation which is capped by MIN_SPEED and MAX_SPEED
     * @return speed of the simulation where 1 is default value
     */
    public float getSpeed() {
        return speed;
    }
}
