package com.constellio.app.services.sip.zip;

import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.data.io.services.facades.IOServices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface SIPZipWriter {

	IOServices getIoServices();

	void close();

	OutputStream newZipFileOutputStream(final String path);

	BufferedWriter newZipFileWriter(final String path)
			throws IOException;

	void insertAll(SIPZipWriterTransaction transaction)
			throws IOException;

	void addToZip(File file, String path)
			throws IOException;

	SIPZipWriterTransaction newInsertTransaction();

	void discard(SIPZipWriterTransaction transaction);

	long length();

	void addDivisionsInfoMap(Map<String, MetsDivisionInfo> divisionInfoMap);

	void addDivisionInfo(MetsDivisionInfo divisionInfo);

	void setSipFileHasher(SIPFileHasher hasher);

	int metsFileEntriesCount();

}
