package com.constellio.app.modules.rm.ui.builders;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMExternalLinkVOExtension.RMExternalLinkVOExtensionParams;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

public class ExternalLinkToVOBuilder extends RecordToVOBuilder {

	transient AppLayerFactory appLayerFactory;

	public ExternalLinkToVOBuilder(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public RecordVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO, SessionContext sessionContext) {
		String collection = sessionContext.getCurrentCollection();
		RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		RecordVO result = rmModuleExtensions.buildExternalLinkVO(new RMExternalLinkVOExtensionParams(record, viewMode, schemaVO, sessionContext));
		if (result == null) {
			result = super.build(record, viewMode, schemaVO, sessionContext);
		}
		return result;
	}

}
