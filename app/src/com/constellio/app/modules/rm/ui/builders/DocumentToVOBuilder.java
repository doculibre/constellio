package com.constellio.app.modules.rm.ui.builders;

import java.io.IOException;
import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.factories.ModelLayerFactory;

public class DocumentToVOBuilder extends RecordToVOBuilder {

	transient boolean parsedContentFetched;
	transient ParsedContent parsedContent;

	transient ModelLayerFactory modelLayerFactory;

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
	}

	@Override
	public DocumentVO build(Record record, VIEW_MODE viewMode) {
		return (DocumentVO) super.build(record, viewMode);
	}

	@Override
	public DocumentVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (DocumentVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	public DocumentVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO) {
		return (DocumentVO) super.build(record, viewMode, schemaVO);
	}

	@Override
	protected DocumentVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode) {
		return new DocumentVO(id, metadataValueVOs, viewMode);
	}

	@Override
	protected Object getValue(Record record, Metadata metadata) {
		if (isExtractedMetadata(metadata)) {
			if (!parsedContentFetched) {
				parsedContentFetched = true;
				ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
				ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();

				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(record.getCollection(), modelLayerFactory);
				Content content = rm.wrapDocument(record).getContent();
				if (content != null) {
					ContentVersion contentVersion = content.getCurrentVersion();
					try {
						parsedContent = modelLayerFactory.getContentManager().getParsedContent(contentVersion.getHash());
					} catch (ContentManagerRuntimeException_NoSuchContent e) {
						//OK
					}
				}
			}
		}

		return super.getValue(record, metadata);
	}

	private boolean isExtractedMetadata(Metadata metadata) {
		String localCode = metadata.getLocalCode();
		return localCode.equals("author") || localCode.equals("company") || localCode.equals("subject");
	}
}
