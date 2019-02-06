package com.constellio.app.modules.rm.services.sip;

import au.edu.apsr.mtk.base.METSException;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.sip.data.SIPObjectsProvider;
import com.constellio.app.modules.rm.services.sip.ead.RecordEADBuilder;
import com.constellio.app.modules.rm.services.sip.exceptions.SIPMaxFileCountReachedException;
import com.constellio.app.modules.rm.services.sip.exceptions.SIPMaxFileLengthReachedException;
import com.constellio.app.modules.rm.services.sip.mets.MetsContentFileReference;
import com.constellio.app.modules.rm.services.sip.mets.MetsDivisionInfo;
import com.constellio.app.modules.rm.services.sip.mets.MetsEADMetadataReference;
import com.constellio.app.modules.rm.services.sip.model.SIPDocument;
import com.constellio.app.modules.rm.services.sip.model.SIPFolder;
import com.constellio.app.modules.rm.services.sip.model.SIPObject;
import com.constellio.app.modules.rm.services.sip.slip.SIPSlip;
import com.constellio.app.modules.rm.services.sip.zip.SIPZipWriter;
import com.constellio.app.modules.rm.services.sip.zip.SIPZipWriterTransaction;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.frameworks.validation.ValidationErrors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
public class ConstellioSIP {

	private static final long SIP_MAX_FILES_LENGTH = (6 * FileUtils.ONE_GB);

	private static final int SIP_MAX_FILES = 9000;

	private static final char[] RESERVED_PATH_CHARS = {
			';',
			'/',
			'\\',
			'?',
			':',
			'@',
			'&',
			'=',
			'+',
			'$',
			',',
			'{',
			'}',
			'|',
			'^',
			'[',
			']',
			};

	private static final String BAG_INFO_FILE_NAME = "bag-info.txt";

	private static final String HASH_TYPE = "sha256";

	private SIPObjectsProvider sipObjectsProvider;

	private List<String> providedBagInfoLines;

	private SIPZipWriter sipZipWriter;

	private SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

	private SimpleDateFormat sdfTimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private Map<String, Integer> extensionCounts = new HashMap<String, Integer>();

	private SIPSlip sipSlip = new SIPSlip();

	private int currentDocumentIndex;

	private boolean limitSize;

	private String currentVersion;

	private ProgressInfo progressInfo;

	private Locale locale;

	private RMSchemasRecordsServices rm;

	public ConstellioSIP(SIPObjectsProvider sipObjectsProvider, List<String> bagInfoLines, boolean limitSize,
						 String currentVersion, ProgressInfo progressInfo, Locale locale) {
		this.sipObjectsProvider = sipObjectsProvider;
		this.providedBagInfoLines = bagInfoLines;
		this.currentDocumentIndex = sipObjectsProvider.getStartIndex();
		this.limitSize = limitSize;
		this.currentVersion = currentVersion;
		this.progressInfo = progressInfo;
		this.locale = locale;
		this.rm = new RMSchemasRecordsServices(sipObjectsProvider.getCollection(), sipObjectsProvider.getAppLayerCollection());
	}

