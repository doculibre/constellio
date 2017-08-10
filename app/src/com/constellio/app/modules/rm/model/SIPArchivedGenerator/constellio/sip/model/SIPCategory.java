package com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model;

public interface SIPCategory extends SIPObject {
	
	String CATEGORY_TYPE = "RUBRIQUE";
	
	String getCode();

	String getTitle();
	
	String getDescription();
	
	SIPCategory getParentCategory();

}
