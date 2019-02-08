package com.constellio.app.services.sip;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.ead.RecordEADBuilder;
import com.constellio.app.services.sip.mets.MetsContentFileReference;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.app.services.sip.mets.MetsEADMetadataReference;
import com.constellio.app.services.sip.zip.SIPZipWriter;
import com.constellio.app.services.sip.zip.SIPZipWriterTransaction;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.TimeProvider;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static org.apache.commons.io.FilenameUtils.getExtension;


public class RecordSIPWriter {

	private static final String BAG_INFO_FILE_NAME = "bag-info.txt";

	private static final String READ_VAULT_FILE_STREAM_NAME = RecordSIPWriter.class.getSimpleName() + "-ReadVaultFile";

	private static final String TEMP_EAD_FILE_STREAM_NAME = RecordSIPWriter.class.getSimpleName() + "-ReadVaultFile";

	private SIPZipWriter sipZipWriter;

	private SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

	private Map<String, Integer> extensionCounts = new HashMap<String, Integer>();

	private Locale locale;

	private AppLayerFactory appLayerFactory;

	private RecordServices recordServices;

	private IOServices ioServices;

	private ContentManager contentManager;

	private MetadataSchemasManager metadataSchemasManager;

	private SIPBuilderParams params;

	private String collection;

	private RecordPathProvider recordPathProvider;

