package com.constellio.app.modules.rm.services.sip;

import au.edu.apsr.mtk.base.METSException;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
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
import com.constellio.data.utils.Provider;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;
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

/**
 * metsHdr CREATEDATE="..." RECORDSTATUS="Complete"
 * - agent ROLE="CREATOR" ORGANIZATION=""
 * - name
 * <p>
 * dmdSec
 * - mdWrap MDTYPE="OTHER"
 * - xmlData
 * - field type="unité administrative"
 * - field*
 * <p>
 * TODO : Obtenir la liste des versions de logiciels/formats utilisés (Tika?)
 * amdSec
 * - digiprovMD ID="???"
 * - mdWrap MDTYPE="PREMIS"
 * - xmlData
 * - PREMIS:premis version="2.0"
 * - PREMIS:object xsi:type="PREMIS:file"
 * - PREMIS:objectIdentifier
 * - PREMIS:objectIdentifierType (internal)
 * - PREMIS:objectIdentifierValue (???)
 * - PREMIS:objectCharacteristics
 * - PREMIS:compositionLevel (0)
 * - PREMIS:format
 * - PREMIS:formatDesignation
 * - PREMIS:formatName (Acrobat PDF = Portable Document Format)
 * - PREMIS:formatVersion (1.5)
 * <p>
 * - digiprovMD ID="???"
 * - mdWrap MDTYPE="PREMIS"
 * - xmlData
 * - PREMIS:premis version="2.0"
 * - PREMIS:object xsi:type="PREMIS:file"
 * - PREMIS:objectIdentifier
 * - PREMIS:objectIdentifierType (internal)
 * - PREMIS:objectIdentifierValue (???)
 * - PREMIS:objectCharacteristics
 * - PREMIS:compositionLevel (0)
 * - PREMIS:format
 * - PREMIS:formatDesignation
 * - PREMIS:formatName (image/tiff)
 * - PREMIS:formatVersion (6.0)
 * <p>
 * - digiprovMD ID="???"
 * - mdWrap MDTYPE="PREMIS"
 * - xmlData
 * - PREMIS:premis version="2.0"
 * - PREMIS:object xsi:type="PREMIS:file"
 * - PREMIS:objectIdentifier
 * - PREMIS:objectIdentifierType (internal)
 * - PREMIS:objectIdentifierValue (???)
 * - PREMIS:objectCharacteristics
 * - PREMIS:compositionLevel (0)
 * - PREMIS:format
 * - PREMIS:formatDesignation
 * - PREMIS:formatName (text/plain)
 * - PREMIS:formatVersion (1.0)
 * <p>
 * fileSec
 * - fileGrp
 * - file ID="constellio_meta_mets_id" MIMETYPE="text/xml" SIZE="..." CHECKSUM="..." CHECKSUMTYPE="SHA2"
 * - FLocat LOCTYPE="URL" xlink:href="bag/constellio_meta_mets.xml"
 * - file ID="constellio_paquet_info_id" MIMETYPE="text/plain" SIZE="..." CHECKSUM="..." CHECKSUMTYPE="SHA2"
 * - FLocat LOCTYPE="URL" xlink:href="bag/constellio_paquet_info.txt"
 * - file ID="constellio_manifest_sha2_id" MIMETYPE="text/plain" SIZE="..." CHECKSUM="..." CHECKSUMTYPE="SHA2"
 * - FLocat LOCTYPE="URL" xlink:href="bag/constellio_manifest_sha2.txt"
 * <p>
 * - fileGrp
 * - file ID="fichier_1_id" DMDID="[id dmdSec]" AMDID="[id amdSec]"
 * TODO
 * <p>
 * structMap
 * - div LABEL="bag" TYPE="folder"
 * - fptr* (fichiers descriptifs du SIP)
 * <p>
 * - div* LABEL="1234 - unité administrative 1000" TYPE="folder" DMDID="[Référence dmdSec]"
 * - div* LABEL="5678 - poste classement 1001" TYPE="folder" DMDID="[Référence dmdSec]"
 * - div* LABEL="d001 - Dossier machin 001" TYPE="folder" DMDID="[Référence dmdSec]"
 * - fptr* (fichiers électroniques des fiches de document)
 * <p>
 * - div* LABEL="d002 - Sous-dossier machin 002" TYPE="folder" DMDID="[Référence dmdSec]".
 *
 * @author Vincent
 */
public class RecordSIPWriter {

	public static final String JOINT_FILES_KEY = "attachments";

