package cz.kakosa.pixelsim;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Image holds image file, with image properties and can read per pixel
 */
public class Image {
    private int width, height;
    private int[] pixels;

    /**
     * Constructor loads image and sets fields
     * @param path points to an image inside resources folder. Starts with /
     */
    public Image(String path){
        BufferedImage image = null;

        try {
            image = ImageIO.read(Image.class.getResourceAsStream((path)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        width = image.getWidth();
        height = image.getHeight();
        pixels = image.getRGB(0,0,width, height, null, 0, width);

        image.flush();
    }

    /**
     * returns width of the image in in pixels
     * @return returns width
     */
    public int getWidth() {
        return width;
    }
    /**
     * returns height of the image in in pixels
     * @return returns height
     */
    public int getHeight() {
        return height;
    }

    /**
     * returns array of pixels
     * @return array is single dimensional array of pixels storet in int format
     */
    public int[] getPixels() {
        return pixels;
    }
}
