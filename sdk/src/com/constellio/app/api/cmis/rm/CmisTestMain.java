package com.constellio.app.api.cmis.rm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public class CmisTestMain {

	static Session session;

	public static void main(String argv[])
			throws IOException {

		String url = argv[0];
		String username = argv[1];
		String password = argv[2];
		String collection = argv[3];
		String id = argv[4];

		createSession(url, username, password, collection);

		Folder folder = (Folder) session.getObject(id);
		System.out.println(folder.getId() + "-" + folder.getName());
		Iterator<CmisObject> objectIterator = folder.getChildren().iterator();
		while (objectIterator.hasNext()) {
			CmisObject object = objectIterator.next();
			System.out.println("\t" + object.getId() + "-" + object.getName());
		}

	}

	private static void createSession(String url, String username, String password, String collection) {
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		parameter.put(SessionParameter.USER, username);
		parameter.put(SessionParameter.PASSWORD, password);

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, url + "/atom");
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.REPOSITORY_ID, collection);

		session = factory.createSession(parameter);
		session.getDefaultContext().setMaxItemsPerPage(100000);
		session.getDefaultContext().setFilterString("*");
		session.getDefaultContext().setCacheEnabled(true);

		// Include every properties
		session.getDefaultContext().setRenditionFilterString("*");
	}
}
