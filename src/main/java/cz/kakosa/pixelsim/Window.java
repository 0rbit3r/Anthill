package cz.kakosa.pixelsim;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

/**
 * Window manager using Jframe to manage the Window and the simulation viewport
 */
public class Window {
    private JFrame frame;
    private BufferedImage image;
    private Canvas canvas;

    private BufferStrategy bs;
    private Graphics g;

    /**
     * Sets up JFrame, Canvas and Graphics
     * @param sc
     */
    public Window(SimContainer sc){
        image = new BufferedImage(sc.getWidth(), sc.getHeight(), BufferedImage.TYPE_INT_RGB);
        canvas = new Canvas();
        Dimension s = new Dimension((int) (sc.getWidth() * sc.getScale()), (int)(sc.getHeight() * sc.getScale()));
        canvas.setPreferredSize(s);
        canvas.setMinimumSize(s);
        canvas.setMaximumSize(s);

        frame = new JFrame(sc.getTitle());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(canvas, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        canvas.createBufferStrategy(2);
        bs = canvas.getBufferStrategy();
        g = bs.getDrawGraphics();

    }

    /**
     * Redraws screen using Rednerer from Image
     */
    public void update(){
        g.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight(), null);
        bs.show();
    }

    /**
     * Returns Canvas (Mainly to get its Listeners
     * @return Canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Returns BufferedImage used by Renderer
     * @return
     */
    public BufferedImage getImage() {
        return image;
    }

    public void showErrorMsg(){
        JOptionPane.showMessageDialog(frame, "Sorry. Something went wrong :-(");
    }
}
