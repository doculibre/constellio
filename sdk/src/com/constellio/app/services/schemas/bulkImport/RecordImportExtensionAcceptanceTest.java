package com.constellio.app.services.schemas.bulkImport;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static com.constellio.sdk.tests.TestUtils.frenchMessages;
import static org.assertj.core.api.Assertions.assertThat;

public class RecordImportExtensionAcceptanceTest extends ConstellioTest {

	private Users users = new Users();
	private RMTestRecords records = new RMTestRecords(zeCollection);

	private BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();
	private RecordsImportServices importServices;
	private User admin;

	private LocalDateTime now = new LocalDateTime().minusHours(3);

	@Before
	public void setUp()
			throws Exception {
		givenHashingEncodingIs(BASE64_URL_ENCODED);
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
		);

		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);

		givenTimeIs(now);

		importServices = new RecordsImportServices(getModelLayerFactory());

		admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);
	}

	@Test
	public void whenImportActiveFolderWithTransferDispositionAndDestructionDateThenValidationError() throws ValidationException {
		importParameters();
		XMLImportDataProvider folder = toXMLFileWithFile661("folder.xml");
		try {
			importServices.bulkImport(folder, progressionListener, admin);
		} catch (ValidationException e) {
			assertThat((frenchMessages(e.getValidationErrors()))).containsOnly(
							"Dossier 661 : Le dossier «661» a une date de transfert alors que son statut archivistique est \"Active\".",
							"Dossier 661 : Le dossier «661» a une date de versement alors que son statut archivistique est \"Active\".",
							"Dossier 661 : Le dossier «661» a une date de destruction alors que son statut archivistique est \"Active\"."
			);
		}
	}

	@Test
	public void whenImportSemiActiveFolderWithDispositionAndDestructionDateThenValidationError() throws ValidationException {
		importParameters();
		XMLImportDataProvider folder = toXMLFileWithFile662("folder.xml");
		try {
			importServices.bulkImport(folder, progressionListener, admin);
		} catch (ValidationException e) {
			assertThat((frenchMessages(e.getValidationErrors()))).containsOnly(
							"Dossier 662 : Le dossier «662» a une date de versement alors que son statut archivistique est \"Semi-active\".",
							"Dossier 662 : Le dossier «662» a une date de destruction alors que son statut archivistique est \"Semi-active\"."
					);
		}
	}

	@Test
	public void whenImportSemiActiveFolderWithoutTransferDateThenValidationError() throws ValidationException {
		importParameters();
		XMLImportDataProvider folder = toXMLFileWithFile663("folder.xml");
		try {
			importServices.bulkImport(folder, progressionListener, admin);
		} catch (ValidationException e) {
			assertThat((frenchMessages(e.getValidationErrors()))).containsOnly(
							"Dossier 663 : Le dossier «663» n'a pas de date de transfert alors que son statut archivistique est \"Semi-active\"."
					);
		}
	}

	@Test
	public void whenImportDepositedFolderWithDestructionDateThenValidationError() throws ValidationException {
		importParameters();
		XMLImportDataProvider folder = toXMLFileWithFile664("folder.xml");
		try {
			importServices.bulkImport(folder, progressionListener, admin);
		} catch (ValidationException e) {
			System.out.println("ici");
			assertThat((frenchMessages(e.getValidationErrors()))).containsOnly(
					"Dossier 664 : Le dossier «664» a une date de destruction alors que son statut archivistique est \"Versé\"."
			);
		}
	}

	@Test
	public void whenImportDepositedFolderWithoutDepositDateThenValidationError() throws ValidationException {
		importParameters();
		XMLImportDataProvider folder = toXMLFileWithFile665("folder.xml");
		try {
			importServices.bulkImport(folder, progressionListener, admin);
		} catch (ValidationException e) {
			assertThat((frenchMessages(e.getValidationErrors()))).containsOnly(
					"Dossier 665 : Le dossier «665» n'a pas de date de versement alors que son statut archivistique est \"Versé\"."
			);
		}
	}

	@Test
	public void whenImportDestroyedFolderWithDepositDateThenValidationError() throws ValidationException {
		importParameters();
		XMLImportDataProvider folder = toXMLFileWithFile666("folder.xml");
		try {
			importServices.bulkImport(folder, progressionListener, admin);
		} catch (ValidationException e) {
			assertThat((frenchMessages(e.getValidationErrors()))).containsOnly(
					"Dossier 666 : Le dossier «666» a une date de versement alors que son statut archivistique est \"Détruit\"."
			);
		}
	}

	private void importParameters() throws ValidationException {
		XMLImportDataProvider administrativeUnit = toXMLFile("administrativeUnit.xml");
		XMLImportDataProvider category = toXMLFile("category.xml");
		XMLImportDataProvider retentionRule = toXMLFile("retentionRule.xml");
		XMLImportDataProvider[] files = new XMLImportDataProvider[] {category, administrativeUnit, retentionRule};
		for (ImportDataProvider importDataProvider : files) {
			importServices.bulkImport(importDataProvider, progressionListener, admin);
		}
	}

	private XMLImportDataProvider toXMLFileWithFile661(String name) {
		return toXMLFileWithoutTextBetween(name, 21,29);
	}

	private XMLImportDataProvider toXMLFileWithFile662(String name) {
		return toXMLFileWithoutTextBetween(name, 30,38);
	}

	private XMLImportDataProvider toXMLFileWithFile663(String name) {
		return toXMLFileWithoutTextBetween(name, 39,44);
	}

	private XMLImportDataProvider toXMLFileWithFile664(String name) {
		return toXMLFileWithoutTextBetween(name, 45,52);
	}

	private XMLImportDataProvider toXMLFileWithFile665(String name) {
		return toXMLFileWithoutTextBetween(name, 53,58);
	}

	private XMLImportDataProvider toXMLFileWithFile666(String name) {
		return toXMLFileWithoutTextBetween(name, 59,66);
	}

	private XMLImportDataProvider toXMLFileWithoutTextBetween(String name, int start, int end){
		File resourceFile = getTestResourceFile(name);
		File tempFile = new File(newTempFolder(), name);
		try {
			String strLine;
			FileWriter fileWriter = new FileWriter(tempFile,true);
			BufferedReader bufferedReader = new BufferedReader(new FileReader(resourceFile));
			int line = 1;
			while ((strLine = bufferedReader.readLine()) != null){
				if(line>=21 && line<=66){
					if(line >= start && line <= end){
						fileWriter.append(strLine);
						fileWriter.append("\n");
					}
				}else{
					fileWriter.append(strLine);
					fileWriter.append("\n");
				}
				line ++;
			}
			fileWriter.close();
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return XMLImportDataProvider.forSingleXMLFile(getModelLayerFactory(), tempFile);
	}

	private XMLImportDataProvider toXMLFile(String name) {
		File resourceFile = getTestResourceFile(name);
		File tempFile = new File(newTempFolder(), name);
		try {
			FileUtils.copyFile(resourceFile, tempFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return XMLImportDataProvider.forSingleXMLFile(getModelLayerFactory(), tempFile);
	}
}
