package cz.kakosa.pixelsim;

public class SimContainer implements  Runnable{

    private Thread thread;
    private Window window;
    private Renderer renderer;
    private Input input;
    private AbstractSimulation sim;

    private int width, height;
    private float scale = 1.5f;
    private String title = "Anthill";

    private boolean running = false;
    private final double UPDATE_CAP = 1.0/60.0;

    /**
     * Sets up Dimensions and stores Simulation Object that called the constructor
     * @param sim
     * @param width
     * @param height
     */
    public SimContainer(AbstractSimulation sim, int  width, int height){

        this.width = width;
        this.height = height;
        this.sim = sim;
    }

    /**
     * Starts the simulation in a new Thread
     */
    public void start(){
        window = new Window(this);
        renderer = new Renderer(this);
        input = new Input(this);

        thread = new Thread(this);
        thread.run();
    }

    public void stop(){

    }

    /**
     * Main sim engine loop. Calls updates until they are up do date with real time, renders the Image and updates Window
     */
    public void run(){
        running = true;

        boolean render;
        double firstTime;
        double lastTime = System.nanoTime() / 1_000_000_000.0;
        double passedTime;
        double unprocessedTime = 0;

        double frameTime = 0;

        try {
            while (running) {

                render = false;
                firstTime = System.nanoTime() / 1_000_000_000.0;
                passedTime = firstTime - lastTime;
                lastTime = firstTime;

                unprocessedTime += passedTime;
                frameTime += passedTime;

                while (unprocessedTime >= UPDATE_CAP) {
                    unprocessedTime -= UPDATE_CAP;
                    render = true;


                    sim.update(this, (float) UPDATE_CAP);

                    input.update();

                    if (frameTime >= 1.0) {
                        frameTime = 0;
                    }

                }
                if (render) {
                    renderer.clear();

                    sim.render(this, renderer);

                    //renderer.drawText("FPS: " + fps, 0, 0, 0xff00ffff, 1f);

                    window.update();
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch(Exception e){
            window.showErrorMsg();
        }
    }

    /**
     * Returns width of the Window in pixels
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns height of the Window in pixels
     * @return
     */
    public int getHeight() {
        return height;
    }


    /**
     * Returns scale of the Window
     * @return Scale is best looking when it's a power of two.
     */
    public float getScale() {
        return scale;
    }

    /**
     * Returns title of the Window
     * @return Returns the same title as the one displayed on the Window
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the Window Object
     * @return Window that is responsible for JFrame handling
     */
    public Window getWindow() {
        return window;
    }

    /**
     * Returns Input (Inputs that are only in the main Window)
     * @return
     */
    public Input getInput() {
        return input;
    }
}
