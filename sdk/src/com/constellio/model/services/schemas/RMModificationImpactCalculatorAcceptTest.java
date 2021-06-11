package com.constellio.model.services.schemas;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.CacheBasedTaxonomyVisitingServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.schemas.ModificationImpactCalculator.MINIMUM_HIERARCHY_SIZE_TO_BE_CONSIDERED_CASCADING;
import static org.assertj.core.api.Assertions.assertThat;

public class RMModificationImpactCalculatorAcceptTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	ModificationImpactCalculator calculator;
	RMSchemasRecordsServices rm;
	CacheBasedTaxonomyVisitingServices visitingServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus());

		MetadataSchemaTypes metadataSchemaTypes = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		List<Taxonomy> taxonomies = getModelLayerFactory().getTaxonomiesManager().getEnabledTaxonomies(zeCollection);
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());

		calculator = new ModificationImpactCalculator(metadataSchemaTypes, taxonomies, searchServices, recordServices, visitingServices);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void whenDetectingIfChangeIsCascadingThenBasedOnRecordAndDepth() throws Exception {
		waitForCacheLoading();
//		visitingServices.visit(records.getUnit10().getWrappedRecord(), item -> {
//			System.out.println(StringUtils.repeat("\t", item.getLevel()) + item.getRecord().getSchemaIdTitle());
//			return TaxonomyVisitingStatus.CONTINUE;
//		});
//
//		visitingServices.visit(records.getCategory_X().getWrappedRecord(), item -> {
//			System.out.println(StringUtils.repeat("\t", item.getLevel()) + item.getRecord().getSchemaIdTitle());
//			return TaxonomyVisitingStatus.CONTINUE;
//		});

		Transaction tx = new Transaction();

		for(int i = 0 ; i < MINIMUM_HIERARCHY_SIZE_TO_BE_CONSIDERED_CASCADING ; i++) {
			tx.add(records.newFolderWithValues().setAdministrativeUnitEntered(records.getUnit10a()));
		}

		long recordsInFolder10 = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory()).streamAllIn(records.getFolder_A01()).count();
		System.out.println(recordsInFolder10);
		for(int i = 0 ; i < MINIMUM_HIERARCHY_SIZE_TO_BE_CONSIDERED_CASCADING  - recordsInFolder10; i++) {
			tx.add(records.newChildFolderIn(records.getFolder_A01()));
		}
		getModelLayerFactory().newRecordServices().execute(tx);

		assertThat(calculator.isCascading(modification(records.getUnit10(), rm.administrativeUnit.parent()))).isTrue();
		assertThat(calculator.isCascading(modification(records.getUnit10a(), rm.administrativeUnit.parent()))).isTrue();

		assertThat(calculator.isCascading(modification(records.getUnit10(), rm.administrativeUnit.title()))).isFalse();
		assertThat(calculator.isCascading(modification(records.getUnit10a(), rm.administrativeUnit.title()))).isFalse();

		assertThat(calculator.isCascading(modification(records.getUnit10(), rm.administrativeUnit.description()))).isFalse();
		assertThat(calculator.isCascading(modification(records.getUnit10a(), rm.administrativeUnit.description()))).isFalse();


		//Not enough child levels
		assertThat(calculator.isCascading(modification(records.getFolder_A01(), rm.folder.category()))).isFalse();
		assertThat(calculator.isCascading(modification(records.getFolder_A01(), rm.folder.category()))).isFalse();
		assertThat(calculator.isCascading(modification(records.getFolder_A01(), rm.folder.parentFolder()))).isFalse();
		assertThat(calculator.isCascading(modification(records.getFolder_A01(), rm.folder.parentFolder()))).isFalse();

		tx = new Transaction();
		Folder child = tx.add(rm.newFolder().setTitle("Child").setParentFolder(records.getFolder_A01())).setOpenDate(LocalDate.now());
		Folder childChild = tx.add(rm.newFolder().setTitle("childChild").setParentFolder(child)).setOpenDate(LocalDate.now());
		rm.executeTransaction(tx);

		//Now ok
		assertThat(calculator.isCascading(modification(records.getFolder_A01(), rm.folder.category()))).isTrue();
		assertThat(calculator.isCascading(modification(records.getFolder_A01(), rm.folder.category()))).isTrue();
		assertThat(calculator.isCascading(modification(records.getFolder_A01(), rm.folder.parentFolder()))).isTrue();
		assertThat(calculator.isCascading(modification(records.getFolder_A01(), rm.folder.parentFolder()))).isTrue();
		assertThat(calculator.isCascading(modification(records.getFolder_A01(), rm.folder.title()))).isTrue();
		assertThat(calculator.isCascading(modification(records.getFolder_A01(), rm.folder.title()))).isTrue();
		assertThat(calculator.isCascading(modification(records.getFolder_A01(), rm.folder.description()))).isFalse();
		assertThat(calculator.isCascading(modification(records.getFolder_A01(), rm.folder.description()))).isFalse();


		//Not enough child levels
		assertThat(calculator.isCascading(modification(child, rm.folder.category()))).isFalse();
		assertThat(calculator.isCascading(modification(child, rm.folder.category()))).isFalse();
		assertThat(calculator.isCascading(modification(child, rm.folder.parentFolder()))).isFalse();
		assertThat(calculator.isCascading(modification(child, rm.folder.parentFolder()))).isFalse();

		//Not enough child levels
		assertThat(calculator.isCascading(modification(childChild, rm.folder.category()))).isFalse();
		assertThat(calculator.isCascading(modification(childChild, rm.folder.category()))).isFalse();
		assertThat(calculator.isCascading(modification(childChild, rm.folder.parentFolder()))).isFalse();
		assertThat(calculator.isCascading(modification(childChild, rm.folder.parentFolder()))).isFalse();


	}

	RecordsModification modification(RecordWrapper wrapper, Metadata metadata) {
		return new RecordsModification(
				Arrays.asList(wrapper.getWrappedRecord()),
				Arrays.asList(metadata),
				metadata.getSchemaType(), new RecordUpdateOptions());
	}
}
