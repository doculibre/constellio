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
package com.constellio.app.modules.rm.exports;

import static com.constellio.app.modules.rm.exports.RetentionRuleXMLExporter.forAllApprovedRulesInCollection;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.exports.RetentionRuleXMLExporterRuntimeException.RetentionRuleXMLExporterRuntimeException_InvalidFile;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.Transaction;
import com.constellio.sdk.tests.ConstellioTest;

public class RetentionRuleXMLExporterAcceptanceTest extends ConstellioTest {

	File builtXML;

	RMTestRecords records;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule();
		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());

		builtXML = new File(newTempFolder(), "test.xml");

		Transaction transaction = new Transaction();
		transaction.add(records.getCategory_X100().setTitle("Ze X100"));
		transaction.add(records.getCategory_X110().setTitle("Ze X110"));
		transaction.add(records.getUnit10().setTitle("Ze 10"));
		transaction.add(records.getUnit20().setTitle("Ze 20"));

		RetentionRule rule1 = records.getRule1();
		rule1.getCopyRetentionRules().get(0).setCode("42");
		rule1.getCopyRetentionRules().get(1).setCode("666");
		rule1.setEssentialDocuments(true);
		rule1.setConfidentialDocuments(true);

		transaction.add(rule1);
		getModelLayerFactory().newRecordServices().execute(transaction);
	}

	@Test
	public void whenExportingRetentionRulesThenValidXMLIsProduced()
			throws Exception {

		RetentionRuleXMLExporter exporter = forAllApprovedRulesInCollection(zeCollection, builtXML, getModelLayerFactory());
		exporter.run();

		assertThat(readWithoutIndent(builtXML)).isEqualTo(readWithoutIndent(getTestResourceFile("expected.xml")));

	}

	private String readWithoutIndent(File file)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String line : FileUtils.readLines(file)) {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(line.trim());
		}
		return sb.toString();
	}

	@Test
	public void whenValidatingValidFileThenOK() {

		RetentionRuleXMLExporter.validate(getTestResourceFile("expected.xml"));

	}

	@Test(expected = RetentionRuleXMLExporterRuntimeException_InvalidFile.class)
	public void whenValidatingInvalidFileThenException() {

		RetentionRuleXMLExporter.validate(getTestResourceFile("invalid.xml"));

	}

}
