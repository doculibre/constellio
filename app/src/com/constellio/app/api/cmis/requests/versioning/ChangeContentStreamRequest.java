package com.constellio.app.api.cmis.requests.versioning;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_InvalidArgument;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.binding.utils.CmisContentUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.extensions.api.cmis.params.GetObjectParams;
import com.constellio.app.extensions.api.cmis.params.UpdateDocumentParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

public class ChangeContentStreamRequest extends CmisCollectionRequest<Boolean> {

	private static final String TEMP_FILE_RESOURCE_NAME = "ChangeContentStreamRequest-TempFile";
	private static final String COPY_TO_TEMP_FILE = "ChangeContentStreamRequest-CopyToTempFile";
	private static final String READ_TEMP_FILE = "ChangeContentStreamRequest-ReadTempFile";

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private static final int BUFFER_SIZE = 64 * 1024;
	private final Holder<String> objectId;
	private final Boolean overwriteFlag;
	private final ContentStream contentStream;
	private final boolean append;

	public ChangeContentStreamRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, Holder<String> objectId, Boolean overwriteFlag, ContentStream contentStream, boolean append) {
		super(context, repository, appLayerFactory);
		this.objectId = objectId;
		this.overwriteFlag = overwriteFlag;
		this.contentStream = contentStream;
		this.append = append;
	}

	/**
	 * CMIS setContentStream, deleteContentStream, and appendContentStream.
	 */
	@Override
	public Boolean process() {

		if (objectId == null) {
			throw new CmisExceptions_InvalidArgument("Id");
		}
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		ContentCmisDocument contentCmisDocument = CmisContentUtils.getContent(objectId.getValue(), recordServices, types);
		ensureUserHasAllowableActionsOnRecord(contentCmisDocument.getRecord(), Action.CAN_SET_CONTENT_STREAM);
		Content content = contentCmisDocument.getContent();

		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		ContentManager contentManager = modelLayerFactory.getContentManager();

//		UpdateDocumentParams params = new UpdateDocumentParams(user, contentCmisDocument.getRecord());
		//		appLayerFactory.getExtensions().forCollection(collection).onUpdateCMISDocument(params);

		setContent(user, recordServices, contentCmisDocument, content, ioServices, contentManager);

		return true;
	}

	private void setContent(User user, RecordServices recordServices, ContentCmisDocument contentCmisDocument, Content content,
			IOServices ioServices, ContentManager contentManager) {
		File file = null;
		OutputStream out = null;
		InputStream inFromCopy = null;
		try {
			file = ioServices.newTemporaryFile(TEMP_FILE_RESOURCE_NAME);
			out = ioServices.newFileOutputStream(file, COPY_TO_TEMP_FILE);
			ioServices.copy(contentStream.getStream(), out);
			ioServices.closeQuietly(out);
			inFromCopy = ioServices.newFileInputStream(file, READ_TEMP_FILE);

			if (content.getCheckoutUserId() != null) {
				if (user.getId().equals(content.getCheckoutUserId())) {
					ContentVersionDataSummary dataSummary = contentManager.upload(inFromCopy);
					content.updateCheckedOutContentWithName(dataSummary, contentStream.getFileName());
				} else {
					throw new RuntimeException("TODO : Cannot modify content checked out by other user");
				}
			} else {
				ContentVersionDataSummary dataSummary = contentManager.upload(inFromCopy);
				content.updateContentWithName(user, dataSummary, false, contentStream.getFileName());
			}
			recordServices.update(contentCmisDocument.getRecord(), user);
		} catch (IOException | RecordServicesException e) {
			throw new RuntimeException(e);
		} finally {
			ioServices.closeQuietly(out);
			ioServices.closeQuietly(inFromCopy);
			ioServices.deleteQuietly(file);
		}
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
