package com.constellio.model.services.contents;

import com.constellio.data.dao.services.contents.DaoFile;
import com.constellio.model.entities.records.ParsedContent;

import java.util.Optional;

public abstract class VaultContentEntry {

	DaoFile vaultFile;

	public VaultContentEntry(DaoFile daoFile) {
		this.vaultFile = daoFile;
	}

	public DaoFile getFile() {
		return vaultFile;
	}

	public DaoFile getPreview() {
		String name = vaultFile.getName() + ".preview";
		return vaultFile.getContentDao().getFile(name);
	}

	public DaoFile getThumbnails() {
		String name = vaultFile.getName() + ".thumbnail";
		return vaultFile.getContentDao().getFile(name);
	}

	public DaoFile getIcapScan() {
		String name = vaultFile.getName() + ".icapscan";
		return vaultFile.getContentDao().getFile(name);
	}

	public DaoFile getParsedContent() {
		String name = vaultFile.getName() + "__parsed";
		return vaultFile.getContentDao().getFile(name);
	}

	public DaoFile getJpegConversion() {
		String name = vaultFile.getName() + ".jpegConversion";
		return vaultFile.getContentDao().getFile(name);
	}

	public abstract Optional<ParsedContent> loadParsedContent();
}
