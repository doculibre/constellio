package com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.intelligid;

import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.SIPDocument;
import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.SIPFolder;
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

public class IntelliGIDSIPDocument extends IntelliGIDSIPFicheMetadonnees implements SIPDocument {
	
	private SIPFolder folder;
	
//	private Long idFichierElectronique;
	
	private Long tailleFichierElectronique;

	private EntityRetriever entityRetriever;
	
	private String sipFilename;
	
	private File sipFile;
	
	public IntelliGIDSIPDocument(Document ficheDocument, List<Metadata> metadonneesDocument, List<Metadata> metadonneesDossier, EntityRetriever entityRetriever) {
		super(ficheDocument, metadonneesDocument, metadonneesDossier);
		
		Content fichierElectronique = ficheDocument.getContent();
		if(fichierElectronique != null) {
			tailleFichierElectronique = fichierElectronique.getLastMajorContentVersion().getLength();
			sipFilename = fichierElectronique.getLastMajorContentVersion().getFilename();
			findCommonsTransactionFilename(fichierElectronique);
		}

		this.entityRetriever = entityRetriever;
		Folder ficheDossier = entityRetriever.getFoldersFromString(ficheDocument.getFolder());
		if (ficheDossier != null) {
			folder = new IntelliGIDSIPFolder(ficheDossier, metadonneesDossier);
		}
	}
	
	private void findCommonsTransactionFilename(Content fichierElectronique) {
		try{
			InputStream inputStream = entityRetriever.getContentFromHash(fichierElectronique.getLastMajorContentVersion().getHash());
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
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getType() {
		return DOCUMENT_TYPE;
	}

	@Override
	public String getTitle() {
		return getFicheMetadonnees().getTitle();
	}

	@Override
	public String getFileId() {
//		return "" + idFichierElectronique;
		return getId();
	}

	@Override
	public long getLength() {
		return tailleFichierElectronique;
	}

	@Override
	public String getFilename() {
		return sipFilename;
	}

	@Override
	public SIPFolder getFolder() {
		return folder;
	}

	@Override
	public File getFile() {
		return sipFile;
	}

	@Override
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
