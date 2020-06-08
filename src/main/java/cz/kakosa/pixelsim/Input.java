package cz.kakosa.pixelsim;

import java.awt.event.*;

/**
 * manages all mouse/keyboard inputs
 */
public class Input implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    private SimContainer sc;

    private final int NUM_KEYS = 256;
    private boolean[] keys = new boolean[NUM_KEYS];
    private boolean[] keysLast = new boolean[NUM_KEYS];

    private final int NUM_BUTTONS = 5;
    private boolean[] buttons = new boolean[NUM_BUTTONS];
    private boolean[] buttonsLast = new boolean[NUM_BUTTONS];

    private int mouseX, mouseY;
    private int scroll;

    /**
     * Creates Input instance
     * @param sc SimContainer calling the constructor. Used to add Listeners of Inputs form Simulation window.
     */
    public Input(SimContainer sc){
        this.sc = sc;
        mouseX = 0;
        mouseY = 0;
        scroll = 0;

        sc.getWindow().getCanvas().addKeyListener(this);
        sc.getWindow().getCanvas().addMouseMotionListener(this);
        sc.getWindow().getCanvas().addMouseListener(this);
        sc.getWindow().getCanvas().addMouseWheelListener(this);
    }

    /**
     * Sets current state of pressed keys and buttons. Should be called after every update.
     */
    public void update(){

        scroll = 0;

        for (int i = 0; i < NUM_KEYS; i++) {
            keysLast[i] = keys[i];
        }
        for (int i = 0; i < NUM_BUTTONS; i++) {
            buttonsLast[i] = buttons[i];
        }
    }

    /**
     * Checks is key is pressed down
     * @param keyCode integer keycode of keyboard found in KeyEvent.VK_#
     * @return returns true if button is pressed down, false otherwise
     */
    public boolean isKey(int keyCode){
        return keys[keyCode];
    }

    /**
     * Checks is key has been released at this point in time
     * @param keyCode integer keycode of keyboard found in KeyEvent.VK_#
     * @return returns true if key is not pressed and was pressed in last update, false otherwise
     */
    public boolean isKeyUp(int keyCode){
        return !keys[keyCode] && keysLast[keyCode];
    }

    /**
     * Checks is key has been pushed at this point in time
     * @param keyCode integer keycode of keyboard found in KeyEvent.VK_#
     * @return returns true if key is pressed down and was not so in last update, false otherwise
     */
    public boolean isKeyDown(int keyCode){
        return keys[keyCode] && !keysLast[keyCode];
    }

    /**
     * Checks is button is pressed down
     * @param buttonCode integer code of mouse button found in MouseEvent.#
     * @return returns true if button is pressed down, false otherwise
     */
    public boolean isButton(int buttonCode){
        return buttons[buttonCode];
    }

    /**
     * Checks is button has been released at this point in time
     * @param buttonCode integer code of mouse button found in MouseEvent.#
     * @return returns true if button is not pressed and was pressed in last update, false otherwise
     */
    public boolean isButtonUp(int buttonCode){
        return !buttons[buttonCode] && buttonsLast[buttonCode];
    }

    /**
     * Checks is button has been clicked at this point in time
     * @param buttonCode integer code of mouse button found in MouseEvent.#
     * @return returns true if button is pressed and was not pressed in last update, false otherwise
     */
    public boolean isButtonDown(int buttonCode){
        return buttons[buttonCode] && !buttonsLast[buttonCode];
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        buttons[e.getButton()] = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        buttons[e.getButton()] = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = (int)(e.getX() / sc.getScale()); // + sc.getWindow().getFrame().getBounds());
        mouseY = (int)(e.getY() / sc.getScale());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = (int)(e.getX() / sc.getScale()); // + sc.getWindow().getFrame().getBounds());
        mouseY = (int)(e.getY() / sc.getScale());
    }

    public int getScroll() {
        return scroll;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scroll = e.getWheelRotation();
    }

    /**
     * Returns mouse x coordinates
     * @return integer of number pixels from the left first pixel of the simulation window
     */
    public int getMouseX() {
        return mouseX;
    }

    /**
     * Returns mouse y coordinates
     * @return integer of number pixels from the top first pixel of the simulation window
     */
    public int getMouseY() {
        return mouseY;
    }
}

