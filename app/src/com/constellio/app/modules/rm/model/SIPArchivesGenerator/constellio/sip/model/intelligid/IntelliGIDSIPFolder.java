package com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.intelligid;

import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.SIPCategory;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.SIPFolder;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.Metadata;
import org.joda.time.LocalDate;

import java.util.List;

public class IntelliGIDSIPFolder extends IntelliGIDSIPFicheMetadonnees implements SIPFolder {
	
	private String title;
	
	private SIPFolder parentFolder;
	
	private SIPCategory category;

	private EntityRetriever entityRetriever;
	
	public IntelliGIDSIPFolder(Folder ficheDossier, List<Metadata> metadonneesDossier, EntityRetriever entityRetriever) {
		super(adjust(ficheDossier), metadonneesDossier, metadonneesDossier);
		this.title = ficheDossier.getTitle();
		this.entityRetriever = entityRetriever;
		Folder ficheDossierParent = entityRetriever.getFoldersFromString(ficheDossier.getParentFolder());
		if (ficheDossierParent != null) {
			parentFolder = new IntelliGIDSIPFolder(ficheDossierParent, metadonneesDossier, entityRetriever);
		} else {
			Category processusActivite = entityRetriever.getCategoryById(ficheDossier.getCategory());
			category = new IntelliGIDSIPCategory(processusActivite, entityRetriever);
		}
	}
	
	private static Folder adjust(Folder ficheDossier) {
		ficheDossier.setCloseDateEntered(new LocalDate());
		return ficheDossier;
	}

	@Override
	public String getType() {
		return FOLDER_TYPE;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public SIPFolder getParentFolder() {
		return parentFolder;
	}

	@Override
	public SIPCategory getCategory() {
		return category;
	}

	@Override
	public String getZipPath() {
		StringBuffer sb = new StringBuffer();
		SIPFolder currentFolder = this;
		while (currentFolder != null) {
			String currentFolderId = currentFolder.getId();
			if (sb.length() > 0) {
				sb.insert(0, "/");
			}
			sb.insert(0, currentFolderId);
			
			if (currentFolder.getParentFolder() == null) {
				SIPCategory category = currentFolder.getCategory();
				// Recursive call
				String categoryZipFolderPath = category.getZipPath();
				sb.insert(0, "/");
				sb.insert(0, categoryZipFolderPath);
			}
			currentFolder = currentFolder.getParentFolder();
		}
		return sb.toString();
	}

}
