package com.constellio.data.utils;

import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
		} catch (IOException ignored) {
		}
		throw new RuntimeException("No image reader found for image : " + file.getName());
	}

	public static BufferedImage resizeWithSubSampling(InputStream inputStream) throws IOException {
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
		Iterator iter = ImageIO.getImageReaders(imageInputStream);
		if (!iter.hasNext()) {
			log.error("No ImageIO reader found");
			return null;
		}
		ImageReader reader = (ImageReader) iter.next();
		reader.setInput(imageInputStream, true, true);

		int width = reader.getWidth(0);
		int height = reader.getHeight(0);
		ImageReadParam params = reader.getDefaultReadParam();

		int newHeight = OVERSIZED_HEIGHT_LIMIT;
		int newWidth = newHeight * width / height;
		params.setSourceSubsampling(width / newWidth, height / newHeight, 0, 0);

		// resulting image will not be exactly newWidth x newHeight but close
		BufferedImage resizedImage = reader.read(0, params);
		reader.dispose();
		imageInputStream.close();

		return resizedImage;
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
