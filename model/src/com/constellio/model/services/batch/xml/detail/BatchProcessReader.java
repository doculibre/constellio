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
package com.constellio.model.services.batch.xml.detail;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

public class BatchProcessReader {

	private static final String ERRORS = "errors";
	private final Document document;

	public BatchProcessReader(Document document) {
		this.document = document;
	}

	public List<String> getRecordsWithError() {
		List<String> recordsWithError = new ArrayList<>();
		Element batchProcessElement = document.getRootElement();
		List<Element> recordElements = batchProcessElement.getChild(ERRORS).getChildren();
		for (Element recordElement : recordElements) {
			recordsWithError.add(recordElement.getText());
		}
		return recordsWithError;
	}
}
