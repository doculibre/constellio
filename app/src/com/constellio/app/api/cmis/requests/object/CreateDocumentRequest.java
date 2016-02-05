package com.constellio.app.api.cmis.requests.object;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.ConstellioCmisException.ConstellioCmisException_UnsupportedVersioningState;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServicesException;

public class CreateDocumentRequest extends CmisCollectionRequest<ContentCmisDocument> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final CallContext context;
	private final Properties properties;
	private final String folderId;
	private final ContentStream contentStream;
	private final VersioningState versioningState;

	public CreateDocumentRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, Properties properties, String folderId, ContentStream contentStream,
			VersioningState versioningState) {
		super(repository, appLayerFactory);
		this.context = context;
		this.properties = properties;
		this.folderId = folderId;
		this.contentStream = contentStream;
		this.versioningState = versioningState;
	}

	@Override
	public ContentCmisDocument process()
			throws ConstellioCmisException {

		String metadataLocalCode = (String) properties.getProperties().get("metadata").getFirstValue();
		User user = (User) context.get(ConstellioCmisContextParameters.USER);

		Record record = modelLayerFactory.newRecordServices().getDocumentById(folderId);
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(record.getCollection());
		MetadataSchema metadataSchema = types.getSchema(record.getSchemaCode());
		Metadata metadata = metadataSchema.getMetadata(metadataLocalCode);

		ContentManager contentManager = modelLayerFactory.getContentManager();
		Content content;
		ContentVersionDataSummary dataSummary = contentManager.upload(contentStream.getStream());
		if (versioningState == VersioningState.MAJOR) {
			content = contentManager.createMajor(user, contentStream.getFileName(), dataSummary);
		} else if (versioningState == VersioningState.MINOR) {
			content = contentManager.createMinor(user, contentStream.getFileName(), dataSummary);
		} else {
			throw new ConstellioCmisException_UnsupportedVersioningState();
		}
		if (metadata.isMultivalue() == true) {
			List<Object> contentsInRecord = new ArrayList<>();
			contentsInRecord.addAll(record.getList(metadata));
			contentsInRecord.add(content);
			record.set(metadata, contentsInRecord);
		} else {
			record.set(metadata, content);
		}
		try {
			modelLayerFactory.newRecordServices().update(record);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return ContentCmisDocument.createForVersionSeenBy(content, record, metadataLocalCode, user);
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
