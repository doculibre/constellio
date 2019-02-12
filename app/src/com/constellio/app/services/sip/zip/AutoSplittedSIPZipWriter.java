package com.constellio.app.services.sip.zip;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.bagInfo.SIPZipBagInfoFactory;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.data.io.services.facades.IOServices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AutoSplittedSIPZipWriter implements SIPZipWriter {

	private AppLayerFactory appLayerFactory;
	private IOServices ioServices;
	private SIPFileHasher sipFileHasher;
	private FileSIPZipWriter currentWriter;
	private Map<String, MetsDivisionInfo> divisionsInfoMap = new HashMap<>();
	private SIPZipBagInfoFactory bagInfoFactory;
	private long sipBytesLimit;
	private int index = 0;
	private SIPFileNameProvider sipFileProvider;

	public AutoSplittedSIPZipWriter(AppLayerFactory appLayerFactory, SIPFileNameProvider sipFileProvider,
									long sipBytesLimit, SIPZipBagInfoFactory bagInfoFactory) {
		this.appLayerFactory = appLayerFactory;
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.sipFileHasher = new SIPFileHasher();
		this.sipFileProvider = sipFileProvider;
		this.sipBytesLimit = sipBytesLimit;
		this.bagInfoFactory = bagInfoFactory;
	}

	@Override
	public IOServices getIoServices() {
		return ioServices;
	}

	@Override
	public void close() {
		if (currentWriter != null) {
			currentWriter.close();
		}
	}

	@Override
	public OutputStream newZipFileOutputStream(String path) {
		return currentWriter.newZipFileOutputStream(path);
	}

	@Override
	public BufferedWriter newZipFileWriter(String path) {
		return currentWriter.newZipFileWriter(path);
	}

	@Override
	public void insertAll(SIPZipWriterTransaction transaction) throws IOException {

		if (currentWriter == null) {
			newCurrentWriter();
		}
		long currentWriterLength = currentWriter.length();
		long transactionLength = transaction.length();
		if (currentWriterLength > 0 && currentWriterLength + transactionLength > sipBytesLimit) {
			currentWriter.close();
			newCurrentWriter();
		}
		currentWriter.insertAll(transaction);

	}

	@Override
	public void addToZip(File file, String path) throws IOException {
		SIPZipWriterTransaction transaction = newInsertTransaction();
		transaction.moveFileToSIPAsUnreferencedContentFile(path, file);
		insertAll(transaction);
	}

	@Override
	public SIPZipWriterTransaction newInsertTransaction() {
		if (currentWriter == null) {
			try {
				newCurrentWriter();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return currentWriter.newInsertTransaction();
	}

	@Override
	public void discard(SIPZipWriterTransaction transaction) {
		currentWriter.discard(transaction);
	}

	@Override
	public long length() {
		return currentWriter == null ? 0 : currentWriter.length();
	}

	@Override
	public void addDivisionsInfoMap(Map<String, MetsDivisionInfo> divisionInfoMap) {
		this.divisionsInfoMap.putAll(divisionInfoMap);
	}

	@Override
	public void setSipFileHasher(SIPFileHasher hasher) {
		this.sipFileHasher = hasher;
		if (currentWriter != null) {
			currentWriter.setSipFileHasher(hasher);
		}
	}


	private void newCurrentWriter() throws IOException {
		int newWriterIndex = ++index;
		currentWriter = new FileSIPZipWriter(appLayerFactory,
				sipFileProvider.newSIPFile(newWriterIndex),
				sipFileProvider.newSIPName(newWriterIndex), bagInfoFactory);
		currentWriter.divisionsInfoMap = divisionsInfoMap;
		currentWriter.setSipFileHasher(sipFileHasher);
	}


}
