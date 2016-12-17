package com.constellio.app.services.schemas.bulkImport.data.xml;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIteratorRuntimeException;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIteratorTest;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.services.records.ContentImportVersion;

public class XMLFileImportDataIteratorAcceptanceTest extends ImportDataIteratorTest {

	IOServices ioServices;

	ImportDataIterator importDataIterator;

	@Before
	public void setUp()
			throws Exception {
		ioServices = spy(getIOLayerFactory().newIOServices());

	}

	@Test
	public void whenIteratingThen()
			throws Exception {

		LocalDateTime localDateTime = new LocalDateTime(2010, 4, 29, 9, 31, 46, 235);
		LocalDate localDate = new LocalDate(2010, 4, 29);
		LocalDate anOtherLocalDate = new LocalDate(2010, 5, 15);
		LocalDate aThirdLocalDate = new LocalDate(2010, 5, 16);

		importDataIterator = new XMLFileImportDataIterator(getTestResourceReader("data.xml"), ioServices);

		assertThat(importDataIterator.next()).has(id("1")).has(index(1)).has(schema("default"))
				.has(noField("id")).has(noField("schema"))
				.has(field("title", "Ze title"))
				.has(field("createdOn", localDateTime))
				.has(field("referenceToAnotherSchema", "42"));

		assertThat(importDataIterator.next()).has(id("42")).has(index(2)).has(schema("default"))
				.has(noField("id")).has(noField("schema"))
				.has(field("title", "Another title"))
				.has(field("referenceToAThirdSchema", "666"))
				.has(noField("zeEmptyField"));

		assertThat(importDataIterator.next()).has(id("666")).has(index(3)).has(schema("cust"
				+ ""
				+ ""
				+ ""
				+ ""
				+ ""
				+ "omSchema"))
				.has(noField("id")).has(noField("schema"))
				.has(field("createdOn", localDate))
				.has(field("modifyOn", asList(anOtherLocalDate, aThirdLocalDate)))
				.has(field("keywords", asList("keyword1", "keyword2")))
				.has(noField("zeNullField"))
				.has(field("title", "A third title"));

	}

	@Test
	public void whenReadXMLFileThenValidOptions()
			throws Exception {
		importDataIterator = new XMLFileImportDataIterator(getTestResourceReader("data.xml"), ioServices);
		assertThat(importDataIterator.getOptions().isImportAsLegacyId()).isTrue();
		importDataIterator.close();

		importDataIterator = new XMLFileImportDataIterator(getTestResourceReader("dataWithOptions1.xml"), ioServices);
		assertThat(importDataIterator.getOptions().isImportAsLegacyId()).isFalse();
		importDataIterator.close();

		importDataIterator = new XMLFileImportDataIterator(getTestResourceReader("dataWithOptions2.xml"), ioServices);
		assertThat(importDataIterator.getOptions().isImportAsLegacyId()).isTrue();
		importDataIterator.close();
	}

