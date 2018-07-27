package com.constellio.model.services.contents.thumbnail;

import com.constellio.model.utils.MimeTypes;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ThumbnailGeneratorAcceptanceTest extends ConstellioTest {

    private ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();

    private File pdf1File, pdf2File;

    private BufferedImage thumbnail;
    private String filename;

    @Before
    public void setUp() throws Exception {
        thumbnail = null;
        filename = null;

        pdf1File = getTestResourceFile("pdf1.pdf");
        pdf2File = getTestResourceFile("pdf2.pdf");

        thumbnailGenerator.generate(new FileInputStream(pdf1File), MimeTypes.MIME_APPLICATION_PDF);
    }

    @After
    public void cleanUp() throws Exception {
        //ImageIO.write(thumbnail, "png", new File(filename));
    }

    @Test
    public void testGenerateThumbnailFromPdf1() throws Exception {
        benchmark("Pdf1Thumbnail.png", pdf1File, MimeTypes.MIME_APPLICATION_PDF);
    }

    @Test
    public void testGenerateThumbnailFromPdf2() throws Exception {
        benchmark("Pdf2Thumbnail.png", pdf2File, MimeTypes.MIME_APPLICATION_PDF);
    }

    private void benchmark(String filename, File file, String mimeType) throws Exception {
        int tries = 5;

        long sum = 0;
        for (int i = 0; i < tries; i++) {
            InputStream stream = new FileInputStream(file);

            long t0 = System.currentTimeMillis();
            this.filename = filename;
            thumbnail = thumbnailGenerator.generate(stream, mimeType);
            sum += time(t0);
        }
        average(sum, tries);
    }

    private long time(long start) {
        long time = System.currentTimeMillis() - start;
        System.out.println(String.format("Generated in %d ms", time));
        return time;
    }

    private void average(long sum, int tries) {
        System.out.println(String.format("Average %d ms", sum / tries));
    }
}
