package com.constellio.app.modules.rm.ui.pages.decommissioning.breadcrumb;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrailPresenter;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.SearchViewImpl;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.SchemaUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissionBreadcrumbTrailPresenter implements Serializable {

	private String recordId;
	
	private String collection;

	private DecommissionBreadcrumbTrail breadcrumbTrail;

	private String titre;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

	private transient SchemaPresenterUtils folderPresenterUtils;

	private String searchId;
	private SearchType searchType;

	public DecommissionBreadcrumbTrailPresenter(String titre, SearchType searchType, String searchId, String recordId, DecommissionBreadcrumbTrail breadcrumbTrail) {
		this.recordId = recordId;
		this.titre = titre;
		this.breadcrumbTrail = breadcrumbTrail;
		this.searchId = searchId;
		this.searchType = searchType;
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

		SessionContext sessionContext = breadcrumbTrail.getSessionContext();
		collection = sessionContext.getCurrentCollection();
		folderPresenterUtils = new SchemaPresenterUtils(Folder.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, breadcrumbTrail);
	}

	private void addBreadcrumbItems() {
		List<BreadcrumbItem> breadcrumbItems = new ArrayList<>();

		breadcrumbItems.add(new DispositionListBreadcrumbItem());

		if(searchId == null) {
			breadcrumbItems.add(new DispositionListItemBreadcrumbItem(null, null) {
				@Override
				public String getLabel() {
					return titre;
				}
			});
		} else {
			breadcrumbItems.add(new DispositionListItemBreadcrumbItem(searchId, searchType) {
				@Override
				public String getLabel() {
					return titre;
				}

				@Override
				public boolean isEnabled() {
					return true;
				}
			});
		}

		int size = breadcrumbItems.size();

		String currentRecordId = recordId;
		while (currentRecordId != null) {
			Record currentRecord = folderPresenterUtils.getRecord(currentRecordId);
			String currentSchemaCode = currentRecord.getSchemaCode();
			String currentSchemaTypeCode = SchemaUtils.getSchemaTypeCode(currentSchemaCode);
			if (Folder.SCHEMA_TYPE.equals(currentSchemaTypeCode)) {
				breadcrumbItems.add(size, new FolderBreadcrumbItem(currentRecordId));

				Folder folder = rmSchemasRecordsServices.wrapFolder(currentRecord);
				currentRecordId = folder.getParentFolder();
			} else if (Document.SCHEMA_TYPE.equals(currentSchemaTypeCode)) {
				breadcrumbItems.add(new FolderDocumentBreadcrumbTrailPresenter.DocumentBreadcrumbItem(currentRecordId));

				Document document = rmSchemasRecordsServices.wrapDocument(currentRecord);
				currentRecordId = document.getFolder();
			} else {
				throw new RuntimeException("Unrecognized schema type code : " + currentSchemaTypeCode);
			}
		}

		for (BreadcrumbItem breadcrumbItem : breadcrumbItems) {
			breadcrumbTrail.addItem(breadcrumbItem);
		}
	}

	public boolean itemClicked(BreadcrumbItem item) {
		boolean handled;

		breadcrumbTrail.getUIContext().clearAttribute(SearchViewImpl.DECOMMISSIONING_BUILDER_TYPE);
		breadcrumbTrail.getUIContext().clearAttribute(SearchViewImpl.SAVE_SEARCH_DECOMMISSIONING);

		if (item instanceof DecommissionBreadcrumbTrailPresenter.FolderBreadcrumbItem) {
			handled = true;
			String folderId = ((DecommissionBreadcrumbTrailPresenter.FolderBreadcrumbItem) item).getFolderId();

			breadcrumbTrail.getUIContext().setAttribute(SearchViewImpl.DECOMMISSIONING_BUILDER_TYPE, searchType);
			breadcrumbTrail.getUIContext().setAttribute(SearchViewImpl.SAVE_SEARCH_DECOMMISSIONING, searchId);

			breadcrumbTrail.navigate().to(RMViews.class).displayFolder(folderId);
		} else if (item instanceof FolderDocumentBreadcrumbTrailPresenter.DocumentBreadcrumbItem) {
			handled = true;
			String documentId = ((FolderDocumentBreadcrumbTrailPresenter.DocumentBreadcrumbItem) item).getDocumentId();

			breadcrumbTrail.getUIContext().setAttribute(SearchViewImpl.DECOMMISSIONING_BUILDER_TYPE, searchType);
			breadcrumbTrail.getUIContext().setAttribute(SearchViewImpl.SAVE_SEARCH_DECOMMISSIONING, searchId);

			breadcrumbTrail.navigate().to(RMViews.class).displayDocument(documentId);
		} else if(item instanceof  DispositionListBreadcrumbItem) {
			handled = true;
			breadcrumbTrail.navigate().to(RMViews.class).decommissioning();
		} else if (item instanceof DispositionListItemBreadcrumbItem) {
			handled = true;
			DispositionListItemBreadcrumbItem dispositionListItemBreadcrumbItem = (DispositionListItemBreadcrumbItem) item;
			String lSearchId = dispositionListItemBreadcrumbItem.getSearchId();
			String lType = dispositionListItemBreadcrumbItem.getType().name();

			if(searchId != null) {
				breadcrumbTrail.navigate().to(RMViews.class).decommissioningListBuilderReplay(lType, lSearchId);
			} else {
				breadcrumbTrail.navigate().to(RMViews.class).decommissioningListBuilder(lType);
			}
		} else {
			handled = false;
		}

		return handled;
	}

	class DispositionListBreadcrumbItem implements BreadcrumbItem {

		@Override
		public String getLabel() {
			return $("DecommissioningMainView.viewTitle");
		}

		@Override
		public boolean isEnabled() {
			return true;
		}
	}

	class DispositionListItemBreadcrumbItem implements BreadcrumbItem {
		private String searchId;
		private SearchType type;

		public DispositionListItemBreadcrumbItem(String searchId, SearchType type) {
			this.searchId = searchId;
			this.type = type;
		}

		@Override
		public String getLabel() {
			throw new NotImplementedException();
		}

		public String getSearchId() {
			return searchId;
		}

		public SearchType getType() {
			return type;
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
}
