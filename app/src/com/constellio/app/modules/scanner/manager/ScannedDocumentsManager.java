package com.constellio.app.modules.scanner.manager;

import com.vaadin.server.VaadinServletService;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.ITesseract.RenderedFormat;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannedDocumentsManager {

	private static ScannedDocumentsManager instance;

	private ServletContext servletContext;

	public static ScannedDocumentsManager get(ServletRequest request) {
		if (instance == null) {
			instance = new ScannedDocumentsManager(request.getServletContext());
		}
		return instance;
	}

	public static ScannedDocumentsManager get() {
		if (instance == null) {
			instance = new ScannedDocumentsManager(VaadinServletService.getCurrentServletRequest().getServletContext());
		}
		return instance;
	}

	public ScannedDocumentsManager(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> ensureMap() {
		String attributeName = "map";
		Map<String, Object> map = (Map<String, Object>) servletContext.getAttribute(attributeName);
		if (map == null) {
			map = new HashMap<String, Object>();
			servletContext.setAttribute(attributeName, map);
		}
		return map;
	}

	public boolean isFinished(String id) {
		boolean completed;
		Object forId = ensureMap().get(id);
		if (forId instanceof String) {
			completed = true;
		} else if (forId instanceof ScannedDocument) {
			completed = ((ScannedDocument) forId).isScanComplete();
		} else if (forId == null) {
			completed = false;
		} else {
			throw new RuntimeException("Unexpected object for id " + id + " : " + forId);
		}
		return completed;
	}

	public boolean isScanSuccess(String id) {
		boolean success;
		Object forId = ensureMap().get(id);
		if (forId instanceof String) {
			success = false;
		} else if (forId instanceof ScannedDocument) {
			success = ((ScannedDocument) forId).isScanComplete();
		} else if (forId == null) {
			success = false;
		} else {
			throw new RuntimeException("Unexpected object for id " + id + " : " + forId);
		}
		return success;
	}

	public String getErrorMessage(String id) {
		Object result = ensureMap().get(id);
		return result instanceof String ? (String) result : null;
	}

	public void setErrorMessage(String id, String message) {
		ensureMap().put(id, message);
	}

	public List<byte[]> getScannedImages(String id) {
		List<byte[]> result;
		Object forId = ensureMap().get(id);
		if (forId instanceof ScannedDocument) {
			ScannedDocument scannedDocument = (ScannedDocument) forId;
			result = scannedDocument.isScanComplete() ? scannedDocument.getPages() : null;
		} else {
			result = null;
		}
		return result;
	}

	public byte[] getScannedImage(String id, int page) {
		byte[] result;
		Object forId = ensureMap().get(id);
		if (forId instanceof ScannedDocument) {
			ScannedDocument scannedDocument = (ScannedDocument) forId;
			result = scannedDocument.isScanComplete() ? scannedDocument.getPages().get(page) : null;
		} else {
			result = null;
		}
		return result;
	}

	public void addScannedImage(String id, byte[] imageBytes, boolean lastPage) {
		ScannedDocument scannedDocument;
		Object forId = ensureMap().get(id);
		if (forId instanceof ScannedDocument) {
			scannedDocument = (ScannedDocument) forId;
		} else if (forId == null) {
			scannedDocument = new ScannedDocument();
			ensureMap().put(id, scannedDocument);
		} else {
			throw new RuntimeException("Unexpected object for id " + id + " : " + forId);
		}
		scannedDocument.addPage(imageBytes);
		scannedDocument.setScanComplete(lastPage);
	}

	public void setScannedImage(String id, List<byte[]> imagesBytes) {
		ensureMap().put(id, imagesBytes);
	}

	public void clear(String id) {
		ensureMap().remove(id);
	}

	public byte[] getPDFContent(String id) throws IOException {
		byte[] result;
		List<byte[]> scannedImagesBytes = getScannedImages(id);
		if (scannedImagesBytes != null) {
			PDDocument document = new PDDocument();

			for (int i = 0; i < scannedImagesBytes.size(); i++) {
				byte[] scannedImageBytes = scannedImagesBytes.get(i);

				InputStream in = new ByteArrayInputStream(scannedImageBytes);
				BufferedImage bimg = ImageIO.read(in);
				float width = bimg.getWidth();
				float height = bimg.getHeight();
				PDPage page = new PDPage(new PDRectangle(width, height));
				document.addPage(page);

				PDImageXObject img = PDImageXObject.createFromByteArray(document, scannedImageBytes, id + "_" + i + ".jpg");
				PDPageContentStream contentStream = new PDPageContentStream(document, page);
				contentStream.drawImage(img, 0, 0);
				contentStream.close();
				in.close();
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			document.save(out);
			document.close();
			result = out.toByteArray();
		} else {
			result = null;
		}
		return result;
	}

	public byte[] getPDFWithOCRContent(String id) throws IOException {
		byte[] result;

		List<byte[]> scannedImagesBytes = getScannedImages(id);
		if (scannedImagesBytes != null) {
			List<File> filesToDelete = new ArrayList<>();
			try {
				ITesseract tessaractInstance = new Tesseract();

				String tessDataDirPath = servletContext.getRealPath("WEB-INF/modules-resources/scanner/tessdata");
				List<RenderedFormat> list = new ArrayList<RenderedFormat>();
				list.add(RenderedFormat.PDF);
				tessaractInstance.setLanguage("eng");
				tessaractInstance.setDatapath(tessDataDirPath);

				List<String> inFilePaths = new ArrayList<>();
				List<String> outFilePaths = new ArrayList<>();

				for (int i = 0; i < scannedImagesBytes.size(); i++) {
					byte[] scannedImageBytes = scannedImagesBytes.get(i);

					File scannedImageFile = File.createTempFile("ScannedImages_" + id + "_" + i, ".jpg");
					String scannedImageFilenameWithoutExtention = FilenameUtils.removeExtension(scannedImageFile.getName());
					File scannedImagePdfFile = new File(scannedImageFile.getParentFile(), scannedImageFilenameWithoutExtention);
					filesToDelete.add(scannedImageFile);
					inFilePaths.add(scannedImageFile.getAbsolutePath());
					outFilePaths.add(scannedImagePdfFile.getAbsolutePath());

					FileUtils.writeByteArrayToFile(scannedImageFile, scannedImageBytes);
				}
				tessaractInstance.createDocuments(inFilePaths.toArray(new String[0]), outFilePaths.toArray(new String[0]), list);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PDFMergerUtility pdfMerger = new PDFMergerUtility();
				pdfMerger.setDestinationStream(out);
				for (String outFilePath : outFilePaths) {
					File outFile = new File(outFilePath + ".pdf");
					pdfMerger.addSource(outFile);
				}
				MemoryUsageSetting memSettings = MemoryUsageSetting.setupMainMemoryOnly();
				pdfMerger.mergeDocuments(memSettings);
				result = out.toByteArray();
			} catch (TesseractException e) {
				e.printStackTrace();
				result = null;
			} finally {
				for (File fileToDelete : filesToDelete) {
					FileUtils.deleteQuietly(fileToDelete);
				}
			}
		} else {
			result = null;
		}
		return result;
	}

	private static class ScannedDocument {

		private boolean scanComplete;

		private List<byte[]> pages = new ArrayList<>();

		public boolean isScanComplete() {
			return scanComplete;
		}

		public void setScanComplete(boolean scanComplete) {
			this.scanComplete = scanComplete;
		}

		public List<byte[]> getPages() {
			return pages;
		}

		public void addPage(byte[] page) {
			pages.add(page);
		}

	}

}
