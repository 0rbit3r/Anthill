package cz.kakosa.pixelsim;

import java.awt.image.DataBufferInt;

/**
 * Renderer Is the deepest layer of rendering the simulation. It renders single pixels, images, shapes and text.
 */
public class Renderer {

    private int pW, pH;
    private int[] pixels;

    private Font font = Font.DEFAULT;

    /**
     * Consructor sets dimensions and gets reference to pixel array of the Window Viewport
     * @param sc SimContainer calling the constructor
     */
    public Renderer(SimContainer sc){
        pW = sc.getWidth();
        pH= sc.getHeight();
        pixels = ((DataBufferInt)sc.getWindow().getImage().getRaster().getDataBuffer()).getData();
    }

    /**
     * Clears the entire viewport with black color
     */
    public void clear(){
        for (int i = 0; i < pixels.length; i++){
            pixels[i] = 0;
        }
    }

    /**
     * Sets color of a single pixel in viewport
     * @param x X position
     * @param y Y position
     * @param value integer coded color of the pixel (In format 0xff######)
     */
    public void setPixel(int x, int y, int value){
        if ((x < 0 || x >= pW || y < 0 || y >= pH) || value == 0xffff00ff){
            return;
        }
        pixels[x + y * pW] = value;
    }

    /**
     * Types text over the viewport
     * @param text String to write. Should be non null
     * @param offX Horizontal position of the the top-left corner of the text bounding box in pixels
     * @param offY Vertical position of the the top-left corner of the text bounding box in pixels
     * @param color integer coded color of the rectangle (In format 0xff######)
     * @param scale float that scales the image. Not every value works well - best works with powers of 2.
     */
    public void drawText(String text, int offX, int offY, int color, float scale){

        text = text.toUpperCase();
        int offset = 0;

        for (int i = 0; i < text.length(); i++) {
            int unicode = text.codePointAt(i) - 32;

            for (int y = 0; y < (int)(font.getFontImg().getHeight() * scale); y++) {
                for (int x = 0; x < (int)(font.getWidths()[unicode] * scale); x++) {
                    if (font.getFontImg().getPixels()[((int)(x / scale) + font.getOffsets()[unicode]) + (int)(y / scale) * font.getFontImg().getWidth()] == 0xffffffff)
                        setPixel((int)(x + offX + offset), (y + offY), color);
                }
            }
            offset += (int)(font.getWidths()[unicode] * scale);
        }
    }

    /**
     * Draws image on the viewport
     * @param image Image that will be drawn
     * @param offX Horizontal position of the the top-left corner of the image in pixels
     * @param offY Vertical position of the the top-left corner of the image in pixels
     */
    public void drawImage(Image image, int offX, int offY) {

        if (offX < -image.getWidth() || offX >= pW) return;
        if (offY < -image.getHeight() || offY >= pH) return;

        int newX = 0;
        int newY = 0;
        int newWidth = image.getWidth();
        int newHeight = image.getHeight();


        if (offX < 0){ newX -= offX;}
        if (offY < 0){ newY -= offY; }
        if (newWidth + offX > pW){ newWidth -= (newWidth + offX - pW); }
        if (newHeight + offY > pH){ newHeight -= (newHeight + offY - pH); }

        for (int y = newY; y < newHeight; y++) {
            for (int x = newX; x < newWidth; x++) {
                setPixel(x + offX,y + offY,image.getPixels()[x + y * image.getWidth()]);
            }
        }
    }

    /**
     * Draws filled rectangle on the viewPort
     * @param offX Horizontal position of the the top-left corner of the rectangle in pixels
     * @param offY Vertical position of the the top-left corner of the rectangle in pixels
     * @param width Width in pixels
     * @param height Height in pixels
     * @param color integer coded color of the rectangle (In format 0xff######)
     */
    public void drawFullRectangle(int offX, int offY, int width, int height, int color){
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                setPixel(offX + x, offY + y, color);
            }
        }
    }

}