	public ValidationErrors build(File zipFile)
			throws IOException, JDOMException {
		ValidationErrors errors = new ValidationErrors();


		File outputDir = zipFile.getParentFile();
		outputDir.mkdirs();

		IOServicesFactory ioServicesFactory = ConstellioFactories.getInstance().getIoServicesFactory();

		Map<String, MetsDivisionInfo> divisionInfoMap = new HashMap<>();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(sipObjectsProvider.getCollection(),
				ConstellioFactories.getInstance().getAppLayerFactory());
		for (Category category : rm.getAllCategories()) {

			String parentCode = category.getParent() == null ? null : rm.getCategory(category.getParent()).getCode();

			MetsDivisionInfo metsDivisionInfo = new MetsDivisionInfo(category.getCode(), parentCode, category.getTitle(), Category.SCHEMA_TYPE);
			divisionInfoMap.put(category.getCode(), metsDivisionInfo);
		}

		String sipFilename = FilenameUtils.removeExtension(zipFile.getName());
		sipZipWriter = new SIPZipWriter(ioServicesFactory, zipFile, sipFilename, divisionInfoMap) {

			@Override
			protected String computeHashOfFile(File file, String filePath) throws IOException {
				return ConstellioSIP.this.getHash(file, filePath);
			}
		};

		List<String> bagInfoLines = collectBagInfoLines();
		try {
			BufferedWriter bufferedWriter = sipZipWriter.newZipFileWriter("/" + BAG_INFO_FILE_NAME);
			IOUtils.writeLines(bagInfoLines, "\n", bufferedWriter);
			IOUtils.closeQuietly(bufferedWriter);

			buildMetsFileAndBagDir(errors);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		String slipFilename = sipFilename + ".xls";
		File slipFile = new File(outputDir, slipFilename);
		OutputStream slipFileOutputStream = new FileOutputStream(slipFile);
		sipSlip.write(slipFileOutputStream, bagInfoLines);

		sipZipWriter.close();
		slipFileOutputStream.close();

		return errors;
	}


	private void buildMetsFileAndBagDir(ValidationErrors errors)
			throws IOException, METSException, SAXException, JDOMException, RecordDaoException.NoSuchRecordWithId {
		File tempFile = File.createTempFile(ConstellioSIP.class.getSimpleName(), ".temp");
		tempFile.deleteOnExit();

		buildMetsFile(errors);
		tempFile.delete();
	}

	private String getZipPath(SIPObject sipObject) {
		return sipObject.getZipPath();
	}

	private void addToSIP(SIPZipWriterTransaction transaction, SIPObject sipObject, ValidationErrors errors)
			throws METSException {
		try {


			if (sipObject instanceof SIPDocument) {
				String dmdSecId = getDmdSecId(sipObject);
				long documentFilesLength = 0;
				int documentFilesCount = 1;

				SIPDocument sipDocument = (SIPDocument) sipObject;
				SIPFolder sipFolder = sipDocument.getFolder();

				Document document = rm.wrapDocument(sipDocument.getRecord());
				Content content = document.getContent();
				String zipFilePath = getZipPath(sipDocument);
				if (content != null) {

					for (ContentVersion contentVersion : document.getContent().getVersions()) {
						File file = sipDocument.getFile();
						try {
							String fileId = document.getId() + "-content-" + contentVersion.getVersion();
							String filename = sipDocument.getFilename();


							long length = sipDocument.getLength();
							documentFilesLength += length;

							if (limitSize) {
								Map<String, Object> errorsMap = new HashMap<>();
								if (sipZipWriter.sipFilesLength + documentFilesLength > SIP_MAX_FILES_LENGTH) {
									errorsMap.put("sipObjectType", sipObject.getType());
									errorsMap.put("sipObjectId", sipObject.getId());
									errorsMap.put("sipObjectTitle", sipObject.getTitle());
									errorsMap.put("sipFilesLength", sipZipWriter.sipFilesLength + documentFilesLength);
									errorsMap.put("sipMaxFilesLength", SIP_MAX_FILES_LENGTH);
									errorsMap.put("lastDocumentIndex", currentDocumentIndex);
									errors.add(SIPMaxFileLengthReachedException.class, "SIPMaxFileLengthReached", errorsMap);
								} else if (sipZipWriter.sipFilesCount + documentFilesCount > SIP_MAX_FILES) {
									errorsMap.put("sipObjectType", sipObject.getType());
									errorsMap.put("sipObjectId", sipObject.getId());
									errorsMap.put("sipObjectTitle", sipObject.getTitle());
									errorsMap.put("sipFilesCount", sipZipWriter.sipFilesCount + documentFilesCount);
									errorsMap.put("sipMaxFilesCount", SIP_MAX_FILES);
									errorsMap.put("lastDocumentIndex", currentDocumentIndex);
									errors.add(SIPMaxFileCountReachedException.class, "SIPMaxFileCountReached", errorsMap);
								} else {
									currentDocumentIndex++;
								}
							} else {
								currentDocumentIndex++;
							}
							String hash = null;

							if (file != null) {
								hash = getHash(file, zipFilePath);
							}
							String extension = FilenameUtils.getExtension(filename);
							Integer extensionCount = extensionCounts.get(extension);
							if (extensionCount == null) {
								extensionCounts.put(extension, 1);
							} else {
								extensionCounts.put(extension, extensionCount + 1);
							}

							String folderDmdSecId = getDmdSecId(sipFolder);

							if (!transaction.containsEADMetadatasOf(folderDmdSecId) &&
								!sipZipWriter.containsEADMetadatasOf(folderDmdSecId)) {
								addToSIP(transaction, sipFolder, errors);
							}

							MetsContentFileReference reference = new MetsContentFileReference();
							reference.setId(fileId);
							reference.setDmdid(dmdSecId);
							reference.setSize(length);
							reference.setCheckSum(hash);
							reference.setCheckSumType("SHA-256");
							reference.setPath(zipFilePath);
							reference.setTitle(filename);
							transaction.add(reference);


							if (file != null) {
								sipZipWriter.addToZip(file, zipFilePath);
							}

						} finally {
							rm.getModelLayerFactory().getIOServicesFactory().newIOServices().deleteQuietly(file);
						}
					}
				}

				Map<String, byte[]> extraFiles = sipObjectsProvider.getExtraFiles(sipDocument);
				if (extraFiles != null) {
					for (byte[] extraFileBytes : extraFiles.values()) {
						documentFilesLength += extraFileBytes.length;
						documentFilesCount++;
					}
				}

				if (extraFiles != null) {

					int i = 1;
					for (Entry<String, byte[]> entry : extraFiles.entrySet()) {

						String extraFileId = document.getId() + "-extra-" + "-" + i;
						String extraFilename = entry.getKey();
						String extraFileExtension = FilenameUtils.getExtension(extraFilename);
						if (StringUtils.isNotBlank(extraFileExtension)) {
							File extraTempFile = File.createTempFile(ConstellioSIP.class.getName(), extraFilename);

							byte[] extraFileBytes = entry.getValue();
							FileUtils.writeByteArrayToFile(extraTempFile, extraFileBytes);

							String extraZipFilePath =
									StringUtils.substringBeforeLast(zipFilePath, ".") + "-" + i + "." + extraFileExtension;
							String extraFileHash = getHash(extraTempFile, extraZipFilePath);

							MetsContentFileReference reference = new MetsContentFileReference();
							reference.setId(extraFileId);
							reference.setPath(document.getFolder());
							reference.setSize(extraFileBytes.length);
							reference.setCheckSum(extraFileHash);
							reference.setCheckSumType("SHA-256");
							reference.setPath(extraZipFilePath);
							reference.setTitle(extraFilename);
							transaction.add(reference);

							sipZipWriter.addToZip(extraTempFile, extraZipFilePath);

							extraTempFile.delete();

							i++;
						}
					}
				}

				sipSlip.add(sipDocument);

				if (!transaction.containsEADMetadatasOf(dmdSecId) &&
					!sipZipWriter.containsEADMetadatasOf(dmdSecId)) {

					addMdRefAndGenerateEAD(transaction, sipObject, errors);
				}

			} else if (sipObject instanceof SIPFolder) {
				SIPFolder folder = (SIPFolder) sipObject;

				SIPFolder parentFolder = folder.getParentFolder();
				if (parentFolder != null) {
					String currentFolderId = getDmdSecId(parentFolder);
					if (!transaction.containsEADMetadatasOf(currentFolderId) &&
						!sipZipWriter.containsEADMetadatasOf(currentFolderId)) {
						// Recursive call
						addToSIP(transaction, parentFolder, errors);
					}
				}

				String currentFolderId = getDmdSecId(folder);
				if (!transaction.containsEADMetadatasOf(currentFolderId) &&
					!sipZipWriter.containsEADMetadatasOf(currentFolderId)) {
					addMdRefAndGenerateEAD(transaction, folder, errors);
				}

			}

		} catch (
				IOException e) {
			errors.add(IOException.class, e.getMessage());
		} finally {
			IOServices ioServices = sipObjectsProvider.getAppLayerCollection().getModelLayerFactory().getIOServicesFactory()
					.newIOServices();
		}

	}

	private void addMdRefAndGenerateEAD(SIPZipWriterTransaction transaction, SIPObject sipObject,
										ValidationErrors errors)
			throws IOException, METSException {

		RecordEADBuilder recordEadBuilder = new RecordEADBuilder(sipObjectsProvider.getAppLayerCollection(), errors);

		if (sipObject instanceof SIPDocument) {
			SIPDocument sipDocument = (SIPDocument) sipObject;
			SIPFolder sipFolder = sipDocument.getFolder();

			String zipFolderPath = getZipPath(sipFolder);
			String fileId = sipDocument.getFileId();
			String zipXMLPath = zipFolderPath + "/" + sipDocument.getRecord().getTypeCode() + "-" + fileId + ".xml";

			File tempXMLFile = File.createTempFile(ConstellioSIP.class.getSimpleName(), ".xml");
			tempXMLFile.deleteOnExit();

			recordEadBuilder.build(sipObject.getRecord(), zipXMLPath, tempXMLFile);

			transaction.add(new MetsEADMetadataReference(getDmdSecId(sipObject), getDmdSecId(sipFolder),
					Document.SCHEMA_TYPE, sipObject.getTitle(), zipXMLPath));
			sipZipWriter.addToZip(tempXMLFile, zipXMLPath);

			tempXMLFile.delete();

		} else if (sipObject instanceof SIPFolder) {
			SIPFolder sipFolder = (SIPFolder) sipObject;

			String zipParentPath;
			String parentDivisionId;
			if (sipFolder.getParentFolder() != null) {
				zipParentPath = getZipPath(sipFolder.getParentFolder());
				parentDivisionId = getDmdSecId(sipFolder.getParentFolder());
			} else {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(sipFolder.getRecord().getCollection(), ConstellioFactories.getInstance().getAppLayerFactory());
				zipParentPath = getZipPath(sipFolder.getCategory());
				parentDivisionId = rm.getCategory(sipFolder.getCategory().getId()).getCode();
			}

			String folderId = sipFolder.getId();
			String zipXMLPath = zipParentPath + "/" + sipFolder.getRecord().getTypeCode() + "-" + folderId + ".xml";

			File tempXMLFile = File.createTempFile(ConstellioSIP.class.getSimpleName(), ".xml");
			tempXMLFile.deleteOnExit();

			recordEadBuilder.build(sipObject.getRecord(), zipXMLPath, tempXMLFile);

			transaction.add(new MetsEADMetadataReference(getDmdSecId(sipObject), parentDivisionId, Folder.SCHEMA_TYPE, sipObject.getTitle(), zipXMLPath));

			sipZipWriter.addToZip(tempXMLFile, zipXMLPath);

			tempXMLFile.delete();


		}
	}

	private String getDmdSecId(SIPObject sipObject) {
		return sipObject.getId();//sipObject.getType() + "-" + sipObject.getId();
	}


	private void buildMetsFile(ValidationErrors errors)
			throws IOException, METSException, SAXException, JDOMException {


		List<SIPObject> sipObjects = sipObjectsProvider.list();
		int index = 0;
		progressInfo.setEnd(sipObjects.size());
		IOServicesFactory ioServicesFactory = ConstellioFactories.getInstance().getIoServicesFactory();

		for (SIPObject sipObject : sipObjects) {
			progressInfo.setCurrentState(++index);
			SIPZipWriterTransaction transaction = new SIPZipWriterTransaction(
					ioServicesFactory.newIOServices().newTemporaryFolder("ConstellioSIP-transaction"));

			addToSIP(transaction, sipObject, errors);

			sipZipWriter.addToZip(transaction);
		}

		progressInfo.setDone(true);
	}


	private List<String> collectBagInfoLines() {
		List<String> bagInfoLines = new ArrayList<>();
		bagInfoLines.addAll(this.providedBagInfoLines);

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


	protected String getHash(File file, String sipPath)
			throws IOException {
		FileInputStream fileInputStream = new FileInputStream(file);

		try {
			return DigestUtils.sha256Hex(fileInputStream);
		} finally {
			IOUtils.closeQuietly(fileInputStream);
		}
	}

}
