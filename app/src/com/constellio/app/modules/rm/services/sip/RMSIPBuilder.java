package com.constellio.app.modules.rm.services.sip;

import au.edu.apsr.mtk.base.METSException;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.sip.ead.RecordEADBuilder;
import com.constellio.app.modules.rm.services.sip.exceptions.SIPMaxFileCountReachedException;
import com.constellio.app.modules.rm.services.sip.exceptions.SIPMaxFileLengthReachedException;
import com.constellio.app.modules.rm.services.sip.mets.MetsContentFileReference;
import com.constellio.app.modules.rm.services.sip.mets.MetsDivisionInfo;
import com.constellio.app.modules.rm.services.sip.mets.MetsEADMetadataReference;
import com.constellio.app.modules.rm.services.sip.zip.SIPZipWriter;
import com.constellio.app.modules.rm.services.sip.zip.SIPZipWriterTransaction;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
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
import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
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
public class RMSIPBuilder {

	public static final String JOINT_FILES_KEY = "attachments";

	private static final String BAG_INFO_FILE_NAME = "bag-info.txt";

	private static final String READ_VAULT_FILE_STREAM_NAME = RMSIPBuilder.class.getSimpleName() + "-ReadVaultFile";
	private static final String READ_VAULT_FILE_TEMP_FILE_STREAM_NAME = RMSIPBuilder.class.getSimpleName() + "-ReadVaultFileTempFile";
	private static final String WRITE_VAULT_FILE_TO_TEMP_FILE_STREAM_NAME = RMSIPBuilder.class.getSimpleName() + "-WriteVaultFileToTempFile";

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

	public RMSIPBuilder(SIPBuilderParams params,
						String collection,
						AppLayerFactory appLayerFactory) {
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

	}

	public ValidationErrors build(File zipFile, Iterator<Record> recordsIterator, ProgressInfo progressInfo)
			throws IOException, JDOMException {
		ValidationErrors errors = new ValidationErrors();

		if (progressInfo == null) {
			progressInfo = new ProgressInfo();
		}

		File outputDir = zipFile.getParentFile();
		outputDir.mkdirs();

		Map<String, MetsDivisionInfo> divisionInfoMap = new HashMap<>();

		for (Category category : rm.getAllCategories()) {
			String parentCode = category.getParent() == null ? null : rm.getCategory(category.getParent()).getCode();
			MetsDivisionInfo metsDivisionInfo = new MetsDivisionInfo(category.getCode(), parentCode, category.getTitle(), Category.SCHEMA_TYPE);
			divisionInfoMap.put(category.getCode(), metsDivisionInfo);
		}

		String sipFilename = FilenameUtils.removeExtension(zipFile.getName());
		sipZipWriter = new SIPZipWriter(ioServices, params.getSipFileHasher(), zipFile, sipFilename, divisionInfoMap);

		List<String> bagInfoLines = collectBagInfoLines();
		try {
			BufferedWriter bufferedWriter = sipZipWriter.newZipFileWriter("/" + BAG_INFO_FILE_NAME);
			IOUtils.writeLines(bagInfoLines, "\n", bufferedWriter);
			IOUtils.closeQuietly(bufferedWriter);

			buildMetsFile(errors, recordsIterator, progressInfo);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}


		sipZipWriter.close();

		return errors;
	}


