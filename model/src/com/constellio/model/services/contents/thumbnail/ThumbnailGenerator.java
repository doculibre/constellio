package com.constellio.model.services.contents.thumbnail;

import com.constellio.data.utils.MimeTypes;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.imgscalr.Scalr;

import java.awt.image.BufferedImage;
import java.io.InputStream;

public class ThumbnailGenerator {

    private final ImageThumbnailGenerator imageThumbnailGenerator;

    private static final int THUMBNAIL_WIDTH = 100;

    public ThumbnailGenerator() {
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");

        imageThumbnailGenerator = new JavaImageScalingThumbnailGenerator(THUMBNAIL_WIDTH);
    }

    public BufferedImage generate(InputStream inputStream, String mimeType) throws Exception {
        if (mimeType.startsWith("image/")) {
            return imageThumbnailGenerator.generateThumbnail(inputStream);
        } else if (mimeType.equals(MimeTypes.MIME_APPLICATION_PDF)) {
            return generateThumbnailFromPdf(inputStream);
        }

        throw new UnsupportedOperationException(String.format("Cannot generate thumbnail for mime-type '%s'", mimeType));
    }

    private BufferedImage generateThumbnailFromPdf(InputStream inputStream) throws Exception {
        try (final PDDocument pdfDocument = PDDocument.load(inputStream)) {
            PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);

            BufferedImage pageImage = pdfRenderer.renderImage(0);
            return Scalr.resize(pageImage, THUMBNAIL_WIDTH);
        }
    }

}
