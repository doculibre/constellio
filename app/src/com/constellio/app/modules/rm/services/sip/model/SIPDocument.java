package com.constellio.app.modules.rm.services.sip.model;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.Metadata;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class SIPDocument extends SIPFicheMetadonnees implements SIPObject {

    private SIPFolder folder;

    private Long contentLength;

    private EntityRetriever entityRetriever;

    private String sipFilename;

    private File sipFile;

    public SIPDocument(Document document, List<Metadata> documentMetadata, List<Metadata> folderMetadata, EntityRetriever entityRetriever) {
        super(document, documentMetadata);

        Content content = document.getContent();
        if (content != null) {
            contentLength = content.getLastMajorContentVersion().getLength();
            sipFilename = content.getLastMajorContentVersion().getFilename();
            findCommonsTransactionFilename(content);
        }

        this.entityRetriever = entityRetriever;
        if (document.getFolder() != null) {
            Folder ficheDossier = entityRetriever.getFoldersFromString(document.getFolder());
            if (ficheDossier != null) {
                folder = new SIPFolder(ficheDossier, folderMetadata, entityRetriever);
            }
        }
    }

    private void findCommonsTransactionFilename(Content content) {
        InputStream inputStream = null;
        try {
            inputStream = entityRetriever.getContentFromHash(content.getLastMajorContentVersion().getHash());
            sipFile = entityRetriever.newTempFile();
            FileUtils.copyInputStreamToFile(inputStream, sipFile);
//		// FIXME
            if (!sipFile.exists()) {
                if (sipFilename.endsWith("msg")) {
                    sipFile = new File("in/test.msg");
                } else {
                    sipFile = new File("in/Baginfo.txt");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Shhhhhhh...
                }
            }
        }
    }

    public String getType() {
        return DOCUMENT_TYPE;
    }

    public String getTitle() {
        return getFicheMetadonnees().getTitle();
    }

    public String getFileId() {
        return getId();
    }

    public long getLength() {
        return contentLength == null ? 0 : contentLength;
    }

    public String getFilename() {
        return sipFilename;
    }

    public SIPFolder getFolder() {
        return folder;
    }

    public File getFile() {
        return sipFile;
    }

    public String getZipPath() {
        StringBuffer sb = new StringBuffer();
        String fileId = getFileId();
        String filename = getFilename();
        String fileExtension = FilenameUtils.getExtension(filename);
        String documentFilename = fileId + "." + fileExtension;

        sb.append("/");
        sb.append(documentFilename);
        SIPFolder folder = getFolder();
        String folderZipPath = folder.getZipPath();
        sb.insert(0, folderZipPath);
        return sb.toString();
    }

}
