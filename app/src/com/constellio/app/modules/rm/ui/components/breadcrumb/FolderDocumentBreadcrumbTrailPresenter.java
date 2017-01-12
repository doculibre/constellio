package com.constellio.app.modules.rm.ui.components.breadcrumb;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class FolderDocumentBreadcrumbTrailPresenter implements Serializable {

	private String recordId;
	
	private String taxonomyCode;
	
	private String collection;

	private FolderDocumentBreadcrumbTrail breadcrumbTrail;
	
	private transient TaxonomiesManager taxonomiesManager;
	
	private transient SchemaPresenterUtils folderPresenterUtils;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

	public FolderDocumentBreadcrumbTrailPresenter(String recordId, String taxonomyCode, FolderDocumentBreadcrumbTrail breadcrumbTrail) {
		this.recordId = recordId;
		this.taxonomyCode = taxonomyCode;
		this.breadcrumbTrail = breadcrumbTrail;
		initTransientObjects();
		addBreadcrumbItems();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ConstellioFactories constellioFactories = breadcrumbTrail.getConstellioFactories();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		
		SessionContext sessionContext = breadcrumbTrail.getSessionContext();
		collection = sessionContext.getCurrentCollection();

		folderPresenterUtils = new SchemaPresenterUtils(Folder.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, breadcrumbTrail);
	}

	private void addBreadcrumbItems() {
		List<BreadcrumbItem> breadcrumbItems = new ArrayList<>();
		
		String currentRecordId = recordId;
		while (currentRecordId != null) {
			Record currentRecord = folderPresenterUtils.getRecord(currentRecordId);
			String currentSchemaCode = currentRecord.getSchemaCode();
			String currentSchemaTypeCode = SchemaUtils.getSchemaTypeCode(currentSchemaCode);
			if (Folder.SCHEMA_TYPE.equals(currentSchemaTypeCode)) {
				breadcrumbItems.add(0, new FolderBreadcrumbItem(currentRecordId));

				Folder folder = rmSchemasRecordsServices.wrapFolder(currentRecord);
				currentRecordId = folder.getParentFolder();
			} else if (Document.SCHEMA_TYPE.equals(currentSchemaTypeCode)) {
				breadcrumbItems.add(new DocumentBreadcrumbItem(currentRecordId));

				Document document = rmSchemasRecordsServices.wrapDocument(currentRecord);
				currentRecordId = document.getFolder();
			} else {
				throw new RuntimeException("Unrecognized schema type code : " + currentSchemaTypeCode);
			}
		}

		if (taxonomyCode == null) {
			UIContext uiContext = breadcrumbTrail.getUIContext();
			taxonomyCode = uiContext.getAttribute(FolderDocumentBreadcrumbTrail.TAXONOMY_CODE);
		}

		if (taxonomyCode != null) {
			Record record = folderPresenterUtils.getRecord(recordId);
			String schemaCode = record.getSchemaCode();
			String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
			
			Taxonomy selectedTaxonomy;
			Taxonomy administrativeUnitTaxonomy = taxonomiesManager.getTaxonomyFor(collection, AdministrativeUnit.SCHEMA_TYPE);
			Taxonomy categoryTaxonomy = taxonomiesManager.getTaxonomyFor(collection, Category.SCHEMA_TYPE);

			Folder folder;
			if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
				folder = rmSchemasRecordsServices.wrapFolder(record);
			} else {
				Document document = rmSchemasRecordsServices.wrapDocument(record);
				folder = rmSchemasRecordsServices.getFolder(document.getFolder());
			}
			if (administrativeUnitTaxonomy != null && administrativeUnitTaxonomy.getCode().equals(taxonomyCode)) {
				selectedTaxonomy = administrativeUnitTaxonomy;
				String currentAdministrativeUnitId = folder.getAdministrativeUnit();
				while (currentAdministrativeUnitId != null) {
					AdministrativeUnit currentAdministrativeUnit = rmSchemasRecordsServices.getAdministrativeUnit(currentAdministrativeUnitId);
					breadcrumbItems.add(0, new TaxonomyElementBreadcrumbItem(currentAdministrativeUnitId));
					currentAdministrativeUnitId = currentAdministrativeUnit.getParent();
				}
			} else if (categoryTaxonomy != null && categoryTaxonomy.getCode().equals(taxonomyCode)) {
				selectedTaxonomy = categoryTaxonomy;
				String currentCategoryId = folder.getCategory();
				while (currentCategoryId != null) {
					Category currentCategory = rmSchemasRecordsServices.getCategory(currentCategoryId);
					breadcrumbItems.add(0, new TaxonomyElementBreadcrumbItem(currentCategoryId));
					currentCategoryId = currentCategory.getParent();
				}
			} else {
				selectedTaxonomy = null;
			}
			if (selectedTaxonomy != null) {
				breadcrumbItems.add(0, new TaxonomyBreadcrumbItem(taxonomyCode, selectedTaxonomy.getTitle()));
			}
		}
		
		for (BreadcrumbItem breadcrumbItem : breadcrumbItems) {
			breadcrumbTrail.addItem(breadcrumbItem);
		}
	}

	public void itemClicked(BreadcrumbItem item) {
		if (item instanceof FolderBreadcrumbItem) {
			String folderId = ((FolderBreadcrumbItem) item).getFolderId();
			breadcrumbTrail.navigate().to(RMViews.class).displayFolder(folderId);
		} else {
			String documentId = ((DocumentBreadcrumbItem) item).getDocumentId();
			breadcrumbTrail.navigate().to(RMViews.class).displayDocument(documentId);
		}
	}

	class TaxonomyBreadcrumbItem implements BreadcrumbItem {

		private String taxonomyCode;
		
		private String taxonomyLabel;

		TaxonomyBreadcrumbItem(String taxonomyCode, String taxonomyLabel) {
			this.taxonomyCode = taxonomyCode;
			this.taxonomyLabel = taxonomyLabel;
		}

		public final String getTaxonomyCode() {
			return taxonomyCode;
		}

		@Override
		public String getLabel() {
			return taxonomyLabel;
		}

		@Override
		public boolean isEnabled() {
			return false;
		}

	}

	class TaxonomyElementBreadcrumbItem implements BreadcrumbItem {

		private String taxonomyElementId;

		TaxonomyElementBreadcrumbItem(String taxonomyElementId) {
			this.taxonomyElementId = taxonomyElementId;
		}

		public final String getTaxonomyElementId() {
			return taxonomyElementId;
		}

		@Override
		public String getLabel() {
			return SchemaCaptionUtils.getCaptionForRecordId(taxonomyElementId);
		}

		@Override
		public boolean isEnabled() {
			return false;
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
