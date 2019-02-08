package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.ead.RecordEADBuilder;
import com.constellio.app.services.sip.exceptions.SIPMaxFileCountReachedException;
import com.constellio.app.services.sip.exceptions.SIPMaxFileLengthReachedException;
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
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentManager;
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
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;


public class RecordSIPWriter {

	private static final String JOINT_FILES_KEY = "attachments";

	private static final String BAG_INFO_FILE_NAME = "bag-info.txt";

	private static final String READ_VAULT_FILE_STREAM_NAME = RecordSIPWriter.class.getSimpleName() + "-ReadVaultFile";
	private static final String READ_VAULT_FILE_TEMP_FILE_STREAM_NAME = RecordSIPWriter.class.getSimpleName() + "-ReadVaultFileTempFile";
	private static final String WRITE_VAULT_FILE_TO_TEMP_FILE_STREAM_NAME = RecordSIPWriter.class.getSimpleName() + "-WriteVaultFileToTempFile";

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

	public ValidationErrors add(Iterable<Record> records)
			throws IOException {
		return add(records.iterator());
	}

	public ValidationErrors add(Iterator<Record> recordsIterator) {
		ValidationErrors errors = new ValidationErrors();

		try {
			while (recordsIterator.hasNext()) {
				Record record = recordsIterator.next();
				SIPZipWriterTransaction transaction = new SIPZipWriterTransaction(ioServices.newTemporaryFolder("ConstellioSIP-transaction"));
				addToSIP(transaction, record, errors);
				sipZipWriter.addToZip(transaction);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}


		return errors;
	}

	public void close() {
		try {
			sipZipWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void addToSIP(SIPZipWriterTransaction transaction, Record record, ValidationErrors errors) {

		String zipRecordPath = recordPathProvider.getPath(record);
		String zipXMLPath = zipRecordPath + ".xml";
		String recordPathPart = StringUtils.substringAfterLast(zipRecordPath, "/");
		String parentPath = StringUtils.substringBeforeLast(zipXMLPath, "/");
		String parent = StringUtils.substringAfterLast(parentPath, "/");
		if (parent.equals("data")) {
			parent = null;
			parentPath = null;
		}

		MetadataSchema schema = metadataSchemasManager.getSchemaOf(record);
		try {

			String path = recordPathProvider.getPath(record);
			for (Metadata contentMetadata : schema.getMetadatas().onlyWithType(MetadataValueType.CONTENT)) {
				for (Content content : record.<Content>getValues(contentMetadata)) {
					long documentFilesLength = 0;
					int documentFilesCount = 1;

					for (ContentVersion contentVersion : content.getVersions()) {
						String fileId = record.getId() + "-content-" + contentVersion.getVersion();
						String filename = contentVersion.getFilename();

						long length = contentVersion.getLength();
						documentFilesLength += length;

						if (params.getSipBytesLimit() > 0 && sipZipWriter.sipFilesLength + documentFilesLength > params.getSipBytesLimit()) {
							Map<String, Object> errorsMap = new HashMap<>();
							errorsMap.put("sipObjectType", record.getTypeCode());
							errorsMap.put("sipObjectId", record.getId());
							errorsMap.put("sipObjectTitle", record.getTitle());
							errorsMap.put("sipFilesLength", sipZipWriter.sipFilesLength + documentFilesLength);
							errorsMap.put("sipMaxFilesLength", params.getSipBytesLimit());
							errors.add(SIPMaxFileLengthReachedException.class, "SIPMaxFileLengthReached", errorsMap);

						} else if (params.getSipFilesLimit() > 0 && sipZipWriter.sipFilesCount + documentFilesCount > params.getSipFilesLimit()) {
							Map<String, Object> errorsMap = new HashMap<>();
							errorsMap.put("sipObjectType", record.getTypeCode());
							errorsMap.put("sipObjectId", record.getId());
							errorsMap.put("sipObjectTitle", record.getTitle());
							errorsMap.put("sipFilesCount", sipZipWriter.sipFilesCount + documentFilesCount);
							errorsMap.put("sipMaxFilesCount", params.getSipFilesLimit());
							errors.add(SIPMaxFileCountReachedException.class, "SIPMaxFileCountReached", errorsMap);
						}
						String extension = FilenameUtils.getExtension(filename);
						Integer extensionCount = extensionCounts.get(extension);
						if (extensionCount == null) {
							extensionCounts.put(extension, 1);
						} else {
							extensionCounts.put(extension, extensionCount + 1);
						}

						//TODO Stream and temp file safety
						File tempFile = ioServices.newTemporaryFile(READ_VAULT_FILE_TEMP_FILE_STREAM_NAME);
						InputStream inputStream = contentManager.getContentInputStream(contentVersion.getHash(), READ_VAULT_FILE_STREAM_NAME);
						OutputStream outputStream = ioServices.newBufferedFileOutputStream(tempFile, WRITE_VAULT_FILE_TO_TEMP_FILE_STREAM_NAME);
						ioServices.copyAndClose(inputStream, outputStream);

						String zipFilePath = path + "-" + contentVersion.getVersion() + "." + extension;
						MetsContentFileReference reference = new MetsContentFileReference();
						reference.setId(fileId);
						reference.setDmdid(recordPathPart);
						reference.setSize(length);
						reference.setCheckSum(params.getSipFileHasher().computeHash(tempFile, zipFilePath));
						reference.setCheckSumType(params.getSipFileHasher().getFunctionName());
						reference.setPath(zipFilePath);
						reference.setTitle(filename);
						transaction.add(reference);

						sipZipWriter.addToZip(tempFile, zipFilePath);
						ioServices.deleteQuietly(tempFile);


						Map<String, byte[]> extraFiles = getExtraFiles(record, contentVersion);
						if (extraFiles != null) {
							for (byte[] extraFileBytes : extraFiles.values()) {
								documentFilesLength += extraFileBytes.length;
								documentFilesCount++;
							}
						}

						if (extraFiles != null) {

							for (Entry<String, byte[]> entry : extraFiles.entrySet()) {
								String extraFilename = entry.getKey();
								String extraFileExtension = FilenameUtils.getExtension(extraFilename);
								if (StringUtils.isNotBlank(extraFileExtension)) {
									String extraFileId = record.getId() + "-" + contentMetadata.getLocalCode() + "-" + contentVersion.getVersion() + "-" + extraFilename;
									String extraZipFilePath = path + "-" + contentMetadata.getLocalCode() + "-" + contentVersion.getVersion() + "-" + extraFilename;
									File extraTempFile = File.createTempFile(RecordSIPWriter.class.getName(), extraFilename);

									byte[] extraFileBytes = entry.getValue();
									FileUtils.writeByteArrayToFile(extraTempFile, extraFileBytes);
									String extraFileHash = params.getSipFileHasher().computeHash(extraTempFile, extraZipFilePath);
									reference = new MetsContentFileReference();
									reference.setId(extraFileId);
									reference.setDmdid(recordPathPart);
									reference.setSize(extraFileBytes.length);
									reference.setCheckSum(extraFileHash);
									reference.setCheckSumType(params.getSipFileHasher().getFunctionName());
									reference.setPath(extraZipFilePath);
									reference.setTitle(extraFilename);
									reference.setUse("Attachement");
									transaction.add(reference);

									sipZipWriter.addToZip(extraTempFile, extraZipFilePath);

									extraTempFile.delete();
								}
							}
						}
					}
				}
			}

			RecordEADBuilder recordEadBuilder = new RecordEADBuilder(appLayerFactory, errors);

			File tempXMLFile = File.createTempFile(RecordSIPWriter.class.getSimpleName(), ".xml");
			tempXMLFile.deleteOnExit();

			recordEadBuilder.build(record, zipXMLPath, tempXMLFile);

			transaction.add(new MetsEADMetadataReference(recordPathPart, parent,
					record.getTypeCode(), record.getTitle(), zipXMLPath));
			sipZipWriter.addToZip(tempXMLFile, zipXMLPath);

			tempXMLFile.delete();


		} catch (
				IOException e) {
			errors.add(IOException.class, e.getMessage());
		}

	}

	private Map<String, byte[]> getExtraFiles(Record record, ContentVersion contentVersion) {
		String filename = contentVersion.getFilename();
		Map<String, byte[]> result = null;
		if (filename.toLowerCase().endsWith(".eml") || filename.toLowerCase().endsWith(".msg")) {

			try {
				//TODO : Move in RM
				InputStream in = contentManager.getContentInputStream(contentVersion.getHash(), READ_VAULT_FILE_STREAM_NAME);
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
				Map<String, Object> parsedMessage = rm.parseEmail(filename, in);
				if (parsedMessage != null) {
					result = new LinkedHashMap<>();
					Map<String, InputStream> streamMap = (Map<String, InputStream>) parsedMessage.get(JOINT_FILES_KEY);
					for (Entry<String, InputStream> entry : streamMap.entrySet()) {
						InputStream fichierJointIn = entry.getValue();
						byte[] joinFilesBytes = IOUtils.toByteArray(fichierJointIn);
						result.put(entry.getKey(), joinFilesBytes);
					}
				} else {
					result = null;
				}
			} catch (Throwable t) {
				t.printStackTrace();
				result = null;
			}
		} else {
			result = null;
		}
		return result;
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
}
