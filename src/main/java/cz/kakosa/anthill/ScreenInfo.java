package cz.kakosa.anthill;

import cz.kakosa.anthill.ants.*;
import cz.kakosa.pixelsim.Renderer;

/**
 * Screen info is information about controls and the simulation displayed on the righthand side of the viewport.
 */
public class ScreenInfo {

    Ant selectedAnt = null;

    protected int infobarWidth = 300;


    /**
     * Constructor sets the width of the infobar
     * @param infobar width of the infobar in pixels
     */
    public ScreenInfo(int infobar){
            infobarWidth = infobar;
    }

    /**
     * Renders all the data and controls hints
     * @param sm SimManager object to access the information
     * @param r Renderer that renders the information
     */
    public void render(SimManager sm, Renderer r){
        r.drawFullRectangle(sm.getWindowWidth() - infobarWidth, 0, infobarWidth, sm.getWindowHeight(), 0xff2a2a2a);

        r.drawText(sm.isPaused() ? "ii" : ">", sm.getWindowWidth() - infobarWidth + 20, 20, 0xffffffff,2);
        r.drawText("Speed: " + sm.getSpeed(), sm.getWindowWidth() - infobarWidth + 20, 40, 0xffffffff,2);
        r.drawText("Depth: " + (int)sm.getVisibleLayer(), sm.getWindowWidth() - infobarWidth + 20, 60, 0xffffffff,2);
        r.drawText("Visibility: " + sm.terrain.getVisDepth(), sm.getWindowWidth() - infobarWidth + 20, 80, 0xffffffff,2);
        r.drawText("X-RAY: " + (sm.terrain.isxRay() ? "ON" : "OFF"), sm.getWindowWidth() - infobarWidth + 20, 100, 0xffffffff,2);
        r.drawText("Clicked at: (" + sm.cursorX + ", " + sm.cursorY + ")", sm.getWindowWidth() - infobarWidth + 20, 140, 0xffffffff,2);

        if (selectedAnt != null) writeAntInfo(r, sm);
        else {
            r.drawText("There are currently", sm.getWindowWidth() - infobarWidth + 20, 160, 0xffffffff, 2);
            r.drawText(sm.antManager.getNumofAnts() + " ants", sm.getWindowWidth() - infobarWidth + 20, 180, 0xffffffff,2);
        }

        //Controls
        r.drawText("<Space> - Pause / Play",sm.getWindowWidth() - infobarWidth + 20, sm.getWindowHeight() - 140, 0xffffffff, 2);
        r.drawText("<F, G> - Change speed",sm.getWindowWidth() - infobarWidth + 20, sm.getWindowHeight() - 120, 0xffffffff, 2);
        r.drawText("<Q, A> - Change depth",sm.getWindowWidth() - infobarWidth + 20, sm.getWindowHeight() - 100, 0xffffffff, 2);
        r.drawText("<W, S> - Zoom in / out",sm.getWindowWidth() - infobarWidth + 20, sm.getWindowHeight() - 80, 0xffffffff, 2);
        r.drawText("<Arrows> - Move",sm.getWindowWidth() - infobarWidth + 20, sm.getWindowHeight() - 60, 0xffffffff, 2);
        r.drawText("<E, D> - Change visibility",sm.getWindowWidth() - infobarWidth + 20, sm.getWindowHeight() - 40, 0xffffffff, 2);
        r.drawText("<X> - Toggle X-Ray",sm.getWindowWidth() - infobarWidth + 20, sm.getWindowHeight() - 20, 0xffffffff, 2);
    }

