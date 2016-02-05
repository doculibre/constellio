package com.constellio.app.api.cmis.requests.object;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.ConstellioCmisException.ConstellioCmisException_RecordServicesError;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.utils.CmisContentUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

public class DeleteObjectRequest extends CmisCollectionRequest<Boolean> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final String objectId;

	public DeleteObjectRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			String objectId) {
		super(repository, appLayerFactory);
		this.objectId = objectId;
	}

	@Override
	public Boolean process()
			throws ConstellioCmisException {
		if (objectId.startsWith("content_")) {
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager()
					.getSchemaTypes(repository.getCollection());
			ContentCmisDocument content = CmisContentUtils.getContent(objectId, recordServices, types);
			Record record = content.getRecord();
			String metadataCode = record.getSchemaCode() + "_" + content.getMetadataLocalCode();
			Metadata metadata = types.getMetadata(metadataCode);
			if (metadata.isMultivalue() == true) {
				List<Object> contentsInRecord = new ArrayList<>();
				contentsInRecord.addAll(record.getList(metadata));
				contentsInRecord.remove(content.getContent());
				record.set(metadata, contentsInRecord);
			} else {
				record.set(metadata, null);
			}
			try {
				recordServices.update(record);
			} catch (RecordServicesException e) {
				throw new ConstellioCmisException_RecordServicesError(e);
			}
		}
		return true;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
