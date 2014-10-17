package net.orzo.data.graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
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
	 * 
	 */
	public void dump(String path) throws IOException {
		for (int i = 0; i < this.images.length; i++) {
			ImageIO.write(this.images[i], "PNG",
					new File(String.format("%s/out-img-%d.png", path, i)));
		}
	}
}
