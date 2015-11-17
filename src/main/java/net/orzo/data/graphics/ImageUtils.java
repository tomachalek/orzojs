/*
 * Copyright (C) 2010 Tomas Machalek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.orzo.data.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class ImageUtils {

    /**
     * Creates BufferedImage from array of int color values (as obtained by
     * {@link BufferedImage#getRGB(int, int, int, int, int[], int, int)} (or
     * {@link BufferedImage#getRGB(int, int)})
     *
     * @param width  width of image in pixels
     * @param height height of image in pixels
     * @param pixels pixel data itself
     * @return image from provided pixels
     */
    public static BufferedImage createImageFromPixels(int width, int height,
                                                      int[] pixels) {
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        MemoryImageSource ims = new MemoryImageSource(width, height, pixels, 0,
                width);

        Image im = Toolkit.getDefaultToolkit().createImage(ims);
        image.getGraphics().drawImage(im, 0, 0, null);
        return image;
    }

    /**
     * Converts RGB color information integer into an awt's Color object
     *
     * @param color RGB color as integer
     * @return color object created from proper bytes representing R, G, B
     */
    public static Color intToColor(int color) {
        return new Color((color >>> 16) & 0xff, (color >>> 8) & 0xff,
                color & 0xff);
    }

    /**
     * Converts array of integers into array of as bytes with the same data
     *
     * @return array of bytes
     */
    public static byte[] intArrayToByteArray(final int[] integer)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        for (int i = 0; i < integer.length; i++) {
            dos.writeInt(integer[i]);
        }
        byte[] out = bos.toByteArray();
        dos.close();
        bos.close();
        return out;
    }

    /**
     *
     */
    public static BufferedImage scaleImage(BufferedImage image,
                                           double scaleFactor) {
        int newW = (int) (image.getWidth() * scaleFactor);
        int newH = (int) (image.getHeight() * scaleFactor);
        BufferedImage outImage = new BufferedImage(newW, newH,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) outImage.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(image, 0, 0, newW, newH, null);
        return outImage;
    }

    /**
     * Creates resized version of image
     *
     * @param width  requested width in pixels
     * @param height requested height in pixels
     * @return resized image
     */
    public static BufferedImage resize(BufferedImage image, int width,
                                       int height) {
        double scaleX = (double) width / (double) image.getWidth();
        double scaleY = (double) height / (double) image.getHeight();
        // double newYByX = scaleX * this.image.getHeight(); // TODO
        // double newXByY = scaleY * this.image.getWidth();
        BufferedImageOp op = new AffineTransformOp(
                AffineTransform.getScaleInstance(scaleX, scaleY),
                new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC));
        BufferedImage dst = op.filter(image, null);
        return dst;
    }

    /**
     * @todo
     */
    public void drawHistogram(int[] histogram, String path) throws IOException {
        BufferedImage img = new BufferedImage(640, 480,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setPaint(Color.white);
        g2d.fillRect(0, 0, 640, 480);
        g2d.setPaint(Color.black);
        for (int i = 0; i < histogram.length; i++) {
            g2d.drawLine(i, 480, i, 480 - histogram[i] * 100);
        }
        g2d.dispose();
        ImageIO.write(img, "PNG", new File(path));
    }

}
