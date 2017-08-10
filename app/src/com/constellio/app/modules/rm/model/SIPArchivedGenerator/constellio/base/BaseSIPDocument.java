package com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.base;

import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.SIPDocument;
import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.SIPFolder;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class BaseSIPDocument extends BaseSIPObject implements SIPDocument {
	
	private File file;
	
	private SIPFolder folder;

	public BaseSIPDocument(String id, String title, List<String> metadataIds,
			Map<String, String> metadataLabels,
			Map<String, List<String>> metadataValues, File file, SIPFolder folder) {
		super(id, DOCUMENT_TYPE, title, metadataIds, metadataLabels, metadataValues);
		this.file = file;
		this.folder = folder;
	}

	@Override
	public String getFileId() {
		return "FILE" + getId();
	}

	@Override
	public long getLength() {
		return file.length();
	}

	@Override
	public String getFilename() {
		return file.getName();
	}

	@Override
	public SIPFolder getFolder() {
		return folder;
	}

	@Override
	public File getFile() {
		return file;
	}

}
