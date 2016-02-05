package com.constellio.app.api.cmis.requests.versioning;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.ConstellioCmisException.ConstellioCmisException_ContentAlreadyCheckedOut;
import com.constellio.app.api.cmis.ConstellioCmisException.ConstellioCmisException_IOError;
import com.constellio.app.api.cmis.ConstellioCmisException.ConstellioCmisException_RecordServicesError;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.binding.utils.CmisContentUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

public class CheckInRequest extends CmisCollectionRequest<Boolean> {

	private static final String TEMP_FILE_RESOURCE_NAME = "CheckInRequest-TempFile";
	private static final String COPY_TO_TEMP_FILE = "CheckInRequest-CopyToTempFile";
	private static final String READ_TEMP_FILE = "CheckInRequest-ReadTempFile";

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckInRequest.class);

	String repositoryId;
	Holder<String> objectId;
	Boolean major;
	Properties properties;
	ContentStream contentStream;
	String checkinComment;
	List<String> policies;
	Acl addAces;
	org.apache.chemistry.opencmis.commons.data.Acl removeAces;
	ExtensionsData extension;
	CallContext context;

	public CheckInRequest(ConstellioCollectionRepository repository, CallContext context,
			AppLayerFactory appLayerFactory,
			String repositoryId,
			Holder<String> objectId, Boolean major,
			Properties properties, ContentStream contentStream, String checkinComment, List<String> policies,
			Acl addAces, Acl removeAces, ExtensionsData extension) {
		super(repository, appLayerFactory);
		this.context = context;
		this.repositoryId = repositoryId;
		this.objectId = objectId;
		this.major = major;
		this.properties = properties;
		this.contentStream = contentStream;
		this.checkinComment = checkinComment;
		this.policies = policies;
		this.addAces = addAces;
		this.removeAces = removeAces;
		this.extension = extension;
	}

	@Override
	protected Boolean process()
			throws ConstellioCmisException {

		User user = (User) context.get(ConstellioCmisContextParameters.USER);

		RecordServices recordServices = modelLayerFactory.newRecordServices();
		ContentManager contentManager = modelLayerFactory.getContentManager();
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(repository.getCollection());
		ContentCmisDocument contentCmisDocument = CmisContentUtils.getContent(objectId.getValue(), recordServices, types);
		Content content = contentCmisDocument.getContent();

		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();

		File file = null;
		OutputStream out = null;
		InputStream inFromCopy = null;
		if (contentStream != null) {
			try {
				file = ioServices.newTemporaryFile(TEMP_FILE_RESOURCE_NAME);
				out = ioServices.newFileOutputStream(file, COPY_TO_TEMP_FILE);
				ioServices.copy(contentStream.getStream(), out);
				ioServices.closeQuietly(out);
				inFromCopy = ioServices.newFileInputStream(file, READ_TEMP_FILE);

				if (user.getId().equals(content.getCheckoutUserId())) {
					ContentVersionDataSummary dataSummary = contentManager.upload(inFromCopy);
					content.checkInWithModificationAndName(dataSummary, major, contentStream.getFileName());
				} else {
					throw new ConstellioCmisException_ContentAlreadyCheckedOut();
				}
				recordServices.update(contentCmisDocument.getRecord(), user);
			} catch (IOException e) {
				throw new ConstellioCmisException_IOError(e);
			} catch (RecordServicesException e) {
				throw new ConstellioCmisException_RecordServicesError(e);
			} finally {
				ioServices.closeQuietly(out);
				ioServices.closeQuietly(inFromCopy);
				ioServices.deleteQuietly(file);
			}
		} else {

			if (user.getId().equals(content.getCheckoutUserId())) {
				if (major) {
					content.finalizeVersion();
				} else {
					content.checkIn();
				}
			} else {
				throw new ConstellioCmisException_ContentAlreadyCheckedOut();
			}
			try {
				recordServices.update(contentCmisDocument.getRecord(), user);
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
