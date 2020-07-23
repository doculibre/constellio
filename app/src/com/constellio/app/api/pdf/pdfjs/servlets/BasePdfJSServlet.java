package com.constellio.app.api.pdf.pdfjs.servlets;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.api.pdf.pdfjs.services.PdfJSServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.ExternalAccessUser;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.pdf.pdfjs.signature.PdfJSAnnotations;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.UUID;

public abstract class BasePdfJSServlet extends HttpServlet {

	protected HttpServletRequestAuthenticator authenticator;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		authenticator = new HttpServletRequestAuthenticator(modelLayerFactory);
	}

	@Override
	protected final void service(HttpServletRequest request, HttpServletResponse response)
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
			//			else if ("post".equalsIgnoreCase(request.getMethod())) {
			//				response.addHeader("Access-Control-Allow-Origin", origin);
			//				response.addHeader("Access-Control-Allow-Credentials", "true");
			//				super.service(request, response);
			//				return;
			//			}
		}

		String recordId = request.getParameter("recordId");
		String metadataCode = request.getParameter("metadataCode");
		String localeCode = request.getParameter("locale");
		String accessId = request.getParameter("accessId");

		ModelLayerFactory modelLayerFactory = getAppLayerFactory().getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		Record record = recordServices.getDocumentById(recordId);
		MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaOf(record);
		Metadata metadata = schema.get(metadataCode);
		String collection = record.getCollection();

		SystemWideUserInfos userCredentials = authenticator.authenticate(request);
		User user;
		if (userCredentials != null) {
			user = getUser(userCredentials.getUsername(), collection);
		} else if (accessId != null) {
			MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
			RolesManager rolesManager = modelLayerFactory.getRolesManager();

			MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
			MetadataSchema userSchema = types.getDefaultSchema(User.SCHEMA_TYPE);
			Record tempUserRecord = recordServices.newRecordWithSchema(userSchema, UUID.randomUUID().toString());
			Roles roles = rolesManager.getCollectionRoles(collection);
			ExternalAccessUrl externalAccessUrl = new ExternalAccessUrl(recordServices.getDocumentById(accessId), types);
			user = new ExternalAccessUser(tempUserRecord, types, roles, externalAccessUrl);
		} else {
			user = null;
		}
		if (user != null) {
			doService(record, metadata, user, localeCode, request, response);
		} else {
			throw new ServletException("Null user");
		}
	}

	/**
	 * Check that the page Origin header is allowed.
	 */
	private boolean isAllowedRequestOrigin(String origin) {
		// TODO: Remember to limit the origins.
		return origin.matches(".*");
	}

	protected AppLayerFactory getAppLayerFactory() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		return constellioFactories.getAppLayerFactory();
	}

	protected User getUser(String username, String collection) {
		UserServices userServices = getAppLayerFactory().getModelLayerFactory().newUserServices();
		return userServices.getUserInCollection(username, collection);
	}

	protected PdfJSServices newPdfJSServices() {
		return new PdfJSServices(getAppLayerFactory());
	}

	protected String readRequestInputStream(HttpServletRequest request) throws IOException {
		String result;
		try (InputStream in = request.getInputStream()) {
			result = IOUtils.toString(in, "UTF-8");
		}
		return result;
	}

	protected PdfJSAnnotations getAnnotationsFromRequest(HttpServletRequest request) throws IOException {
		PdfJSAnnotations result;
		String jsonString = readRequestInputStream(request);
		if (StringUtils.isNotBlank(jsonString)) {
			JSONObject jsonObject = new JSONObject(jsonString);
			result = new PdfJSAnnotations(jsonObject);
		} else {
			result = null;
		}
		return result;
	}

	protected void writeJSONResponse(JSONObject jsonObject, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		writeResponse(jsonObject.toString(4), request, response);
	}

	protected void writeResponse(String content, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter writer = response.getWriter();
		writer.write(content);
		writer.flush();
		writer.close();
	}

	protected abstract void doService(
			Record record,
			Metadata metadata,
			User user,
			String localeCode,
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException;

}
