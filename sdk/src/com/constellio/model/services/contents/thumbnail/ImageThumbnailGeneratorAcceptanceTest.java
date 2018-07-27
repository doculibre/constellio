package com.constellio.model.services.contents.thumbnail;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageThumbnailGeneratorAcceptanceTest extends ConstellioTest {

    private ImagScalrImageThumbnailGenerator imagScalr = new ImagScalrImageThumbnailGenerator(100);
    private ThumbnailatorImageThumbnailGenerator thumbnailator = new ThumbnailatorImageThumbnailGenerator(100);
    private JavaImageScalingThumbnailGenerator javaImageScaling = new JavaImageScalingThumbnailGenerator(100);

    private File jpegFile, bmpFile, tiffFile, gifFile, pngFile, cmykFile, hugeFile, giganticFile;

    private BufferedImage thumbnail;
    private String filename;

    @Before
    public void setUp() throws Exception {
        thumbnail = null;
        filename = null;

        jpegFile = getTestResourceFile("1920x1080.jpg");
        bmpFile = getTestResourceFile("1920x1080.bmp");
        tiffFile = getTestResourceFile("1024x1024.tif");
        gifFile = getTestResourceFile("2560x1600.gif");
        pngFile = getTestResourceFile("2560x1440.png");
        cmykFile = getTestResourceFile("CMYK.jpg");
        hugeFile = getTestResourceFile("4454x3191.jpg");
        giganticFile = getTestResourceFile("8000x8000.jpg");

        // do a conversion to warm-up generators
        imagScalr.generateThumbnail(jpegFile);
        thumbnailator.generateThumbnail(jpegFile);
        javaImageScaling.generateThumbnail(jpegFile);
    }

    @After
    public void cleanUp() throws Exception {
        //ImageIO.write(thumbnail, "png", new File(filename));
    }

    //
    // ImagScalr
    //

    @Test
    public void testJPEGImagScalr() throws Exception {
        benchmark(imagScalr, "ImagScalrWithJPEG.png", jpegFile);
    }

    @Test
    public void testBMPImagScalr() throws Exception {
        benchmark(imagScalr, "ImagScalrWithBMP.png", bmpFile);
    }

    @Test
    public void testTIFFImagScalr() throws Exception {
        benchmark(imagScalr, "ImagScalrWithTIFF.png", tiffFile);
    }

    @Test
    public void testGIFImagScalr() throws Exception {
        benchmark(imagScalr, "ImagScalrWithGIF.png", gifFile);
    }

    @Test
    public void testPNGImagScalr() throws Exception {
        benchmark(imagScalr, "ImagScalrWithPNG.png", pngFile);
    }

    @Test
    public void testCMYKImagScalr() throws Exception {
        benchmark(imagScalr, "ImagScalrWithCMYK.png", cmykFile);
    }

    @Test
    public void testHugeImageScalr() throws Exception {
        benchmark(imagScalr, "ImageScalrWithHuge.png", hugeFile);
    }

    @Test
    public void testGiganticImageScalr() throws Exception {
        benchmark(imagScalr, "ImageScalrWithGigantic.png", giganticFile);
    }

    //
    // Thumbnailator
    //

    @Test
    public void testJPEGThumbnailator() throws Exception {
        benchmark(thumbnailator, "ThumbnailatorWithJPEG.png", jpegFile);
    }

    @Test
    public void testBMPThumbnailator() throws Exception {
        benchmark(thumbnailator, "ThumbnailatorWithBMP.png", bmpFile);
    }

    @Test
    public void testTIFFThumbnailator() throws Exception {
        benchmark(thumbnailator, "ThumbnailatorWithTIFF.png", tiffFile);
    }

    @Test
    public void testGIFThumbnailator() throws Exception {
        benchmark(thumbnailator, "ThumbnailatorWithGIF.png", gifFile);
    }

    @Test
    public void testPNGThumbnailator() throws Exception {
        benchmark(thumbnailator, "ThumbnailatorWithPNG.png", pngFile);
    }

    @Test
    public void testCMYKThumbnailator() throws Exception {
        benchmark(thumbnailator, "ThumbnailatorWithCMYK.png", cmykFile);
    }

    @Test
    public void testHugeThumbnailator() throws Exception {
        benchmark(thumbnailator, "ThumbnailatorWithHuge.png", hugeFile);
    }

    @Test
    public void testGiganticThumbnailator() throws Exception {
        benchmark(thumbnailator, "ThumbnailatorWithGigantic.png", giganticFile);
    }

    //
    // JavaImageScaling
    //

    @Test
    public void testJPEGJavaImageScaling() throws Exception {
        benchmark(javaImageScaling, "JavaImageScalingWithJPEG.png", jpegFile);
    }

    @Test
    public void testBMPJavaImageScaling() throws Exception {
        benchmark(javaImageScaling, "JavaImageScalingWithBMP.png", bmpFile);
    }

    @Test
    public void testTIFFJavaImageScaling() throws Exception {
        benchmark(javaImageScaling, "JavaImageScalingWithTIFF.png", tiffFile);
    }

    @Test
    public void testGIFJavaImageScaling() throws Exception {
        benchmark(javaImageScaling, "JavaImageScalingWithGIF.png", gifFile);
    }

    @Test
    public void testPNGJavaImageScaling() throws Exception {
        benchmark(javaImageScaling, "JavaImageScalingWithPNG.png", pngFile);
    }

    @Test
    public void testCMYKJavaImageScaling() throws Exception {
        benchmark(javaImageScaling, "JavaImageScalingWithCMYK.png", cmykFile);
    }

    @Test
    public void testHugeJavaImageScaling() throws Exception {
        benchmark(javaImageScaling, "JavaImageScalingWithHuge.png", hugeFile);
    }

    @Test
    public void testGiganticJavaImageScaling() throws Exception {
        benchmark(javaImageScaling, "javaImageScalingWithGigantic.png", giganticFile);
    }

    private void benchmark(ImageThumbnailGenerator generator, String filename, File file) throws Exception {
        int tries = 5;

        long sum = 0;
        for (int i = 0; i < tries; i++) {
            long t0 = System.currentTimeMillis();
            this.filename = filename;
            thumbnail = generator.generateThumbnail(file);
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