    /**
     * If user clicked on an Ant, this method is called displaying info about the Ant based on its Ant class
     * @param r Renderer that renders the information
     * @param sm SimManager to get the dimensions of the window
     */
    private void writeAntInfo(Renderer r, SimManager sm){
        if (selectedAnt instanceof AntEgg){
            r.drawText("Ant egg", sm.getWindowWidth() - infobarWidth + 20, 160, 0xffffffff,2);
            r.drawText("Food to hatch: " + (((AntEgg)selectedAnt).foodToHatchThresh - ((AntEgg)selectedAnt).foodEaten), sm.getWindowWidth() - infobarWidth + 20, 180, 0xffffffff,2);
            r.drawText("Time to hatch: " + ((AntEgg)selectedAnt).getTimeToHatch(), sm.getWindowWidth() - infobarWidth + 20, 200, 0xffffffff,2);
        }
        else if (selectedAnt instanceof QueenAnt){
            r.drawText("Queen", sm.getWindowWidth() - infobarWidth + 20, 160, 0xffffffff,2);
            r.drawText("Food level: " + ((QueenAnt)selectedAnt).getFoodEaten(), sm.getWindowWidth() - infobarWidth + 20, 180, 0xffffffff,2);
            r.drawText("(New ant cost: " + ((QueenAnt)selectedAnt).foodToLayThresh + ")", sm.getWindowWidth() - infobarWidth + 20, 200, 0xffffffff,2);
        }
        else if (selectedAnt instanceof ExplorerAnt){
            r.drawText("Explorer", sm.getWindowWidth() - infobarWidth + 20, 160, 0xffffffff,2);
            if ((selectedAnt).getCarrying() != null)
                r.drawText("Carrying: " + selectedAnt.getCarrying(), sm.getWindowWidth() - infobarWidth + 20, 180, 0xffffffff,2);
            switch (((ExplorerAnt)selectedAnt).getMode()){
                case 0:
                    r.drawText("Mode: Getting out", sm.getWindowWidth() - infobarWidth + 20, 200, 0xffffffff,2);
                    break;
                case 1:
                    r.drawText("Mode: Finding food", sm.getWindowWidth() - infobarWidth + 20, 200, 0xffffffff,2);
                    break;
                case 2:
                    r.drawText("Mode: Going home", sm.getWindowWidth() - infobarWidth + 20, 200, 0xffffffff,2);
                    break;
                case 3:
                    r.drawText("Mode: Storing food", sm.getWindowWidth() - infobarWidth + 20, 200, 0xffffffff,2);
                    break;
            }
        }

        else if (selectedAnt instanceof MaintainingAnt){
            r.drawText("Maintainer", sm.getWindowWidth() - infobarWidth + 20, 160, 0xffffffff,2);
            if (((MaintainingAnt)selectedAnt).getCarriedEgg() != null)
                r.drawText("Carrying egg: YES", sm.getWindowWidth() - infobarWidth + 20, 180, 0xffffffff,2);
            else
                r.drawText("Carrying egg: NO ", sm.getWindowWidth() - infobarWidth + 20, 180, 0xffffffff,2);
            if (((MaintainingAnt)selectedAnt).isCarryingFood())
                r.drawText("Carrying food: YES", sm.getWindowWidth() - infobarWidth + 20, 200, 0xffffffff,2);
            else
                r.drawText("Carrying food: NO ", sm.getWindowWidth() - infobarWidth + 20, 200, 0xffffffff,2);
            switch (((MaintainingAnt)selectedAnt).getMode()){
                case 0:
                    r.drawText("Mode: going up", sm.getWindowWidth() - infobarWidth + 20, 220, 0xffffffff,2);
                    break;
                case 1:
                    r.drawText("Mode: going down", sm.getWindowWidth() - infobarWidth + 20, 220, 0xffffffff,2);
                    break;
                case 2:
                case 3:
                    r.drawText("Mode: Digging", sm.getWindowWidth() - infobarWidth + 20, 220, 0xffffffff,2);
                    break;
            }
        }
    }

    /**
     * Returns the width of the infobar
     * @return infobar width in pixels
     */
    public int getInfobarWidth() {
        return infobarWidth;
    }

    /**
     * Sets pointer to an ant which information should be displayed
     * @param ant Ant object (can be ExplorerAnt, QueenAnt, AntEgg or MaintainingAnt)
     */
    public void setAnt(Ant ant){
        selectedAnt = ant;
    }
}
