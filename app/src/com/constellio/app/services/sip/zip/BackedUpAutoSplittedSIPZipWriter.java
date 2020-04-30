package com.constellio.app.services.sip.zip;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.bagInfo.SIPZipBagInfoFactory;

import java.io.File;
import java.io.IOException;

public class BackedUpAutoSplittedSIPZipWriter extends AutoSplittedSIPZipWriter {

	File backupFolder;

	public BackedUpAutoSplittedSIPZipWriter(AppLayerFactory appLayerFactory,
											SIPFileNameProvider sipFileProvider, long sipBytesLimit,
											SIPZipBagInfoFactory bagInfoFactory, File backupFolder) {
		super(appLayerFactory, sipFileProvider, sipBytesLimit, bagInfoFactory);
		this.backupFolder = backupFolder;
	}

	protected FileSIPZipWriter newFileSIPZipWriter(int newWriterIndex)
			throws IOException {

		return new BackedUpFileSIPZipWriter(appLayerFactory,
				sipFileProvider.newSIPFile(newWriterIndex),
				sipFileProvider.newSIPName(newWriterIndex), bagInfoFactory, backupFolder);
	}


}