	private String getZipPath(Record record) {

		if (Category.SCHEMA_TYPE.equals(record.getTypeCode())) {
			Category currentCategory = rm.wrapCategory(record);
			if (currentCategory.getParent() != null) {
				return getZipPath(recordServices.getDocumentById(currentCategory.getParent())) + "/" + currentCategory.getCode();
			} else {
				return "/data/" + currentCategory.getCode();
			}

		} else if (Folder.SCHEMA_TYPE.equals(record.getTypeCode())) {
			Folder folder = rm.wrapFolder(record);
			if (folder.getParentFolder() != null) {
				return getZipPath(recordServices.getDocumentById(folder.getParentFolder())) + "/" + folder.getId();
			} else {
				return getZipPath(recordServices.getDocumentById(folder.getCategory())) + "/" + folder.getId();
			}

		} else if (Document.SCHEMA_TYPE.equals(record.getTypeCode())) {
			Document document = rm.wrapDocument(record);
			return getZipPath(recordServices.getDocumentById(document.getFolder())) + "/" + document.getId();

		} else {
			return "/data/" + record.getId();
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
						//File file = sipDocument.getFile();

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

						if (!transaction.containsEADMetadatasOf(folderDmdSecId) &&
							!sipZipWriter.containsEADMetadatasOf(folderDmdSecId)) {
							addToSIP(transaction, folder.getWrappedRecord(), errors);
						}

						//TODO Stream and temp file safety
						File tempFile = ioServices.newTemporaryFile(READ_VAULT_FILE_TEMP_FILE_STREAM_NAME);
						InputStream inputStream = contentManager.getContentInputStream(contentVersion.getHash(), READ_VAULT_FILE_STREAM_NAME);
						OutputStream outputStream = ioServices.newBufferedFileOutputStream(tempFile, WRITE_VAULT_FILE_TO_TEMP_FILE_STREAM_NAME);
						ioServices.copyAndClose(inputStream, outputStream);

						String zipFilePath = getZipPath(folder.getWrappedRecord()) + "/document-" + document.getId() +
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
									File extraTempFile = File.createTempFile(RMSIPBuilder.class.getName(), extraFilename);

									byte[] extraFileBytes = entry.getValue();
									FileUtils.writeByteArrayToFile(extraTempFile, extraFileBytes);

									String extraZipFilePath =
											getZipPath(folder.getWrappedRecord()) + "/document-" + extraFileId;
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
				Folder parentFolder = folder.getParentFolder() == null ? null : rm.getFolder(folder.getParentFolder());

				if (parentFolder != null) {
					if (!transaction.containsEADMetadatasOf(record.getId()) &&
						!sipZipWriter.containsEADMetadatasOf(record.getId())) {
						// Recursive call
						addToSIP(transaction, parentFolder.getWrappedRecord(), errors);
					}
				}

				if (!transaction.containsEADMetadatasOf(record.getId()) &&
					!sipZipWriter.containsEADMetadatasOf(record.getId())) {
					addMdRefAndGenerateEAD(transaction, record, errors);
				}

			}

		} catch (
				IOException e) {
			errors.add(IOException.class, e.getMessage());
		}

	}

	private Map<String, byte[]> getExtraFiles(Record record, ContentVersion contentVersion) {
		if (Document.SCHEMA_TYPE.equals(record.getTypeCode())) {
			Map<String, byte[]> result;
			Document document = rm.wrapDocument(record);
			boolean isEmail = document.getSchema().getCode().equals(Email.SCHEMA);
			if (isEmail) {
				String filename = contentVersion.getFilename();
				Map<String, Object> parsedMessage;
				try {
					InputStream in = contentManager.getContentInputStream(contentVersion.getHash(), READ_VAULT_FILE_STREAM_NAME);
					parsedMessage = rm.parseEmail(filename, in);
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
		} else {
			return null;
		}
	}

	private void addMdRefAndGenerateEAD(SIPZipWriterTransaction transaction, Record record,
										ValidationErrors errors)
			throws IOException, METSException {

		RecordEADBuilder recordEadBuilder = new RecordEADBuilder(appLayerFactory, errors);

		if (Document.SCHEMA_TYPE.equals(record.getTypeCode())) {

			Document document = rm.wrapDocument(record);
			Folder folder = rm.getFolder(document.getFolder());


			String zipFolderPath = getZipPath(folder.getWrappedRecord());
			String fileId = record.getId();
			String zipXMLPath = zipFolderPath + "/" + record.getTypeCode() + "-" + fileId + ".xml";

			File tempXMLFile = File.createTempFile(RMSIPBuilder.class.getSimpleName(), ".xml");
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
				zipParentPath = getZipPath(recordServices.getDocumentById(folder.getParentFolder()));
				parentDivisionId = record.getId();
			} else {
				Category category = rm.getCategory(folder.getCategory());
				zipParentPath = getZipPath(category.getWrappedRecord());
				parentDivisionId = category.getCode();
			}

			String zipXMLPath = zipParentPath + "/" + record.getTypeCode() + "-" + record.getId() + ".xml";

			File tempXMLFile = File.createTempFile(RMSIPBuilder.class.getSimpleName(), ".xml");
			tempXMLFile.deleteOnExit();

			recordEadBuilder.build(record, zipXMLPath, tempXMLFile);

			transaction.add(new MetsEADMetadataReference(record.getId(), parentDivisionId, Folder.SCHEMA_TYPE, record.getTitle(), zipXMLPath));

			sipZipWriter.addToZip(tempXMLFile, zipXMLPath);

			tempXMLFile.delete();


		}
	}

	private void buildMetsFile(ValidationErrors errors, Iterator<Record> recordsIterator, ProgressInfo progressInfo)
			throws IOException, METSException, SAXException, JDOMException {


		int recordsHandled = 0;
		while (recordsIterator.hasNext()) {
			Record record = recordsIterator.next();

			progressInfo.setCurrentState(recordsHandled + 1);
			SIPZipWriterTransaction transaction = new SIPZipWriterTransaction(ioServices.newTemporaryFolder("ConstellioSIP-transaction"));
			addToSIP(transaction, record, errors);
			sipZipWriter.addToZip(transaction);

			recordsHandled++;

		}

		progressInfo.setDone(true);
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
