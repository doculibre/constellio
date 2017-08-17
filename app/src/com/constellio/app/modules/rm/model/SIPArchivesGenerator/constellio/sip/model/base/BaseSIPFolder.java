package com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.base;

import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.SIPCategory;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.SIPFolder;

import java.util.List;
import java.util.Map;

public abstract class BaseSIPFolder extends BaseSIPObject implements SIPFolder {
	
	private SIPFolder parentFolder;
	
	private SIPCategory category;

	public BaseSIPFolder(String id, List<String> metadataIds,
			Map<String, String> metadataLabels,
			Map<String, List<String>> metadataValues, String title, SIPFolder parentFolder, SIPCategory category) {
		super(id, FOLDER_TYPE, title, metadataIds, metadataLabels, metadataValues);
		this.parentFolder = parentFolder;
		this.category = category;
	}

	@Override
	public SIPFolder getParentFolder() {
		return parentFolder;
	}

	@Override
	public SIPCategory getCategory() {
		return category;
	}

}
