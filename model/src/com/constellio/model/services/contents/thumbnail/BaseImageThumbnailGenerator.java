package com.constellio.model.services.contents.thumbnail;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

abstract class BaseImageThumbnailGenerator implements ImageThumbnailGenerator {

    protected int thumbnailMaxSize;

    private static final int MAX_WIDTH = 2000;
    private static final int MAX_HEIGHT = 2000;

    protected BaseImageThumbnailGenerator(int thumbnailMaxSize) {
        this.thumbnailMaxSize = thumbnailMaxSize;
    }

    BufferedImage readImage(File file) throws Exception {
        return loadImage(file);
    }

    BufferedImage readImage(InputStream inputStream) throws  Exception {
        return loadImage(inputStream);
    }

    private BufferedImage loadImage(Object input) throws Exception {
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(input);

        Iterator iter = ImageIO.getImageReaders(imageInputStream);
        if (!iter.hasNext()) {
            return null;
        }

        ImageReader reader = (ImageReader) iter.next();
        reader.setInput(imageInputStream, true, true);
        int width = reader.getWidth(0);
        int height = reader.getHeight(0);

        ImageReadParam params = reader.getDefaultReadParam();

        // when image is too large, we load a reduced version of it in memory
        if (width > MAX_WIDTH || height > MAX_HEIGHT) {
            int maxWidth = width > MAX_WIDTH ? MAX_WIDTH : width * MAX_HEIGHT / height;
            int maxHeight = width > MAX_WIDTH ? height * MAX_WIDTH / width : MAX_HEIGHT;
            params.setSourceSubsampling(width / maxWidth, height / maxHeight, 0, 0);
        }

        BufferedImage image = reader.read(0, params);
        reader.dispose();

        return image;
    }
}
