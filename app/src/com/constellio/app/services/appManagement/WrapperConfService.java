package com.constellio.app.services.appManagement;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;

public class WrapperConfService {
	private static final Logger LOGGER = LoggerFactory.getLogger(WrapperConfService.class);

	FileService fileService;

	FoldersLocator foldersLocator;

	public WrapperConfService(FileService fileService, FoldersLocator foldersLocator) {
		this.fileService = fileService;
		this.foldersLocator = foldersLocator;
	}

	public WrapperConfService() {
		this.foldersLocator = new FoldersLocator();
		this.fileService = new FileService(foldersLocator.getDefaultTempFolder());
	}


	public void updateWrapperConf(File deployFolder) {

		LOGGER.info("New webapp path is '" + deployFolder.getAbsolutePath() + "'");
		File wrapperConf = foldersLocator.getWrapperConf();
		if (foldersLocator.getFoldersLocatorMode().equals(FoldersLocatorMode.PROJECT) && !wrapperConf.exists()) {
			return;
		}
		List<String> lines = fileService.readFileToLinesWithoutExpectableIOException(wrapperConf);
		for (int i = 0; i < lines.size(); i++) {

			String line = lines.get(i);
			if (line.startsWith("wrapper.java.classpath.2=")) {
				lines.set(i, "wrapper.java.classpath.2=" + deployFolder.getAbsolutePath() + "/WEB-INF/lib/*.jar");
			}
			if (line.startsWith("wrapper.java.classpath.3=")) {
				lines.set(i, "wrapper.java.classpath.3=" + deployFolder.getAbsolutePath() + "/WEB-INF/classes");
			}
			if (line.startsWith("wrapper.commandfile=")) {
				lines.set(i, "wrapper.commandfile=" + deployFolder.getAbsolutePath() + "/WEB-INF/command/cmd");
			}
		}
		fileService.writeLinesToFile(wrapperConf, lines);
	}
}
