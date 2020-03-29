package com.constellio.app.api.content;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GetRecordContentServlet extends HttpServlet {

	private HttpServletRequestAuthenticator authenticator;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		authenticator = new HttpServletRequestAuthenticator(modelLayerFactory);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String origin = request.getHeader("Origin");
		if (origin != null && isAllowedRequestOrigin(origin)) {
			// Handle a preflight "option" requests
			if ("options".equalsIgnoreCase(request.getMethod())) {
				response.addHeader("Access-Control-Allow-Origin", origin);
				response.setHeader("Allow", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS");

				// allow the requested method
				String method = request.getHeader("Access-Control-Request-Method");
				response.addHeader("Access-Control-Allow-Methods", method);

				// allow the requested headers
				String headers = request.getHeader("Access-Control-Request-Headers");
				response.addHeader("Access-Control-Allow-Headers", headers);

				response.addHeader("Access-Control-Allow-Credentials", "true");
				response.setContentType("text/plain");
				response.setCharacterEncoding("utf-8");
				response.getWriter().flush();
				return;
			} // Handle UIDL post requests
			else if ("post".equalsIgnoreCase(request.getMethod())) {
				response.addHeader("Access-Control-Allow-Origin", origin);
				response.addHeader("Access-Control-Allow-Credentials", "true");
				super.service(request, response);
				return;
			}
		}

		String recordId = request.getParameter("recordId");
		String metadataCode = request.getParameter("metadataCode");
		boolean preview = "true".equalsIgnoreCase(request.getParameter("preview"));

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();

		RecordServices recordServices = modelLayerFactory.newRecordServices();
		ContentManager contentManager = modelLayerFactory.getContentManager();
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		UserServices userServices = modelLayerFactory.newUserServices();

		Record record = recordServices.getDocumentById(recordId);
		String collection = record.getCollection();
		MetadataSchema schema = schemasManager.getSchemaOf(record);
		Metadata contentMetadata = schema.get(metadataCode);
		Content content = record.get(contentMetadata);

		UserCredential userCredentials = authenticator.authenticate(request);
		if (userCredentials == null) {
			throw new ServletException("User not authenticated");
		} else {
			String username = userCredentials.getUsername();
			User user = userServices.getUserInCollection(username, collection);
			if (user != null && user.hasReadAccess().on(record)) {
				ContentVersion contentVersion = content.getCurrentVersionSeenBy(user);
				String contentHash = contentVersion.getHash();

				String mimeType;
				int contentLength;
				String filename = contentVersion.getFilename();
				InputStream in;
				if (preview && contentManager.hasContentPreview(contentHash)) {
					mimeType = "application/pdf";
					contentLength = -1;
					filename += ".pdf";
					in = contentManager.getContentPreviewInputStream(contentHash, getClass().getName());
				} else {
					// gets MIME type of the file
					mimeType = contentVersion.getMimetype();
					if (mimeType == null) {
						// set to binary type if MIME mapping not found
						mimeType = "application/octet-stream";
					}
					contentLength = (int) contentVersion.getLength();
					in = contentManager.getContentInputStream(contentHash, getClass().getName());
				}

				// modifies response
				response.setContentType(mimeType);
				if (contentLength != -1) {
					response.setContentLength(contentLength);
				}

				// forces download
				String headerKey = "Content-Disposition";
				String headerValue = String.format("attachment; filename=\"%s\"", filename);
				response.setHeader(headerKey, headerValue);

				try (OutputStream out = response.getOutputStream()) {
					IOUtils.copy(in, out);
				} finally {
					IOUtils.closeQuietly(in);
				}
			} else {
				throw new ServletException("Username " + username + " doesn't have permission to see record " + record.getId() + "'s content");
			}
		}
	}

	/**
	 * Check that the page Origin header is allowed.
	 */
	private boolean isAllowedRequestOrigin(String origin) {
		// TODO: Remember to limit the origins.
		return origin.matches(".*");
	}

}
