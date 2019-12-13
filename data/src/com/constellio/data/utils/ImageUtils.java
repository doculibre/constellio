package com.constellio.data.utils;

import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
public class ImageUtils {

	private final static int OVERSIZED_HEIGHT_LIMIT = 1080;

	public static Dimension getImageDimension(File file) {
		try (ImageInputStream in = ImageIO.createImageInputStream(file)) {
			if (in != null) {
				final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
				if (readers.hasNext()) {
					ImageReader reader = readers.next();
					try {
						reader.setInput(in);
						return new Dimension(reader.getWidth(0), reader.getHeight(0));
					} finally {
						reader.dispose();
					}
				}
			}
		} catch (IOException e) {
			// swallow it
		}
		throw new RuntimeException("No image reader found for image : " + file.getName());
	}

	public static BufferedImage resize(BufferedImage img) {
		return resize(img, OVERSIZED_HEIGHT_LIMIT);
	}

	public static BufferedImage resize(BufferedImage img, int height) {
		int width = height * img.getWidth() / img.getHeight();
		return Scalr.resize(img, width, height);
	}

	public static boolean isImageOversized(File file) {
		Dimension dimension = getImageDimension(file);
		return dimension.getHeight() > OVERSIZED_HEIGHT_LIMIT;
	}

	public static boolean isImageOversized(double height) {
		return height > OVERSIZED_HEIGHT_LIMIT;
	}

}
