package com.constellio.app.modules.rm.ui.components.breadcrumb;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbTrail;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.SchemaUtils;

public class FolderDocumentBreadcrumbTrailPresenter implements Serializable {

	private String recordId;

	private BreadcrumbTrail breadcrumbTrail;

	private transient SchemaPresenterUtils folderPresenterUtils;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

	public FolderDocumentBreadcrumbTrailPresenter(String recordId, BreadcrumbTrail breadcrumbTrail) {
		this.recordId = recordId;
		this.breadcrumbTrail = breadcrumbTrail;
		initTransientObjects();
		addBreadcrumbItems();
	}

	private void addBreadcrumbItems() {
		List<BreadcrumbItem> breadcrumbItems = new ArrayList<>();

		String currentRecordId = recordId;
		while (currentRecordId != null) {
			Record record = folderPresenterUtils.getRecord(currentRecordId);
			String schemaCode = record.getSchemaCode();
			String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
			if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
				breadcrumbItems.add(0, new FolderBreadcrumbItem(currentRecordId));

				Folder folder = rmSchemasRecordsServices.getFolder(currentRecordId);
				currentRecordId = folder.getParentFolder();
			} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
				breadcrumbItems.add(new DocumentBreadcrumbItem(currentRecordId));

				Document document = rmSchemasRecordsServices.getDocument(currentRecordId);
				currentRecordId = document.getFolder();
			} else {
				throw new RuntimeException("Unrecognized schema type code : " + schemaTypeCode);
			}
		}
		for (BreadcrumbItem breadcrumbItem : breadcrumbItems) {
			breadcrumbTrail.addItem(breadcrumbItem);
		}
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ConstellioFactories constellioFactories = breadcrumbTrail.getConstellioFactories();
		SessionContext sessionContext = breadcrumbTrail.getSessionContext();
		String collection = sessionContext.getCurrentCollection();

		folderPresenterUtils = new SchemaPresenterUtils(Folder.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, breadcrumbTrail);
	}

	public void itemClicked(BreadcrumbItem item) {
		if (item instanceof FolderBreadcrumbItem) {
			String folderId = ((FolderBreadcrumbItem) item).getFolderId();
			breadcrumbTrail.navigate().to(RMViews.class).displayFolder(folderId);
		} else {
			String documentId = ((DocumentBreadcrumbItem) item).getDocumentId();
			breadcrumbTrail.navigateTo().displayDocument(documentId);
		}
	}

	class FolderBreadcrumbItem implements BreadcrumbItem {

		private String folderId;

		FolderBreadcrumbItem(String folderId) {
			this.folderId = folderId;
		}

		public final String getFolderId() {
			return folderId;
		}

		@Override
		public String getLabel() {
			return SchemaCaptionUtils.getCaptionForRecordId(folderId);
		}

		@Override
		public boolean isEnabled() {
			boolean enabled;
			if (folderId.equals(recordId)) {
				enabled = false;
			} else {
				Record record = folderPresenterUtils.getRecord(folderId);
				User user = folderPresenterUtils.getCurrentUser();
				enabled = user.hasReadAccess().on(record);
			}
			return enabled;
		}

	}

	class DocumentBreadcrumbItem implements BreadcrumbItem {

		private String documentId;

		DocumentBreadcrumbItem(String documentId) {
			this.documentId = documentId;
		}

		public final String getDocumentId() {
			return documentId;
		}

		@Override
		public String getLabel() {
			return SchemaCaptionUtils.getCaptionForRecordId(documentId);
		}

		@Override
		public boolean isEnabled() {
			// Always last item
			return false;
		}

	}

}
