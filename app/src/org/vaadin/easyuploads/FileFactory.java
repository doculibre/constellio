package org.vaadin.easyuploads;

import java.io.File;
import java.io.Serializable;

public interface FileFactory extends Serializable {
	public File createFile(String fileName, String mimeType);
}