package com.constellio.app.services.sip.zip;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.bagInfo.SIPZipBagInfoFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class BackedUpFileSIPZipWriter extends FileSIPZipWriter {

	File backupFolder;

	public BackedUpFileSIPZipWriter(AppLayerFactory appLayerFactory, File zipFile, String sipFileName,
									SIPZipBagInfoFactory bagInfoFactory, File backupFolder) throws IOException {
		super(appLayerFactory, zipFile, sipFileName, bagInfoFactory);
		this.backupFolder = new File(backupFolder, sipFileName);
	}


	@Override
	protected void addFileWithoutFlushing(File file, String hash, String path) throws IOException {

		File dest = new File(backupFolder, path.replace("/", File.separator));
		FileUtils.copyFile(file, dest);
		super.addFileWithoutFlushing(file, hash, path);
	}
}
