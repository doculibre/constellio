package com.constellio.data.io;

import com.constellio.data.conf.HashingEncoding;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.utils.hashing.HashingService;

import java.io.File;

public class IOServicesFactory {

	private final File tempFolder;

	public IOServicesFactory(File tempFolder) {
		super();
		this.tempFolder = tempFolder;
	}

	public FileService newFileService() {
		return new FileService(tempFolder);
	}

	public ZipService newZipService() {
		return new ZipService(newIOServices());
	}

	public IOServices newIOServices() {
		return new IOServices(tempFolder);
	}

	public EncodingService newEncodingService() {
		return new EncodingService();
	}

	public HashingService newHashingService(HashingEncoding hashingEncoding) {
		return HashingService.forSHA1(newEncodingService(), hashingEncoding);
	}

	public File getTempFolder() {
		return tempFolder;
	}
}
