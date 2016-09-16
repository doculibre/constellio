package com.constellio.app.modules.es.ui.components;

import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.ui.components.content.ConstellioAgentLink;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.ui.Component;

public class SmbSearchResultDisplay extends SearchResultDisplay {

	public SmbSearchResultDisplay(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory, AppLayerFactory appLayerFactory) {
		super(searchResultVO, componentFactory, appLayerFactory);
	}

	@Override
	protected Component newTitleComponent(SearchResultVO searchResultVO) {
		RecordVO recordVO = searchResultVO.getRecordVO();

		String schemaCode = recordVO.getSchema().getCode();
		Component titleComponent;

		SystemConfigurationsManager systemConfigurationsManager = getAppLayerFactory().getModelLayerFactory().getSystemConfigurationsManager();
		RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
		if (rmConfigs.isAgentEnabled() && ConstellioAgentUtils.isAgentSupported() && new SchemaUtils().getSchemaTypeCode(schemaCode).equals(ConnectorSmbDocument.SCHEMA_TYPE)) {
			String smbPath = recordVO.get(ConnectorSmbDocument.URL);
			String agentURL = ConstellioAgentUtils.getAgentSmbURL(smbPath);
			if (agentURL != null) {
				titleComponent = new ConstellioAgentLink(agentURL, null, recordVO.getTitle(), false);
			} else {
				titleComponent = super.newTitleComponent(searchResultVO);
			}
		} else {
			titleComponent = super.newTitleComponent(searchResultVO);
		}

		return titleComponent;
	}
}
