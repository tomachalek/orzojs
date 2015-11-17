package net.orzo.data.graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This class represents a grayscale image and provides some convenient data
 * access functions. There are no data modification functions available.
 *
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class GreyscalePicture {

    /**
     * Weight used when converting color image to a grayscaled one (BT.709
     * compliant)
     */
    private static final double RED_WEIGHT = 0.2125;

    /**
     * Weight used when converting color image to a grayscaled one (BT.709
     * compliant)
     */
    private static final double GREEN_WEIGHT = 0.7154;

    /**
     * Weight used when converting color image to a grayscaled one (BT.709
     * compliant)
     */
    private static final double BLUE_WEIGHT = 0.072;

    /**
     *
     */
    private final BufferedImage image;

    /**
     * @param image
     */
    public GreyscalePicture(BufferedImage image) {
        this.image = image;
    }

    /**
     * Returns wrapped BufferedImage
     *
     * @return
     */
    public BufferedImage getImage() {
        return this.image;
    }

    /**
     * @return
     */
    public int getWidth() {
        return this.image.getWidth();
    }

    /**
     * @return
     */
    public int getHeight() {
        return this.image.getHeight();
    }

    /**
     * @param path
     * @return
     * @throws IOException
     */
    public static GreyscalePicture load(String path) throws IOException {
        BufferedImage img = ImageIO.read(new File(path));
        if (img != null) {
            return new GreyscalePicture(img);
        }
        return null;
    }

    /**
     * @param img
     * @return
     */
    public static GreyscalePicture fromColorImage(BufferedImage img) {
        return new GreyscalePicture(convertToGrayscale(img));
    }

    /**
     * Creates image object using provided data
     *
     * @
     */
    public static BufferedImage fromArray(float[] data, int width, int height) {
        int[] convertedData = new int[width * height];
        int colorValue;
        for (int i = 0; i < convertedData.length; i++) {
            colorValue = Math.round(data[i]);
            convertedData[i] = ((colorValue << 16) & 0xff0000)
                    + ((colorValue << 8) & 0x00ff00) + (colorValue & 0xff);
        }
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, convertedData, 0, width);
        return image;
    }

    /**
     *
     */
    public static BufferedImage convertToGrayscale(BufferedImage image) {
        int[] data = new int[image.getWidth() * image.getHeight()];
        float[] convertedData = new float[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), data, 0,
                image.getWidth());
        for (int i = 0; i < data.length; i++) {
            convertedData[i] = (float) ((data[i] & 0xff) * BLUE_WEIGHT
                    + ((data[i] >> 8) & 0xff) * GREEN_WEIGHT + ((data[i] >> 16) & 0xff)
                    * RED_WEIGHT);
        }
        return fromArray(convertedData, image.getWidth(), image.getHeight());
    }

    /**
     * Generates 256-bin histogram of used grey shades. Values range from 0 to
     * 1.
     *
     * @param image
     * @return
     */
    public double[] histogram() {
        int[][] data = toArray();
        double[] ans = new double[256];
        int totalSize = data.length * data[0].length;

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                ans[data[i][j]]++;
            }
        }
        for (int i = 0; i < ans.length; i++) {
            ans[i] = ans[i] / totalSize;
        }
        return ans;
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public int[] areaToVector(double x, double y, double width, double height) {
        int ix = (int) Math.round(x);
        int iy = (int) Math.round(y);
        int iw = (int) Math.round(width);
        int ih = (int) Math.round(height);
        int[] rgbs = new int[iw * ih];
        this.image.getRGB(ix, iy, iw, ih, rgbs, 0, iw);
        for (int i = 0; i < rgbs.length; i++) {
            rgbs[i] = rgbs[i] & 0xff;
        }
        return rgbs;
    }

    /**
     * Generates 256-bin histogram of used grey shades in the specified
     * rectangular area [x, y] - [x + width, y + height]. Values range from 0 to
     * 1.
     *
     */
    public double[] areaHistogram(double x, double y, double width,
                                  double height) {
        int[] data = areaToVector(x, y, width, height);
        double[] ans = new double[256];
        int totalSize = data.length;

        for (int i = 0; i < data.length; i++) {
            ans[data[i]] += 1.0 / totalSize;
        }
        return ans;
    }

    /**

     */
    public int[][] toArray() {
        int[][] ans = new int[this.image.getHeight()][this.image.getWidth()];
        for (int i = 0; i < this.image.getHeight(); i++) {
            for (int j = 0; j < this.image.getWidth(); j++) {
                ans[i][j] = (this.image.getRGB(j, i) & 0x0000ff);
            }
        }
        return ans;
    }

}
