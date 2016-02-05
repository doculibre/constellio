package com.constellio.model.services.taxonomies;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.DocumentSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.FolderSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1FirstSchemaType;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1SecondSchemaType;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy2CustomSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy2DefaultSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.TaxonomyRecords;

public class TaxonomiesPathsAcceptanceTest extends ConstellioTest {

	TaxonomiesManager taxonomiesManager;

	TwoTaxonomiesContainingFolderAndDocumentsSetup schemas =
			new TwoTaxonomiesContainingFolderAndDocumentsSetup(zeCollection);
	Taxonomy1FirstSchemaType taxonomy1FirstSchema = schemas.new Taxonomy1FirstSchemaType();
	Taxonomy1SecondSchemaType taxonomy1SecondSchema = schemas.new Taxonomy1SecondSchemaType();
	Taxonomy2DefaultSchema taxonomy2DefaultSchema = schemas.new Taxonomy2DefaultSchema();
	Taxonomy2CustomSchema taxonomy2CustomSchema = schemas.new Taxonomy2CustomSchema();
	FolderSchema folderSchema = schemas.new FolderSchema();
	DocumentSchema documentSchema = schemas.new DocumentSchema();

	TaxonomyRecords records;

	MetadataSchemasManager schemasManager;
	RecordServices recordServices;

	private static String taxo1Path(Record... records) {
		String collection = "";
		StringBuilder sb = new StringBuilder(collection + "/taxo1");
		for (Record record : records) {
			sb.append("/");
			sb.append(record.getId());
		}
		return sb.toString();
	}

	private static String taxo2Path(Record... records) {
		String collection = "";
		StringBuilder sb = new StringBuilder(collection + "/taxo2");
		for (Record record : records) {
			sb.append("/");
			sb.append(record.getId());
		}
		return sb.toString();
	}

	@Before
	public void setUp()
			throws Exception {
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		defineSchemasManager().using(schemas);
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		for (Taxonomy taxonomy : schemas.getTaxonomies()) {
			taxonomiesManager.addTaxonomy(taxonomy, schemasManager);
		}

		records = schemas.givenTaxonomyRecords(recordServices);

	}

	@Test
	public void whenAddingRootTaxonomyItemThenHasCorrectPath()
			throws Exception {

		Record record = recordServices.newRecordWithSchema(taxonomy1FirstSchema.instance());
		recordServices.add(record);
		assertThatPathIsEqualTo(record, taxo1Path(record));
	}

	@Test
	public void whenAddingNodeTaxonomyItemThenHasCorrectPath()
			throws Exception {

		Record record = recordServices.newRecordWithSchema(taxonomy1FirstSchema.instance());
		record.set(taxonomy1FirstSchema.parent(), records.taxo1_firstTypeItem2);
		recordServices.add(record);
		assertThatPathIsEqualTo(record, taxo1Path(records.taxo1_firstTypeItem2, record));
	}

