/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
		documentProperties.put("document_default_title", title);
		documentProperties.put("document_default_folder", savedInFolder);
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
