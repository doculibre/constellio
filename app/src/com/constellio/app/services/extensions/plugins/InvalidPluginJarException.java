package com.constellio.app.services.extensions.plugins;

import java.io.FileNotFoundException;

public class InvalidPluginJarException extends Exception {
	public InvalidPluginJarException(Exception e) {
		super(e);
	}

	protected InvalidPluginJarException() {

	}

	public static class InvalidPluginJarException_NonExistingFile extends InvalidPluginJarException {

		public InvalidPluginJarException_NonExistingFile(Exception e) {
			super(e);
		}
	}

	public static class InvalidPluginJarException_InvalidManifest extends InvalidPluginJarException {

		public InvalidPluginJarException_InvalidManifest() {

		}
	}

	public static class InvalidPluginJarException_NoVersion extends InvalidPluginJarException {

		public InvalidPluginJarException_NoVersion() {
			super();
		}
	}

	public static class InvalidPluginJarException_NoCode extends InvalidPluginJarException {

		public InvalidPluginJarException_NoCode(Exception e) {
			super(e);
		}

		public InvalidPluginJarException_NoCode() {

		}
	}

	public static class InvalidPluginJarException_InvalidJar extends InvalidPluginJarException {

		public InvalidPluginJarException_InvalidJar(Exception e) {
			super(e);
		}
	}
}
