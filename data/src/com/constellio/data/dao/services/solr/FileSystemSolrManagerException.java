package com.constellio.data.dao.services.solr;

import java.io.File;

@SuppressWarnings("serial")
public final class FileSystemSolrManagerException extends RuntimeException {

	private FileSystemSolrManagerException(String message) {
		super(message);
	}

	public static RuntimeException noSuchFolder(File defaultCoreFolder) {
		String message = "Folder '" + defaultCoreFolder.getAbsolutePath() + "' doesn't exist.";
		return new FileSystemSolrManagerException(message);
	}

	public static RuntimeException noSuchSolrConfig(File defaultCoreFolder) {
		String message = "No solrconfig.xml in folder '" + defaultCoreFolder.getAbsolutePath() + "'";
		return new FileSystemSolrManagerException(message);
	}

	public static RuntimeException noSuchSchema(File defaultCoreFolder) {
		String message = "No schema.xml in folder '" + defaultCoreFolder.getAbsolutePath() + "'";
		return new FileSystemSolrManagerException(message);
	}

	public static RuntimeException cannotCopyCoreInTempWorkFolder(File tempWorkFolder) {
		String message = "No schema.xml in folder '" + tempWorkFolder.getAbsolutePath() + "'";
		return new FileSystemSolrManagerException(message);
	}

}
