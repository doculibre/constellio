package com.constellio.app.ui.framework.components.table;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.enums.TableMode;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.structures.TablePanelProperties;
import com.constellio.model.services.configs.UserConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.users.UserServices;

public class TableModeManager {

	private UserConfigurationsManager userConfigManager;
	private User currentUser;
	private ConstellioEIMConfigs configs;

	public TableModeManager() {
		ModelLayerFactory modelLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory();
		userConfigManager = modelLayerFactory.getUserConfigurationsManager();
		configs = modelLayerFactory.getSystemConfigs();

		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		String username = null;
		if (currentUserVO != null) {
			username = currentUserVO.getUsername();
		}

		String collection = null;
		if (sessionContext.getCurrentCollection() != null) {
			collection = sessionContext.getCurrentCollection();
		}

		if (collection != null && username != null) {
			UserServices userServices = modelLayerFactory.newUserServices();
			currentUser = userServices.getUserInCollection(username, collection);
		}
	}

	public TableMode getTableModeForCurrentUser(String tablePanelId) {
		if (currentUser != null) {
			TablePanelProperties properties = userConfigManager.getTablePanelPropertiesValue(currentUser, tablePanelId);
			String tableMode = properties.getTableMode();
			if (tableMode != null) {
				return TableMode.byCode(tableMode);
			}
		}
		return configs.getDefaultTableMode();
	}

	public void saveTableModeForCurrentUser(String tablePanelId, TableMode tableMode) {
		if (currentUser != null) {
			TablePanelProperties properties = userConfigManager.getTablePanelPropertiesValue(currentUser, tablePanelId);
			if (!tableMode.getCode().equals(properties.getTableMode())) {
				properties.setTableMode(tableMode.getCode());
				userConfigManager.setTablePanelPropertiesValue(currentUser, tablePanelId, properties);
			}
		}
	}

}
