package com.constellio.app.modules.rm.ui.pages.decommissioning.breadcrumb;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.components.breadcrumb.DocumentBreadCrumbItem;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderBreadCrumbItem;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrailPresenter;
import com.constellio.app.modules.rm.ui.pages.viewGroups.ArchivesManagementViewGroup;
import com.constellio.app.modules.rm.util.DecommissionNavUtil;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import org.apache.commons.lang.NotImplementedException;

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

		if(!(breadcrumbTrail.getView() instanceof ArchivesManagementViewGroup)) {
			breadcrumbItems.add(new ArchiveManagementBreadCrumbItem());
		}

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

		breadcrumbItems.addAll(FolderDocumentContainerBreadcrumbTrailPresenter.
				getGetFolderDocumentBreadCrumbItems(recordId, folderPresenterUtils, rmSchemasRecordsServices));

		for (BreadcrumbItem breadcrumbItem : breadcrumbItems) {
			breadcrumbTrail.addItem(breadcrumbItem);
		}
	}

	public boolean itemClicked(BreadcrumbItem item) {
		boolean handled;

		if(item instanceof ArchiveManagementBreadCrumbItem) {
			handled = true;
			breadcrumbTrail.navigate().to(RMViews.class).archiveManagement();
		} else if (item instanceof FolderBreadCrumbItem) {
			handled = true;
			String folderId = ((FolderBreadCrumbItem) item).getFolderId();

			breadcrumbTrail.navigate().to(RMViews.class).displayFolder(folderId);

			if(searchId != null && searchType != null) {
				breadcrumbTrail.navigate().to(RMViews.class).displayFolderFromDecommission(folderId,
						DecommissionNavUtil.getHomeUri(breadcrumbTrail.getConstellioFactories().getAppLayerFactory()), false,
						searchId, searchType.toString());
			} else {
				breadcrumbTrail.navigate().to(RMViews.class).displayDocument(folderId);
			}

		} else if (item instanceof DocumentBreadCrumbItem) {
			handled = true;
			String documentId = ((DocumentBreadCrumbItem) item).getDocumentId();

			if(searchId != null && searchType != null) {
				breadcrumbTrail.navigate().to(RMViews.class).displayDocumentFromDecommission(documentId,
						DecommissionNavUtil.getHomeUri(breadcrumbTrail.getConstellioFactories().getAppLayerFactory()), false,
						searchId, searchType.name());
			} else {
				breadcrumbTrail.navigate().to(RMViews.class).displayDocument(documentId);
			}


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

	class ArchiveManagementBreadCrumbItem implements BreadcrumbItem {

		@Override
		public String getLabel() {
			return $("ArchiveManagementView.viewTitle");
		}

		@Override
		public boolean isEnabled() {
			return true;
		}
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

}
