package com.constellio.data.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.UnexpectedPropertySetTypeException;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypeException;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.OfficeDocumentsServicesException.CannotReadDocumentsProperties;
import com.constellio.data.utils.OfficeDocumentsServicesException.NotCompatibleExtension;
import com.constellio.data.utils.OfficeDocumentsServicesException.PropertyDoesntExist;
import com.constellio.data.utils.OfficeDocumentsServicesException.RTFFileIsNotCompatible;

public class OfficeDocumentsServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(OfficeDocumentsServices.class);

	public void setProperty(StreamFactory<InputStream> inputStreamFactory, StreamFactory<OutputStream> outputStreamFactory,
			String propertyName, String propertyValue, String ext)
			throws NotCompatibleExtension, WritingNotSupportedException, CannotReadDocumentsProperties, IOException,
			PropertyDoesntExist, MimeTypeException, RTFFileIsNotCompatible {

		Tika tika = new Tika();
		String mimeType = null;

		InputStream inputStream = inputStreamFactory.create(getClass().getName() + ".setProperty");
		try {
			mimeType = tika.detect(inputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		String modifiedExtension = ext;

		if ("application/x-tika-msoffice".equals(mimeType)) {
			setProperty(inputStreamFactory, outputStreamFactory, propertyName, propertyValue);
		} else if ("application/x-tika-ooxml".equals(mimeType) || "application/zip".equals(mimeType)) {
			modifiedExtension = addXToExtension(ext);
			setPropertyNewDocument(modifiedExtension, inputStreamFactory, outputStreamFactory, propertyName, propertyValue);
		} else if ("application/rtf".equals(mimeType)) {
			throw new OfficeDocumentsServicesException.RTFFileIsNotCompatible();
		} else {
			throw new OfficeDocumentsServicesException.NotCompatibleExtension(ext);
		}
	}

	public String getProperty(StreamFactory<InputStream> inputStreamFactory, String propertyName, String ext)
			throws CannotReadDocumentsProperties, PropertyDoesntExist, IOException, NotCompatibleExtension,
			RTFFileIsNotCompatible {

		Tika tika = new Tika();
		InputStream inputStream = inputStreamFactory.create(getClass().getName() + ".getProperty");

		String mimeType = null;

		try {
			mimeType = tika.detect(inputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		String modifiedExtension = ext;
		if ("application/x-tika-msoffice".equals(mimeType)) {
			return getPropertyDocument(inputStreamFactory, propertyName);
		} else if ("application/x-tika-ooxml".equals(mimeType) || "application/zip".equals(mimeType)) {
			modifiedExtension = addXToExtension(modifiedExtension);
			return getPropertyNewDocument(modifiedExtension, inputStreamFactory, propertyName);
		} else if ("application/rtf".equals(mimeType)) {
			throw new OfficeDocumentsServicesException.RTFFileIsNotCompatible();
		} else {
			throw new OfficeDocumentsServicesException.NotCompatibleExtension(modifiedExtension);
		}
	}

	public String getPropertyDocument(StreamFactory<InputStream> inputStreamFactory, String propertyName)
			throws IOException, CannotReadDocumentsProperties, PropertyDoesntExist {
		String idIGIDStr = "";

		// POIFSFileSystem will close this stream
		POIFSFileSystem poifs = new POIFSFileSystem(inputStreamFactory.create(getClass().getName() + ".getPropertyDocument"));

		DirectoryEntry dir = poifs.getRoot();
		DocumentSummaryInformation dsi = getDocumentSummaryInfo(dir);

		CustomProperties customProperties = dsi.getCustomProperties();
		if (customProperties != null) {
			idIGIDStr = (String) customProperties.get(propertyName);
			if (idIGIDStr == null) {
				throw new OfficeDocumentsServicesException.PropertyDoesntExist(propertyName);
			}
		}
		return idIGIDStr;
	}

	private DocumentSummaryInformation getDocumentSummaryInfo(DirectoryEntry dir)
			throws CannotReadDocumentsProperties {
		DocumentSummaryInformation dsi;
		try {
			DocumentEntry dsiEntry = (DocumentEntry) dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
			DocumentInputStream dis = new DocumentInputStream(dsiEntry);
			PropertySet ps = new PropertySet(dis);
			dis.close();
			dsi = new DocumentSummaryInformation(ps);
		} catch (FileNotFoundException ex) {
			LOGGER.debug("No summary in Office document. Creating new DocumentSummaryInformation", ex);
			dsi = PropertySetFactory.newDocumentSummaryInformation();
		} catch (UnexpectedPropertySetTypeException | NoPropertySetStreamException | MarkUnsupportedException | IOException e) {
			throw new OfficeDocumentsServicesException.CannotReadDocumentsProperties(e);
		}
		return dsi;
	}

	public String getPropertyNewDocument(String ext, StreamFactory<InputStream> inputStreamFactory, String propertyName)
			throws IOException, PropertyDoesntExist, NotCompatibleExtension, CannotReadDocumentsProperties {
		POIXMLDocument doc = parseDocument(ext, inputStreamFactory, propertyName);
		org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties props = doc.getProperties()
				.getCustomProperties().getUnderlyingProperties();

		List<CTProperty> properties = props.getPropertyList();

		for (CTProperty prop : properties) {
			if (prop.getName().equals(propertyName)) {
				return prop.getLpwstr();
			}
		}

		throw new OfficeDocumentsServicesException.PropertyDoesntExist(propertyName);
	}

	private POIXMLDocument parseDocument(String ext, StreamFactory<InputStream> inputStreamFactory, String propertyName)
			throws IOException, CannotReadDocumentsProperties, PropertyDoesntExist, NotCompatibleExtension {
		POIXMLDocument doc;
		if ("docx".equalsIgnoreCase(ext)) {
			doc = new XWPFDocument(inputStreamFactory.create(getClass().getName() + ".parseDocument"));
		} else if ("xlsx".equalsIgnoreCase(ext)) {
			doc = new XSSFWorkbook(inputStreamFactory.create(getClass().getName() + ".parseDocument"));
		} else if ("pptx".equalsIgnoreCase(ext)) {
			try {
				OPCPackage opcpPackage = OPCPackage.open(inputStreamFactory.create(getClass().getName() + ".parseDocument"));
				if (opcpPackage == null) {
					throw new OfficeDocumentsServicesException.PropertyDoesntExist(propertyName);
				}
				doc = new XSLFSlideShow(opcpPackage);
			} catch (OpenXML4JException | XmlException e) {
				throw new OfficeDocumentsServicesException.CannotReadDocumentsProperties(e);
			}
		} else {
			throw new OfficeDocumentsServicesException.NotCompatibleExtension(ext);
		}
		return doc;
	}

	public void setProperty(StreamFactory<InputStream> inputStreamFactory, StreamFactory<OutputStream> outputStreamFactory,
			String propertyName, String propertyValue)
			throws IOException, CannotReadDocumentsProperties, WritingNotSupportedException {
		OutputStream outputStream = outputStreamFactory.create(getClass().getName() + ".setProperty");
		try {
			POIFSFileSystem poifs = new POIFSFileSystem(inputStreamFactory.create(getClass().getName() + ".setProperty"));

			DirectoryEntry dir = poifs.getRoot();
			DocumentSummaryInformation dsi = getDocumentSummaryInfo(dir);

			CustomProperties customProperties = dsi.getCustomProperties();
			if (customProperties == null) {
				customProperties = new CustomProperties();
			}

			customProperties.put(propertyName, propertyValue);

			dsi.setCustomProperties(customProperties);

			dsi.write(dir, DocumentSummaryInformation.DEFAULT_STREAM_NAME);
			poifs.writeFilesystem(outputStream);
		} finally {
			IOUtils.closeQuietly(outputStream);
		}

	}

	public void setPropertyNewDocument(String ext, StreamFactory<InputStream> inputStreamFactory,
			StreamFactory<OutputStream> outputStreamFactory, String propertyName, String propertyValue)
			throws IOException, PropertyDoesntExist, NotCompatibleExtension, CannotReadDocumentsProperties {
		POIXMLDocument doc = parseDocument(ext, inputStreamFactory, propertyName);

		org.apache.poi.POIXMLProperties.CustomProperties customProperties = doc.getProperties().getCustomProperties();

		int index = 0;
		for (CTProperty prop : customProperties.getUnderlyingProperties().getPropertyList()) {
			if (prop.getName().equals(propertyName)) {
				customProperties.getUnderlyingProperties().removeProperty(index);
			}
			index++;
		}

		customProperties.addProperty(propertyName, propertyValue);
		doc.write(outputStreamFactory.create(getClass().getName() + ".setPropertyNewDocument"));
	}

	private String addXToExtension(String ext) {
		String modifiedExtension = ext;
		if ("xls".equals(ext) || "ppt".equals(ext) || "doc".equals(ext)) {
			modifiedExtension = ext.replace("xls", "xlsx").replace("doc", "docx").replace("ppt", "pptx");
		}
		return modifiedExtension;
	}

}
