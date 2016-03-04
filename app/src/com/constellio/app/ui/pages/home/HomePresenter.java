package com.constellio.app.ui.pages.home;

import java.util.List;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.entities.navigation.PageItem;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.SchemaUtils;

public class HomePresenter extends BasePresenter<HomeView> {
	private String currentTab;

	public HomePresenter(HomeView view) {
		super(view);
	}

	public HomePresenter forParams(String params) {
		currentTab = params;
		return this;
	}

	public List<NavigationItem> getMenuItems() {
		return navigationConfig().getNavigation(HomeView.ACTION_MENU);
	}

	public List<PageItem> getTabs() {
		return navigationConfig().getFragments(HomeView.TABS);
	}

	public String getDefaultTab() {
		return getCurrentUser().getStartTab();
	}

	public void recordClicked(String id) {
		if (id != null && !id.startsWith("dummy")) {
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
			Record record = getRecord(id);
			String schemaCode = record.getSchemaCode();
			String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
			if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
				view.navigate().to(RMViews.class).displayFolder(id);
			} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
				view.navigateTo().displayDocument(id);
			} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
				view.navigateTo().displayContainer(id);
			}
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	private Record getRecord(String id) {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		return recordServices.getDocumentById(id);
	}
}
