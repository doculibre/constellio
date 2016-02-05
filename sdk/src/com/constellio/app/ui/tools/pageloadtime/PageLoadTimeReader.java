package com.constellio.app.ui.tools.pageloadtime;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class PageLoadTimeReader {
	
	public String readAsString(ConstellioWebDriver webDriver) {
		String pageLoadTimeStr = (String) webDriver.executeScript("return " + PageLoadTimeWriter.VARIABLE_NAME + ";");
//		try {
//			throw new Exception("PageLoadTimeReader.readAsString : " + pageLoadTimeStr);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return pageLoadTimeStr;
	}

}
