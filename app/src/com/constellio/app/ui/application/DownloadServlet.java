package com.constellio.app.ui.application;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DownloadServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {

		String documentId = req.getParameter("id");

		SystemWideUserInfos user = authenticate(req);

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		ContentManager contentManager = modelLayerFactory.getContentManager();
		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();

		Record record = modelLayerFactory.newRecordServices().getDocumentById(documentId);

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(record.getCollection(), appLayerFactory);
		Document document = rm.wrapDocument(record);

		if (isUserReadAccess(user, document) || isDocumentAccessByPublish(document)) {
			ContentVersion version = document.getContent().getLastMajorContentVersion();

			if (version == null) {
				version = document.getContent().getCurrentVersion();
			}

			InputStream inputStream = null;
			try {
				inputStream = contentManager.getContentInputStream(version.getHash(), "download");
				// modifies response
				response.setContentType(version.getMimetype());
				response.setContentLength((int) version.getLength());

				// forces download
				String headerKey = "Content-Disposition";
				String headerValue = String.format("attachment; filename=\"%s\"", version.getFilename());
				response.setHeader(headerKey, headerValue);

				//CORS policy
				response.setHeader("Access-Control-Allow-Origin", "*");

				// obtains response's output stream
				OutputStream outStream = response.getOutputStream();

				byte[] buffer = new byte[4096];
				int bytesRead;

				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, bytesRead);
				}

				outStream.close();
			} finally {
				ioServices.closeQuietly(inputStream);
			}
		}
	}

	private boolean isDocumentAccessByPublish(Document document) {
		return document.isPublished() && document.isActiveAuthorization() && document.getContent() != null;
	}

	private boolean isUserReadAccess(SystemWideUserInfos user, Document document){
		if(user !=null){
			User userRecord = getUserService().getUserInCollection(user.getUsername(),document.getCollection());
			return userRecord.hasReadAccess().on(document);
		} else {
			return false;
		}
	}

	public SystemWideUserInfos authenticate(HttpServletRequest request) {
		HttpServletRequestAuthenticator authenticator = new HttpServletRequestAuthenticator(modelLayerFactory());
		SystemWideUserInfos user = authenticator.authenticate(request);

		return user;
	}

	private synchronized ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	private UserServices getUserService() {
		return new UserServices(modelLayerFactory());
	}

	private ModelLayerFactory modelLayerFactory() {
		return getConstellioFactories().getModelLayerFactory();
	}
}
