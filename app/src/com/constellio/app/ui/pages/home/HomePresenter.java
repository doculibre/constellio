package com.constellio.app.ui.pages.home;

import com.constellio.app.entities.navigation.PageItem;
import com.constellio.app.entities.navigation.PageItem.CustomItem;
import com.constellio.app.entities.navigation.PageItem.RecordTree;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class HomePresenter extends BasePresenter<HomeView> {

	private static Logger LOGGER = LoggerFactory.getLogger(HomePresenter.class);

	private String currentTab;

	private List<PageItem> tabItems;
	private List<String> refreshablePageItem;

	public HomePresenter(HomeView view) {
		super(view);
		tabItems = navigationConfig().getFragments(HomeView.TABS);
		refreshablePageItem = navigationConfig().getRefreshable(HomeView.TABS);
	}

	public HomePresenter forParams(String params) {
		if (getCurrentUser() != null) {
			Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
			String tabParam = paramsMap.get("tab");
			String taxonomyCodeParam = paramsMap.get("taxonomyCode");
			String taxonomyMetadataParam = paramsMap.get("taxonomyMetadata");
			String expandedRecordIdParam = paramsMap.get("expandedRecordId");

			if (tabParam == null) {
				currentTab = getDefaultTab();
			} else {
				currentTab = tabParam;
			}

			SessionContext sessionContext = view.getSessionContext();
			if (taxonomyCodeParam != null) {
				// Looking for a tree tab matching current tab 
				loop1:
				for (PageItem tabItem : tabItems) {
					if ((tabItem instanceof RecordTree) && currentTab.equals(tabItem.getCode())) {
						RecordTree recordTree = (RecordTree) tabItem;
						List<RecordLazyTreeDataProvider> dataProviders = recordTree
								.getDataProviders(appLayerFactory, sessionContext);
						for (int i = 0; i < dataProviders.size(); i++) {
							RecordLazyTreeDataProvider dataProvider = dataProviders.get(i);
							String dataProviderTaxonomyCode = dataProvider.getTaxonomyCode();
							if (taxonomyCodeParam.equals(dataProviderTaxonomyCode)) {
								recordTree.setDefaultDataProvider(i);

								if (expandedRecordIdParam != null) {
									Record expandedRecord = getRecord(expandedRecordIdParam);

									List<String> expandedRecordIds = new ArrayList<>();
									expandedRecordIds.add(0, expandedRecordIdParam);

									Record lastAddedParent = null;
									String currentParentId = expandedRecord.getParentId();
									while (currentParentId != null) {
										lastAddedParent = getRecord(currentParentId);
										expandedRecordIds.add(0, currentParentId);
										currentParentId = lastAddedParent.getParentId();
									}

									String taxonomyRecordId;
									if (taxonomyMetadataParam != null) {
										Record recordWithTaxonomyMetadata;
										if (lastAddedParent != null) {
											recordWithTaxonomyMetadata = lastAddedParent;
										} else {
											recordWithTaxonomyMetadata = expandedRecord;
										}
										MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
										MetadataSchema expandedRecordSchema = schemasManager
												.getSchemaOf(recordWithTaxonomyMetadata);
										Metadata taxonomyMetadata = expandedRecordSchema.get(taxonomyMetadataParam);
										taxonomyRecordId = expandedRecord.get(taxonomyMetadata);
									} else {
										taxonomyRecordId = expandedRecordIdParam;
									}
									if (!expandedRecordIds.contains(taxonomyRecordId)) {
										expandedRecordIds.add(0, taxonomyRecordId);
									}

									Record taxonomyRecord = getRecord(taxonomyRecordId);
									String currentTaxonomyRecordParentId = taxonomyRecord.getParentId();
									while (currentTaxonomyRecordParentId != null) {
										Record taxonomyRecordParent = getRecord(currentTaxonomyRecordParentId);
										expandedRecordIds.add(0, currentTaxonomyRecordParentId);
										currentTaxonomyRecordParentId = taxonomyRecordParent.getParentId();
									}

									recordTree.setExpandedRecordIds(expandedRecordIds);
								}

								break loop1;
							}
						}
					}
				}
			}
		} else {
			view.updateUI();
		}
		return this;
	}

	public boolean isRefreshable(String code) {
		return refreshablePageItem.contains(code);
	}

	public List<PageItem> getTabs() {
		return tabItems;
	}

	public boolean isCustomItemVisible(CustomItem customItem) {
		return customItem.getStateFor(getCurrentUser(), appLayerFactory).isVisible();
	}

	public String getDefaultTab() {
		String startTab = getCurrentUser().getStartTab();
		if (startTab == null) {
			startTab = presenterService().getSystemConfigs().getDefaultStartTab();
		}
		return startTab;
	}

	public String getCurrentTab() {
		return currentTab;
	}

	public void tabSelected(String tabCode) {
		currentTab = tabCode;
	}

	public boolean recordClicked(String id, String taxonomyCode, boolean recentItems) {
		boolean navigating = false;
		if (id != null && !id.startsWith("dummy")) {
			try {
				// Recent folders or documents
				if (taxonomyCode == null) {
					taxonomyCode = RMTaxonomies.CLASSIFICATION_PLAN;
				}
				Record record = getRecord(id);
				String schemaCode = record.getSchemaCode();
				String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
				if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
					if (!recentItems) {
						view.getUIContext().setAttribute(BaseBreadcrumbTrail.TAXONOMY_CODE, taxonomyCode);
						view.navigate().to(RMViews.class).displayFolder(id);
						navigating = true;
					} else {
						navigating = false;
					}
				} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
					if (!recentItems) {
						view.getUIContext().setAttribute(BaseBreadcrumbTrail.TAXONOMY_CODE, taxonomyCode);
						view.navigate().to(RMViews.class).displayDocument(id);
						navigating = true;
					} else {
						navigating = false;
					}
				} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
					view.navigate().to(RMViews.class).displayContainer(id);
					navigating = true;
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
						SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory
								.getSystemConfigurationsManager();
						RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
						if (rmConfigs.isAgentEnabled()) {
							RecordVO recordVO = new RecordToVOBuilder().build(record, VIEW_MODE.DISPLAY, view.getSessionContext());
							MetadataVO smbPathMetadata = recordVO.getMetadata(schemaTypeCode + "_default_" + smbMetadataCode);
							String agentSmbPath = ConstellioAgentUtils.getAgentSmbURL(recordVO, smbPathMetadata);
							view.openURL(agentSmbPath);
						} else {
							Metadata smbUrlMetadata = types().getMetadata(schemaTypeCode + "_default_" + smbMetadataCode);
							String smbPath = record.get(smbUrlMetadata);
							String path = smbPath;
							if (StringUtils.startsWith(path, "smb://")) {
								path = "file://" + StringUtils.removeStart(path, "smb://");
							}
							view.openURL(path);
						}
						navigating = true;
					}
				}
			} catch (NoSuchRecordWithId e) {
				view.showErrorMessage($("HomeView.noSuchRecord"));
				LOGGER.warn("Error while clicking on record id " + id, e);
				navigating = false;
			}
		}

		return navigating;
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
