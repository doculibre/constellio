package com.constellio.model.services.records.reindexing;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RMReindexingSchemaTypeRecordsAcceptanceTest extends ConstellioTest {

	Category c10;
	Category c11;
	Category c12;
	Category c20;

	AdministrativeUnit u10;
	AdministrativeUnit u11;
	AdministrativeUnit u12;
	AdministrativeUnit u20;

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setup() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records));
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, types-> {

			MetadataBuilder unionOfAllTitles = types.getSchemaType("folder").getDefaultSchema()
					.create("unionOfAllTitles").setType(STRING).setMultivalue(true)
					.defineDataEntry().asCalculated(UnionOfAllTitlesCalculator.class);

			types.getSchemaType("folder").getDefaultSchema().create("unionOfChildFolderTitles").setMultivalue(true).setType(STRING)
					.defineDataEntry().asUnion(
							types.getSchemaType("folder").getDefaultSchema().getMetadata("parentFolder"),
					types.getSchemaType("folder").getDefaultSchema().getMetadata("unionOfAllTitles"));

			types.getSchemaType("folder").getDefaultSchema().create("unionOfDocTitles").setMultivalue(true).setType(STRING)
					.defineDataEntry().asUnion(
					types.getSchemaType("document").getDefaultSchema().getMetadata("folder"),
					types.getSchemaType("document").getDefaultSchema().getMetadata("title"));

			unionOfAllTitles.defineDataEntry().asCalculated(UnionOfAllTitlesCalculator.class);

			types.getSchemaType("category").getDefaultSchema().create("hierarchyTitles").setMultivalue(true).setType(STRING)
					.defineDataEntry().asUnion(
					types.getSchemaType("folder").getDefaultSchema().getMetadata("categoryEntered"),
					types.getSchemaType("folder").getDefaultSchema().getMetadata("unionOfAllTitles"));

		});

		Transaction tx = new Transaction();

		c10 = tx.add(rm.newCategory().setCode("c10").setTitle("c10").setRetentionRules(asList(records.ruleId_1)));
		c11 = tx.add(rm.newCategory().setCode("c11").setTitle("c11").setParent(c10)).setRetentionRules(asList(records.ruleId_1));
		c12 = tx.add(rm.newCategory().setCode("c12").setTitle("c12").setParent(c10)).setRetentionRules(asList(records.ruleId_1));
		c20 = tx.add(rm.newCategory().setCode("c20").setTitle("c20")).setRetentionRules(asList(records.ruleId_1));

		u10 = tx.add(rm.newAdministrativeUnit().setCode("u10").setTitle("c10"));
		u11 = tx.add(rm.newAdministrativeUnit().setCode("u11").setTitle("c11").setParent(u10));
		u12 = tx.add(rm.newAdministrativeUnit().setCode("u12").setTitle("c12").setParent(u10));
		u20 = tx.add(rm.newAdministrativeUnit().setCode("u20").setTitle("c20"));


		BiConsumer<Category, AdministrativeUnit> createHierarchy = (category, administrativeUnit) -> {
			String cCode = category.getCode().replace("c", "");
			Folder f10 = tx.add(rm.newFolder()
					.setOpenDate(LocalDate.now())
					.setCategoryEntered(category)
					.setAdministrativeUnitEntered(administrativeUnit)
					.setRetentionRuleEntered(records.ruleId_1).setTitle(cCode + "-10"));
			Folder f20 = tx.add(rm.newFolder()
					.setOpenDate(LocalDate.now())
					.setCategoryEntered(category)
					.setAdministrativeUnitEntered(administrativeUnit)
					.setRetentionRuleEntered(records.ruleId_1).setTitle(cCode + "-20"));

			Folder f11 = tx.add(rm.newFolder().setOpenDate(LocalDate.now()).setParentFolder(f10).setTitle(cCode + "-11"));
			Folder f12 = tx.add(rm.newFolder().setOpenDate(LocalDate.now()).setParentFolder(f10).setTitle(cCode + "-12"));

			Folder f21 = tx.add(rm.newFolder().setOpenDate(LocalDate.now()).setParentFolder(f20).setTitle(cCode + "-21"));
			Folder f22 = tx.add(rm.newFolder().setOpenDate(LocalDate.now()).setParentFolder(f20).setTitle(cCode + "-22"));

			tx.add(rm.newDocument().setFolder(f10).setTitle(f10.getTitle() + "-doc"));
			tx.add(rm.newDocument().setFolder(f20).setTitle(f20.getTitle() + "-doc"));
			tx.add(rm.newDocument().setFolder(f11).setTitle(f11.getTitle() + "-doc"));
			tx.add(rm.newDocument().setFolder(f12).setTitle(f12.getTitle() + "-doc"));
			tx.add(rm.newDocument().setFolder(f21).setTitle(f21.getTitle() + "-doc"));
			tx.add(rm.newDocument().setFolder(f22).setTitle(f22.getTitle() + "-doc"));

		};

		createHierarchy.accept(c10, u10);
		createHierarchy.accept(c11, u11);
		createHierarchy.accept(c12, u12);
		createHierarchy.accept(c20, u20);

		getModelLayerFactory().newRecordServices().execute(tx);

		new ReindexingServices(getModelLayerFactory()).reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);
	}

	public static class UnionOfAllTitlesCalculator implements MetadataValueCalculator<List<String>> {

		LocalDependency<String> titleDependency = LocalDependency.toAString("title");
		LocalDependency<List<String>> unionOfChildFolderTitlesDependency = LocalDependency.toAString("unionOfChildFolderTitles").whichIsMultivalue();
		LocalDependency<List<String>> unionOfDocTitlesDependency = LocalDependency.toAString("unionOfDocTitles").whichIsMultivalue();

		@Override
		public List<String> calculate(CalculatorParameters parameters) {

			List<String> values = new ArrayList<>();
			values.add(parameters.get(titleDependency));
			values.addAll(parameters.get(unionOfChildFolderTitlesDependency));
			values.addAll(parameters.get(unionOfDocTitlesDependency));

			values = values.stream().filter(Objects::nonNull).collect(Collectors.toList());

			return values;
		}

		@Override
		public List<String> getDefaultValue() {
			return null;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(titleDependency, unionOfChildFolderTitlesDependency, unionOfDocTitlesDependency);
		}
	}

	@Test
	public void integration_whenAddSpecificAuthorizationOnFolderThenAppliedOnAllHierarchy() {

	}


	@Test
	public void testPrepare() {

		assertThat(f(11,21).<Integer>getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS))
				.containsExactly(u10.getWrappedRecordId().intValue(), u11.getWrappedRecordId().intValue(),
						f(11,20).getWrappedRecordId().intValue());

		assertThat(f(11,21).<Integer>getList(Schemas.SECONDARY_CONCEPTS_INT_IDS))
				.containsExactly(c10.getWrappedRecordId().intValue(), c11.getWrappedRecordId().intValue());

		assertThat(d(11,21).<Integer>getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS))
				.containsExactly(u10.getWrappedRecordId().intValue(), u11.getWrappedRecordId().intValue(),
						f(11,20).getWrappedRecordId().intValue(), f(11,21).getWrappedRecordId().intValue());

		assertThat(d(11,21).<Integer>getList(Schemas.SECONDARY_CONCEPTS_INT_IDS))
				.containsExactly(c10.getWrappedRecordId().intValue(), c11.getWrappedRecordId().intValue());

		assertThat(f(10,10).<String>getList("unionOfAllTitles")).containsOnly("10-10", "10-12-doc", "10-12", "10-11", "10-11-doc", "10-10-doc");
	}

	Folder f(int category, int folder) {
		return rm.searchFolders(where(Schemas.TITLE).isEqualTo(category + "-" + folder)).get(0);
	}

	Document d(int category, int folder) {
		return rm.searchDocuments(where(Schemas.TITLE).isEqualTo(category + "-" + folder + "-doc")).get(0);
	}

}
