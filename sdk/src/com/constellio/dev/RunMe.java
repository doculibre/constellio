package com.constellio.dev;

import com.constellio.data.conf.FoldersLocator;

public class RunMe {

	public static void main(String argv[]) {

		FoldersLocator foldersLocator = new FoldersLocator();

		System.out.println("Hello world!");
		System.out.println("Property file is '" + foldersLocator.getConstellioProperties().getAbsolutePath() + "'");

	}

}
