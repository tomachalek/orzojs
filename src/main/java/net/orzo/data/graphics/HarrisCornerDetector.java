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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

/**
 * Parts of the code are heavily inspired by the book "Digital Image Processing"
 * by authors Wilhelm Burger & Mark J. Burge. Detector works internally with
 * grayscaled images.
 *
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class HarrisCornerDetector implements FeaturePointDetector {
    /**
     * Presmoothing kernel (Hp)
     */
    private float[] pfilt = {0.223755f, 0.552490f, 0.223755f}; // Hp

    /**
     * Kernel to calculate partial derivatives of presmoothed images (Hdx, Hdy)
     */
    private float[] dfilt = {0.453014f, 0.0f, -0.453014f}; // Hdx, Hdy

    /**
     * Structure matrix smoothing kernel (Hb)
     */
    private float[] bfilt = {0.01563f, 0.09375f, 0.234375f, 0.3125f,
            0.234375f, 0.09375f, 0.01563f}; // Hb

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
     * All points with corner response function's value above the threshold
     * value are considered as a corner (or in more recent terminology
     * 'feature') point. This is the default value.
     */
    public static final int DEFAULT_THRESHOLD = 20000;

    /**
     * Sensitivity of the detector, it's a part of the Q(u,v) = det(M) - alpha
     * (trace(M))^2 equation. Typical values range from 0.04 to 0.06. The
     * greater the value of alpha, the less sensitive detector.
     */
    private float alpha = 0.05f;

    /**
     * Image to be analyzed
     */
    private final BufferedImage image;

    /**
     *
     */
    private List<CornerPoint> points;

    /**
     * Constructs new detector for provided image
     *
     * @param image image to be analyzed
     */
    public HarrisCornerDetector(BufferedImage image) {
        this.image = image;
    }

    /**
     * image getter
     *
     * @return attached image
     */
    public BufferedImage getImage() {
        return this.image;
    }

    /**
     * Creates image object using provided data
     *
     * @
     */
    public BufferedImage createImageFromFloat(float[] data) {
        int[] convertedData = new int[this.image.getWidth()
                * this.image.getHeight()];
        int colorValue;
        for (int i = 0; i < convertedData.length; i++) {
            colorValue = Math.round(data[i]);
            convertedData[i] = ((colorValue << 16) & 0xff0000)
                    + ((colorValue << 8) & 0x00ff00) + (colorValue & 0xff);
        }
        BufferedImage image = new BufferedImage(this.image.getWidth(),
                this.image.getHeight(), BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, this.image.getWidth(), this.image.getHeight(),
                convertedData, 0, this.image.getWidth());
        return image;
    }

    /**
     * Searches for image's feature points using the Harris' corner detection
     * algorithm with provided threshold.
     *
     * @return current instance (provides fluent interface)
     */
    public HarrisCornerDetector analyze(int threshold) {
        float[] dataA = convertImageToFloat();
        float[] dataB = dataA.clone();
        float[] dataC;
        float[] dataQ;

        dataA = convolveHorizontal(dataA, this.image.getWidth(), this.pfilt);
        dataA = convolveHorizontal(dataA, this.image.getWidth(), this.dfilt);

        dataB = convolveVertical(dataB, this.image.getWidth(), this.pfilt);
        dataB = convolveVertical(dataB, this.image.getWidth(), this.dfilt);

        dataC = convolve(multiply(dataA, dataB), this.image.getWidth(),
                this.bfilt);
        dataA = convolve(square(dataA), this.image.getWidth(), this.bfilt);
        dataB = convolve(square(dataB), this.image.getWidth(), this.bfilt);

        dataQ = cornerResponseFunction(dataA, dataB, dataC);
        this.points = detectCorners(dataQ, threshold, 10); // TODO
        Collections.sort(this.points);
        return this;
    }

    /**
     * Analyzes image using default threshold
     */
    public HarrisCornerDetector analyze() {
        return analyze(DEFAULT_THRESHOLD);
    }

    /**
     * Fetches feature points. This method assumes that you called analyze()
     * method before but any subsequent call of fetchPoints() does not require
     * you to call analyze() again. This allows you to make different selections
     * with only one analysis run.
     *
     * @param dmin radius where to select single feature point
     */
    public List<CornerPoint> fetchPoints(int dmin) {
        if (this.points == null) {
            throw new RuntimeException(
                    "Analyze must be run before fetching points.");
        }
        return cleanupCorners(this.points, dmin);
    }

    /**
     * Removes all corners with worse score in provided range of best ones.
     *
     * @param corners a list of all found corners (according to provided threshold)
     * @param dmin    range (in px) in which all worse corners around the best ones
     *                will be removed
     * @return final list of corners
     */
    private List<CornerPoint> cleanupCorners(List<CornerPoint> corners,
                                             double dmin) {
        double dmin2 = dmin * dmin;
        CornerPoint[] cornerArray = corners.toArray(new CornerPoint[corners
                .size()]);
        List<CornerPoint> outCorners = new Vector<>(corners.size());

        for (int i = 0; i < cornerArray.length; i++) {
            if (cornerArray[i] != null) {
                CornerPoint c1 = cornerArray[i];
                outCorners.add(c1);
                for (int j = i + 1; j < cornerArray.length; j++) {
                    if (cornerArray[j] != null) {
                        CornerPoint c2 = cornerArray[j];
                        if (c1.squareDistanceFrom(c2) < dmin2) {
                            cornerArray[j] = null;
                        }
                    }
                }
            }
        }
        return outCorners;
    }

    /**
     * Searches for corners in provided corner response function result array
     * considering the threshold parameter. Also a border of image to be ignored
     * must be specified. Please note that convolution operations always ignore
     * border elements of a processed data (its width is equal to a radius of a
     * kernel) because a complete surrounding of a pixel is needed to calculate
     * its new value. So it is quite wise to ignore these border pixels.
     *
     * @param dataQ     corner response function values
     * @param threshold {@see #threshold}
     * @param border    width (in px) of border to be ignored.
     * @return list of detected corner points
     */
    private List<CornerPoint> detectCorners(final float[] dataQ,
                                            float threshold, int border) {
        List<CornerPoint> cornerPoints = new LinkedList<>();
        int w = this.image.getWidth();
        int h = this.image.getHeight();

        for (int j = border; j < h - border; j++) {
            for (int i = border; i < w - border; i++) {
                float q = dataQ[j * w + i];
                if (q > threshold && isLocalMax(dataQ, i, j)) {
                    cornerPoints.add(new CornerPoint(i, j, q));
                }
            }
        }
        return cornerPoints;
    }

    /**
     * Converts image to a greyscale one and returns it as an array of float
     * values.
     *
     * @return floating point values representing values of gray
     */
    private float[] convertImageToFloat() {
        int[] data = new int[this.image.getWidth() * this.image.getHeight()];
        float[] convertedData = new float[this.image.getWidth()
                * this.image.getHeight()];
        this.image.getRGB(0, 0, this.image.getWidth(), this.image.getHeight(),
                data, 0, this.image.getWidth());
        for (int i = 0; i < data.length; i++) {
            convertedData[i] = (float) ((data[i] & 0xff) * BLUE_WEIGHT
                    + ((data[i] >> 8) & 0xff) * GREEN_WEIGHT + ((data[i] >> 16) & 0xff)
                    * RED_WEIGHT);
        }
        return convertedData;
    }

    /**
     * Horizontal convolution of provided image data (width of encoded image
     * must be specified via the dataLineWidth parameter) and a kernel.
     *
     * @param data          image data (grayscale)
     * @param dataLineWidth width of image represented by the data parameter
     * @param kernel        vector
     * @return converted data (it is a new object, not provided data parameter)
     */
    private float[] convolveHorizontal(final float[] data, int dataLineWidth,
                                       float[] kernel) {
        int radius = calculateKernelRadius(kernel);
        float[] outData = new float[data.length];
        for (int i = 0; i < data.length; i += dataLineWidth) {
            for (int j = radius; j < dataLineWidth - radius; j++) {
                float sum = 0;
                for (int k = -radius; k <= radius; k++) {
                    sum += kernel[radius + k] * data[i + j + k];
                }
                outData[i + j] = sum;
            }
        }
        return outData;
    }

    /**
     * Vertical convolution of provided image data (width of encoded image must
     * be specified via the dataLineWidth parameter) and a kernel.
     *
     * @param data          image data (grayscale)
     * @param dataLineWidth width of image represented by the data parameter
     * @param kernel        vector
     * @return converted data (it is a new object, not provided data parameter)
     */
    private float[] convolveVertical(float[] data, int dataLineWidth,
                                     float[] kernel) {
        int radius = calculateKernelRadius(kernel);
        float[] outData = new float[data.length];
        int imageHeight = data.length / dataLineWidth;
        for (int i = 0; i < dataLineWidth; i++) {
            for (int j = radius; j < imageHeight - radius; j++) {
                float sum = 0;
                for (int k = -radius; k <= radius; k++) {
                    sum += kernel[radius + k]
                            * data[i + j * dataLineWidth + k * dataLineWidth];
                }
                outData[i + j * dataLineWidth] = sum;
            }
        }
        return outData;
    }

    /**
     * Calculates corner response function value (as explained in the Digital
     * Image Processing book, pages 140-141).
     *
     * @param dataA the I^2_x(u, v) matrix
     * @param dataB the I^2_y(u, v) matrix
     * @param dataC the I_x(u, v)I_y(u, v) matrix
     * @return array of corner response function values
     */
    private float[] cornerResponseFunction(final float[] dataA,
                                           final float[] dataB, final float[] dataC) {
        float[] dataQ = new float[dataA.length];
        for (int i = 0; i < this.image.getHeight(); i++) {
            for (int j = 0; j < this.image.getWidth(); j++) {
                int idx = i * this.image.getWidth() + j;
                float a = dataA[idx];
                float b = dataB[idx];
                float c = dataC[idx];
                float det = a * b - c * c;
                float trace = a + b;
                dataQ[idx] = det - this.alpha * trace * trace;
            }
        }
        return dataQ;
    }

    /**
     * Just a helper function to calcuate kernel radius from provided vector
     * width. It throws an exception if data length is invalid.
     *
     * @param data kernel vector
     * @throws IllegalArgumentException
     */
    private int calculateKernelRadius(float[] data) {
        int radius = (data.length - 1) / 2;
        if (radius * 2 + 1 != data.length) {
            throw new IllegalArgumentException(
                    "Kernel width must satisfy equation (data.length - 1) % 2 == 0");
        }
        return radius;
    }

    /**
     * Multiplies two arrays in following manner: y[i] = x1[i] * x2[i]
     *
     * @param data1 first multiplicand
     * @param data2 second multiplicand
     * @return new array containing the result
     */
    private float[] multiply(final float[] data1, final float[] data2) {
        float[] outData = new float[data1.length];
        for (int i = 0; i < outData.length; i++) {
            outData[i] = data1[i] * data2[i];
        }
        return outData;
    }

    /**
     * Calculates a "squre" of an array in following manner: y[i] = x[i]*x[i]
     *
     * @param data input array
     * @return new array containing the result
     */
    private float[] square(final float[] data) {
        float[] outData = new float[data.length];
        for (int i = 0; i < outData.length; i++) {
            outData[i] = data[i] * data[i];
        }
        return outData;
    }

    /**
     * Creates both vertical and horizontal convolutions using this class'
     * methods. Applicable for xy-separable convolutions here.
     *
     * @param data          input image data (grayscaled)
     * @param dataLineWidth width of input image
     * @param kernel        convolution kernel data
     * @return new array containing filtered image
     */
    private float[] convolve(final float[] data, int dataLineWidth,
                             float[] kernel) {
        return convolveHorizontal(
                convolveVertical(data, dataLineWidth, kernel), dataLineWidth,
                kernel);
    }

    /**
     * Used when testing corner response function values - tests whether the
     * pixel specified by i,j coordinates has greater value than its immediate
     * surrounding.
     *
     * @param i the x coordinate
     * @param j the y coordinate
     * @return true if data[i,j] is local maximum else false
     */
    private boolean isLocalMax(float[] data, int i, int j) {
        int w = this.image.getWidth();
        int h = this.image.getHeight();

        if (i <= 0 || i >= w - 1 || j <= 0 || j >= h - 1) {
            return false;

        } else {
            int i0 = (j - 1) * w + i;
            int i1 = (j * w + i);
            int i2 = (j + 1) * w + i;
            float cp = data[i1];
            return cp > data[i0 - 1] && cp > data[i0] && cp > data[i0 + 1]
                    && cp > data[i1 - 1] && cp > data[i1 + 1]
                    && cp > data[i2 - 1] && cp > data[i2] && cp > data[i2 + 1];
        }
    }

    /**
     *
     */
    public static void main(String[] args) throws IOException {
        GreyscalePicture p = GreyscalePicture.load("d:/work/data/images/scarabeo.jpg");
        BufferedImage img = p.getImage(); // TODO possible null pointer except.
        HarrisCornerDetector detector = new HarrisCornerDetector(img);
        List<CornerPoint> cpList = detector.analyze().fetchPoints(10);
        System.out.println("num points: " + cpList.size());
        Graphics2D g = img.createGraphics();
        for (CornerPoint cp : cpList) {
            cp.draw(g);
        }
        ImageIO.write(img, "png", new File(
                "d:/work/data/images/scarabeo-xxx.jpg"));
    }

}
