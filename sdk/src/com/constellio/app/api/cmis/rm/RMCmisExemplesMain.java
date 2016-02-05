package com.constellio.app.api.cmis.rm;

import static org.apache.chemistry.opencmis.commons.enums.VersioningState.MAJOR;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.io.IOUtils;

public class RMCmisExemplesMain {

	static String url = "http://167.114.79.166:9080/constellio";
	static String user = "admin";
	static String password = "password";
	static String collectionName = "collection";

	static Session session;

	public static void main(String argv[])
			throws IOException {
		createSession();

		String savedInFolder = "00000000057";
		String title = "A tiff file";
		String mimeType = "image/tiff";
		File documentFile = new File("/Users/francisbaril/Downloads/tiffimage-5600x2100.tiff");

		addDocumentToFolder(documentFile, title, mimeType, savedInFolder);

	}

	private static void addDocumentToFolder(File documentFile, String title, String mimeType, String savedInFolder)
			throws IOException {

		ObjectId savedInFolderObjectId = new ObjectIdImpl(savedInFolder);

		//
		//-- Create a document (considered as a Folder by CMIS)
		Map<String, Object> documentProperties = new HashMap<>();
		documentProperties.put(PropertyIds.OBJECT_TYPE_ID, "document_default");
		documentProperties.put("title", title);
		documentProperties.put("folder", savedInFolder);
		ObjectId newDocumentId = session.createFolder(documentProperties, savedInFolderObjectId);

		//
		//-- Add a content to the new document (considered as a Document by CMIS)
		Map<String, Object> contentProperties = new HashMap<>();
		contentProperties.put("metadata", "content");
		contentProperties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");

		InputStream inputstream = new BufferedInputStream(new FileInputStream(documentFile));
		try {
			ContentStream contentStream = new ContentStreamImpl(
					documentFile.getName(),
					BigInteger.valueOf(documentFile.length()),
					mimeType,
					inputstream);

			//Major create the document in version 1.0, Minor create it in 0.1
			ObjectId documentContentId = session.createDocument(contentProperties, newDocumentId, contentStream, MAJOR);

		} finally {
			IOUtils.closeQuietly(inputstream);
		}

	}

	private static void createSession() {
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, password);

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, url + "/atom");
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.REPOSITORY_ID, collectionName);

		session = factory.createSession(parameter);
		session.getDefaultContext().setMaxItemsPerPage(100000);
		session.getDefaultContext().setFilterString("*");
		session.getDefaultContext().setCacheEnabled(true);

		// Include every properties
		session.getDefaultContext().setRenditionFilterString("*");
	}
}
