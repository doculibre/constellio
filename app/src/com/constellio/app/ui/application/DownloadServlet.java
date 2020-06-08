package com.constellio.app.ui.application;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;

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

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		ContentManager contentManager = modelLayerFactory.getContentManager();
		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();

		Record record = modelLayerFactory.newRecordServices().getDocumentById(documentId);

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(record.getCollection(), appLayerFactory);
		Document document = rm.wrapDocument(record);

		if (document.isPublished() && document.isActiveAuthorization() && document.getContent() != null) {
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
}
