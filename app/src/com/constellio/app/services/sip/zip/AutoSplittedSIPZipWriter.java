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

public class AutoSplittedSIPZipWriter implements SIPZipWriter {

	private AppLayerFactory appLayerFactory;
	private IOServices ioServices;
	private SIPFileHasher sipFileHasher;
	private FileSIPZipWriter currentWriter;
	private Map<String, MetsDivisionInfo> divisionsInfoMap = new HashMap<>();
	private SIPZipBagInfoFactory bagInfoFactory;
	private long sipBytesLimit;

	private List<AutoSplittedSIPZipWriterListener> listeners = new ArrayList<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(AutoSplittedSIPZipWriter.class);

	/**
	 * Too much files in a SIP archive will create a large METS file. Since it is writen using DOM, it could require too much memory.
	 * This limit will create METS file of 250mb
	 */
	private long metsFilesEntriesLimit;
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

		if (appLayerFactory.getModelLayerFactory().getSystemConfigs().getMemoryConsumptionLevel().isPrioritizingMemoryConsumption()) {
			metsFilesEntriesLimit = 10000;
		} else {
			metsFilesEntriesLimit = 100000;
		}
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


	private void newCurrentWriter()
			throws IOException {
		int newWriterIndex = ++index;
		currentWriter = new FileSIPZipWriter(appLayerFactory,
				sipFileProvider.newSIPFile(newWriterIndex),
				sipFileProvider.newSIPName(newWriterIndex), bagInfoFactory);
		LOGGER.info("Creating zip '" + currentWriter.getZipFile().getAbsolutePath() + "'");
		currentWriter.divisionsInfoMap = divisionsInfoMap;
		currentWriter.setSipFileHasher(sipFileHasher);

		for (AutoSplittedSIPZipWriterListener listener : listeners) {
			listener.onSIPFileCreated(currentWriter.getSipZipInfos().getSipName(), currentWriter.getZipFile());
		}
	}

	public interface AutoSplittedSIPZipWriterListener {

		void onSIPFileCreated(String sipName, File file);

		void onSIPFileClosed(String sipName, File file, boolean lastFile);
	}

}
