package com.constellio.model.services.contents.thumbnail;

import org.imgscalr.Scalr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class ImagScalrImageThumbnailGenerator extends BaseImageThumbnailGenerator {

    public ImagScalrImageThumbnailGenerator(int thumbnailMaxSize) {
        super(thumbnailMaxSize);
    }

    @Override
    public BufferedImage generateThumbnail(File file) throws Exception {
        BufferedImage srcImage = readImage(file);
        return Scalr.resize(srcImage, thumbnailMaxSize);
    }

    @Override
    public BufferedImage generateThumbnail(InputStream inputStream) throws Exception {
        BufferedImage srcImage = readImage(inputStream);
        return Scalr.resize(srcImage, thumbnailMaxSize);
    }
}
