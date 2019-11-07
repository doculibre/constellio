package com.constellio.app.ui.framework.components.display;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import org.apache.commons.lang.StringUtils;
import org.vaadin.activelink.ActiveLink;

import java.net.URI;

public class ReferenceActiveLink extends ActiveLink {

	private final String path;
	private final String recordId;

	public ReferenceActiveLink(String path, String recordId) {
		this.path = path;
		this.recordId = recordId;
	}

	@Override
	public void attach() {
		super.attach();

		configureCaption();

		configureResource();

		configureIcon();
	}

	protected void configureIcon() {
		Resource icon = FileIconUtils.getIconForRecordId(recordId);
		if (icon != null) {
			setIcon(icon);
		}
	}

	protected void configureResource() {
		URI location = getUI().getPage().getLocation();

		StringBuilder url = new StringBuilder();
		url.append(location.getScheme());
		url.append(":");
		url.append(location.getSchemeSpecificPart());

		url.append("#!" + StringUtils.trimToEmpty(this.path) + "/" + this.recordId);

		setResource(new ExternalResource(url.toString()));
	}

	protected void configureCaption() {
		StringBuilder caption = new StringBuilder();
		caption.append(new RecordIdToCaptionConverter().convertToPresentation(recordId, String.class, getLocale()));

		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();

		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		Record record = modelLayerFactory.newRecordServices().getDocumentById(recordId);
		String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());

		SessionContext currentSessionContext = ConstellioUI.getCurrentSessionContext();
		User user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(currentSessionContext.getCurrentUser().getUsername(), currentSessionContext.getCurrentCollection());
		MetadataSchemasManager metadataSchemaManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		if (metadataSchemaManager.getSchemaTypeOf(record).hasSecurity()) {
			this.setEnabled(user.hasReadAccess().on(record));
		} else {
			AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(currentSessionContext.getCurrentCollection());
			this.setEnabled(extensions.doesUserHaveReadAcessToRecord(true, user, record));
		}

		if (Document.SCHEMA_TYPE.equals(schemaType)) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(record.getCollection(), appLayerFactory);
			Document document = rm.wrapDocument(record);

			if (StringUtils.isNotBlank(document.getType())) {
				record = modelLayerFactory.newRecordServices().getDocumentById(document.getType());
				DocumentType documentType = rm.wrapDocumentType(record);
				caption.append(" (" + documentType.getTitle() + ")");
			}
		}

		setCaption(caption.toString());
	}
}
