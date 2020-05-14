package com.constellio.app.services.sip.zip;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.bagInfo.SIPZipBagInfoFactory;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.data.io.services.facades.IOServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

public class AutoSplittedSIPZipWriter implements SIPZipWriter {

	protected AppLayerFactory appLayerFactory;
	protected IOServices ioServices;
	protected SIPFileHasher sipFileHasher;
	protected FileSIPZipWriter currentWriter;
	protected Map<String, MetsDivisionInfo> divisionsInfoMap = new HashMap<>();
	protected SIPZipBagInfoFactory bagInfoFactory;
	protected long sipBytesLimit;
	protected int compressionLevel = Deflater.DEFAULT_COMPRESSION;

	protected List<AutoSplittedSIPZipWriterListener> listeners = new ArrayList<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(AutoSplittedSIPZipWriter.class);

	/**
	 * Too much files in a SIP archive will create a large METS file. Since it is writen using DOM, it could require too much memory.
	 * This limit will create METS file of 250mb
	 */
	protected long metsFilesEntriesLimit;
	protected int index = 0;
	protected SIPFileNameProvider sipFileProvider;

	public AutoSplittedSIPZipWriter(AppLayerFactory appLayerFactory, SIPFileNameProvider sipFileProvider,
									long sipBytesLimit, SIPZipBagInfoFactory bagInfoFactory) {
		this.appLayerFactory = appLayerFactory;
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.sipFileHasher = new SIPFileHasher();
		this.sipFileProvider = sipFileProvider;
		this.sipBytesLimit = sipBytesLimit;
		this.bagInfoFactory = bagInfoFactory;

		//Librairy au.edu.apsr.mtk.base.FileGrp does not scale well writing large mets file (to much search operations in the document while adding)
		metsFilesEntriesLimit = 10000;

	}

	public AutoSplittedSIPZipWriter setCompressionLevel(int compressionLevel) {
		this.compressionLevel = compressionLevel;
		return this;
	}

	public void register(AutoSplittedSIPZipWriterListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public IOServices getIoServices() {
		return ioServices;
	}

	@Override
	public void close() {
		if (currentWriter != null) {
			closeCurrentWriter(true);
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
	public void insertAll(SIPZipWriterTransaction transaction)
			throws IOException {

		if (currentWriter == null) {
			newCurrentWriter();
		}
		long currentWriterLength = currentWriter.length();
		int currentWriterMetsEntries = currentWriter.metsFileEntriesCount();
		long transactionLength = transaction.length();
		int transactionMetsEntries = transaction.filesCount();

		if ((currentWriterLength > 0 && currentWriterLength + transactionLength > sipBytesLimit) ||
			(currentWriterMetsEntries > 0 && currentWriterMetsEntries + transactionMetsEntries > metsFilesEntriesLimit)) {
			LOGGER.info("Closing zip '" + currentWriter.getZipFile().getAbsolutePath() + "'");
			closeCurrentWriter(false);
			newCurrentWriter();
		}
		currentWriter.insertAll(transaction);

	}

	protected void closeCurrentWriter(boolean lastFile) {
		currentWriter.close();
		for (AutoSplittedSIPZipWriterListener listener : listeners) {
			listener.onSIPFileClosed(currentWriter.getSipZipInfos().getSipName(), currentWriter.getZipFile(), lastFile);
		}
		currentWriter = null;
	}

	@Override
	public void addToZip(File file, String path)
			throws IOException {
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
	public int metsFileEntriesCount() {
		return currentWriter == null ? 0 : currentWriter.metsFileEntriesCount();
	}

	@Override
	public void addDivisionsInfoMap(Map<String, MetsDivisionInfo> divisionInfoMap) {
		this.divisionsInfoMap.putAll(divisionInfoMap);
	}

	@Override
	public void addDivisionInfo(MetsDivisionInfo divisionInfo) {
		this.divisionsInfoMap.put(divisionInfo.getId(), divisionInfo);
	}

	@Override
	public void setSipFileHasher(SIPFileHasher hasher) {
		this.sipFileHasher = hasher;
		if (currentWriter != null) {
			currentWriter.setSipFileHasher(hasher);
		}
	}

	protected FileSIPZipWriter newFileSIPZipWriter(int newWriterIndex)
			throws IOException {

		return new FileSIPZipWriter(appLayerFactory,
				sipFileProvider.newSIPFile(newWriterIndex),
				sipFileProvider.newSIPName(newWriterIndex), bagInfoFactory);
	}

	private void newCurrentWriter()
			throws IOException {
		int newWriterIndex = ++index;
		currentWriter = newFileSIPZipWriter(newWriterIndex);
		LOGGER.info("Creating zip '" + currentWriter.getZipFile().getAbsolutePath() + "'");
		currentWriter.divisionsInfoMap = divisionsInfoMap;
		currentWriter.setSipFileHasher(sipFileHasher);
		currentWriter.setCompressionLevel(compressionLevel);
		for (AutoSplittedSIPZipWriterListener listener : listeners) {
			listener.onSIPFileCreated(currentWriter.getSipZipInfos().getSipName(), currentWriter.getZipFile());
		}
	}

	public interface AutoSplittedSIPZipWriterListener {

		void onSIPFileCreated(String sipName, File file);

		void onSIPFileClosed(String sipName, File file, boolean lastFile);
	}

}
