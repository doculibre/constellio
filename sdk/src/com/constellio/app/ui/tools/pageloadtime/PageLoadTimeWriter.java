/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
