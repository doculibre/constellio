package com.constellio.app.modules.rm.ui.components.content;

import java.io.Serializable;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.server.Page;

public class ConstellioAgentClickHandler implements Serializable {

	public void handleClick(String agentURL, RecordVO recordVO, ContentVersionVO contentVersionVO) {
		ConstellioUI ui = ConstellioUI.getCurrent();
		SessionContext sessionContext = ui.getSessionContext();
		ConstellioFactories constellioFactories = ui.getConstellioFactories();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
		if (recordVO != null) {
			String schemaCode = recordVO.getSchema().getCode();
			String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
			if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
				String checkoutUserId = contentVersionVO.getCheckoutUserId();
				String currentUserId = sessionContext.getCurrentUser().getId();
				boolean readOnlyWarning = rmConfigs.isAgentReadOnlyWarning();
				if (checkoutUserId != null && checkoutUserId.equals(currentUserId)) {
					openAgentURL(agentURL);
				} else if (!readOnlyWarning) {
					openAgentURL(agentURL);
				} else {
					DocumentContentVersionWindowImpl warningWindowContent = new DocumentContentVersionWindowImpl(recordVO, contentVersionVO);
					warningWindowContent.openWindow();
				}
			} else {
				openAgentURL(agentURL);
			}
		} else {
			openAgentURL(agentURL);
		}
		
	}
	
	private void openAgentURL(String agentURL) {
		Page.getCurrent().open(agentURL, null);
	}

}
