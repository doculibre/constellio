package com.constellio.app.services.sip;

import java.util.List;
import java.util.Locale;

public class SIPBuilderParams {

	private List<String> providedBagInfoHeaderLines;

	private Locale locale;

	public List<String> getProvidedBagInfoHeaderLines() {
		return providedBagInfoHeaderLines;
	}

	public SIPBuilderParams setProvidedBagInfoHeaderLines(List<String> providedBagInfoHeaderLines) {
		this.providedBagInfoHeaderLines = providedBagInfoHeaderLines;
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
