package com.constellio.app.api.cmis.requests.object;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.ConstellioCmisException.ConstellioCmisException_UnsupportedVersioningState;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServicesException;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CreateDocumentRequest extends CmisCollectionRequest<ContentCmisDocument> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final Properties properties;
	private final String folderId;
	private final ContentStream contentStream;
	private final VersioningState versioningState;

	public CreateDocumentRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, Properties properties, String folderId, ContentStream contentStream,
			VersioningState versioningState) {
		super(context, repository, appLayerFactory);
		this.properties = properties;
		this.folderId = folderId;
		this.contentStream = contentStream;
		this.versioningState = versioningState;
	}

	@Override
	public ContentCmisDocument process()
			throws ConstellioCmisException {

		Record record = recordServices.getDocumentById(folderId);
		ensureUserHasAllowableActionsOnRecord(record, Action.CAN_CREATE_DOCUMENT);
		MetadataSchema metadataSchema = types().getSchema(record.getSchemaCode());

		PropertyData<?> property = properties.getProperties().get("metadata");

		Metadata metadata = null;
		if (property == null) {
			for (Metadata aContentMetadata : metadataSchema.getMetadatas().onlyWithType(MetadataValueType.CONTENT)) {
				metadata = aContentMetadata;
				break;
			}
		} else {
			String metadataLocalCode = (String) property.getFirstValue();
			metadata = metadataSchema.getMetadata(metadataLocalCode);
		}

		if (metadata == null) {
			throw new CmisRuntimeException("No content metadata");
		}

		Content content;
		ContentManager.ContentVersionDataSummaryResponse uploadResponse = uploadContent(contentStream.getStream(), contentStream.getFileName());
		ContentVersionDataSummary dataSummary = uploadResponse.getContentVersionDataSummary();
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
			recordServices.execute(new Transaction(record).setUser(user));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

//		CreateDocumentParams params = new CreateDocumentParams(user, record);
		//		appLayerFactory.getExtensions().forCollection(collection).onCreateCMISDocument(params);

		return ContentCmisDocument.createForVersionSeenBy(content, record, metadata.getLocalCode(), user);
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
