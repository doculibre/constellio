package com.constellio.app.modules.rm.exports;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.exports.RetentionRuleXMLExporterRuntimeException.RetentionRuleXMLExporterRuntimeException_InvalidFile;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.constellio.app.modules.rm.exports.RetentionRuleXMLExporter.forAllApprovedRulesInCollection;
import static org.assertj.core.api.Assertions.assertThat;

public class RetentionRuleXMLExporterAcceptanceTest extends ConstellioTest {

	File builtXML;

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records));

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
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		recordServices.execute(transaction);

		Collection collection = getAppLayerFactory().getCollectionsManager()
				.getCollection(zeCollection).setOrganizationNumber("00101010");

		recordServices.update(collection);
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
			sb.append(line.replace(" />", "/>").trim());
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
