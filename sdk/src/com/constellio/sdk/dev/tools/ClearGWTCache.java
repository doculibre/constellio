package com.constellio.sdk.dev.tools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.constellio.model.conf.FoldersLocator;

public class ClearGWTCache {

	public static void main(String argv[]) {
		File appProject = new FoldersLocator().getAppProject();
		File gwtCache = new File(appProject, "src/main/webapp/VAADIN/gwt-unitCache".replace("/", File.separator));
		try {
			FileUtils.deleteDirectory(gwtCache);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Done!");
	}

}
