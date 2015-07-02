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
package com.constellio.app.services.schemas.bulkImport;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportSchemaTypesDataProvider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaTypeImportServicesAcceptanceTest extends ConstellioTest {
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);

	BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();
	SchemaTypeImportServices importServices;
	SearchServices searchServices;
	UserServices userServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);

		progressionListener = new LoggerBulkImportProgressionListener();
		importServices = new SchemaTypeImportServices(getAppLayerFactory(), getModelLayerFactory(), zeCollection, 100);
		searchServices = getModelLayerFactory().newSearchServices();
		userServices = getModelLayerFactory().newUserServices();

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
	}

	@Test
	public void whenImportingDDVXMLFileThenImportedCorrectly()
			throws Exception {
		File usersFile = getTestResourceFile("valueDomain.xml");
		importDDVAndValidate(XMLImportSchemaTypesDataProvider.forSingleXMLFile(getModelLayerFactory(), usersFile, usersFile.getName()));
	}

	@Test
	public void whenImportingDocumentXMLFileThenImportedCorrectly()
			throws Exception {
		File usersFile = getTestResourceFile("documentSchemas.xml");
		importDocumentAndValidate(XMLImportSchemaTypesDataProvider.forSingleXMLFile(getModelLayerFactory(), usersFile, usersFile.getName()));
	}

	@Test
	public void whenImportingDocumentWithMetadataReferencesThenImportedCorrectly()
			throws Exception {
		File ddvFile = getTestResourceFile("valueDomain.xml");
		importDDV(ddvFile);
		File documentFile = getTestResourceFile("documentSchemasWithreferences.xml");
		importDocumentWithRefAndValidate(XMLImportSchemaTypesDataProvider.forSingleXMLFile(getModelLayerFactory(), documentFile, documentFile.getName()));
	}

	private void importDocumentWithRefAndValidate(XMLImportDataProvider importDataProvider) {
		List<String> collections = new ArrayList<>();
		collections.add(zeCollection);
		BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
		for (ImportError importError : results.getImportErrors()) {
			System.out.println(importError.getCompleteErrorMessage());
		}
		assertThat(results.getImportErrors().size()).isEqualTo(0);
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchemaType schemaType = types.getSchemaType("document");
		assertThat(schemaType).isNotNull();

		validateRefSchemaImport(schemaType);
	}

	private void validateRefSchemaImport(MetadataSchemaType schemaType) {
		MetadataList metadataList = schemaType.getSchema("ref").getMetadatas();
		ListIterator<Metadata> it = metadataList.listIterator();
		List<String> metadataLocalCodes = new ArrayList<>();
		while (it.hasNext()) {
			Metadata metadata = it.next();
			if (metadata.getLocalCode().equals("USRmdRefFolder")) {
				assertThat(metadata.getType()).isEqualTo(MetadataValueType.REFERENCE);
				assertThat(metadata.getLabel()).isEqualTo("ref folder");
				assertThat(metadata.isDefaultRequirement()).isTrue();
				assertThat(metadata.isEnabled()).isTrue();
				assertThat(metadata.isTaxonomyRelationship()).isFalse();
				assertThat(metadata.getAllowedReferences().getAllowedSchemaType()).isEqualTo("folder");
			} else if (metadata.getLocalCode().equals("USRmdRefTaxo")) {
				assertThat(metadata.isTaxonomyRelationship()).isTrue();
				assertThat(metadata.getAllowedReferences().getAllowedSchemaType()).isEqualTo("taxoDomaineHierarchiqueType");
			}
			metadataLocalCodes.add((metadata.getLocalCode()));
		}
		assertThat(metadataLocalCodes).contains("USRmdRefFolder");
		assertThat(metadataLocalCodes).contains("USRmdRefTaxo");
	}

	private void importDDV(File ddvFile) {
		XMLImportDataProvider importDataProvider = XMLImportSchemaTypesDataProvider.forSingleXMLFile(getModelLayerFactory(), ddvFile, ddvFile.getName());
		List<String> collections = new ArrayList<>();
		collections.add(zeCollection);
		BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
		for (ImportError importError : results.getImportErrors()) {
			System.out.println(importError.getCompleteErrorMessage());
		}
		assertThat(results.getImportErrors()).hasSize(0);
	}

	private void importDocumentAndValidate(ImportDataProvider importDataProvider) {
		List<String> collections = new ArrayList<>();
		collections.add(zeCollection);
		BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
		for (ImportError importError : results.getImportErrors()) {
			System.out.println(importError.getCompleteErrorMessage());
		}
		assertThat(results.getImportErrors().size()).isEqualTo(0);
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchemaType schemaType = types.getSchemaType("document");
		assertThat(schemaType).isNotNull();

		validateDefaultSchemaImport(schemaType);
		validatePapierSchemaImport(schemaType);
	}

	private void validatePapierSchemaImport(MetadataSchemaType schemaType) {
		MetadataList metadataList = schemaType.getSchema("papier").getMetadatas();
		ListIterator<Metadata> it = metadataList.listIterator();
		List<String> metadataLocalCodes = new ArrayList<>();
		while (it.hasNext()) {
			Metadata metadata = it.next();
			if (metadata.getLocalCode().equals("USRcat1")) {
				//<element code="regex" label="Regex" required="true" type="string"/>
				assertThat(metadata.getType()).isEqualTo(MetadataValueType.STRING);
				assertThat(metadata.getLabel()).isEqualTo("cat 1");
				assertThat(metadata.isDefaultRequirement()).isFalse();
				assertThat(metadata.isEnabled()).isFalse();
			} else if (metadata.getLocalCode().equals("USRmd2Papier")) {
				assertThat(metadata.getType()).isEqualTo(MetadataValueType.STRING);
				assertThat(metadata.getLabel()).isEqualTo("md2 papier");
				assertThat(metadata.isDefaultRequirement()).isTrue();
			}
			metadataLocalCodes.add((metadata.getLocalCode()));
		}
		assertThat(metadataLocalCodes).contains("USRcat1");
		assertThat(metadataLocalCodes).contains("USRmd2Papier");
	}

	private void validateDefaultSchemaImport(MetadataSchemaType schemaType) {
		MetadataList metadataList = schemaType.getDefaultSchema().getMetadatas();
		ListIterator<Metadata> it = metadataList.listIterator();
		List<String> metadataLocalCodes = new ArrayList<>();
		while (it.hasNext()) {
			Metadata metadata = it.next();
			if (metadata.getLocalCode().equals("USRcat1")) {
				//<element code="regex" label="Regex" required="true" type="string"/>
				assertThat(metadata.getType()).isEqualTo(MetadataValueType.STRING);
				assertThat(metadata.getLabel()).isEqualTo("cat 1");
				assertThat(metadata.isDefaultRequirement()).isFalse();
				assertThat(metadata.isEnabled()).isTrue();
			}
			metadataLocalCodes.add((metadata.getLocalCode()));
		}
		assertThat(metadataLocalCodes).contains("USRcat1");

	}

	private void importDDVAndValidate(ImportDataProvider importDataProvider) {
		List<String> collections = new ArrayList<>();
		collections.add(zeCollection);
		BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
		for (ImportError importError : results.getImportErrors()) {
			System.out.println(importError.getCompleteErrorMessage());
		}
		assertThat(results.getImportErrors()).hasSize(0);
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		validateDDVImport(types);
		validateTaxonomyImport(types);
		validateDDVWithMetadata(types);
	}

	private void validateDDVWithMetadata(MetadataSchemaTypes types) {
		MetadataSchemaType schemaType = types.getSchemaType("ddvMasqueSaisieLocalisation");
		assertThat(schemaType).isNotNull();
		assertThat(schemaType.getLabel()).isEqualTo("Masque saisie");
		MetadataList metadataList = schemaType.getAllMetadatas();
		ListIterator<Metadata> it = metadataList.listIterator();
		List<String> metadataLocalCodes = new ArrayList<>();
		while (it.hasNext()) {
			Metadata metadata = it.next();
			if (metadata.getLocalCode().equals("USRregex")) {
				//<element code="regex" label="Regex" required="true" type="string"/>
				assertThat(metadata.getType()).isEqualTo(MetadataValueType.STRING);
				assertThat(metadata.getLabel()).isEqualTo("Regex");
				assertThat(metadata.isDefaultRequirement()).isTrue();
			}
			metadataLocalCodes.add((metadata.getLocalCode()));
		}
		assertThat(metadataLocalCodes).containsAll(Arrays.asList(new String[] { "USRregex" }));
	}

	private void validateDDVImport(MetadataSchemaTypes types) {
		MetadataSchemaType schemaType = types.getSchemaType("ddvCategoriesOrganisations");
		assertThat(schemaType).isNotNull();
		assertThat(schemaType.getLabel()).isEqualTo("Catégories d'organisations");
	}

	private void validateTaxonomyImport(MetadataSchemaTypes types) {
		MetadataSchemaType schemaType = types.getSchemaType("taxoDomaineHierarchiqueType");
		assertThat(schemaType).isNotNull();
		assertThat(schemaType.getLabel()).isEqualTo("Nouvelle taxo");
		MetadataList metadataList = schemaType.getAllMetadatas();
		List<String> taxoDomaineHierarchiqueTypeLocalCodes = new ArrayList<>();
		ListIterator<Metadata> it = metadataList.listIterator();
		while (it.hasNext()) {
			taxoDomaineHierarchiqueTypeLocalCodes.add((it.next().getLocalCode()));
		}
		assertThat(taxoDomaineHierarchiqueTypeLocalCodes).containsAll(Arrays.asList(new String[] { "USRmd1Taxo", "USRmd2Taxo" }));
		Taxonomy taxonomy = getModelLayerFactory().getTaxonomiesManager()
				.getTaxonomyFor(zeCollection, "taxoDomaineHierarchiqueType");
		assertThat(taxonomy).isNotNull();
		assertThat(taxonomy.getSchemaTypes()).containsOnly(schemaType.getCode());
	}
}
