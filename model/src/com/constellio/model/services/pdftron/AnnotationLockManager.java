package com.constellio.model.services.pdftron;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.model.services.pdftron.PdfTronLockRuntimeException.PdfTronLockRuntimeException_LockIsAlreadyTaken;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.Iterator;

public class AnnotationLockManager {
	public static final String LOCK_XML_CONFIG_FILE = "/lock/lockmanager.xml";

	public static final String RECORD_ID_ATTRIBUTE = "recordId";
	public static final String VERSION_ATTRIBUTE = "version";
	public static final String USER_ID_ATTRIBUTE = "userId";
	public static final String PAGE_ID_ATTRIBUTE = "pageId";
	public static final String HASH_ATTRIBUTE = "hash";

	ConfigManager configManager;

	public AnnotationLockManager(ConfigManager configManager) {
		this.configManager = configManager;
		initialize();
	}

	private void initialize() {
		if (configManager.exist(LOCK_XML_CONFIG_FILE)) {
			configManager.delete(LOCK_XML_CONFIG_FILE);
		}

		configManager.createXMLDocumentIfInexistent(LOCK_XML_CONFIG_FILE, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				document.addContent(new Element("locks"));
			}
		});
	}

	public static boolean isElementEqual(Element element, String hash, String recordId, String version, String userId,
										 String pageId) {
		return element.getAttributeValue(RECORD_ID_ATTRIBUTE).equals(recordId)
			   && element.getAttributeValue(VERSION_ATTRIBUTE).equals(version)
			   && element.getAttributeValue(USER_ID_ATTRIBUTE).equals(userId) && element.getAttributeValue(PAGE_ID_ATTRIBUTE).equals(pageId)
			   && element.getAttributeValue(HASH_ATTRIBUTE).equals(hash);
	}

	public static boolean isElementEqual(Element element, String hash, String recordId, String version, String userId) {
		return element.getAttributeValue(RECORD_ID_ATTRIBUTE).equals(recordId)
			   && element.getAttributeValue(VERSION_ATTRIBUTE).equals(version)
			   && element.getAttributeValue(USER_ID_ATTRIBUTE).equals(userId)
			   && element.getAttributeValue(HASH_ATTRIBUTE).equals(hash);
	}

	public static boolean isElementEqual(Element element, String hash, String recordId, String version) {
		return element.getAttributeValue(RECORD_ID_ATTRIBUTE).equals(recordId)
			   && element.getAttributeValue(VERSION_ATTRIBUTE).equals(version)
			   && element.getAttributeValue(HASH_ATTRIBUTE).equals(hash);
	}

	public boolean obtainLock(String hash, String recordId, String version, String userId, String pageId) {

		try {
			this.configManager.updateXML(LOCK_XML_CONFIG_FILE, document -> {
				Element rootElement = document.getRootElement();

				for (Element currentElement : rootElement.getChildren()) {
					if (isElementEqual(currentElement, hash, recordId, version)) {
						throw new PdfTronLockRuntimeException_LockIsAlreadyTaken();
					}
				}
				Element newSubElement = new Element("lock");
				rootElement.addContent(newSubElement);

				newSubElement.setAttribute(HASH_ATTRIBUTE, hash);
				newSubElement.setAttribute(RECORD_ID_ATTRIBUTE, recordId);
				newSubElement.setAttribute(VERSION_ATTRIBUTE, version);
				newSubElement.setAttribute(USER_ID_ATTRIBUTE, userId);
				newSubElement.setAttribute(PAGE_ID_ATTRIBUTE, pageId);
			});
		} catch (PdfTronLockRuntimeException_LockIsAlreadyTaken e) {
			return false;
		}

		return true;
	}

	public void releaseLock(String hash, String recordId, String version, String userId, String pageId) {
		this.configManager.updateXML(LOCK_XML_CONFIG_FILE, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				Element rootElement = document.getRootElement();

				Iterator childrenIterator = rootElement.getChildren().iterator();

				while (childrenIterator.hasNext()) {
					Element currentElement = (Element) childrenIterator.next();
					if (isElementEqual(currentElement, hash, recordId, version, userId, pageId)) {
						childrenIterator.remove();
					}
				}
			}
		});
	}

	public String getUserIdOfLock(String hash, String recordId, String version) {
		XMLConfiguration xmlConfig = this.configManager.getXML(LOCK_XML_CONFIG_FILE);

		if (xmlConfig != null) {
			Document xmlDocument = xmlConfig.getDocument();

			for (Element element : xmlDocument.getRootElement().getChildren()) {
				if (isElementEqual(element, hash, recordId, version)) {
					return element.getAttributeValue(USER_ID_ATTRIBUTE);
				}
			}
		}

		return null;
	}


	public String getPageIdOfLock(String hash, String recordId, String version) {
		XMLConfiguration xmlConfig = this.configManager.getXML(LOCK_XML_CONFIG_FILE);

		Document xmlDocument = xmlConfig.getDocument();

		for (Element element : xmlDocument.getRootElement().getChildren()) {
			if (isElementEqual(element, hash, recordId, version)) {
				return element.getAttributeValue(PAGE_ID_ATTRIBUTE);
			}
		}

		return null;
	}
}
