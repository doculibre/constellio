package com.constellio.app.modules.rm.ui.builders;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.contents.ContentManagerException.ContentManagerException_ContentNotParsed;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.IOException;
import java.util.List;

public class DocumentToVOBuilder extends RecordToVOBuilder {

	transient ModelLayerFactory modelLayerFactory;
	transient AppLayerFactory appLayerFactory;

	public DocumentToVOBuilder(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
	}

	@Override
	public DocumentVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (DocumentVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	protected DocumentVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode, List<String> excludedMetadata) {
		return new DocumentVO(id, metadataValueVOs, viewMode, excludedMetadata);
	}

	@Override
	protected Object getValue(Record record, Metadata metadata) {
		return super.getValue(record, metadata);
	}
}