	@Test
	public void whenAddingNodeTaxonomyItemOfSecondTypeThenHasCorrectPath()
			throws Exception {

		Record record = recordServices.newRecordWithSchema(taxonomy1SecondSchema.instance());
		record.set(taxonomy1SecondSchema.parentOfType2(), records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2);
		recordServices.add(record);
		assertThatPathIsEqualTo(record, taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_firstTypeItem2,
				records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2, record));
	}

	@Test
	public void givenTransactionOfMultipleRecordsAddedChildBeforeParentsWhenExecutingHaveCorrectPath()
			throws Exception {

		Record firstTypeRecord = recordServices.newRecordWithSchema(taxonomy1FirstSchema.instance());
		Record firstTypeLevel2Record = recordServices.newRecordWithSchema(taxonomy1FirstSchema.instance());
		Record secondTypeRecord = recordServices.newRecordWithSchema(taxonomy1SecondSchema.instance());
		firstTypeLevel2Record.set(taxonomy1FirstSchema.parent(), firstTypeRecord);
		secondTypeRecord.set(taxonomy1SecondSchema.parentOfType1(), firstTypeLevel2Record);

		Transaction transaction = new Transaction();
		transaction.addUpdate(secondTypeRecord);
		transaction.addUpdate(firstTypeLevel2Record);
		transaction.addUpdate(firstTypeRecord);
		recordServices.execute(transaction);

		assertThatPathIsEqualTo(firstTypeRecord, taxo1Path(firstTypeRecord));
		assertThatPathIsEqualTo(firstTypeLevel2Record, taxo1Path(firstTypeRecord, firstTypeLevel2Record));
		assertThatPathIsEqualTo(secondTypeRecord, taxo1Path(firstTypeRecord, firstTypeLevel2Record, secondTypeRecord));
	}

	@Test
	public void givenMultipleRecordsOfDifferentLevelsWithTaxonomyItemInTheirPathWhenTheTaxonomyItemIsMovedThenAllPathModified()
			throws Exception {

		Record firstTypeRecord = recordServices.newRecordWithSchema(taxonomy1FirstSchema.instance());
		Record firstTypeLvl2Record = recordServices.newRecordWithSchema(taxonomy1FirstSchema.instance());
		Record secondTypeRecord = recordServices.newRecordWithSchema(taxonomy1SecondSchema.instance());
		Record folder = recordServices.newRecordWithSchema(folderSchema.instance());
		firstTypeLvl2Record.set(taxonomy1FirstSchema.parent(), firstTypeRecord);
		secondTypeRecord.set(taxonomy1SecondSchema.parentOfType1(), firstTypeLvl2Record);
		folder.set(folderSchema.taxonomy1(), secondTypeRecord);
		recordServices.execute(new Transaction(Arrays.asList(firstTypeRecord, firstTypeLvl2Record, secondTypeRecord, folder)));
		assertThatPathIsEqualTo(folder, taxo1Path(firstTypeRecord, firstTypeLvl2Record, secondTypeRecord, folder));

		firstTypeRecord.set(taxonomy1FirstSchema.parent(), records.taxo1_firstTypeItem2);
		recordServices.update(firstTypeRecord);
		recordServices.refresh(firstTypeRecord, firstTypeLvl2Record, secondTypeRecord, folder);

		assertThatPathIsEqualTo(firstTypeLvl2Record,
				taxo1Path(records.taxo1_firstTypeItem2, firstTypeRecord, firstTypeLvl2Record));
		assertThatPathIsEqualTo(secondTypeRecord,
				taxo1Path(records.taxo1_firstTypeItem2, firstTypeRecord, firstTypeLvl2Record, secondTypeRecord));
		assertThatPathIsEqualTo(folder,
				taxo1Path(records.taxo1_firstTypeItem2, firstTypeRecord, firstTypeLvl2Record, secondTypeRecord, folder));

		secondTypeRecord.set(taxonomy1SecondSchema.parentOfType1(), records.taxo1_firstTypeItem2);
		recordServices.update(secondTypeRecord);
		recordServices.refresh(folder);
		assertThatPathIsEqualTo(secondTypeRecord, taxo1Path(records.taxo1_firstTypeItem2, secondTypeRecord));
		assertThatPathIsEqualTo(folder, taxo1Path(records.taxo1_firstTypeItem2, secondTypeRecord, folder));

	}

	@Test
	public void givenRecordsUsingMultipleTaxonomiesAndMultipleElements()
			throws RecordServicesException {
		Record folder = recordServices.newRecordWithSchema(folderSchema.instance());
		folder.set(folderSchema.taxonomy1(), records.taxo1_firstTypeItem2_secondTypeItem1);
		folder.set(folderSchema.taxonomy2(), Arrays.asList(records.taxo2_defaultSchemaItem2_customSchemaItem1,
				records.taxo2_defaultSchemaItem2_customSchemaItem2));

		Record subFolder = recordServices.newRecordWithSchema(folderSchema.instance());
		subFolder.set(folderSchema.parent(), folder);
		subFolder.set(folderSchema.taxonomy1(), records.taxo1_firstTypeItem2_secondTypeItem1);
		subFolder.set(folderSchema.taxonomy2(), Arrays.asList(records.taxo2_defaultSchemaItem2_customSchemaItem1,
				records.taxo2_defaultSchemaItem2_customSchemaItem2));

		recordServices.execute(new Transaction(subFolder, folder));

		List<String> folderPaths = new ArrayList<>();
		folderPaths.add(taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_secondTypeItem1, folder));
		folderPaths
				.add(taxo2Path(records.taxo2_defaultSchemaItem2, records.taxo2_defaultSchemaItem2_customSchemaItem1, folder));
		folderPaths
				.add(taxo2Path(records.taxo2_defaultSchemaItem2, records.taxo2_defaultSchemaItem2_customSchemaItem2, folder));
		assertThatPathIsEqualTo(folder, folderPaths);

		List<String> subFolderPaths = new ArrayList<>();
		subFolderPaths.add(
				taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_secondTypeItem1, folder, subFolder));
		subFolderPaths
				.add(taxo2Path(records.taxo2_defaultSchemaItem2, records.taxo2_defaultSchemaItem2_customSchemaItem1, folder,
						subFolder));
		subFolderPaths
				.add(taxo2Path(records.taxo2_defaultSchemaItem2, records.taxo2_defaultSchemaItem2_customSchemaItem2, folder,
						subFolder));
		//subFolderPaths.add(taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_secondTypeItem1, subFolder));
		//		subFolderPaths.add(taxo2Path(records.taxo2_defaultSchemaItem2, records.taxo2_defaultSchemaItem2_customSchemaItem1,
		//				subFolder));
		//		subFolderPaths.add(taxo2Path(records.taxo2_defaultSchemaItem2, records.taxo2_defaultSchemaItem2_customSchemaItem2,
		//				subFolder));
		assertThatPathIsEqualTo(subFolder, subFolderPaths);

		//**
		//Change taxonomy 2 in parent folders, all recalculated with new path
		//**
		folder.set(folderSchema.taxonomy2(),
				Arrays.asList(records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem1));
		recordServices.update(folder);
		recordServices.refresh(subFolder);
		folderPaths = new ArrayList<>();
		folderPaths.add(taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_secondTypeItem1, folder));
		folderPaths
				.add(taxo2Path(records.taxo2_defaultSchemaItem2, records.taxo2_defaultSchemaItem2_defaultSchemaItem2,
						records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem1, folder));
		assertThatPathIsEqualTo(folder, folderPaths);

		subFolderPaths = new ArrayList<>();
		subFolderPaths
				.add(taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_secondTypeItem1, folder, subFolder));
		subFolderPaths.add(taxo2Path(records.taxo2_defaultSchemaItem2, records.taxo2_defaultSchemaItem2_defaultSchemaItem2,
				records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem1, folder, subFolder));
		subFolderPaths.add(taxo2Path(records.taxo2_defaultSchemaItem2, records.taxo2_defaultSchemaItem2_customSchemaItem1,
				subFolder));
		subFolderPaths.add(taxo2Path(records.taxo2_defaultSchemaItem2, records.taxo2_defaultSchemaItem2_customSchemaItem2,
				subFolder));
		assertThatPathIsEqualTo(subFolder, subFolderPaths);

		//**
		//Make subFolder a root folder, paths of parent folder are removed
		//**
		subFolder.set(folderSchema.parent(), null);
		recordServices.update(subFolder);
		subFolderPaths = new ArrayList<>();
		subFolderPaths.add(taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_secondTypeItem1, subFolder));
		subFolderPaths.add(taxo2Path(records.taxo2_defaultSchemaItem2, records.taxo2_defaultSchemaItem2_customSchemaItem1,
				subFolder));
		subFolderPaths.add(taxo2Path(records.taxo2_defaultSchemaItem2, records.taxo2_defaultSchemaItem2_customSchemaItem2,
				subFolder));
		assertThatPathIsEqualTo(subFolder, subFolderPaths);
	}

	private void assertThatPathIsEqualTo(Record record, String path) {
		Metadata pathMetadata = schemas.getMetadata(record.getSchemaCode() + "_path");
		assertThat((List) record.get(pathMetadata)).containsOnly(path);
	}

	private void assertThatPathIsEqualTo(Record record, List<String> paths) {
		Metadata pathMetadata = schemas.getMetadata(record.getSchemaCode() + "_path");
		assertThat((List<String>) record.get(pathMetadata)).containsOnly(paths.toArray(new String[0]));
	}
}
