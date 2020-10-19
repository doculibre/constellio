package com.constellio.model.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;

public class FileExifUtils implements java.io.Serializable {

	private static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FileExifUtils.class);

	public static void correctRotationOnImage(File file) {

		String type = URLConnection.guessContentTypeFromName(file.getName());
		correctRotationOnImage(file, type);
	}

	public static void correctRotationOnImage(File file, String type) {

		try {
			if (type != null && type.contains("jpeg")) {
				ImageInformation imageInformation = readImageInformation(file);
				BufferedImage bufferedImage = ImageIO.read(new FileInputStream(file));
				BufferedImage image = transformImage(bufferedImage, getExifTransformation(imageInformation));
				ImageIO.write(image, "jpeg", new File(file.getPath()));
			}
		} catch (MetadataException e) {
			LOGGER.warn("Image metadata could not be read");
		} catch (FileNotFoundException e) {
			LOGGER.warn("File not found during image correction");
		} catch (IOException e) {
			LOGGER.warn("Image could not be opened during image correction");
		} catch (ImageProcessingException e) {
			LOGGER.warn("Image could not be Processed or rendered");
		} catch (EmptyJpegDirectoryException e) {
			LOGGER.warn("Image has no jpeg directory");
		}

	}

	public static ImageInformation readImageInformation(java.io.File inputStream)
			throws IOException, MetadataException, ImageProcessingException, EmptyJpegDirectoryException {
		Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
		Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);

		int orientation = 1;
		try {
			if (directory != null) {
				orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
			}
		} catch (MetadataException me) {
			LOGGER.warn("Could not get orientation");
		}
		if (jpegDirectory == null) {
			throw new EmptyJpegDirectoryException("Jpeg directory empty");
		}
		int width = jpegDirectory.getImageWidth();
		int height = jpegDirectory.getImageHeight();

		return new ImageInformation(orientation, width, height);
	}

	public static AffineTransform getExifTransformation(
			ImageInformation info) {

		AffineTransform t = new AffineTransform();

		switch (info.orientation) {
			case 1:
				break;
			case 2: // Flip X
				t.scale(-1.0, 1.0);
				t.translate(-info.width, 0);
				break;
			case 3: // PI rotation
				t.translate(info.width, info.height);
				t.rotate(Math.PI);
				break;
			case 4: // Flip Y
				t.scale(1.0, -1.0);
				t.translate(0, -info.height);
				break;
			case 5: // - PI/2 and Flip X
				t.rotate(-Math.PI / 2);
				t.scale(-1.0, 1.0);
				break;
			case 6: // -PI/2 and -width
				t.translate(info.height, 0);
				t.rotate(Math.PI / 2);
				break;
			case 7: // PI/2 and Flip
				t.scale(-1.0, 1.0);
				t.translate(-info.height, 0);
				t.translate(0, info.width);
				t.rotate(3 * Math.PI / 2);
				break;
			case 8: // PI / 2
				t.translate(0, info.width);
				t.rotate(3 * Math.PI / 2);
				break;
		}

		return t;
	}

	private static BufferedImage transformImage(BufferedImage bsrc, AffineTransform at) {
		BufferedImage bdest = new BufferedImage(bsrc.getWidth(), bsrc.getHeight(), bsrc.getType());
		Graphics2D g = bdest.createGraphics();
		g.drawRenderedImage(bsrc, at);
		return bdest.getSubimage(0, 0, bsrc.getWidth(), bsrc.getHeight());
	}

	public static class ImageInformation {
		public final int orientation;
		public final int width;
		public final int height;

		public ImageInformation(int orientation, int width, int height) {
			this.orientation = orientation;
			this.width = width;
			this.height = height;
		}

		public String toString() {
			return String.format("%dx%d,%d", this.width, this.height, this.orientation);
		}
	}

	private static class EmptyJpegDirectoryException extends Exception {

		public EmptyJpegDirectoryException(String errorMessage) {
			super(errorMessage);
		}
	}
}