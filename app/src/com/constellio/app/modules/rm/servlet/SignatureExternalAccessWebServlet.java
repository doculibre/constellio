package com.constellio.app.modules.rm.servlet;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.constellio.app.api.pdf.pdfjs.services.PdfJSServices;
import com.constellio.app.modules.rm.wrappers.Document;
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
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.security.roles.RolesManager;

public class SignatureExternalAccessWebServlet extends HttpServlet {
	public static final String HEADER_PARAM_AUTH = "Authorization";

	public static final String PARAM_ID = "id";
	public static final String PARAM_TOKEN = "token";
	public static final String PARAM_SERVICE_KEY = "serviceKey";
	public static final String PARAM_DOCUMENT = "document";
	public static final String PARAM_EXTERNAL_USER_FULLNAME = "externalUserFullname";
	public static final String PARAM_EXPIRATION_DATE = "expirationDate";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		SignatureExternalAccessService service = new SignatureExternalAccessService();

		try {
			String accessId = req.getParameter(PARAM_ID);
			String documentId = req.getParameter(PARAM_DOCUMENT);
			String metadataCode = Document.CONTENT; // TODO Validate
			String serviceKey = req.getParameter(PARAM_SERVICE_KEY);
			String token = req.getParameter(PARAM_TOKEN);
			
			service.accessExternalSignature(accessId, token);

			resp.setStatus(HttpServletResponse.SC_OK);
			// TODO::JOLA --> Redirect to pdf.js viewer for requested document
			
			// TODO: :JOLA --> Move code in service
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
			RolesManager rolesManager = modelLayerFactory.getRolesManager();

			PdfJSServices pdfJSServices = new PdfJSServices(appLayerFactory);
			Record record = recordServices.getDocumentById(documentId);
			Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaOf(record).get(metadataCode);
			Locale locale = req.getLocale(); // TODO Param?
			String collection = record.getCollection();
			
			MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
			MetadataSchema userSchema = types.getDefaultSchema(User.SCHEMA_TYPE);
			Record tempUserRecord = recordServices.newRecordWithSchema(userSchema, UUID.randomUUID().toString());
			Roles roles = rolesManager.getCollectionRoles(collection);
			ExternalAccessUrl externalAccessUrl = new ExternalAccessUrl(recordServices.getDocumentById(accessId), types);
			ExternalAccessUser user = new ExternalAccessUser(tempUserRecord, types, roles, externalAccessUrl);
			
			String pdfJSViewerUrl = pdfJSServices.getExternalViewerUrl(record, metadata, user, locale, serviceKey, token, true);
			resp.sendRedirect(pdfJSViewerUrl);
		} catch (SignatureExternalAccessServiceException e) {
			resp.sendError(e.getStatus(), e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		SignatureExternalAccessService service = new SignatureExternalAccessService();

		try {
			String url = service.createExternalSignatureUrl(req.getHeader(HEADER_PARAM_AUTH),
					req.getParameter(PARAM_SERVICE_KEY), req.getParameter(PARAM_DOCUMENT),
					req.getParameter(PARAM_EXTERNAL_USER_FULLNAME), req.getParameter(PARAM_EXPIRATION_DATE));

			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getOutputStream().print(url);
		} catch (SignatureExternalAccessServiceException e) {
			resp.sendError(e.getStatus(), e.getMessage());
		}
	}
}
