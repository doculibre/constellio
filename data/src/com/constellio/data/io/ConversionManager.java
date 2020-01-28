package com.constellio.data.io;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.extensions.extensions.configManager.ExtensionConverter;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImageUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.util.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.jodconverter.LocalConverter;
import org.jodconverter.OfficeDocumentConverter;
import org.jodconverter.OnlineConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFamily;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.job.AbstractConverter;
import org.jodconverter.office.LocalOfficeManager;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.OnlineOfficeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.io.File.createTempFile;
import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Using https://github.com/sbraconnier/jodconverter
 *
 * @author Vincent
 */
public class ConversionManager implements StatefulService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConversionManager.class);

	private static final long CONVERSION_TIMEOUT = 300000L;

	/*
	 * In addition to OpenDocument formats (.odt, .ott, .oth, and .odm), Writer 2.x can open the formats used by OOo 1.x (.sxw, .stw, and .sxg) and the following text document formats:
	 *   Microsoft Word 6.0/95/97/2000/XP) (.doc and .dot)	WordPerfect Document (.wpd)
	 *   Microsoft Word 2003 XML (.xml)	WPS 2000/Office 1.0 (.wps)
	 *   Microsoft WinWord 5 (.doc)	DocBook (.xml)
	 *   StarWriter formats (.sdw, .sgl, and .vor)	Ichitaro 8/9/10/11 (.jtd and .jtt)
	 *   AportisDoc (Palm) (.pdb)	Hangul WP 97 (.hwp)
	 *   Pocket Word (.psw)	.rtf, .txt, and .csv
	 *   When opening .htm or .html files (used for web pages), OpenOffice.org customizes Writer for working with these files.
	 *
	 * In addition to OpenDocument formats (.odt and .ott), Writer 2.x can save in these formats:
	 *   OpenOffice.org 1.x Text Document(.sxw)
	 *   OpenOffice.org 1.x Text Document Template (.stw)
	 *   Microsoft Word 6.0, 95, and 97/2000/XP (.doc)
	 *   Microsoft Word 2003 XML (.xml)
	 *   Rich Text Format (.rtf)
	 *   StarWriter 3.0, 4.0, and 5.0 (.sdw)
	 *   StarWriter 3.0, 4.0, and 5.0 Template (.vor)
	 *   Text (.txt)
	 *   Text Encoded (.txt)
	 *   HTML Document (OpenOffice.org Writer) (.html and .htm)
	 *   DocBook (.xml)
	 *   AportisDoc (Palm) (.pdb)
	 *   Pocket Word (.psw)
	 */
	public static final String[] TEXT_EXTENSIONS = {
			"odt", "ott", "oth", "odm", "sxw", "stw", "sxg", "doc", "docx", "dot",
			"wpd", "xml", "wps", "sdw", "sgl", "jtd", "jtt", "pdb", "hwp", "psw",
			"rtf", "txt", "htm", "html"
	};

	/*
	 * In addition to OpenDocument formats (.ods and .ots), Calc 2.x can open the formats used by OOo 1.x (.sxc and .stc) and the following spreadsheet formats:
	 *   Microsoft Excel 97/2000/XP (.xls, .xlw, and .xlt)    	Rich Text Format (.rtf)
	 *   Microsoft Excel 4.x-5.0/95 (.xls, .xlw, and .xlt)	Text CSV (.csv and .txt)
	 *   Microsoft Excel 2003 XML (.xml)	Lotus 1-2-3 (.wk1, .wks, and .123)
	 *   Data Interchange Format (.dif)	StarCalc formats (.sdc and .vor)
	 *   dBase (.dbf)	SYLK (.slk)
	 *   .htm and .html files including Web page queries	Pocket Excel (pxl)
	 *   Quattro Pro 6.0 (.wb2)
	 *
	 * In addition to OpenDocument formats (.ods and .ots), Calc 2.x can save in these formats:
	 *   OpenOffice.org 1.x Spreadsheet (.sxc)
	 *   OpenOffice.org 1.x Spreadsheet Template (.stc)
	 *   Microsoft Excel 97/2000/XP (.xls and .xlw)
	 *   Microsoft Excel 97/2000/XP Template (.xlt)
	 *   Microsoft Excel 5.0 and 95 (.xls and .xlw)
	 *   Microsoft Excel 2003 XML (.xml)
	 *   Data Interchange Format (.dif)
	 *   dBase (.dbf)
	 *   SYLK (.slk)
	 *   Text CSV (.csv and .txt)
	 *   StarCalc 3.0, 4.0, and 5.0 formats (.sdc and .vor)
	 *   HTML Document (OpenOffice.org Calc) (.html and .htm)
	 *   Pocket Excel (.pxl)
	 */
	public static final String[] SPREADSHEET_EXTENSIONS = {
			"ods", "ots", "sxc", "stc", "xls", "xlw", "xlt", "xlsx", "xltx", "csv", "wk1",
			"wks", "123", "dif", "sdc", "vor", "dbf", "slk", "pxl", "wb2"
	};

	/*
	 * In addition to OpenDocument formats (.odp, .odg, and .otp), Impress 2.x can open the formats used by OOo 1.x (.sxi and .sti) and the following presentation formats:
	 *   Microsoft PowerPoint 97/2000/XP (.ppt, .pps, and .pot)
	 *   StarDraw and StarImpress (.sda, .sdd, .sdp, and .vor)
	 *   CGM - Computer Graphics Metafile (.cgm).
	 *
	 * In addition to OpenDocument formats (.odp, .otp, and .odg), Impress 2.x can save in these formats:
	 *   OpenOffice.org 1.x Presentation (.sxi)
	 *   OpenOffice.org 1.x Presentation Template (.sti)
	 *   Microsoft PowerPoint 97/2000/XP (.ppt and .pps)
	 *   Microsoft PowerPoint 97/2000/XP Template (.pot)
	 *   StarDraw, StarImpress (.sda, .sdd, and .vor)
	 *   Impress can also export to MacroMedia Flash (.swf) and any of the graphics formats listed for Draw.
	 */
	public static final String[] PRESENTATION_EXTENSIONS = {
			"odp", "odg", "otp", "sxi", "sti", "ppt", "pps", "pot", "pptx", "ppsx", "potx",
			"sda", "sdd", "sdp", "cgm"
	};

	/*
	 * In addition to OpenDocument formats (.odg and .otg), Draw 2.x can open the formats used by OOo 1.x (.sxd and .std) and the following graphic formats:
	 *   BMP	JPEG, JPG	PCX	PSD	SGV	WMF
	 *   DXF	MET	PGM	RAS	SVM	XBM
	 *   EMF	PBM	PLT	SDA	TGA	XPM
	 *   EPS	PCD	PNG	SDD	TIF, TIFF
	 *   GIF	PCT	PPM	SGF	VOR
	 *
	 * Draw can only save in the OpenDocument Drawing formats (.odg and .otg), the OpenOffice.org 1.x formats (.sxd and .std) and StarDraw format (.sda, .sdd, and .vor).
	 * However, Draw can also export to BMP, EMF, EPS, GIF, JPEG, MET, PBM, PCT, PGM, PNG, PPM, RAS, SVG, SVM, TIFF, WMF, and XPM.
	 */
	//REMOVED tif and tiff
	public static final String[] DRAWING_EXTENSIONS = {
			"odg", "otg", "sxd", "std", "bmp", "jpeg", "jpg", "pcx", "psd", "sgv",
			"wmf", "dxf", "met", "pgm", "ras", "svm", "xbm", "emf", "pbm", "plt",
			"sda", "tga", "xpm", "eps", "pcd", "png", "sdd", "gif",
			"pct", "ppm", "sgf", "tif", "tiff"
	};

	private static final Map<String, String> COPY_EXTENSIONS = new HashMap<>();

	public static String[] SUPPORTED_EXTENSIONS = new String[0];

	private static boolean openOfficeOrLibreOfficeInstalled = false;

	static {
		try {
			//			OfficeManager officeManager = LocalOfficeManager.builder().maxTasksPerProcess(10).install().build();
			OfficeManager officeManager = LocalOfficeManager.install();
			OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);

			List<String> supportedExtensionsList = new ArrayList<>();
			for (int i = 0; i < TEXT_EXTENSIONS.length; i++) {
				DocumentFormat documentFormat = converter.getFormatRegistry().getFormatByExtension(TEXT_EXTENSIONS[i]);
				if (documentFormat != null) {
					supportedExtensionsList.add(TEXT_EXTENSIONS[i]);
				}
			}
			for (int i = 0; i < SPREADSHEET_EXTENSIONS.length; i++) {
				DocumentFormat documentFormat = converter.getFormatRegistry().getFormatByExtension(SPREADSHEET_EXTENSIONS[i]);
				if (documentFormat != null) {
					supportedExtensionsList.add(SPREADSHEET_EXTENSIONS[i]);
				}
			}
			for (int i = 0; i < PRESENTATION_EXTENSIONS.length; i++) {
				DocumentFormat documentFormat = converter.getFormatRegistry().getFormatByExtension(PRESENTATION_EXTENSIONS[i]);
				if (documentFormat != null) {
					supportedExtensionsList.add(PRESENTATION_EXTENSIONS[i]);
				}
			}
			for (int i = 0; i < DRAWING_EXTENSIONS.length; i++) {
				DocumentFormat documentFormat = converter.getFormatRegistry().getFormatByExtension(DRAWING_EXTENSIONS[i]);
				if (documentFormat != null) {
					supportedExtensionsList.add(DRAWING_EXTENSIONS[i]);
				} else {
					COPY_EXTENSIONS.put(DRAWING_EXTENSIONS[i], "png");
				}
			}

			COPY_EXTENSIONS.put("dot", "doc");
			COPY_EXTENSIONS.put("pptm", "ppt");
			COPY_EXTENSIONS.put("pps", "ppt");
			supportedExtensionsList.addAll(COPY_EXTENSIONS.keySet());

			SUPPORTED_EXTENSIONS = supportedExtensionsList.toArray(new String[0]);
			LOGGER.info("Conversion to PDF supported for the following extensions: " + Arrays.toString(SUPPORTED_EXTENSIONS));
			openOfficeOrLibreOfficeInstalled = true;
		} catch (Throwable t) {
			LOGGER.error("OpenOffice or LibreOffice not installed", t);
			openOfficeOrLibreOfficeInstalled = false;
		}
	}

	public static int BASE_PORT = 2002;

	private final IOServices ioServices;
	private int numberOfProcesses;
	private String onlineConversionUrl;
	private OfficeManager officeManager;
	private AbstractConverter delegate;
	private ExecutorService executor;
	private static DataLayerSystemExtensions extensions;

	public ConversionManager(IOServices ioServices, int numberOfProcesses, String onlineConversionUrl,
							 DataLayerSystemExtensions extensions) {
		this.ioServices = ioServices;
		this.numberOfProcesses = numberOfProcesses;
		this.onlineConversionUrl = onlineConversionUrl;
		this.extensions = extensions;
	}

	public static String[] getSupportedExtensions() {
		return (String[]) ArrayUtils.addAll(SUPPORTED_EXTENSIONS, extensions.getSupportedExtensionExtensions());
	}

	public boolean isOpenOfficeOrLibreOfficeInstalled() {
		return openOfficeOrLibreOfficeInstalled;
	}

	@Override
	public void initialize() {
	}

	private synchronized void ensureInitialized() {
		if (openOfficeOrLibreOfficeInstalled && executor == null) {
			executor = newFixedThreadPool(numberOfProcesses);

			DocumentFormatRegistry formatRegistry = DefaultDocumentFormatRegistry.getInstance();
			if (onlineConversionUrl != null) {
				officeManager = OnlineOfficeManager.builder().taskExecutionTimeout(CONVERSION_TIMEOUT).poolSize(numberOfProcesses).urlConnection(onlineConversionUrl)
						.build();

				delegate = OnlineConverter.builder()
						.officeManager(officeManager)
						.formatRegistry(formatRegistry)
						.build();
			} else {
				int[] portNumbers = getPortNumbers();
				officeManager = LocalOfficeManager.builder().taskExecutionTimeout(CONVERSION_TIMEOUT).install().portNumbers(portNumbers).build();

				delegate =
						LocalConverter.builder()
								.officeManager(officeManager)
								.formatRegistry(formatRegistry)
								.build();
			}
			try {
				officeManager.start();
			} catch (OfficeException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public int getNumberOfProcesses() {
		return numberOfProcesses;
	}

	private int[] getPortNumbers() {
		int[] ports = new int[numberOfProcesses];
		for (int i = 0; i < numberOfProcesses; i++) {
			ports[i] = BASE_PORT + i;
		}
		return ports;
	}

	public Future<File> convertToPDFAsync(final InputStream inputStream, final String originalName,
										  final File workingFolder) {
		if (openOfficeOrLibreOfficeInstalled) {
			ensureInitialized();
			return executor.submit(new Callable<File>() {
				@Override
				public File call() {
					File personalWorkingFolder = null;
					if (workingFolder != null) {
						personalWorkingFolder = new File(workingFolder, UUIDV1Generator.newRandomId());
						personalWorkingFolder.mkdirs();
					}
					//Each process requires a different working folder
					return convertToPDF(inputStream, originalName, personalWorkingFolder);
				}
			});
		} else {
			return null;
		}
	}

	public File convertToPDF(InputStream inputStream, String originalName, File workingFolder) {
		File input = null;
		File output = null;
		try {
			input = createTempFile("original", originalName, workingFolder);
			save(inputStream, input);
			output = createTempFile("converted", originalName + ".pdf", workingFolder);
			convertToPDF(input, output);
			return output;
		} catch (IOException | OfficeException e) {
			ioServices.deleteQuietly(output);
			throw new RuntimeException(e);
		} finally {
			ioServices.deleteQuietly(input);
		}
	}

	public File convertToJPEG(InputStream inputStream, Dimension dimension, String mimetype, String originalName,
							  File workingFolder) throws Exception {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		if (dimension != null && ImageUtils.isImageOversized(dimension.getHeight())) {
			File outputfile = createTempFile("jpegConversion", originalName + ".jpg", workingFolder);
			BufferedImage resizedImage = ImageUtils.resize(bufferedImage);
			ImageIO.write(resizedImage, "jpg", outputfile);
			return outputfile;
		} else {
			File outputfile = createTempFile("jpegConversion", originalName + ".jpg", workingFolder);
			ImageIO.write(bufferedImage, "jpg", outputfile);
			return outputfile;
		}
	}

	@Override
	public void close() {
		if (executor != null) {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
			}

			// Stop the office process
			try {
				officeManager.stop();
			} catch (OfficeException e) {
				LOGGER.warn("Problem while closing OfficeManager", e);
			}
		}
	}

	private void save(InputStream inputStream, File file)
			throws IOException {
		try (OutputStream outputStream = new FileOutputStream(file)) {
			ioServices.copyLarge(inputStream, outputStream);
		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	private void convertToPDF(File input, File output)
			throws OfficeException {
		if (openOfficeOrLibreOfficeInstalled) {
			ensureInitialized();
			String fileExtension = FilenameUtils.getExtension(input.getName());
			if (Arrays.asList(extensions.getSupportedExtensionExtensions()).contains(fileExtension)) {
				ExtensionConverter converter = extensions.getConverterForSupportedExtension(fileExtension);
				if (converter != null) {
					FileInputStream inputStream = null;
					FileOutputStream outputStream = null;
					try {
						//Source https://beginnersbook.com/2014/05/how-to-copy-a-file-to-another-file-in-java/
						inputStream = new FileInputStream(converter.convert(input));
						outputStream = new FileOutputStream(output);
						byte[] buffer = new byte[1024];

						int length;
						/*copying the contents from input stream to
						 * output stream using read and write methods
						 */
						while ((length = inputStream.read(buffer)) > 0) {
							outputStream.write(buffer, 0, length);
						}
					} catch (FileNotFoundException e) {
						throw new OfficeException(String.format("Could not found file %s", input.getName()));
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						if (inputStream != null) {
							ioServices.closeQuietly(inputStream);
						}

						if (outputStream != null) {
							ioServices.closeQuietly(outputStream);
						}
					}
				}
			} else {
				OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
				DocumentFormat inputFormat = getInputDocumentFormat(input, converter);
				DocumentFormat outputFormat = toPDFa(input);
				if (outputFormat == null) {
					outputFormat = converter.getFormatRegistry().getFormatByExtension("pdf");
				}

				delegate
						.convert(input)
						.as(inputFormat)
						.to(output)
						.as(outputFormat)
						.execute();
			}
		}
	}

	/**
	 * https://wiki.openoffice.org/wiki/Documentation/OOoAuthors_User_Manual/Getting_Started/File_formats
	 */
	private DocumentFamily getDocumentFamily(File file) {
		DocumentFamily documentFamily;
		String extension = StringUtils.toLowerCase(FilenameUtils.getExtension(file.getName()));
		if (asList(TEXT_EXTENSIONS).contains(extension)) {
			documentFamily = DocumentFamily.TEXT;
		} else if (asList(SPREADSHEET_EXTENSIONS).contains(extension)) {
			documentFamily = DocumentFamily.SPREADSHEET;
		} else if (asList(PRESENTATION_EXTENSIONS).contains(extension)) {
			documentFamily = DocumentFamily.PRESENTATION;
		} else if (asList(DRAWING_EXTENSIONS).contains(extension)) {
			documentFamily = DocumentFamily.DRAWING;
		} else {
			documentFamily = null;
		}
		return documentFamily;
	}

	@SuppressWarnings("unused")
	private MediaType getMediaType(File input)
			throws IOException {
		TikaConfig config = TikaConfig.getDefaultConfig();
		Detector detector = config.getDetector();

		TikaInputStream stream = TikaInputStream.get(input.toPath());

		Metadata metadata = new Metadata();
		metadata.add(Metadata.RESOURCE_NAME_KEY, input.getName());
		MediaType mediaType = detector.detect(stream, metadata);
		return mediaType;
	}

	private DocumentFormat getInputDocumentFormat(File input, OfficeDocumentConverter converter) {
		String extension = StringUtils.toLowerCase(FilenameUtils.getExtension(input.getName()));
		DocumentFormat inputFormat = converter.getFormatRegistry().getFormatByExtension(extension);
		if (inputFormat == null && COPY_EXTENSIONS.containsKey(extension)) {
			inputFormat = converter.getFormatRegistry().getFormatByExtension(COPY_EXTENSIONS.get(extension));
		}
		return inputFormat;
	}

	private DocumentFormat toPDFa(File input) {
		DocumentFormat pdfaFormat;
		DocumentFamily sourceDocumentFamily = getDocumentFamily(input);
		if (sourceDocumentFamily != null) {
			String filterName;
			if (sourceDocumentFamily == DocumentFamily.TEXT) {
				filterName = "writer_pdf_Export";
			} else if (sourceDocumentFamily == DocumentFamily.SPREADSHEET) {
				filterName = "calc_pdf_Export";
			} else if (sourceDocumentFamily == DocumentFamily.PRESENTATION) {
				filterName = "impress_pdf_Export";
			} else {
				filterName = "draw_pdf_Export";
			}

			Map<String, Object> filterData = new HashMap<>();
			filterData.put("SelectPdfVersion", 1);
			filterData.put("UseTaggedPDF", Boolean.TRUE);

			Map<String, Object> properties = new HashMap<>();
			properties.put("FilterName", filterName);
			properties.put("FilterData", filterData);

			Map<DocumentFamily, Map<String, Object>> storeProperties = new HashMap<>();
			storeProperties.put(DocumentFamily.TEXT, properties);
			storeProperties.put(DocumentFamily.SPREADSHEET, properties);
			storeProperties.put(DocumentFamily.PRESENTATION, properties);
			storeProperties.put(DocumentFamily.DRAWING, properties);

			pdfaFormat = new DocumentFormat("PDF/A-1", "pdf", "application/pdf", sourceDocumentFamily, properties,
					storeProperties);
		} else {
			pdfaFormat = null;
		}

		return pdfaFormat;
	}

	public static boolean isSupportedExtension(String ext) {
		for (String aSupportedExtension : getSupportedExtensions()) {
			if (aSupportedExtension.equalsIgnoreCase(ext)) {
				return true;
			}
		}

		return false;
	}
}
