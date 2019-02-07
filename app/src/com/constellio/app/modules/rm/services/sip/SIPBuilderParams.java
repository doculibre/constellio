package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.services.sip.zip.SIPFileHasher;

import java.util.List;
import java.util.Locale;

public class SIPBuilderParams {

	private List<String> providedBagInfoHeaderLines;

	private long sipBytesLimit;

	private int sipFilesLimit;

	private Locale locale;

	private SIPFileHasher sipFileHasher = new SIPFileHasher();

	public List<String> getProvidedBagInfoHeaderLines() {
		return providedBagInfoHeaderLines;
	}

	public SIPBuilderParams setProvidedBagInfoHeaderLines(List<String> providedBagInfoHeaderLines) {
		this.providedBagInfoHeaderLines = providedBagInfoHeaderLines;
		return this;
	}

	public SIPFileHasher getSipFileHasher() {
		return sipFileHasher;
	}

	public SIPBuilderParams setSipFileHasher(SIPFileHasher sipFileHasher) {
		this.sipFileHasher = sipFileHasher;
		return this;
	}

	public long getSipBytesLimit() {
		return sipBytesLimit;
	}

	public SIPBuilderParams setSipBytesLimit(long sipBytesLimit) {
		this.sipBytesLimit = sipBytesLimit;
		return this;
	}

	public int getSipFilesLimit() {
		return sipFilesLimit;
	}

	public SIPBuilderParams setSipFilesLimit(int sipFilesLimit) {
		this.sipFilesLimit = sipFilesLimit;
		return this;
	}

	public Locale getLocale() {
		return locale;
	}

	public SIPBuilderParams setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}
}
