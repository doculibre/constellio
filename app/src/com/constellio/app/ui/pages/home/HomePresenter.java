package com.constellio.app.ui.pages.home;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.navigation.PageItem;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
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

	public List<PageItem> getTabs() {
		return navigationConfig().getFragments(HomeView.TABS);
	}

	public String getDefaultTab() {
		String startTab = getCurrentUser().getStartTab();
		if (startTab == null) {
			startTab = presenterService().getSystemConfigs().getDefaultStartTab();
		}
		return startTab;
	}

	public void tabSelected(String tabCode) {
		currentTab = tabCode;
	}

	public void recordClicked(String id, String taxonomyCode) {
		if (id != null && !id.startsWith("dummy")) {
			try {
				Record record = getRecord(id);
				String schemaCode = record.getSchemaCode();
				String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
				if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
					view.getUIContext().setAttribute(FolderDocumentBreadcrumbTrail.TAXONOMY_CODE, taxonomyCode);
					view.navigate().to(RMViews.class).displayFolder(id);
				} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
					view.getUIContext().setAttribute(FolderDocumentBreadcrumbTrail.TAXONOMY_CODE, taxonomyCode);
					view.navigate().to(RMViews.class).displayDocument(id);
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
                        SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
                        RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
                        if (rmConfigs.isAgentEnabled()) {
                            String agentSmbPath = ConstellioAgentUtils.getAgentSmbURL(smbPath);
                            view.openURL(agentSmbPath);
                        } else {
							String path = smbPath;
							if (StringUtils.startsWith(path, "smb://")) {
								path = "file://" + StringUtils.removeStart(path, "smb://");
							}
							view.openURL(path);
                        }
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
