package com.constellio.app.api.cmis.requests.versioning;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.utils.CmisContentUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.extensions.api.cmis.params.CheckInParams;
import com.constellio.app.extensions.api.cmis.params.CheckOutParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

public class CheckOutRequest extends CmisCollectionRequest<Boolean> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckOutRequest.class);
	String repositoryId;
	Holder<String> objectId;
	ExtensionsData extension;
	Holder<Boolean> contentCopied;

	public CheckOutRequest(ConstellioCollectionRepository repository, CallContext context,
			AppLayerFactory appLayerFactory,
			String repositoryId,
			Holder<String> objectId, ExtensionsData extension, Holder<Boolean> contentCopied) {
		super(context, repository, appLayerFactory);
		this.repositoryId = repositoryId;
		this.objectId = objectId;
		this.extension = extension;
		this.contentCopied = contentCopied;
	}

	@Override
	protected Boolean process()
			throws ConstellioCmisException {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		ContentCmisDocument content = CmisContentUtils.getContent(objectId.getValue(), recordServices, types);

		ensureUserHasAllowableActionsOnRecord(content.getRecord(), Action.CAN_CHECK_OUT);

		Content pwcContent = content.getContent().checkOut(user);
		ContentCmisDocument updatedContent = CmisContentUtils.getContent(objectId.getValue(), recordServices, types);

		try {
			recordServices.update(content.getRecord());
		} catch (RecordServicesException e) {
			throw new ConstellioCmisException.ConstellioCmisException_RecordServicesError(e);
		}

		CheckOutParams params = new CheckOutParams(user, updatedContent.getRecord());
		appLayerFactory.getExtensions().forCollection(collection).onCheckOut(params);
		objectId.setValue(updatedContent.getPrivateWorkingCopyVersionId());

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
