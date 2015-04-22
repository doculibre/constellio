/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.io.services.zip;

import java.io.File;

@SuppressWarnings("serial")
public class ZipServiceException extends Exception {

	public ZipServiceException(String message) {
		super(message);
	}

	public ZipServiceException(String message, Exception e) {
		super(message, e);
	}

	public static class ZipFileNotFound extends ZipServiceException {

		public ZipFileNotFound(File zipFile, Exception e) {
			super("Zip file '" + zipFile.getAbsolutePath() + "' doesn't exist", e);
		}

	}

	public static class ZipFileHasNoContent extends ZipServiceException {

		public ZipFileHasNoContent(File zipFile) {
			super("Zip file '" + zipFile.getAbsolutePath() + "' cannot be parsed");
		}

	}

	public static class ZipFileCannotBeParsed extends ZipServiceException {

		public ZipFileCannotBeParsed(File zipFile, Exception e) {
			super("Zip file '" + zipFile.getAbsolutePath() + "' cannot be parsed", e);
		}

	}

	public static class ZipFileCorrupted extends ZipServiceException {

		public ZipFileCorrupted(File zipFile, String entryName, Exception e) {
			super("Entry '" + entryName + "' of zip file '" + zipFile.getAbsolutePath() + "' is corrupted and cannot be read", e);
		}

	}

	public static class ZippedFilesInDifferentParentFolder extends ZipServiceException {

		public ZippedFilesInDifferentParentFolder(File zippedFile, File zippedFileInDifferentParentFolder) {
			super("Zipped file '" + zippedFile.getAbsolutePath() + "' and '"
					+ zippedFileInDifferentParentFolder.getAbsolutePath() + "' are not in the same parent folder");
		}

	}

	public static class CannotCreateZipOutputStreamException extends ZipServiceException {
		public CannotCreateZipOutputStreamException(File zipFile, Exception e) {
			super("Cannot create a zipOutputStream from zipFile: " + zipFile.getAbsolutePath() + " cannot ", e);
		}
	}

	public static class CannotAddFileToZipException extends ZipServiceException {
		public CannotAddFileToZipException(File zippedFile, Exception e) {
			super("Cannot add file " + zippedFile.getAbsolutePath() + " to zip", e);
		}
	}

	public static class ZipFileInvalidExtension extends ZipServiceException {
		public ZipFileInvalidExtension(String fileExtension) {
			super("Invalid zipfile extension: " + fileExtension);
		}
	}

	public static class FileToZipNotFound extends ZipServiceException {
		public FileToZipNotFound(File fileToZip) {
			super("File to zip not found : '" + fileToZip.getAbsolutePath() + "'");
		}
	}
}
