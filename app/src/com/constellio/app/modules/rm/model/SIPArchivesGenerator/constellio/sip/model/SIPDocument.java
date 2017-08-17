package com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model;

import java.io.File;

public interface SIPDocument extends SIPObject {
	
	String DOCUMENT_TYPE = "DOCUMENT";

	String getTitle();
	
	String getFileId();
	
	long getLength();
	
	String getFilename();
	
	SIPFolder getFolder();
	
	File getFile();

}
