package com.constellio.app.api.cmis.requests.object;

import java.io.InputStream;
import java.math.BigInteger;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PartialContentStreamImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.utils.CmisContentUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;

public class GetContentStreamRequest extends CmisCollectionRequest<ContentStream> {

	private static final String READ_CONTENT_STREAM = "GetContentStreamRequest-ReadContentStream";

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final String objectId;
	private final BigInteger offset;
	private final BigInteger length;

	public GetContentStreamRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, String objectId, BigInteger offset, BigInteger length) {
		super(context, repository, appLayerFactory);
		this.objectId = objectId;
		this.offset = offset;
		this.length = length;
	}

	@Override
	public ContentStream process() {
		ContentCmisDocument contentCmisDocument = CmisContentUtils.getContent(objectId, recordServices, types());
		ensureUserHasAllowableActionsOnRecord(contentCmisDocument.getRecord(), Action.CAN_GET_CONTENT_STREAM);
		InputStream stream = contentManager
				.getContentInputStream(contentCmisDocument.getContentVersion().getHash(), READ_CONTENT_STREAM);
		ContentVersion version = contentCmisDocument.getContentVersion();

		ContentStreamImpl result;
		if ((offset != null && offset.longValue() > 0) || length != null) {
			result = new PartialContentStreamImpl();
		} else {
			result = new ContentStreamImpl();
		}

		result.setFileName(version.getFilename());
		result.setLength(BigInteger.valueOf(version.getLength()));
		result.setMimeType(version.getMimetype());
		result.setStream(stream);

		return result;
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
