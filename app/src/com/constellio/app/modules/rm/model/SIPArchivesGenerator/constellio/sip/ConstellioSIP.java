package com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip;

import au.edu.apsr.mtk.base.*;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.data.SIPObjectsProvider;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.ead.EAD;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.ead.EADArchdesc;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.exceptions.SIPMaxFileCountReachedException;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.exceptions.SIPMaxFileLengthReachedException;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.exceptions.SIPMaxReachedException;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.SIPCategory;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.SIPDocument;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.SIPFolder;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.SIPObject;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.slip.SIPSlip;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.xsd.XMLDocumentValidator;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.DOMBuilder;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.DOMOutputter;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * metsHdr CREATEDATE="..." RECORDSTATUS="Complete"
 * - agent ROLE="CREATOR" ORGANIZATION="Commission Charbonneau"
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

    private static final String TAGMANIFEST_FILE_NAME = "tagmanifest-" + HASH_TYPE + ".txt";

    private static final String MANIFEST_FILE_NAME = "manifest-" + HASH_TYPE + ".txt";

    private SIPObjectsProvider sipObjectsProvider;

    private List<String> providedBagInfoLines;

    private File metsFile;

    private String metsFilename;

    private File bagDir;

    private File bagInfoFile;

    private File manifestFile;

    private File tagmanifestFile;

    private ZipArchiveOutputStream zipOutputStream;

    private Date sipCreationDate;

    private SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

    private SimpleDateFormat sdfTimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private Namespace constellioNamespace = Namespace.getNamespace("constellio", "http://www.constellio.com");

    private Namespace xsiNamespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

    private Map<String, Integer> extensionCounts = new HashMap<String, Integer>();

    private List<String> bagInfoLines = new ArrayList<String>();

    private List<String> manifestLines = new ArrayList<String>();

    private SIPSlip sipSlip = new SIPSlip();

    private XMLDocumentValidator validator = new XMLDocumentValidator();

    private long sipFilesLength;

    private int currentDocumentIndex;

    private int sipFilesCount;

    private boolean limitSize;

    public ConstellioSIP(SIPObjectsProvider sipObjectsProvider, List<String> bagInfoLines, boolean limitSize) {
        this.sipObjectsProvider = sipObjectsProvider;
        this.providedBagInfoLines = bagInfoLines;
        this.currentDocumentIndex = sipObjectsProvider.getStartIndex();
        this.limitSize = limitSize;
    }

    public void build(File zipFile) throws IOException, JDOMException, SIPMaxReachedException {
        SIPMaxReachedException maxReachedException = null;
        sipCreationDate = new Date();

        File outputDir = zipFile.getParentFile();
        outputDir.mkdirs();

        OutputStream zipFileOutputStream = new FileOutputStream(zipFile);
        zipOutputStream = new ZipArchiveOutputStream(zipFileOutputStream);
        zipOutputStream.setUseZip64(Zip64Mode.AsNeeded);

        String sipFilename = FilenameUtils.removeExtension(zipFile.getName());
        metsFilename = sipFilename + ".xml";

        try {
            buildMetsFileAndBagDir();
        } catch (SIPMaxReachedException e) {
            maxReachedException = e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        String slipFilename = sipFilename + ".xls";
        File slipFile = new File(outputDir, slipFilename);
        OutputStream slipFileOutputStream = new FileOutputStream(slipFile);
        sipSlip.write(slipFileOutputStream, bagInfoLines);

        zipOutputStream.close();
        zipFileOutputStream.close();
        slipFileOutputStream.close();

        if (maxReachedException != null) {
            throw maxReachedException;
        }
    }

    private void addToZip(File file, String path) throws IOException {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        sipFilesLength += file.length();
        sipFilesCount++;

        ArchiveEntry entry = zipOutputStream.createArchiveEntry(file, path);
        zipOutputStream.putArchiveEntry(entry);
        InputStream fis = new FileInputStream(file);
        IOUtils.copy(fis, zipOutputStream);
        fis.close();
        zipOutputStream.closeArchiveEntry();
//		file.deleteOnExit();
    }

    private void buildMetsFileAndBagDir() throws IOException, METSException, SAXException, JDOMException, SIPMaxReachedException {
        File tempFile = File.createTempFile(ConstellioSIP.class.getSimpleName(), ".temp");
        tempFile.deleteOnExit();

        File tempDir = tempFile.getParentFile();
        String bagDirName = UUID.randomUUID().toString();
        bagDir = new File(tempDir, bagDirName);
        bagDir.mkdirs();

        buildMetsFile();
        tempFile.delete();
    }

    private String getZipPath(SIPObject sipObject) {
        return sipObject.getZipPath();
    }

    private void addMdRefAndGenerateEAD(SIPObject sipObject, DmdSec dmdSec) throws IOException, METSException {
        EADArchdesc archdesc = sipObjectsProvider.getEADArchdesc(sipObject);
        if (sipObject instanceof SIPDocument) {
            SIPDocument sipDocument = (SIPDocument) sipObject;
            SIPFolder sipFolder = sipDocument.getFolder();

            String zipFolderPath = getZipPath(sipFolder);
            String fileId = sipDocument.getFileId();
            String zipXMLPath = zipFolderPath + "/" + fileId + ".xml";

            File tempXMLFile = File.createTempFile(ConstellioSIP.class.getSimpleName(), ".xml");
            tempXMLFile.deleteOnExit();

            EAD ead = new EAD(sipObject, archdesc);
            ead.build(tempXMLFile);

            addToZip(tempXMLFile, zipXMLPath);

            String hash = getHash(tempXMLFile);
            addManifestLine(hash, zipXMLPath);
            tempXMLFile.delete();

            MdRef mdRef = dmdSec.newMdRef();
            mdRef.setLocType("URN");
            mdRef.setMDType("EAD");
            mdRef.setHref(zipXMLPath);
            dmdSec.setMdRef(mdRef);
        } else if (sipObject instanceof SIPFolder) {
            SIPFolder sipFolder = (SIPFolder) sipObject;

            String zipParentPath;
            if (sipFolder.getParentFolder() != null) {
                zipParentPath = getZipPath(sipFolder.getParentFolder());
            } else {
                zipParentPath = getZipPath(sipFolder.getCategory());
            }

            String folderId = sipFolder.getId();
            String zipXMLPath = zipParentPath + "/" + folderId + "-D.xml";

            File tempXMLFile = File.createTempFile(ConstellioSIP.class.getSimpleName(), ".xml");
            tempXMLFile.deleteOnExit();

            EAD ead = new EAD(sipObject, archdesc);
            ead.build(tempXMLFile);

            addToZip(tempXMLFile, zipXMLPath);

            String hash = getHash(tempXMLFile);
            addManifestLine(hash, zipXMLPath);
            tempXMLFile.delete();

            MdRef mdRef = dmdSec.newMdRef();
            mdRef.setLocType("URN");
            mdRef.setMDType("EAD");
            mdRef.setHref(zipXMLPath);
            dmdSec.setMdRef(mdRef);
        }
    }

    private void addToSIP(SIPObject sipObject, METS mets, FileGrp documentFileGroup, Div bagDiv) throws IOException, METSException, SIPMaxFileLengthReachedException, SIPMaxFileCountReachedException {
        String dmdSecId = getDmdSecId(sipObject);
        DmdSec dmdSec = mets.newDmdSec();
        dmdSec.setID(dmdSecId);

        MdWrap mdWrap = dmdSec.newMdWrap();
        dmdSec.setMdWrap(mdWrap);
        mdWrap.setMDType("OTHER");
        Node xmlData = toXmlData(sipObject);
        mdWrap.setXmlData(xmlData);

        if (sipObject instanceof SIPDocument) {
            long documentFilesLength = 0;
            int documentFilesCount = 1;

            SIPDocument sipDocument = (SIPDocument) sipObject;
            SIPFolder sipFolder = sipDocument.getFolder();

            String fileId = "_" + sipDocument.getFileId();
            String filename = sipDocument.getFilename();
            File file = sipDocument.getFile();

            long length = sipDocument.getLength();
            documentFilesLength += length;
            Map<String, byte[]> extraFiles = sipObjectsProvider.getExtraFiles(sipDocument);
            if (extraFiles != null) {
                for (byte[] extraFileBytes : extraFiles.values()) {
                    documentFilesLength += extraFileBytes.length;
                    documentFilesCount++;
                }
            }

            if (limitSize) {
                if (sipFilesLength + documentFilesLength > SIP_MAX_FILES_LENGTH) {
                    throw new SIPMaxFileLengthReachedException(sipFilesLength + documentFilesLength, SIP_MAX_FILES_LENGTH, currentDocumentIndex);
                } else if (sipFilesCount + documentFilesCount > SIP_MAX_FILES) {
                    throw new SIPMaxFileCountReachedException(sipFilesCount + documentFilesCount, SIP_MAX_FILES, currentDocumentIndex);
                } else {
                    currentDocumentIndex++;
                }
            } else {
                currentDocumentIndex++;
            }
            String hash = null;
            if(file != null) {
                hash = getHash(file);
            }
            String extension = FilenameUtils.getExtension(filename);
            Integer extensionCount = extensionCounts.get(extension);
            if (extensionCount == null) {
                extensionCounts.put(extension, 1);
            } else {
                extensionCounts.put(extension, extensionCount + 1);
            }

            String folderDmdSecId = getDmdSecId(sipFolder);
            if (mets.getDmdSec(folderDmdSecId) == null) {
                addToSIP(sipFolder, mets, documentFileGroup, bagDiv);
            }

            String zipFilePath = getZipPath(sipDocument);

            au.edu.apsr.mtk.base.File documentFile = documentFileGroup.newFile();
            documentFileGroup.addFile(documentFile);
            documentFile.setID(fileId);
            documentFile.setDmdID(dmdSecId);
            documentFile.setSize(length);
            if(hash != null) {
                documentFile.setChecksum(hash);
            }
            documentFile.setChecksumType("SHA-256");

            FLocat documentFileFLocat = documentFile.newFLocat();
            documentFile.addFLocat(documentFileFLocat);
            documentFileFLocat.setLocType("URL");
            documentFileFLocat.setHref(zipFilePath);
            documentFileFLocat.setTitle(filename);

            Div folderDiv = findOrCreateFolderDiv(sipDocument, bagDiv);
            Fptr fileFptr = folderDiv.newFptr();
            fileFptr.setFileID(fileId);
            folderDiv.addFptr(fileFptr);

            if(file != null) {

                addToZip(file, zipFilePath);
            }
            addManifestLine(hash, zipFilePath);

            if (extraFiles != null) {
                int i = 1;
                for (Entry<String, byte[]> entry : extraFiles.entrySet()) {
                    String extraFileId = fileId + "-" + i;
                    String extraFilename = entry.getKey();
                    String extraFileExtension = FilenameUtils.getExtension(extraFilename);
                    if (StringUtils.isNotBlank(extraFileExtension)) {
                        File extraTempFile = File.createTempFile(ConstellioSIP.class.getName(), extraFilename);

                        byte[] extraFileBytes = entry.getValue();
                        FileUtils.writeByteArrayToFile(extraTempFile, extraFileBytes);

                        String extraFileHash = getHash(extraTempFile);
                        String extraZipFilePath = StringUtils.substringBeforeLast(zipFilePath, ".") + "-" + i + "." + extraFileExtension;

                        au.edu.apsr.mtk.base.File extraDocumentFile = documentFileGroup.newFile();
                        documentFileGroup.addFile(extraDocumentFile);
                        extraDocumentFile.setID(extraFileId);
                        extraDocumentFile.setSize(extraFileBytes.length);
                        extraDocumentFile.setChecksum(extraFileHash);
                        extraDocumentFile.setChecksumType("SHA-256");

                        FLocat extraDocumentFileFLocat = extraDocumentFile.newFLocat();
                        extraDocumentFile.addFLocat(extraDocumentFileFLocat);
                        extraDocumentFileFLocat.setLocType("URL");
                        extraDocumentFileFLocat.setHref(extraZipFilePath);
                        extraDocumentFileFLocat.setTitle(extraFilename);

                        Fptr extraFileFptr = folderDiv.newFptr();
                        extraFileFptr.setFileID(extraFileId);
                        folderDiv.addFptr(extraFileFptr);

                        addToZip(extraTempFile, extraZipFilePath);
                        addManifestLine(extraFileHash, extraZipFilePath);
                        extraTempFile.delete();

                        i++;
                    }
                }
            }

            List<Div> subDivs = new ArrayList<Div>(folderDiv.getDivs());
            for (Div subDiv : subDivs) {
                String subDivId = subDiv.getID();
                folderDiv.removeDiv(subDivId);
                folderDiv.addDiv(subDiv);
            }
            sipSlip.add(sipDocument);
        } else if (sipObject instanceof SIPFolder) {
            SIPFolder folder = (SIPFolder) sipObject;

            SIPFolder currentFolder = folder.getParentFolder();
            while (currentFolder != null) {
                String currentFolderId = getDmdSecId(currentFolder);
                DmdSec currentFolderDmdSec = mets.getDmdSec(currentFolderId);
                if (currentFolderDmdSec == null) {
                    // Recursive call
                    addToSIP(currentFolder, mets, documentFileGroup, bagDiv);
                }
                currentFolder = currentFolder.getParentFolder();
            }
        }
        mets.addDmdSec(dmdSec);
        addMdRefAndGenerateEAD(sipObject, dmdSec);
    }

    private String getDmdSecId(SIPObject sipObject) {
        return sipObject.getType() + "-" + sipObject.getId();
    }

    private Div findOrCreateFolderDiv(SIPDocument sipDocument, Div bagDiv) throws METSException {
        List<SIPObject> parents = new ArrayList<SIPObject>();

        SIPFolder sipFolder = sipDocument.getFolder();

        SIPFolder currentFolder = sipFolder;
        SIPCategory sipCategory = null;
        while (currentFolder != null) {
            sipCategory = currentFolder.getCategory();
            parents.add(0, currentFolder);
            currentFolder = currentFolder.getParentFolder();
        }

        SIPCategory currentCategory = sipCategory;
        while (currentCategory != null) {
            parents.add(0, currentCategory);
            currentCategory = currentCategory.getParentCategory();
        }

        Div currentParentDiv = bagDiv;
        for (int i = 0; i < parents.size(); i++) {
            SIPObject parent = parents.get(i);
            if (parent instanceof SIPCategory) {
                currentParentDiv = findOrCreateCategoryDiv((SIPCategory) parent, currentParentDiv);
            } else if (parent instanceof SIPFolder) {
                currentParentDiv = findOrCreateFolderDiv((SIPFolder) parent, currentParentDiv);
            } else {
                throw new RuntimeException("Invalid parent : " + parent);
            }
        }
        return currentParentDiv;
    }

    private Div findOrCreateFolderDiv(SIPFolder sipFolder, Div categoryOrFolderDiv) throws METSException {
        Div folderDiv = null;
        String dmdSecIdForFolder = getDmdSecId(sipFolder);
        for (Div categoryOrFolderSubdiv : categoryOrFolderDiv.getDivs()) {
            String subdivDmdId = categoryOrFolderSubdiv.getDmdID();
            if (dmdSecIdForFolder.equals(subdivDmdId)) {
                folderDiv = categoryOrFolderSubdiv;
                break;
            }
        }
        if (folderDiv == null) {
            String label = sipFolder.getTitle();
            folderDiv = categoryOrFolderDiv.newDiv();
            folderDiv.setID("_" + dmdSecIdForFolder);
            folderDiv.setDmdID(dmdSecIdForFolder);
            folderDiv.setLabel(label);
            folderDiv.setType(sipFolder.getType());
            categoryOrFolderDiv.addDiv(folderDiv);
        }
        return folderDiv;
    }

    private Div findOrCreateCategoryDiv(SIPCategory sipCategory, Div bagOrCategoryDiv) throws METSException {
        Div categoryDiv = null;
        String divIdForCategory = "_" + sipCategory.getCode();
        for (Div bagOrCategorySubdiv : bagOrCategoryDiv.getDivs()) {
            String subdivId = bagOrCategorySubdiv.getID();
            if (divIdForCategory.equals(subdivId)) {
                categoryDiv = bagOrCategorySubdiv;
                break;
            }
        }
        if (categoryDiv == null) {
            String label = sipCategory.getCode() + " - " + sipCategory.getTitle();
            categoryDiv = bagOrCategoryDiv.newDiv();
            categoryDiv.setID(divIdForCategory);
            categoryDiv.setLabel(label);
            categoryDiv.setType(sipCategory.getType());
            bagOrCategoryDiv.addDiv(categoryDiv);
        }
        return categoryDiv;
    }

    private void buildMetsFile() throws IOException, METSException, SAXException, JDOMException, SIPMaxReachedException {
        SIPMaxReachedException maxReachedException = null;
        METSWrapper metsWrapper = new METSWrapper();
        METS mets = metsWrapper.getMETSObject();

        MetsHdr metsHeader = mets.newMetsHdr();
        metsHeader.setCreateDate(sdfTimestamp.format(sipCreationDate));
        metsHeader.setRecordStatus("COMPLETE");
        mets.setMetsHdr(metsHeader);

        Agent agent = metsHeader.newAgent();
        agent.setRole("CREATOR");
        agent.setType("ORGANIZATION");
        agent.setName("Commission d'enquête sur l'octroi et la gestion des contrats publics dans l'industrie de la construction");
        metsHeader.addAgent(agent);

        FileSec fileSec = mets.newFileSec();

        FileGrp documentFileGroup = fileSec.newFileGrp();
        fileSec.addFileGrp(documentFileGroup);

        StructMap structMap = mets.newStructMap();

        Div bagDiv = structMap.newDiv();
        structMap.addDiv(bagDiv);
        bagDiv.setLabel("bag");
        bagDiv.setType("folder");

        List<SIPObject> sipObjects = sipObjectsProvider.list();
        for (SIPObject sipObject : sipObjects) {
            try {
                addToSIP(sipObject, mets, documentFileGroup, bagDiv);
            } catch (SIPMaxFileLengthReachedException e) {
                maxReachedException = e;
                break;
            } catch (SIPMaxFileCountReachedException e) {
                maxReachedException = e;
                break;
            }
        }
        mets.setFileSec(fileSec);
        mets.addStructMap(structMap);

        collectBagInfoLines();
        buildBagInfoFile();
        String bagInfoFileZipPath = "/" + BAG_INFO_FILE_NAME;
        addToZip(bagInfoFile, bagInfoFileZipPath);

        metsFile = File.createTempFile(ConstellioSIP.class.getSimpleName(), metsFilename);
        metsFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(metsFile);

        org.w3c.dom.Document domDoc = metsWrapper.getMETSDocument();
        DOMBuilder domBuilder = new DOMBuilder();
        org.jdom2.Document jdomDoc = domBuilder.build(domDoc);

        Element rootElement = jdomDoc.getRootElement();
        rootElement.addNamespaceDeclaration(Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink"));
        rootElement.addNamespaceDeclaration(Namespace.getNamespace("PREMIS", "info:lc/xmlns/premis-v2"));
        rootElement.addNamespaceDeclaration(xsiNamespace);
        rootElement.addNamespaceDeclaration(constellioNamespace);
        rootElement.setAttribute("schemaLocation", "http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.w3.org/1999/xlink http://www.loc.gov/standards/mets/xlink.xsd", xsiNamespace);
        rootElement.setAttribute("TYPE", "sa_all-formats-01_dss-01");

        XMLOutputter xml = new XMLOutputter();
        xml.setFormat(Format.getPrettyFormat());
        xml.output(jdomDoc, fos);
        fos.close();

        validator.validate(jdomDoc, "xlink.xsd", "mets.xsd");

        SAXBuilder builder = new SAXBuilder();
        builder.build(metsFile);

        buildManifestFile();
        String manifestFileZipPath = "/" + MANIFEST_FILE_NAME;
        addToZip(manifestFile, manifestFileZipPath);

        String metsFileZipPath = "/" + metsFilename;
        addToZip(metsFile, metsFileZipPath);

        buildTagmanifestFile();
        String tagmanifestFileZipPath = "/" + TAGMANIFEST_FILE_NAME;
        addToZip(tagmanifestFile, tagmanifestFileZipPath);

        bagInfoFile.delete();
        manifestFile.delete();
        metsFile.delete();
        tagmanifestFile.delete();

        if (maxReachedException != null) {
            throw maxReachedException;
        }
    }

    private String printable(String text) {
        String result;
        if (text != null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                sb.append(c);
            }
            result = sb.toString();
        } else {
            result = null;
        }
        return result;
    }

    private Node toXmlData(SIPObject sipObject) {
        org.jdom2.Document jdomDoc = new org.jdom2.Document();
        Element xmlData = new Element("extends", constellioNamespace);
        jdomDoc.setRootElement(xmlData);

        List<String> metadataIds = sipObjectsProvider.getMetadataIds(sipObject);
        for (String metadataId : metadataIds) {
//			String metadataLabel = sipObject.getMetadataLabel(metadataId);
            List<String> metadataValues = sipObjectsProvider.getMetadataValues(sipObject, metadataId);
            if (!metadataValues.isEmpty()) {
                Element field = new Element("field", constellioNamespace);
                xmlData.addContent(field);

                field.setAttribute("name", metadataId);
//				field.setAttribute("label", metadataLabel);
                if (metadataValues.size() == 1) {
                    String metadataValue = metadataValues.get(0);
                    field.setText(printable(metadataValue));
                } else {
                    for (String metadataValue : metadataValues) {
                        Element value = new Element("value");
                        field.addContent(value);
                        field.setText(printable(metadataValue));
                    }
                }
            }
        }

        DOMOutputter domOutputter = new DOMOutputter();
        org.w3c.dom.Document w3cDoc;
        try {
            w3cDoc = domOutputter.output(jdomDoc);
        } catch (JDOMException e) {
            throw new RuntimeException(e);
        }
        return w3cDoc.getDocumentElement();
    }

    private void collectBagInfoLines() {
        bagInfoLines.addAll(this.providedBagInfoLines);

        bagInfoLines.add("Nombre de fichiers numériques : " + sipFilesCount);
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
        bagInfoLines.add("Taille des fichiers numériques non compressés : " + FileUtils.byteCountToDisplaySize(sipFilesLength) + " (" + sipFilesLength + " octets)");
        bagInfoLines.add("");
        bagInfoLines.add("Logiciel : Constellio");
        bagInfoLines.add("Site web de l’éditeur : http://www.constellio.com");
        bagInfoLines.add("Version du logiciel : 5.1.1");
        bagInfoLines.add("Date de création du paquet : " + sdfDate.format(new Date()));
        bagInfoLines.add("");
    }

    private void buildBagInfoFile() throws IOException {
        bagInfoFile = File.createTempFile(ConstellioSIP.class.getSimpleName(), BAG_INFO_FILE_NAME);
        bagInfoFile.deleteOnExit();
        writeFile(bagInfoFile, bagInfoLines);
    }

    private void buildManifestFile() throws IOException {
        manifestFile = File.createTempFile(ConstellioSIP.class.getSimpleName(), MANIFEST_FILE_NAME);
        manifestFile.deleteOnExit();
        writeFile(manifestFile, manifestLines);
    }

    private void buildTagmanifestFile() throws IOException {
        List<String> tagmanifestLines = new ArrayList<String>();

        String bagInfoFileHash = getHash(bagInfoFile);
        tagmanifestLines.add(bagInfoFileHash + " " + BAG_INFO_FILE_NAME);

        String metsFileHash = getHash(metsFile);
//		String packageInfoFileHash = getHash(packageInfoFile);
        String manifestFileHash = getHash(manifestFile);

        tagmanifestLines.add(metsFileHash + " " + metsFilename);
//		manifestLines.add(packageInfoFileHash + " " + PACKAGE_INFO_FILE_NAME);
        tagmanifestLines.add(manifestFileHash + " " + MANIFEST_FILE_NAME);

        tagmanifestFile = File.createTempFile(ConstellioSIP.class.getSimpleName(), TAGMANIFEST_FILE_NAME);
        tagmanifestFile.deleteOnExit();
        writeFile(tagmanifestFile, tagmanifestLines);
    }

    private void addManifestLine(String hash, String filePath) {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        manifestLines.add(hash + " " + filePath);
    }

    private void writeFile(File file, List<String> lines) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.writeLines(lines, "\n", fos, "UTF-8");
        IOUtils.closeQuietly(fos);
    }

    private String getHash(File file) throws IOException {
        return getHash(new FileInputStream(file));
    }

    private String getHash(InputStream in) throws IOException {
        String hash = DigestUtils.sha256Hex(in);
        IOUtils.closeQuietly(in);
        return hash;
    }

    /**
     * https://share.fcla.edu/FDAPublic/Affiliates/FDASipSpecification_version2.2.pdf (page 4)
     * <p>
     * semi-colon: “;”
     * slash: “/”
     * reverse slash: “\”
     * question mark: “?”
     * colon: “:”
     * at sign: “@”
     * ampersand: “&”
     * equals sign: “=”
     * plus sign: “+”
     * dollar sign: “$”
     * comma: “,”
     * curly brackets: “{“ and “}”
     * vertical line: “|”
     * caret: “^”
     * square brackets: “[“ and “]”
     * multiple spaces
     * SIP directory names may not start with dot/period ( . )
     *
     * @param text
     * @return
     */
    protected String escapePath(String text) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            boolean escape = false;
            if (i == 0 && c == '.') {
                escape = true;
            } else if (c == ' ' && sb.toString().endsWith(" ")) {
                escape = true;
            } else {
                for (int j = 0; j < RESERVED_PATH_CHARS.length; j++) {
                    char reservedPathChar = RESERVED_PATH_CHARS[j];
                    if (reservedPathChar == c) {
                        escape = true;
                    }
                }
            }
            if (escape) {
                sb.append("_");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
