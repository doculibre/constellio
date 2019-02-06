package com.constellio.app.modules.rm.services.sip.model;

import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

import java.util.List;

public class SIPFolder extends SIPMetadataObject implements SIPObject {

	private String title;

	private SIPFolder parentFolder;

	private SIPCategory category;

	private EntityRetriever entityRetriever;

	public SIPFolder(Folder folder, List<Metadata> folderMetadatas, EntityRetriever entityRetriever) {
		super(folder.getWrappedRecord(), folderMetadatas);
		this.title = folder.getTitle();
		this.entityRetriever = entityRetriever;
		Folder ficheDossierParent = entityRetriever.getFoldersFromString(folder.getParentFolder());
		if (ficheDossierParent != null) {
			parentFolder = new SIPFolder(ficheDossierParent, folderMetadatas, entityRetriever);
		} else {
			Category processusActivite = entityRetriever.getCategoryById(folder.getCategory());
			category = new SIPCategory(processusActivite, entityRetriever);
		}
	}


	public String getType() {
		return FOLDER_TYPE;
	}

	public String getTitle() {
		return title;
	}

	public SIPFolder getParentFolder() {
		return parentFolder;
	}

	public SIPCategory getCategory() {
		return category;
	}

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

	@Override
	public List<Metadata> getMetadataList() {
		return getSchemaMetadata();
	}

	@Override
	public Record getRecord() {
		return super.getRecord();
	}

}
