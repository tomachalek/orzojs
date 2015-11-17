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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * Simple Gaussian blur filter.
 *
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class GaussianBlur {

    /**
     *
     */
    private static int TYPE_HORIZONTAL = 1;

    /**
     *
     */
    private static int TYPE_VERTICAL = 2;

    /**
     * Generates 1D (vertical or horizontal) Gaussian Blur convolution matrix
     * and returns proper ConvolveOp object.
     *
     * @param radius defines size of the convolution matrix (= 2 * radius + 1)
     * @return
     * @author Jerry Huxtable
     */
    public ConvolveOp getGaussianBlurFilter(int radius, int type) {
        int rows = radius * 2 + 1;
        float[] matrix = new float[rows];
        float sigma = radius / 3;
        float sigma22 = 2 * sigma * sigma;
        float sigmaPi2 = (float) (2 * Math.PI * sigma);
        float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
        float radius2 = radius * radius;
        float total = 0;
        int index = 0;
        for (int row = -radius; row <= radius; row++) {
            float distance = row * row;
            if (distance > radius2) {
                matrix[index] = 0;

            } else {
                matrix[index] = (float) Math.exp(-(distance) / sigma22)
                        / sqrtSigmaPi2;
            }
            total += matrix[index];
            index++;
        }
        for (int i = 0; i < rows; i++) {
            matrix[i] /= total;
        }

        Kernel kernel = null;
        if (type == TYPE_HORIZONTAL) {
            kernel = new Kernel(rows, 1, matrix);

        } else {
            kernel = new Kernel(1, rows, matrix);
        }
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }

    /**
     * Applies Gaussian blur effect with provided radius on an image.
     *
     * @param image  input image
     * @param radius "radius" of convolution matrix
     */
    public BufferedImage blurImage(BufferedImage image, int radius) {
        if (radius <= 0) {
            return image;
        }

        BufferedImage importedImage = importImage(image);
        importedImage = addBordersToImage(importedImage, radius); // to make
        // blur look
        // well on
        // borders

        BufferedImageOp op;
        op = getGaussianBlurFilter(radius, TYPE_HORIZONTAL);
        BufferedImage outImage = new BufferedImage(importedImage.getWidth(),
                importedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        op.filter(importedImage, outImage);

        op = getGaussianBlurFilter(radius, TYPE_VERTICAL);
        BufferedImage outImage2 = new BufferedImage(importedImage.getWidth(),
                importedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        op.filter(outImage, outImage2);
        return outImage2.getSubimage(radius, radius, outImage2.getWidth() - 2
                * radius, outImage.getHeight() - 2 * radius);
    }

    /**
     */
    private BufferedImage importImage(BufferedImage image) {
        BufferedImage originalImage = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = originalImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return originalImage;
    }

    /**
     * Adds borders to an image using its 1px width top, bottom, left, and right
     * borders.
     *
     * @param image       input image
     * @param borderWidth how many pixels to add
     */
    private BufferedImage addBordersToImage(BufferedImage image, int borderWidth) {
        BufferedImage subImg;
        BufferedImage outImage = new BufferedImage(image.getWidth() + 2
                * borderWidth, image.getHeight() + 2 * borderWidth,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = outImage.createGraphics();
        g.drawImage(image, borderWidth, borderWidth, null);

        // top
        subImg = outImage.getSubimage(borderWidth, borderWidth,
                outImage.getWidth() - 2 * borderWidth, 1);
        for (int i = 0; i < borderWidth; i++) {
            g.drawImage(subImg, borderWidth, i, outImage.getWidth() - 2
                    * borderWidth, 1, null);
        }

        // bottom
        subImg = outImage.getSubimage(borderWidth, outImage.getHeight()
                - borderWidth - 1, outImage.getWidth() - 2 * borderWidth, 1);
        for (int i = outImage.getHeight() - borderWidth; i < outImage
                .getHeight(); i++) {
            g.drawImage(subImg, borderWidth, i, outImage.getWidth() - 2
                    * borderWidth, 1, null);
        }

        // left
        subImg = outImage.getSubimage(borderWidth, borderWidth, 1,
                outImage.getHeight() - 2 * borderWidth);
        for (int i = 0; i < borderWidth; i++) {
            g.drawImage(subImg, i, borderWidth, 1, outImage.getHeight() - 2
                    * borderWidth, null);
        }

        // right
        subImg = outImage.getSubimage(outImage.getWidth() - borderWidth - 1,
                borderWidth, 1, outImage.getHeight() - 2 * borderWidth);
        for (int i = outImage.getWidth() - borderWidth; i < outImage.getWidth(); i++) {
            g.drawImage(subImg, i, borderWidth, 1, outImage.getHeight() - 2
                    * borderWidth, null);
        }
        g.dispose(); // just for sure
        return outImage;
    }

}
