package com.constellio.app.services.sip.zip;

import com.constellio.app.services.sip.mets.MetsContentFileReference;
import com.constellio.app.services.sip.mets.MetsEADMetadataReference;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SIPZipWriterTransaction {

	private File tempFolder;

	private Map<String, File> files = new HashMap<>();

	private List<MetsContentFileReference> contentFileReferences = new ArrayList<>();

	private List<MetsEADMetadataReference> eadMetadataReferences = new ArrayList<>();

	public SIPZipWriterTransaction(File tempFolder) {
		this.tempFolder = tempFolder;
	}

	public File createEntryFile(String path) {
		File file = new File(tempFolder, UUIDV1Generator.newRandomId());
		files.put(path, file);
		return file;
	}

	private long length() {
		long length = 0;

		for (File file : files.values()) {
			length += file.length();
		}

		return length;
	}

	public Map<String, File> getFiles() {
		return files;
	}

	public void add(MetsContentFileReference contentFileReference) {
		contentFileReferences.add(contentFileReference);
	}

	public void add(MetsEADMetadataReference eadMetadataReference) {
		eadMetadataReferences.add(eadMetadataReference);
	}

	public List<MetsContentFileReference> getContentFileReferences() {
		return contentFileReferences;
	}

	public List<MetsEADMetadataReference> getEadMetadataReferences() {
		return eadMetadataReferences;
	}

	public boolean containsEADMetadatasOf(String id) {
		for (MetsEADMetadataReference reference : eadMetadataReferences) {
			if (id.equals(reference.getId())) {
				return true;
			}
		}
		return false;
	}

}