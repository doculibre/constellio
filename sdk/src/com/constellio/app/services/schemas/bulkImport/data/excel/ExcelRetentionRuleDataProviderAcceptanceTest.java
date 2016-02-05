package com.constellio.app.services.schemas.bulkImport.data.excel;

import static com.constellio.sdk.tests.TestUtils.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIteratorTest;
import com.constellio.data.io.services.facades.IOServices;

public class ExcelRetentionRuleDataProviderAcceptanceTest extends ImportDataIteratorTest {

	IOServices ioServices;

	ImportDataIterator importDataIterator;

	@Before
	public void setUp()
			throws Exception {
		ioServices = spy(getIOLayerFactory().newIOServices());

	}

	@Test
	public void whenIteratingOnDataWithSubstructures()
			throws FileNotFoundException {
		importDataIterator = Excel2007ImportDataProvider.fromFile(getTestResourceFile("retentionRule.xlsx")).newDataIterator("retentionRule");

		String firstDescription = "Documents produits ou reçus relatifs à la gestion des documents constitutifs. Les documents peuvent";
		String secondDescription = "Documents produits ou reçus relatifs à l'historique de l'Ordre et aux événements qui ont marqué le cours de son développement. Les documents peuvent comprendre les textes, les notes, les images fixes ou animées.  comprendre les certifications, les lettres patentes, la charte et les statuts.";

		LocalDate localDate = new LocalDate(2015, 5, 1);

		Map<String, String> copyRetentionRuleOne = new HashMap<>();
		copyRetentionRuleOne.put("code", "123");
		copyRetentionRuleOne.put("copyType", "S");
		copyRetentionRuleOne.put("mediumTypes", null);
		copyRetentionRuleOne.put("contentTypesComment", null);
		copyRetentionRuleOne.put("activeRetentionPeriod", "999");
		copyRetentionRuleOne.put("activeRetentionPeriodComment", null);
		copyRetentionRuleOne.put("semiActiveRetentionPeriod", "0");
		copyRetentionRuleOne.put("semiActiveRetentionPeriodComment", null);
		copyRetentionRuleOne.put("inactiveDisposalType", "D");
		copyRetentionRuleOne.put("inactiveDisposalComment", null);

		Map<String, String> copyRetentionRuleTwo = new HashMap<>();
		copyRetentionRuleTwo.put("code", "123");
		copyRetentionRuleTwo.put("copyType", "P");
		copyRetentionRuleTwo.put("mediumTypes", null);
		copyRetentionRuleTwo.put("contentTypesComment", null);
		copyRetentionRuleTwo.put("activeRetentionPeriod", "999");
		copyRetentionRuleTwo.put("activeRetentionPeriodComment", "R1");
		copyRetentionRuleTwo.put("semiActiveRetentionPeriod", "0");
		copyRetentionRuleTwo.put("semiActiveRetentionPeriodComment", null);
		copyRetentionRuleTwo.put("inactiveDisposalType", "C");
		copyRetentionRuleTwo.put("inactiveDisposalComment", null);

		Map<String, String> documentTypeDetailOne = new HashMap<>();
		documentTypeDetailOne.put("archivisticStatus", "D");
		documentTypeDetailOne.put("code", "1234");

		assertThat(importDataIterator.next()).has(id("1")).has(index(2))
				.has(field("description", firstDescription))
				.has(field("approved", "true"))
				.has(field("approvalDate", localDate))
				.has(structure("documentTypesDetails", singletonList(documentTypeDetailOne)))
				.has(field("essentialDocuments", "true"))
				.has(field("confidentialDocuments", "false"))
				.has(structure("copyRetentionRules", asList(copyRetentionRuleOne, copyRetentionRuleTwo)))
				.has(field("administrativeUnits", singletonList("1")));

		assertThat(importDataIterator.next()).has(id("2")).has(index(3))
				.has(field("code", "111200"))
				.has(field("description", secondDescription))
				.has(field("approved", "true"))
				.has(structure("documentTypesDetails", singletonList(documentTypeDetailOne)))
				.has(field("essentialDocuments", "false"))
				.has(field("confidentialDocuments", "true"))
				.has(structure("copyRetentionRules", asList(copyRetentionRuleOne, copyRetentionRuleTwo)))
				.has(field("administrativeUnits", asList("1", "2")));

	}
}
