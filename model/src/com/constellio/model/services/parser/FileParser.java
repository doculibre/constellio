package com.constellio.model.services.parser;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.io.streamFactories.StreamFactoryWithFilename;
import com.constellio.data.io.streamFactories.impl.CopyInputStreamFactory;
import com.constellio.data.utils.ImageUtils;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager.ParseOptions;
import com.constellio.model.services.parser.FileParserException.FileParserException_CannotExtractStyles;
import com.constellio.model.services.parser.FileParserException.FileParserException_CannotParse;
import com.constellio.model.services.parser.FileParserException.FileParserException_FileSizeExceedLimitForParsing;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.hwpf.model.StyleDescription;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.tika.Tika;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.fork.ForkParser;
import org.apache.tika.metadata.Message;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.parser.pdf.PDFParserConfig.OCR_STRATEGY;
import org.apache.tika.sax.BodyContentHandler;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static com.constellio.model.services.migrations.ConstellioEIMConfigs.CONTENT_MAX_LENGTH_FOR_PARSING_IN_MEGAOCTETS;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.join;

public class FileParser {

	enum StringCompressor {
		;

		public static byte[] compress(String text) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				OutputStream out = new DeflaterOutputStream(baos);
				out.write(text.getBytes("UTF-8"));
				out.close();
			} catch (IOException e) {
				throw new AssertionError(e);
			}
			return baos.toByteArray();
		}

		public static String decompress(byte[] bytes) {
			InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				byte[] buffer = new byte[8192];
				int len;
				while ((len = in.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				return new String(baos.toByteArray(), "UTF-8");
			} catch (IOException e) {
				throw new AssertionError(e);
			}
		}
	}

	private static final String MS_DOC_MIMETYPE = "application/msword";
	private static final String MS_DOCX_MIMETYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	private static final boolean OCR_INLINE_IMAGES_ONLY_MODE = false;

	static final String READ_STREAM_FOR_STYLES_EXTRACTION = "FileParser-ReadStreamForStylesExtraction";
	static final String READ_STREAM_FOR_PARSING_WITH_TIKA = "FileParser-ReadStreamForParsingWithTika";
	static final String READ_STREAM_FOR_MIMETYPE_DETECTION = "FileParser-MimetypeDetection";

	private final IOServices ioServices;
	private final ForkParsers parsers;
	private boolean forkParserEnabled;
	private LanguageDetectionManager languageDetectionManager;
	private ThreadLocal<AutoDetectParser> autoDetectParsers = new ThreadLocal<>();
	private SystemConfigurationsManager systemConfigurationsManager;

	public FileParser(ForkParsers parsers, LanguageDetectionManager languageDetectionManager, IOServices ioServices,
					  SystemConfigurationsManager systemConfigurationsManager, boolean forkParserEnabled) {
		super();
		this.parsers = parsers;
		this.ioServices = ioServices;
		this.forkParserEnabled = forkParserEnabled;
		this.languageDetectionManager = languageDetectionManager;
		this.systemConfigurationsManager = systemConfigurationsManager;
	}

	public ParsedContent parse(StreamFactory<InputStream> inputStreamStreamFactory, long length,
							   ParseOptions options) throws FileParserException {
		if (!options.isBeautify()) {
			return parseWithoutBeautifying(inputStreamStreamFactory, length, options.isDetectLanguage(), options.isOcr());
		}
		return parse(inputStreamStreamFactory, length, options.isDetectLanguage(), options.isOcr());
	}

	public ParsedContent parse(StreamFactory<InputStream> inputStreamFactory, long length)
			throws FileParserException {
		return parse(inputStreamFactory, length, true);
	}

	public ParsedContent parse(InputStream inputStream, boolean detectLanguage)
			throws FileParserException {
		return parse(inputStream, detectLanguage, false);
	}

	public ParsedContent parse(InputStream inputStream, boolean detectLanguage, boolean ocr)
			throws FileParserException {

		CopyInputStreamFactory inputStreamFactory = null;
		try {
			inputStreamFactory = ioServices.copyToReusableStreamFactory(inputStream, null);
			return parse(inputStreamFactory, inputStreamFactory.length(), detectLanguage, ocr);
		} finally {
			ioServices.closeQuietly(inputStream);
			ioServices.closeQuietly(inputStreamFactory);
		}
	}

	public ParsedContent parse(StreamFactory<InputStream> inputStreamFactory, long length, boolean detectLanguage)
			throws FileParserException {
		return parse(inputStreamFactory, length, detectLanguage, false);
	}

	private ParsedContent parse(StreamFactory<InputStream> inputStreamFactory, long length, boolean detectLanguage,
								boolean ocr)
			throws FileParserException {

		//Pattern patternForChar = Pattern.compile("([^\u0000-\u00FF]+)");
		Pattern patternForSpaceAndReturn = Pattern.compile("((\\n{3,})|( ){2,})|(\\t)");
		Pattern patternForCharWeDontWant = Pattern.compile("[\\[\\]\\(\\)]");
		Pattern patternForSingleCharLine = Pattern.compile("^([\\w]){1}\\n$");

		int contentMaxLengthForParsingInMegaoctets = systemConfigurationsManager
				.getValue(CONTENT_MAX_LENGTH_FOR_PARSING_IN_MEGAOCTETS);
		if (length > 1024L * 1024 * contentMaxLengthForParsingInMegaoctets) {
			String detectedMimeType = null;
			if (inputStreamFactory instanceof StreamFactoryWithFilename) {
				String filename = ((StreamFactoryWithFilename) inputStreamFactory).getFilename();
				if (filename != null) {
					detectedMimeType = new Tika().detect(filename);
				}
			}

			throw new FileParserException_FileSizeExceedLimitForParsing(contentMaxLengthForParsingInMegaoctets, detectedMimeType);
		}

		int maxParsedContentLengthInKO = systemConfigurationsManager.getValue(PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS);
		BodyContentHandler handler = new BodyContentHandler(maxParsedContentLengthInKO * 1000);
		Metadata metadata = new Metadata();

		InputStream inputStream = null;
		try {
			inputStream = inputStreamFactory.create(READ_STREAM_FOR_PARSING_WITH_TIKA);
			if (forkParserEnabled) {
				ForkParser forkParser = parsers.getForkParser();
				forkParser.parse(inputStream, handler, metadata, configureParseContext(forkParser, ocr));
			} else {

				AutoDetectParser parser = autoDetectParsers.get();

				if (parser == null) {
					autoDetectParsers.set(parser = newAutoDetectParser());
				}
				parser.parse(inputStream, handler, metadata, configureParseContext(parser, ocr));
			}

		} catch (Throwable t) {
			if (!t.getClass().getSimpleName().equals("WriteLimitReachedException")) {
				String detectedMimetype = metadata.get(Metadata.CONTENT_TYPE);
				throw new FileParserException_CannotParse(t, detectedMimetype);
			}

		} finally {
			ioServices.closeQuietly(inputStream);
		}

		String type = metadata.get(Metadata.CONTENT_TYPE);
		String parsedContent = handler.toString().trim();
		//parsedContent = patternForChar.matcher(parsedContent).replaceAll("");
		parsedContent = patternForSpaceAndReturn.matcher(parsedContent).replaceAll("");
		String language = detectLanguage ? languageDetectionManager.tryDetectLanguage(parsedContent) : null;
		Map<String, Object> properties = getPropertiesHashMap(metadata, type);
		Map<String, List<String>> styles = null;
		try {
			styles = getStylesDoc(inputStreamFactory, type);
		} catch (Throwable t) {
			throw new FileParserException_CannotExtractStyles(t, type);
		}
		Dimension imageDimension = ImageUtils.getImageDimension(inputStream);
		ParsedContent content = new ParsedContent(parsedContent, language, type, length, properties, styles, imageDimension);
		content.setDescription(metadata.get(Metadata.DESCRIPTION));
		content.setTitle(metadata.get(Metadata.TITLE));

		return content;

	}

	public ParsedContent parseWithoutBeautifying(StreamFactory<InputStream> inputStreamFactory, long length)
			throws FileParserException {
		return parseWithoutBeautifying(inputStreamFactory, length, true);
	}

	public ParsedContent parseWithoutBeautifying(InputStream inputStream, boolean detectLanguage)
			throws FileParserException {
		return parseWithoutBeautifying(inputStream, detectLanguage, false);
	}

	public ParsedContent parseWithoutBeautifying(InputStream inputStream, boolean detectLanguage, boolean ocr)
			throws FileParserException {

		CopyInputStreamFactory inputStreamFactory = null;
		try {
			inputStreamFactory = ioServices.copyToReusableStreamFactory(inputStream, null);
			return parseWithoutBeautifying(inputStreamFactory, inputStreamFactory.length(), detectLanguage, ocr);
		} finally {
			ioServices.closeQuietly(inputStream);
			ioServices.closeQuietly(inputStreamFactory);
		}
	}

	public ParsedContent parseWithoutBeautifying(StreamFactory<InputStream> inputStreamFactory, long length,
												 boolean detectLanguage)
			throws FileParserException {
		return parseWithoutBeautifying(inputStreamFactory, length, detectLanguage, false);
	}

	private ParsedContent parseWithoutBeautifying(StreamFactory<InputStream> inputStreamFactory, long length,
												  boolean detectLanguage, boolean ocr)
			throws FileParserException {

		int contentMaxLengthForParsingInMegaoctets = systemConfigurationsManager
				.getValue(CONTENT_MAX_LENGTH_FOR_PARSING_IN_MEGAOCTETS);
		if (length > 1024L * 1024 * contentMaxLengthForParsingInMegaoctets) {
			String detectedMimeType = null;
			if (inputStreamFactory instanceof StreamFactoryWithFilename) {
				String filename = ((StreamFactoryWithFilename) inputStreamFactory).getFilename();
				if (filename != null) {
					detectedMimeType = new Tika().detect(filename);
				}
			}

			throw new FileParserException_FileSizeExceedLimitForParsing(contentMaxLengthForParsingInMegaoctets, detectedMimeType);
		}

		int maxParsedContentLengthInKO = systemConfigurationsManager.getValue(PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS);
		BodyContentHandler handler = new BodyContentHandler(maxParsedContentLengthInKO * 1000);
		Metadata metadata = new Metadata();

		InputStream inputStream = null;
		try {
			inputStream = inputStreamFactory.create(READ_STREAM_FOR_PARSING_WITH_TIKA);
			if (forkParserEnabled) {
				ForkParser forkParser = parsers.getForkParser();
				forkParser.parse(inputStream, handler, metadata, configureParseContext(forkParser, ocr));
			} else {

				AutoDetectParser parser = autoDetectParsers.get();

				if (parser == null) {
					autoDetectParsers.set(parser = newAutoDetectParser());
				}
				parser.parse(inputStream, handler, metadata, configureParseContext(parser, ocr));
			}
		} catch (Throwable t) {
			if (!t.getClass().getSimpleName().equals("WriteLimitReachedException")) {
				String detectedMimetype = metadata.get(Metadata.CONTENT_TYPE);
				throw new FileParserException_CannotParse(t, detectedMimetype);
			}

		} finally {
			ioServices.closeQuietly(inputStream);
		}

		String type = metadata.get(Metadata.CONTENT_TYPE);
		String parsedContent = handler.toString().trim();
		String language = detectLanguage ? languageDetectionManager.tryDetectLanguage(parsedContent) : null;
		Map<String, Object> properties = getPropertiesHashMap(metadata, type);
		Map<String, List<String>> styles = null;
		try {
			styles = getStylesDoc(inputStreamFactory, type);
		} catch (Throwable t) {
			throw new FileParserException_CannotExtractStyles(t, type);
		}
		Dimension imageDimension = ImageUtils.getImageDimension(inputStream);
		return new ParsedContent(parsedContent, language, type, length, properties, styles, imageDimension);
	}

	protected Map<String, Object> getPropertiesHashMap(Metadata metadata, String mimeType) {
		HashMap<String, Object> properties = new HashMap<String, Object>();

		addKeywordsTo(properties, metadata, "Keywords", TikaCoreProperties.KEYWORDS);
		addPropertyTo(properties, metadata, "Title", TikaCoreProperties.TITLE);
		addPropertyTo(properties, metadata, "Comments", TikaCoreProperties.COMMENTS);
		addPropertyTo(properties, metadata, "Author", TikaCoreProperties.CREATOR);
		addPropertyTo(properties, metadata, "Subject", "subject");
		addPropertyTo(properties, metadata, "Category", "Category");
		addPropertyTo(properties, metadata, "Manager", "Manager");
		addPropertyTo(properties, metadata, "BCC", Message.MESSAGE_BCC);
		addPropertyTo(properties, metadata, "CC", Message.MESSAGE_CC);
		addPropertyTo(properties, metadata, "From", Message.MESSAGE_FROM);
		addPropertyTo(properties, metadata, "To", Message.MESSAGE_TO);

		if (mimeType.contains("xml")) {
			addCommentsTo(properties, metadata, "Comments", TikaCoreProperties.DESCRIPTION, "_x000d_");
			addPropertyTo(properties, metadata, "Company", TikaCoreProperties.PUBLISHER);
		} else {
			addCommentsTo(properties, metadata, "Comments", TikaCoreProperties.COMMENTS, "[\r]");
			addPropertyTo(properties, metadata, "Company", "Company");
		}

		return properties;
	}

	//For Property
	private void addPropertyTo(HashMap<String, Object> properties, Metadata metadata, String key, Property property) {
		if (metadata.get(property) != null && metadata.get(property).isEmpty() == false) {
			properties.put(key, metadata.get(property));
		}
	}

	//For String
	protected void addPropertyTo(HashMap<String, Object> properties, Metadata metadata, String key, String value) {
		if (metadata.get(value) != null && metadata.get(value).isEmpty() == false) {
			properties.put(key, metadata.get(value));
		}
	}

	private void addKeywordsTo(HashMap<String, Object> properties, Metadata metadata, String key, Property property) {
		if (metadata.get(property) != null) {
			List<String> finalKeywordsList = new ArrayList<String>();
			String[] keywordsAfterFirstSplit = metadata.get(property).split(";");
			for (String aKeyword : keywordsAfterFirstSplit) {
				String[] keywordsAfterSecondSplit = aKeyword.split(",");
				for (String zeKeyword : keywordsAfterSecondSplit) {
					finalKeywordsList.add(zeKeyword.trim());
				}
			}
			properties.put("List:" + key, finalKeywordsList);
		}
	}

	private void addCommentsTo(HashMap<String, Object> properties, Metadata metadata, String key, Property property,
							   String regex) {
		if (metadata.get(property) != null) {
			String[] commentsListAfterSplit = metadata.get(property).split(regex);
			properties.put(key, join(commentsListAfterSplit, " "));
		}
	}

	private Map<String, List<String>> getStylesDoc(StreamFactory<InputStream> inputStreamFactory, String mimeType)
			throws IOException {
		InputStream inputStream = null;

		try {

			if (MS_DOC_MIMETYPE.equals(mimeType)) {
				inputStream = inputStreamFactory.create(READ_STREAM_FOR_STYLES_EXTRACTION);
				return getStylesDoc(inputStream);

			} else if (MS_DOCX_MIMETYPE.equals(mimeType)) {
				inputStream = inputStreamFactory.create(READ_STREAM_FOR_STYLES_EXTRACTION);
				return getStylesDocX(inputStream);

			} else {
				return new HashMap<>();
			}

		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	private Map<String, List<String>> getStylesDoc(InputStream inputStream)
			throws IOException {

		KeyListMap<String, String> styles = new KeyListMap<>();

		POIFSFileSystem fis = new POIFSFileSystem(inputStream);

		HWPFDocumentCore wdDoc;
		try {
			wdDoc = new HWPFDocument(fis);
		} catch (OldWordFileFormatException e) {
			wdDoc = new HWPFOldDocument(fis);
		}

		StyleSheet styleSheet = wdDoc.getStyleSheet();
		if (styleSheet != null) {
			Range range = wdDoc.getRange();

			int parasSize = range.numParagraphs();
			int maxPara = 20;
			if (range.numParagraphs() > maxPara) {
				parasSize = maxPara;
			}
			for (int i = 0; i < parasSize; i++) {
				Paragraph p = range.getParagraph(i);

				short styleIndex = p.getStyleIndex();
				StyleDescription style = styleSheet.getStyleDescription(styleIndex);
				String styleName = style.getName();

				if (styleName != null) {
					styleName = styleName.toLowerCase().replace(" ", "");
					if (!excludedStyles.contains(styleName)) {
						String text = p.text().trim();
						if (StringUtils.isNotBlank(text)) {
							if (!styles.get(styleName).contains(text)) {
								styles.add(styleName, text);
							}
						}
					}
				}
			}
		}

		return styles.getNestedMap();
	}

	private static List<String> excludedStyles = asList("normal", "nospacing");

	public static Map<String, List<String>> getStylesDocX(InputStream inputStream)
			throws IOException {
		KeyListMap<String, String> styles = new KeyListMap<>();

		XWPFDocument wdDoc = new XWPFDocument(inputStream);

		List<XWPFParagraph> paras = wdDoc.getParagraphs();
		int parasSize = paras.size();
		int maxPara = 20;
		if (paras.size() > maxPara) {
			parasSize = maxPara;
		}

		for (int i = 0; i < parasSize; i++) {
			XWPFParagraph para = paras.get(i);
			String styleName = para.getStyle();

			if (styleName != null) {
				styleName = styleName.toLowerCase().replace(" ", "");
				if (!excludedStyles.contains(styleName)) {
					String text = para.getText().trim();
					if (StringUtils.isNotBlank(text)) {
						if (!styles.get(styleName).contains(text)) {
							styles.add(styleName, text);
						}
					}
				}
			}
		}
		return styles.getNestedMap();
	}

	AutoDetectParser newAutoDetectParser() {
		return new AutoDetectParser();
	}

	public String detectMimetype(StreamFactory<InputStream> inputStreamFactory, String fileName)
			throws FileParserException {

		if (fileName != null) {
			String lcFilename = fileName.toLowerCase();
			if (lcFilename.endsWith(".odt")) {
				return "application/vnd.oasis.opendocument.text";
			}
		}

		InputStream inputStream = null;
		try {
			inputStream = inputStreamFactory.create(READ_STREAM_FOR_MIMETYPE_DETECTION);
			return getTikaMediaType(inputStream, fileName).toString();
		} catch (IOException e) {
			throw new FileParserException_CannotParse(e, "application/octet-stream");

		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	private MediaType getTikaMediaType(InputStream is, String fileName) {
		Metadata md = new Metadata();
		md.set(Metadata.RESOURCE_NAME_KEY, fileName);
		Detector detector = new DefaultDetector();

		try {
			return detector.detect(is, md);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ParseContext configureParseContext(Parser parser, boolean ocr) {
		ParseContext parseContext = new ParseContext();
		if (!ocr) {
			TesseractOCRConfig tesseractOCRConfig = new TesseractOCRConfig();
			// needed to disable tesseract when installed on drive
			if (System.getProperty("os.name").startsWith("Windows")) {
				tesseractOCRConfig.setTesseractPath("\\Device\\Null\\");
			} else {
				tesseractOCRConfig.setTesseractPath("/dev/null/");
			}
			parseContext.set(TesseractOCRConfig.class, tesseractOCRConfig);
			return parseContext;
		}

		TesseractOCRConfig tesseractOCRConfig = new TesseractOCRConfig();
		//tesseractOCRConfig.setEnableImageProcessing(1);
		tesseractOCRConfig.setLanguage("fra+eng");
		//		tesseractOCRConfig.setTesseractPath("C:\\Program Files (x86)\\Tesseract-OCR");
		//		tesseractOCRConfig.setTessdataPath("C:\\Program Files (x86)\\Tesseract-OCR\\tessdata");
		parseContext.set(TesseractOCRConfig.class, tesseractOCRConfig);

		PDFParserConfig pdfConfig = new PDFParserConfig();
		if (OCR_INLINE_IMAGES_ONLY_MODE) {
			// mode 1
			pdfConfig.setExtractInlineImages(true);
			pdfConfig.setOcrStrategy(PDFParserConfig.OCR_STRATEGY.NO_OCR);
		} else {
			// mode 2
			pdfConfig.setOcrStrategy(OCR_STRATEGY.OCR_AND_TEXT_EXTRACTION);
		}
		pdfConfig.setOcrImageType("rgb");
		pdfConfig.setOcrDPI(300); // FIXME can be adjusted, 300 seems to be the best value overall
		parseContext.set(PDFParserConfig.class, pdfConfig);

		// needed to activate recursive parsing
		parseContext.set(Parser.class, parser);
		return parseContext;
	}
}