	public RecordSIPWriter(SIPBuilderParams params,
						   String collection,
						   AppLayerFactory appLayerFactory,
						   File zipFile,
						   Map<String, MetsDivisionInfo> divisionInfoMap,
						   RecordPathProvider recordPathProvider) {

		this.collection = collection;
		this.params = params;
		this.locale = params.getLocale();
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		if (this.locale == null) {
			this.locale = appLayerFactory.getModelLayerFactory().getCollectionsListManager()
					.getCollectionInfo(collection).getMainSystemLocale();
		}
		this.recordPathProvider = recordPathProvider;
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		String sipFilename = FilenameUtils.removeExtension(zipFile.getName());
		try {
			sipZipWriter = new SIPZipWriter(ioServices, params.getSipFileHasher(), zipFile, sipFilename, divisionInfoMap);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		List<String> bagInfoLines = collectBagInfoLines();
		BufferedWriter bufferedWriter = sipZipWriter.newZipFileWriter("/" + BAG_INFO_FILE_NAME);
		try {
			IOUtils.writeLines(bagInfoLines, "\n", bufferedWriter);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		IOUtils.closeQuietly(bufferedWriter);


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
		try {
			sipZipWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

	private void buildRecordEADFile(SIPZipWriterTransaction transaction, RecordInsertionContext recordInsertionContext)
			throws IOException {
		RecordEADBuilder recordEadBuilder = new RecordEADBuilder(appLayerFactory, recordInsertionContext.errors);
		File tempXMLFile = ioServices.newTemporaryFile(TEMP_EAD_FILE_STREAM_NAME);
		try {
			recordEadBuilder.build(recordInsertionContext.record, recordInsertionContext.sipXMLPath, tempXMLFile);

			recordInsertionContext.transaction.add(new MetsEADMetadataReference(recordInsertionContext.dmdId, recordInsertionContext.parent,
					recordInsertionContext.record.getTypeCode(), recordInsertionContext.record.getTitle(), recordInsertionContext.sipXMLPath));
			transaction.addUnreferencedContentFileFromFile(recordInsertionContext.sipXMLPath, tempXMLFile);


		} finally {
			ioServices.deleteQuietly(tempXMLFile);
		}
	}

	private void insertContentVersion(RecordInsertionContext recordInsertionContext, Metadata contentMetadata,
									  ContentVersion contentVersion) throws IOException {
		String fileId = recordInsertionContext.record.getId() + "-" + contentMetadata.getLocalCode() + "-" + contentVersion.getVersion();
		String filename = contentVersion.getFilename();

		String zipFilePath = recordInsertionContext.sipRecordPath + "-" + contentVersion.getVersion() + "." + getExtension(filename);
		MetsContentFileReference reference = recordInsertionContext.transaction.addContentFileFromInputStream(
				zipFilePath, contentManager.getContentInputStreamFactory(contentVersion.getHash()));
		reference.setId(fileId);
		reference.setDmdid(recordInsertionContext.dmdId);
		reference.setTitle(filename);

		String extension = StringUtils.lowerCase(getExtension(filename));
		if ("eml".equals(extension) || "msg".equals(extension)) {
			InputStream in = contentManager.getContentInputStream(contentVersion.getHash(), READ_VAULT_FILE_STREAM_NAME);
			try {
				for (Entry<String, byte[]> entry : new EmailParser().parseEmailAttachements(filename, in).entrySet()) {

					String extraFilename = entry.getKey();
					String extraFileExtension = getExtension(extraFilename);
					if (StringUtils.isNotBlank(extraFileExtension)) {
						String extraFileId = recordInsertionContext.record.getId() + "-" + contentMetadata.getLocalCode() + "-" + contentVersion.getVersion() + "-" + extraFilename;
						String extraZipFilePath = recordInsertionContext.sipRecordPath + "-" + contentMetadata.getLocalCode() + "-" + contentVersion.getVersion() + "-" + extraFilename;

						MetsContentFileReference extraReference = recordInsertionContext.transaction
								.addContentFileFromBytes(extraZipFilePath, entry.getValue());
						extraReference.setId(extraFileId);
						extraReference.setDmdid(recordInsertionContext.dmdId);
						extraReference.setTitle(extraFilename);
						extraReference.setUse("Attachement");
					}
				}

			} finally {
				ioServices.closeQuietly(in);
			}
		}
	}

	private List<String> collectBagInfoLines() {
		String currentVersion = appLayerFactory.newApplicationService().getWarVersion();
		List<String> bagInfoLines = new ArrayList<>();
		if (params.getProvidedBagInfoHeaderLines() != null) {
			bagInfoLines.addAll(params.getProvidedBagInfoHeaderLines());
		}

		bagInfoLines.add("Nombre de fichiers numériques : " + sipZipWriter.sipFilesCount);
		StringBuffer extensionsAndCounts = new StringBuffer();
		for (Entry<String, Integer> extensionAndCount : extensionCounts.entrySet()) {
			if (extensionsAndCounts.length() > 0) {
				extensionsAndCounts.append(", ");
			}
			String extension = extensionAndCount.getKey();
			Integer count = extensionAndCount.getValue();
			extensionsAndCounts.append("." + extension + " = " + count);
		}
		bagInfoLines.add("Portrait général des formats numériques : " + extensionsAndCounts);
		bagInfoLines
				.add("Taille des fichiers numériques non compressés : " + FileUtils.byteCountToDisplaySize(sipZipWriter.sipFilesLength) + " ("
					 + sipZipWriter.sipFilesLength + " octets)");
		bagInfoLines.add("");
		bagInfoLines.add("Logiciel : Constellio");
		bagInfoLines.add("Site web de l’éditeur : http://www.constellio.com");
		bagInfoLines.add("Version du logiciel : " + currentVersion);
		bagInfoLines.add("Date de création du paquet : " + sdfDate.format(TimeProvider.getLocalDateTime().toDate()));
		bagInfoLines.add("");
		return bagInfoLines;
	}


	public interface RecordPathProvider {

		String getPath(Record record);
	}

	private class RecordInsertionContext {

		String dmdId;
		String sipRecordPath;
		String sipXMLPath;
		String parent;

		SIPZipWriterTransaction transaction;
		Record record;
		ValidationErrors errors;
		MetadataSchema schema;

		public RecordInsertionContext(SIPZipWriterTransaction transaction, Record record, ValidationErrors errors) {
			this.transaction = transaction;
			this.record = record;
			this.errors = errors;
			sipRecordPath = recordPathProvider.getPath(record);
			sipXMLPath = sipRecordPath + ".xml";
			dmdId = StringUtils.substringAfterLast(sipRecordPath, "/");
			String parentPath = StringUtils.substringBeforeLast(sipXMLPath, "/");
			parent = StringUtils.substringAfterLast(parentPath, "/");
			schema = metadataSchemasManager.getSchemaOf(record);
			if (parent.equals("data")) {
				parent = null;
				parentPath = null;
			}
		}
	}
}
