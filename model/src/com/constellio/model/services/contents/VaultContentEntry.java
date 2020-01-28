package com.constellio.model.services.contents;

import com.constellio.model.entities.records.ParsedContent;

import java.io.File;
import java.util.Optional;

public abstract class VaultContentEntry {

	File file;

	public VaultContentEntry(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public File getPreview() {
		return new File(file.getParentFile(), file.getName() + ".preview");
	}

	public File getThumbnails() {
		return new File(file.getParentFile(), file.getName() + ".thumbnails");
	}

	public File getParsedContent() {
		return new File(file.getParentFile(), file.getName() + "__parsed");
	}

	public File getJpegConversion() {
		return new File(file.getParentFile(), file.getName() + ".jpegConversion");
	}

	public abstract Optional<ParsedContent> loadParsedContent();
}
