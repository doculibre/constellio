package com.constellio.app.services.sip.record;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.ead.RecordEADBuilder;
import com.constellio.app.services.sip.mets.MetsContentFileReference;
import com.constellio.app.services.sip.mets.MetsEADMetadataReference;
import com.constellio.app.services.sip.zip.SIPZipWriter;
import com.constellio.app.services.sip.zip.SIPZipWriterTransaction;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.parser.EmailParser;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static org.apache.commons.io.FilenameUtils.getExtension;


public class RecordSIPWriter {

	private static final String READ_VAULT_FILE_STREAM_NAME = RecordSIPWriter.class.getSimpleName() + "-ReadVaultFile";

	private static final String TEMP_EAD_FILE_STREAM_NAME = RecordSIPWriter.class.getSimpleName() + "-ReadVaultFile";

	private SIPZipWriter sipZipWriter;

	private Locale locale;

	private AppLayerFactory appLayerFactory;

	private RecordServices recordServices;

	private IOServices ioServices;

	private ContentManager contentManager;

	private MetadataSchemasManager metadataSchemasManager;

	private RecordPathProvider recordPathProvider;

	public RecordSIPWriter(AppLayerFactory appLayerFactory,
						   SIPZipWriter sipZipWriter,
						   RecordPathProvider recordPathProvider,
						   Locale locale) throws IOException {

		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.contentManager = appLayerFactory.getModelLayerFactory().getContentManager();

		this.recordPathProvider = recordPathProvider;
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.sipZipWriter = sipZipWriter;

		this.locale = locale;
	}

	public ValidationErrors add(Record record) throws IOException {
		return add(Collections.singletonList(record));
	}


	public ValidationErrors add(Iterable<Record> records) throws IOException {
		return add(records.iterator());
	}

	public ValidationErrors add(Iterator<Record> recordsIterator) throws IOException {
		ValidationErrors errors = new ValidationErrors();

		SIPZipWriterTransaction transaction = sipZipWriter.newInsertTransaction();
		try {

			while (recordsIterator.hasNext()) {
				Record record = recordsIterator.next();
				addToSIP(transaction, record, errors);
			}

		} catch (Throwable t) {
			sipZipWriter.discard(transaction);
			throw new RuntimeException(t);

		}

		sipZipWriter.insertAll(transaction);

		return errors;
	}

	public void close() {
		sipZipWriter.close();
	}

	private void addToSIP(SIPZipWriterTransaction transaction, Record record, ValidationErrors errors)
			throws IOException {

		RecordInsertionContext recordInsertionContext = new RecordInsertionContext(transaction, record, errors);
		buildRecordEADFile(transaction, recordInsertionContext);

		for (Metadata contentMetadata : recordInsertionContext.schema.getMetadatas().onlyWithType(CONTENT)) {
			for (Content content : recordInsertionContext.record.<Content>getValues(contentMetadata)) {
				for (ContentVersion contentVersion : content.getVersions()) {
					insertContentVersion(recordInsertionContext, contentMetadata, contentVersion);
				}
			}
		}
	}

	private void buildRecordEADFile(SIPZipWriterTransaction transaction, RecordInsertionContext ctx)
			throws IOException {
		RecordEADBuilder recordEadBuilder = new RecordEADBuilder(appLayerFactory, ctx.errors);
		File tempXMLFile = ioServices.newTemporaryFile(TEMP_EAD_FILE_STREAM_NAME);
		try {
			recordEadBuilder.build(ctx.record, ctx.sipXMLPath, tempXMLFile);

			ctx.transaction.add(ctx.newMetsEADMetadataReference());
			transaction.moveFileToSIPAsUnreferencedContentFile(ctx.sipXMLPath, tempXMLFile);


		} finally {
			ioServices.deleteQuietly(tempXMLFile);
		}
	}

	private void insertContentVersion(RecordInsertionContext ctx, Metadata metadata,
									  ContentVersion contentVersion) throws IOException {
		String fileId = ctx.dmdId + "-" + metadata.getLocalCode() + "-" + contentVersion.getVersion();
		String zipFilePath = ctx.parentPath + "/" + fileId + "." + getExtension(contentVersion.getFilename());

		File vaultFile = contentManager.getContentDao().getFileOf(contentVersion.getHash());
		MetsContentFileReference reference = ctx.transaction.addContentFileFromVaultFile(zipFilePath, vaultFile);
		reference.setId(fileId);
		reference.setDmdid(ctx.dmdId);
		reference.setTitle(contentVersion.getFilename());

		String extension = StringUtils.lowerCase(getExtension(contentVersion.getFilename()));
		if ("eml".equals(extension) || "msg".equals(extension)) {
			addEmailAttachements(ctx, contentVersion, fileId);
		}
	}

	private void addEmailAttachements(RecordInsertionContext ctx, ContentVersion content, String fileId)
			throws IOException {
		InputStream in = contentManager.getContentInputStream(content.getHash(), READ_VAULT_FILE_STREAM_NAME);

		try {
			Map<String, byte[]> attachements = new EmailParser().parseEmailAttachements(content.getFilename(), in);
			for (Entry<String, byte[]> entry : attachements.entrySet()) {

				String attachmentFilename = entry.getKey();
				if (StringUtils.isNotBlank(getExtension(attachmentFilename))) {
					String attachmentFileId = fileId + "-" + attachmentFilename;
					String attachmentPath = ctx.parentPath + "/" + attachmentFileId;

					MetsContentFileReference ref = ctx.transaction.addContentFile(attachmentPath, entry.getValue());
					ref.setId(attachmentFileId);
					ref.setDmdid(ctx.dmdId);
					ref.setTitle(attachmentFilename);
					ref.setUse("Attachment");
				}
			}

		} finally {
			ioServices.closeQuietly(in);
		}
	}


	private class RecordInsertionContext {

		String dmdId;
		String sipRecordPath;
		String sipXMLPath;
		String parent;
		String parentPath;

		SIPZipWriterTransaction transaction;
		Record record;
		ValidationErrors errors;
		MetadataSchema schema;

		RecordInsertionContext(SIPZipWriterTransaction transaction, Record record, ValidationErrors errors) {
			this.transaction = transaction;
			this.record = record;
			this.errors = errors;
			sipRecordPath = recordPathProvider.getPath(record);
			sipXMLPath = sipRecordPath + ".xml";
			dmdId = StringUtils.substringAfterLast(sipRecordPath, "/");
			parentPath = StringUtils.substringBeforeLast(sipXMLPath, "/");
			parent = StringUtils.substringAfterLast(parentPath, "/");
			schema = metadataSchemasManager.getSchemaOf(record);
			if (parent.equals("data")) {
				parent = null;
				parentPath = null;
			}
		}

		MetsEADMetadataReference newMetsEADMetadataReference() {
			return new MetsEADMetadataReference(dmdId, parent, record.getTypeCode(), record.getTitle(), sipXMLPath);
		}
	}
}
