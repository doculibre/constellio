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
package com.constellio.data.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;

public class LoggerUtils {

	public static void logDocument(Logger logger, String comment, Document document) {
		Format format = Format.getPrettyFormat();
		StringWriter stringWriter = new StringWriter();
		try {
			new XMLOutputter(format).output(document, stringWriter);
			logger.info(comment + " : \n" + stringWriter.getBuffer().toString());
			stringWriter.close();
		} catch (IOException e) {
			throw new ImpossibleRuntimeException(e);
		}

	}

	public static String toParamsString(SolrParams params) {
		StringBuilder stringBuilder = new StringBuilder();
		Iterator<String> itIterator = params.getParameterNamesIterator();
		boolean first = true;
		while (itIterator.hasNext()) {
			String key = itIterator.next();
			for (String value : params.getParams(key)) {
				if (!first) {
					stringBuilder.append(",  ");
				}
				stringBuilder.append(key);
				stringBuilder.append("=");
				stringBuilder.append(value);
				first = false;
			}
		}
		return stringBuilder.toString();
	}

	private void logExceptionWhileAdding(Logger logger, List<SolrInputDocument> inputDocuments, Exception e) {
		for (SolrInputDocument inputDocument : inputDocuments) {
			logger.info("ADD document '" + inputDocument.getFieldValue("id") + "' with version '" + inputDocument
					.getFieldValue("_version_") + "'");
		}
		logger.error("SolrServerException", e);
	}

}
