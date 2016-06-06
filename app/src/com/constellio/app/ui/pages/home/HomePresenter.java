package com.constellio.app.ui.pages.home;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.entities.navigation.PageItem;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.SchemaUtils;

public class HomePresenter extends BasePresenter<HomeView> {

	private static Logger LOGGER = LoggerFactory.getLogger(HomePresenter.class);

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
	
	public void tabSelected(String tabCode) {
		currentTab = tabCode;
	}

	public void recordClicked(String id, String taxonomyCode) {
		if (id != null && !id.startsWith("dummy")) {
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
			try {
				Record record = getRecord(id);
				String schemaCode = record.getSchemaCode();
				String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
				if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
					view.navigate().to(RMViews.class).displayFolder(id, taxonomyCode);
				} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
					view.navigate().to(RMViews.class).displayDocument(id, taxonomyCode);
				} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
					view.navigate().to(RMViews.class).displayContainer(id);
				} else if (ConstellioAgentUtils.isAgentSupported()) {
					String smbMetadataCode;
					if (ConnectorSmbDocument.SCHEMA_TYPE.equals(schemaTypeCode)) {
						smbMetadataCode = ConnectorSmbDocument.URL;
//					} else if (ConnectorSmbFolder.SCHEMA_TYPE.equals(schemaTypeCode)) {
//						smbMetadataCode = ConnectorSmbFolder.URL;
					} else {
						smbMetadataCode = null;
					}
					if (smbMetadataCode != null) {
						Metadata smbUrlMetadata = types().getMetadata(schemaTypeCode + "_default_" + smbMetadataCode);
						String smbPath = record.get(smbUrlMetadata);
						String agentSmbPath = ConstellioAgentUtils.getAgentSmbURL(smbPath);
						view.openAgentURL(agentSmbPath);
					}
				}
			} catch (NoSuchRecordWithId e) {
				view.showErrorMessage($("HomeView.noSuchRecord"));
				LOGGER.warn("Error while clicking on record id " + id, e);
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
