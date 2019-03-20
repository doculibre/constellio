package com.constellio.model.services.contents.thumbnail;

import com.mortennobel.imagescaling.DimensionConstrain;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class JavaImageScalingThumbnailGenerator extends BaseImageThumbnailGenerator {

    public JavaImageScalingThumbnailGenerator(int thumbnailMaxSize) {
        super(thumbnailMaxSize);
    }

    @Override
    public BufferedImage generateThumbnail(File file) throws Exception {
        BufferedImage sourceImage = readImage(file);
        return generateThumbnail(sourceImage);
    }

    @Override
    public BufferedImage generateThumbnail(InputStream inputStream) throws Exception {
        BufferedImage sourceImage = readImage(inputStream);
        return generateThumbnail(sourceImage);
    }

    private BufferedImage generateThumbnail(BufferedImage sourceImage) throws Exception {
        ResampleOp resampleOp = new ResampleOp(DimensionConstrain.createMaxDimension(thumbnailMaxSize, thumbnailMaxSize));
        resampleOp.setFilter(ResampleFilters.getLanczos3Filter());
        //resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Normal);
        return resampleOp.filter(sourceImage, null);
    }
}
