package com.constellio.model.services.contents.thumbnail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public interface ImageThumbnailGenerator {

    public BufferedImage generateThumbnail(File file) throws Exception;

    public BufferedImage generateThumbnail(InputStream inputStream) throws Exception;

}