	@Test
	public void whenIteratingOnDataWithSubstructures()
			throws FileNotFoundException {
		importDataIterator = new XMLFileImportDataIterator(getTestResourceReader("calendrierConservationTest.xml"), ioServices);

		String firstDescription = "Documents produits ou reçus relatifs à la gestion des documents constitutifs. "
				+ "Les documents peuvent comprendre les certifications, les lettres patentes, la charte et les statuts.";
		String secondDescription =
				"Documents produits ou reçus relatifs à l'historique de l'Ordre et aux événements qui ont marqué le cours de son développement. "
						+ "Les documents peuvent comprendre les textes, les notes, les images fixes ou animées.";

		LocalDate localDate = new LocalDate(2015, 5, 1);

		Map<String, String> copyRetentionRuleOne = new HashMap<>();
		copyRetentionRuleOne.put("code", "123");
		copyRetentionRuleOne.put("copyType", "S");
		copyRetentionRuleOne.put("mediumTypes", "");
		copyRetentionRuleOne.put("contentTypesComment", "");
		copyRetentionRuleOne.put("activeRetentionPeriod", "999");
		copyRetentionRuleOne.put("activeRetentionPeriodComment", "");
		copyRetentionRuleOne.put("semiActiveRetentionPeriod", "0");
		copyRetentionRuleOne.put("semiActiveRetentionPeriodComment", "");
		copyRetentionRuleOne.put("inactiveDisposalType", "D");
		copyRetentionRuleOne.put("inactiveDisposalComment", "");

		Map<String, String> copyRetentionRuleTwo = new HashMap<>();
		copyRetentionRuleTwo.put("code", "123");
		copyRetentionRuleTwo.put("copyType", "P");
		copyRetentionRuleTwo.put("mediumTypes", "");
		copyRetentionRuleTwo.put("contentTypesComment", "");
		copyRetentionRuleTwo.put("activeRetentionPeriod", "999");
		copyRetentionRuleTwo.put("activeRetentionPeriodComment", "R1");
		copyRetentionRuleTwo.put("semiActiveRetentionPeriod", "0");
		copyRetentionRuleTwo.put("semiActiveRetentionPeriodComment", "");
		copyRetentionRuleTwo.put("inactiveDisposalType", "C");
		copyRetentionRuleTwo.put("inactiveDisposalComment", "");

		Map<String, String> documentTypeDetailOne = new HashMap<>();
		documentTypeDetailOne.put("archivisticStatus", "D");
		documentTypeDetailOne.put("code", "1234");

		assertThat(importDataIterator.next()).has(id("2")).has(index(1))
				.has(field("description", firstDescription))
				.has(field("approved", "true"))
				.has(field("approvalDate", localDate))
				.has(structure("documentTypeDetails", singletonList(documentTypeDetailOne)))
				.has(field("essentialDocuments", "true"))
				.has(field("confidentialDocuments", "false"))
				.has(structure("copyRetentionRules", asList(copyRetentionRuleOne, copyRetentionRuleTwo)))
				.has(field("administrativeUnits", singletonList("1")));

		assertThat(importDataIterator.next()).has(id("3")).has(index(2))
				.has(field("code", "111200"))
				.has(field("description", secondDescription))
				.has(field("approved", "true"))
				.has(structure("documentTypeDetails", singletonList(documentTypeDetailOne)))
				.has(field("essentialDocuments", "false"))
				.has(field("confidentialDocuments", "false"))
				.has(structure("copyRetentionRules", asList(copyRetentionRuleOne, copyRetentionRuleTwo)))
				.has(field("administrativeUnits", asList("1", "2")));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void givenContentThenOK()
			throws Exception {

		String url = "https://dl.dropboxusercontent.com/u/422508/pg338.txt";
		String url2 = "https://dl.dropboxusercontent.com/u/422508/pg339.txt";
		String fileName = "The Kings Return";

		importDataIterator = new XMLFileImportDataIterator(getTestResourceReader("content.xml"), ioServices);
		assertThat(importDataIterator.next()).has(id("1")).has(index(1)).has(contentSize(1))
				.has(content(new ContentImportVersion(url, "aName", true, null, null)));

		assertThat(importDataIterator.next()).has(id("2")).has(index(2)).has(contentSize(2))
				.has(content(new ContentImportVersion(url, fileName, true, null, null),
						new ContentImportVersion(url2, fileName, false, null, null)));

	}

	@Test(expected = ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate.class)
	public void givenWrongDateInfourthRecordThenExceptionExpected()
			throws Exception {

		importDataIterator = new XMLFileImportDataIterator(getTestResourceReader("data.xml"), ioServices);

		importDataIterator.next();
		importDataIterator.next();
		importDataIterator.next();

		//this one throw the exception :
		importDataIterator.next();

	}

	@Test
	public void whenClosingIteratorThenReaderClosed()
			throws Exception {

		Reader reader = getTestResourceReader("data.xml");
		importDataIterator = new XMLFileImportDataIterator(reader, ioServices);

		importDataIterator.next();
		importDataIterator.close();

		verify(ioServices).closeQuietly(reader);
	}
}
