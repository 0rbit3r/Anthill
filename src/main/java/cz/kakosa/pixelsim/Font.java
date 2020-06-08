package cz.kakosa.pixelsim;

/**
 * Used as holder for a font with variable length stored in a image file.
 */
public class Font {

    //Default pixel styled font.
    public static final Font DEFAULT = new Font("/font.png");

    private Image fontImg ;
    //offset for each letter
    private int[] offsets;
    //widths of letters
    private int[] widths;

    /**
     * Constructor opens image and loads widths and heights of letters
     * @param path Path to a image file with white text, background
     */
    public Font(String path){
        fontImg = new Image(path);

        offsets = new int[59];
        widths = new int[59];

        int unicode = 0;

        for (int i = 0; i < fontImg.getWidth() ; i++) {
            if (fontImg.getPixels()[i] == 0xff0000ff){
                offsets[unicode] = i;
            }
            if (fontImg.getPixels()[i] == 0xffffff00) {
                widths[unicode] = i - offsets[unicode];
                unicode++;
            }
        }
    }

    /**
     * returns image file to be used in rendering
     * @return Unfiltered or edited Image
     */
    public Image getFontImg() {
        return fontImg;
    }

    /**
     * Returns offsets array
     * @return Offsets have on each unicode position offset of the letter from the left start of image
     */
    public int[] getOffsets() {
        return offsets;
    }
    /**
     * Returns widths array
     * @return widths have on each unicode position width of the letter in pixels
     */
    public int[] getWidths() {
        return widths;
    }
}
