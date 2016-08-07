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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class ImagePyramid {

    /**
     *
     */
    private final BufferedImage[] images;

    /**
     *
     */
    public ImagePyramid(int depth, BufferedImage img) {
        this.images = new BufferedImage[depth];
        this.images[0] = img;
        GaussianBlur blur = new GaussianBlur();
        for (int i = 0; i < depth - 1; i++) {
            this.images[i + 1] = blur.blurImage(img, (i + 1) * 10);
        }
    }

    /**
     * @throws IOException
     */
    public void dump(String path) throws IOException {
        for (int i = 0; i < this.images.length; i++) {
            ImageIO.write(this.images[i], "PNG",
                    new File(String.format("%s/out-img-%d.png", path, i)));
        }
    }
}
