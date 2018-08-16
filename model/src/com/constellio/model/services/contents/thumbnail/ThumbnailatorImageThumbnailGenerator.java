package com.constellio.model.services.contents.thumbnail;

import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class ThumbnailatorImageThumbnailGenerator extends BaseImageThumbnailGenerator {

    public ThumbnailatorImageThumbnailGenerator(int thumbnailMaxSize) {
        super(thumbnailMaxSize);
    }

    @Override
    public BufferedImage generateThumbnail(File file) throws Exception {
        return Thumbnails.of(readImage(file)).size(thumbnailMaxSize, thumbnailMaxSize).asBufferedImage();
    }

    @Override
    public BufferedImage generateThumbnail(InputStream inputStream) throws Exception {
        return Thumbnails.of(readImage(inputStream)).size(thumbnailMaxSize, thumbnailMaxSize).asBufferedImage();
    }
}
