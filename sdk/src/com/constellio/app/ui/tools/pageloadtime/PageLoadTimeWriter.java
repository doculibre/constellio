package com.constellio.app.ui.tools.pageloadtime;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.vaadin.ui.JavaScript;

public class PageLoadTimeWriter {

	public static final String VARIABLE_NAME = "constellio_pageLoadTime";
	public static final String DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss_SSS";
	
	public void write(Date loadTime) {
		String pageLoadTimeStr = new SimpleDateFormat(DATE_FORMAT).format(loadTime);
		String javascript = VARIABLE_NAME + " = \"" + pageLoadTimeStr + "\"";
		JavaScript.getCurrent().execute(javascript);
//		try {
//			throw new Exception("PageLoadTimeWriter.write : " + javascript);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

}
