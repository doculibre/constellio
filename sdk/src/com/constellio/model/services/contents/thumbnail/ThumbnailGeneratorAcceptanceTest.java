package com.constellio.model.services.contents.thumbnail;

import com.constellio.data.utils.MimeTypes;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ThumbnailGeneratorAcceptanceTest extends ConstellioTest {

    private ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();

    private File pdf1File, pdf1RoatedFile, pdf2File, image1File;

    private BufferedImage thumbnail;
    private String filename;

    @Before
    public void setUp() throws Exception {
        thumbnail = null;
        filename = null;

        pdf1File = getTestResourceFile("pdf1.pdf");
        pdf1RoatedFile = getTestResourceFile("pdf1-rotated.pdf");
        pdf2File = getTestResourceFile("pdf2.pdf");

        image1File = getTestResourceFile("image1.jpg");

        // warm-up for better benchmarks results
        thumbnailGenerator.generate(new FileInputStream(pdf1File), MimeTypes.MIME_APPLICATION_PDF);
    }

    @Test
    public void testGenerateThumbnailFromPdf1() throws Exception {
        benchmark("Pdf1Thumbnail.png", pdf1File, MimeTypes.MIME_APPLICATION_PDF);
    }

    @Test
    public void testGenerateThumbnailFromPdf2() throws Exception {
        benchmark("Pdf2Thumbnail.png", pdf2File, MimeTypes.MIME_APPLICATION_PDF);
    }

    @Test
    public void testCheckThatThumbnailAlwaysHaveSameSizeForAFile() throws Exception {
        BufferedImage thumbnail1 = thumbnailGenerator.generate(new FileInputStream(pdf1File), MimeTypes.MIME_APPLICATION_PDF);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ImageIO.write(thumbnail1, "png", out1);

        BufferedImage thumbnail2 = thumbnailGenerator.generate(new FileInputStream(pdf1File), MimeTypes.MIME_APPLICATION_PDF);
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ImageIO.write(thumbnail2, "png", out2);

        assertThat(out1.toByteArray().length).isEqualTo(out2.toByteArray().length);

        BufferedImage thumbnail3 = thumbnailGenerator.generate(new FileInputStream(image1File), MimeTypes.MIME_IMAGE_JPEG);
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        ImageIO.write(thumbnail3, "png", out1);

        BufferedImage thumbnail4 = thumbnailGenerator.generate(new FileInputStream(image1File), MimeTypes.MIME_IMAGE_JPEG);
        ByteArrayOutputStream out4 = new ByteArrayOutputStream();
        ImageIO.write(thumbnail4, "png", out2);

        assertThat(out3.toByteArray().length).isEqualTo(out4.toByteArray().length);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testUnsupportedMimeTypeThrowsException() throws Exception {
        thumbnailGenerator.generate(
                new FileInputStream(newTempFileWithContent("test.txt", "Test")), MimeTypes.MIME_TEXT_PLAIN);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testNonExistingMimeTypeThrowsException() throws Exception {
        thumbnailGenerator.generate(new FileInputStream(image1File), "images/jpeg");
    }

    @Test
    public void testPdfThumbnailHasCorrectMaximumSize() throws Exception {
        BufferedImage thumbnail = thumbnailGenerator.generate(new FileInputStream(pdf1File), MimeTypes.MIME_APPLICATION_PDF);
        assertThat(thumbnail.getWidth() < 100).isTrue();
        assertThat(thumbnail.getHeight() == 100).isTrue();

        thumbnail = thumbnailGenerator.generate(new FileInputStream(pdf1RoatedFile), MimeTypes.MIME_APPLICATION_PDF);
        assertThat(thumbnail.getWidth() == 100).isTrue();
        assertThat(thumbnail.getHeight() < 100).isTrue();
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

            //ImageIO.write(thumbnail, "png", new File(filename));
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