	private static final String BAG_INFO_FILE_NAME = "bag-info.txt";

	private static final String READ_VAULT_FILE_STREAM_NAME = RecordSIPWriter.class.getSimpleName() + "-ReadVaultFile";
	private static final String READ_VAULT_FILE_TEMP_FILE_STREAM_NAME = RecordSIPWriter.class.getSimpleName() + "-ReadVaultFileTempFile";
	private static final String WRITE_VAULT_FILE_TO_TEMP_FILE_STREAM_NAME = RecordSIPWriter.class.getSimpleName() + "-WriteVaultFileToTempFile";

	private SIPZipWriter sipZipWriter;

	private SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

	private Map<String, Integer> extensionCounts = new HashMap<String, Integer>();

	private String currentVersion;

	private Locale locale;

	private AppLayerFactory appLayerFactory;

	private RecordServices recordServices;

	private RMSchemasRecordsServices rm;

	private IOServices ioServices;

	private ContentManager contentManager;

	private SIPBuilderParams params;

	private File zipFile;
	private Map<String, MetsDivisionInfo> divisionInfoMap;

	private Provider<Record, String> recordPathProvider;

	public RecordSIPWriter(SIPBuilderParams params,
						   String collection,
						   AppLayerFactory appLayerFactory,
						   File zipFile,
						   Map<String, MetsDivisionInfo> divisionInfoMap,
						   Provider<Record, String> recordPathProvider) {
		this.params = params;
		this.currentVersion = appLayerFactory.newApplicationService().getWarVersion();
		this.locale = params.getLocale();
		this.appLayerFactory = appLayerFactory;
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.ioServices = rm.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		if (this.locale == null) {
			this.locale = appLayerFactory.getModelLayerFactory().getCollectionsListManager()
					.getCollectionInfo(collection).getMainSystemLocale();
		}
		this.divisionInfoMap = divisionInfoMap;
		this.recordPathProvider = recordPathProvider;
		this.zipFile = zipFile;

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

	public ValidationErrors add(Iterator<Record> recordsIterator)
			throws IOException {
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

	private void addToSIP(SIPZipWriterTransaction transaction, Record record, ValidationErrors errors)
			throws METSException {
		try {

			if (Document.SCHEMA_TYPE.equals(record.getTypeCode())) {
				String dmdSecId = record.getId();
				long documentFilesLength = 0;
				int documentFilesCount = 1;


				Document document = rm.wrapDocument(record);
				Folder folder = rm.getFolder(document.getFolder());
				Content content = document.getContent();

				if (content != null) {

					for (ContentVersion contentVersion : document.getContent().getVersions()) {
						String fileId = document.getId() + "-content-" + contentVersion.getVersion();
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

						String folderDmdSecId = folder.getId();

						//						if (!transaction.containsEADMetadatasOf(folderDmdSecId) &&
						//							!sipZipWriter.containsEADMetadatasOf(folderDmdSecId)) {
						//							addToSIP(transaction, folder.getWrappedRecord(), errors);
						//						}

						//TODO Stream and temp file safety
						File tempFile = ioServices.newTemporaryFile(READ_VAULT_FILE_TEMP_FILE_STREAM_NAME);
						InputStream inputStream = contentManager.getContentInputStream(contentVersion.getHash(), READ_VAULT_FILE_STREAM_NAME);
						OutputStream outputStream = ioServices.newBufferedFileOutputStream(tempFile, WRITE_VAULT_FILE_TO_TEMP_FILE_STREAM_NAME);
						ioServices.copyAndClose(inputStream, outputStream);

						String zipFilePath = recordPathProvider.get(folder.getWrappedRecord()) + "/document-" + document.getId() +
											 "-" + contentVersion.getVersion() + "." + extension;
						MetsContentFileReference reference = new MetsContentFileReference();
						reference.setId(fileId);
						reference.setDmdid(dmdSecId);
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

							int i = 1;
							for (Entry<String, byte[]> entry : extraFiles.entrySet()) {

								String extraFilename = entry.getKey();
								String extraFileId = document.getId() + "-content-" + contentVersion.getVersion() + "-" + extraFilename;

								String extraFileExtension = FilenameUtils.getExtension(extraFilename);
								if (StringUtils.isNotBlank(extraFileExtension)) {
									File extraTempFile = File.createTempFile(RecordSIPWriter.class.getName(), extraFilename);

									byte[] extraFileBytes = entry.getValue();
									FileUtils.writeByteArrayToFile(extraTempFile, extraFileBytes);

									String extraZipFilePath =
											recordPathProvider.get(folder.getWrappedRecord()) + "/document-" + extraFileId;
									String extraFileHash = params.getSipFileHasher().computeHash(extraTempFile, extraZipFilePath);

									reference = new MetsContentFileReference();
									reference.setId(extraFileId);
									reference.setDmdid(dmdSecId);
									reference.setPath(document.getFolder());
									reference.setSize(extraFileBytes.length);
									reference.setCheckSum(extraFileHash);
									reference.setCheckSumType(params.getSipFileHasher().getFunctionName());
									reference.setPath(extraZipFilePath);
									reference.setTitle(extraFilename);
									reference.setUse("Attachement");
									transaction.add(reference);

									sipZipWriter.addToZip(extraTempFile, extraZipFilePath);

									extraTempFile.delete();

									i++;
								}
							}
						}
					}
				}


				if (!transaction.containsEADMetadatasOf(dmdSecId) &&
					!sipZipWriter.containsEADMetadatasOf(dmdSecId)) {

					addMdRefAndGenerateEAD(transaction, record, errors);
				}

			} else if (Folder.SCHEMA_TYPE.equals(record.getTypeCode())) {
				Folder folder = rm.wrapFolder(record);
				//Folder parentFolder = folder.getParentFolder() == null ? null : rm.getFolder(folder.getParentFolder());

				//				if (parentFolder != null) {
				//					if (!transaction.containsEADMetadatasOf(record.getId()) &&
				//						!sipZipWriter.containsEADMetadatasOf(record.getId())) {
				//						// Recursive call
				//						addToSIP(transaction, parentFolder.getWrappedRecord(), errors);
				//					}
				//				}
				//
				//				if (!transaction.containsEADMetadatasOf(record.getId()) &&
				//					!sipZipWriter.containsEADMetadatasOf(record.getId())) {
				addMdRefAndGenerateEAD(transaction, record, errors);
				//				}

			}

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
				InputStream in = contentManager.getContentInputStream(contentVersion.getHash(), READ_VAULT_FILE_STREAM_NAME);
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

	private void addMdRefAndGenerateEAD(SIPZipWriterTransaction transaction, Record record,
										ValidationErrors errors)
			throws IOException, METSException {

		RecordEADBuilder recordEadBuilder = new RecordEADBuilder(appLayerFactory, errors);

		if (Document.SCHEMA_TYPE.equals(record.getTypeCode())) {

			Document document = rm.wrapDocument(record);
			Folder folder = rm.getFolder(document.getFolder());


			String zipFolderPath = recordPathProvider.get(folder.getWrappedRecord());
			String fileId = record.getId();
			String zipXMLPath = zipFolderPath + "/" + record.getTypeCode() + "-" + fileId + ".xml";

			File tempXMLFile = File.createTempFile(RecordSIPWriter.class.getSimpleName(), ".xml");
			tempXMLFile.deleteOnExit();

			recordEadBuilder.build(record, zipXMLPath, tempXMLFile);

			transaction.add(new MetsEADMetadataReference(record.getId(), folder.getId(),
					Document.SCHEMA_TYPE, record.getTitle(), zipXMLPath));
			sipZipWriter.addToZip(tempXMLFile, zipXMLPath);

			tempXMLFile.delete();

		} else if (Folder.SCHEMA_TYPE.equals(record.getTypeCode())) {

			Folder folder = rm.wrapFolder(record);
			String zipParentPath;
			String parentDivisionId;
			if (folder.getParentFolder() != null) {
				zipParentPath = recordPathProvider.get(recordServices.getDocumentById(folder.getParentFolder()));
				parentDivisionId = record.getId();
			} else {
				Category category = rm.getCategory(folder.getCategory());
				zipParentPath = recordPathProvider.get(category.getWrappedRecord());
				parentDivisionId = category.getCode();
			}

			String zipXMLPath = zipParentPath + "/" + record.getTypeCode() + "-" + record.getId() + ".xml";

			File tempXMLFile = File.createTempFile(RecordSIPWriter.class.getSimpleName(), ".xml");
			tempXMLFile.deleteOnExit();

			recordEadBuilder.build(record, zipXMLPath, tempXMLFile);

			transaction.add(new MetsEADMetadataReference(record.getId(), parentDivisionId, Folder.SCHEMA_TYPE, record.getTitle(), zipXMLPath));

			sipZipWriter.addToZip(tempXMLFile, zipXMLPath);

			tempXMLFile.delete();


		}
	}

	private List<String> collectBagInfoLines() {
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

}
