package com.constellio.model.services.records.reindexing;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class RMReindexingSchemaTypeRecords1MillionRecordsAcceptanceTest extends ConstellioTest {

	Category c10;
	Category c11;
	Category c12;
	Category c20;

	AdministrativeUnit u10;
	AdministrativeUnit u11;
	AdministrativeUnit u12;
	AdministrativeUnit u20;

	RMSchemasRecordsServices rm;

	Folder f10;

	Folder f20;

	Folder f10Sub;

	Users users = new Users();

	AuthorizationsServices authServices;

	SearchServices searchServices;

	LogicalSearchQuery docsInF10, allDocs, aliceDocs, bobDocs, charlesDocs, dakotaDocs;
	int docsCount;

	@Before
	public void setup() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users));
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		getModelLayerFactory().getRecordsCaches().disableVolatileCache();

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, types -> {

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

		u10 = tx.add(rm.newAdministrativeUnit().setCode("u1").setTitle("c1"));
		u20 = tx.add(rm.newAdministrativeUnit().setCode("u2").setTitle("c2"));

		CopyRetentionRule principal5_2_T = CopyRetentionRuleBuilder.UUID().newPrincipal(asList(rm.PA(), rm.DM()), "5-2-T");
		CopyRetentionRule secondary2_0_D = CopyRetentionRuleBuilder.UUID().newSecondary(asList(rm.PA(), rm.DM()), "2-0-D");
		RetentionRule rule = tx.add(rm.newRetentionRule()).setCode("1")
				.setTitle("Règle de conservation #1").setTitle(ENGLISH, "Retention rule #1")
				.setAdministrativeUnits(asList(u10, u20)).setApproved(true)
				.setCopyRetentionRules(asList(principal5_2_T, secondary2_0_D)).setKeywords(asList("Rule #1"))
				.setCorpus("Corpus Rule 1").setDescription("Description Rule 1")
				.setJuridicReference("Juridic reference Rule 1").setGeneralComment("General Comment Rule 1")
				.setCopyRulesComment(asList("R1:comment1", "R2:comment2", "R3:comment3", "R4:comment4"));

		c10 = tx.add(rm.newCategory().setCode("c1").setTitle("c1").setRetentionRules(asList(rule)));
		c20 = tx.add(rm.newCategory().setCode("c2").setTitle("c2")).setRetentionRules(asList(rule));


		f10 = tx.add(rm.newFolder()
				.setOpenDate(LocalDate.now())
				.setCategoryEntered(c10)
				.setAdministrativeUnitEntered(u10)
				.setRetentionRuleEntered(rule).setTitle("A"));
		f20 = tx.add(rm.newFolder()
				.setOpenDate(LocalDate.now())
				.setCategoryEntered(c20)
				.setAdministrativeUnitEntered(u20)
				.setRetentionRuleEntered(rule).setTitle("B"));

		f10Sub = tx.add(rm.newFolder()
				.setOpenDate(LocalDate.now())
				.setParentFolder(f10).setTitle("AA"));

		getModelLayerFactory().newRecordServices().execute(tx);


		authServices = getModelLayerFactory().newAuthorizationsServices();
		searchServices = getModelLayerFactory().newSearchServices();

		docsInF10 = new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.PATH_PARTS).isEqualTo(f10.getId()));
		allDocs = new LogicalSearchQuery(from(rm.document.schemaType()).returnAll());
		aliceDocs = new LogicalSearchQuery(allDocs).filteredWithUserRead(users.aliceIn(zeCollection));
		bobDocs = new LogicalSearchQuery(allDocs).filteredWithUserRead(users.bobIn(zeCollection));
		charlesDocs = new LogicalSearchQuery(allDocs).filteredWithUserRead(users.charlesIn(zeCollection));
		dakotaDocs = new LogicalSearchQuery(allDocs).filteredWithUserRead(users.dakotaIn(zeCollection));
		assertThat(searchServices.getResultsCount(aliceDocs)).isEqualTo(0);

		authServices.add(authorizationForUsers(users.aliceIn(zeCollection)).givingReadAccess().on(f10));
		authServices.add(authorizationForUsers(users.bobIn(zeCollection)).givingReadAccess().on(f20));

		AtomicInteger addedCounter = new AtomicInteger();

		List<Folder> previousLevelFolders = asList(f10Sub);
		try (BulkRecordTransactionHandler handler = new BulkRecordTransactionHandler(getModelLayerFactory().newRecordServices(), SDK_STREAM)) {
			for (int level = 0; level < 4; level++) {
				//for (int level = 0; level < 8; level++) {

				List<Folder> currentLevelFolders = new ArrayList<>();

				for (Folder previousLevelFolder : previousLevelFolders) {

					List<Record> recordsToAdd = new ArrayList<>();
					for (int folderIndex = 1; folderIndex <= 5; folderIndex++) {
						Folder folder = rm.newFolder()
								.setOpenDate(LocalDate.now())
								.setParentFolder(previousLevelFolder)
								.setTitle(previousLevelFolder.getTitle() + "-" + folderIndex);

						Document document = rm.newDocument().setFolder(folder).setTitle(folder.getTitle() + "-doc");
						if (level < 7) {
							currentLevelFolders.add(folder);
						}
						recordsToAdd.add(folder.getWrappedRecord());
						recordsToAdd.add(document.getWrappedRecord());


					}
					handler.append(recordsToAdd, asList(previousLevelFolder.getWrappedRecord()));
					addedCounter.addAndGet(recordsToAdd.size());

				}

				handler.barrier();
				previousLevelFolders = currentLevelFolders;

			}
		}

		docsCount = (int) searchServices.getResultsCount(allDocs);
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
	public void whenMoveAddAuthorizationsThenAppliedAsync() throws Exception {

		fail("TODO FRANCIS - BRISÉ!");
		System.out.println("Docs count : " + docsCount);

		System.out.println("Duck count : 0");

		assertThat(searchServices.getResultsCount(aliceDocs)).isEqualTo(docsCount);
		assertThat(searchServices.getResultsCount(bobDocs)).isEqualTo(0);

		waitForBatchProcess();

		getModelLayerFactory().newRecordServices().executeHandlingImpactsAsync(new Transaction(f10Sub.setParentFolder(f20)));

		assertThat(searchServices.hasResults(docsInF10)).isTrue();
		assertThat(searchServices.hasResults(aliceDocs)).isTrue();

		waitForBatchProcess();

		allDocs = new LogicalSearchQuery(from(rm.document.schemaType()).returnAll());
		aliceDocs = new LogicalSearchQuery(allDocs).filteredWithUserRead(users.aliceIn(zeCollection));
		bobDocs = new LogicalSearchQuery(allDocs).filteredWithUserRead(users.bobIn(zeCollection));
		charlesDocs = new LogicalSearchQuery(allDocs).filteredWithUserRead(users.charlesIn(zeCollection));
		dakotaDocs = new LogicalSearchQuery(allDocs).filteredWithUserRead(users.dakotaIn(zeCollection));
		assertThat(searchServices.hasResults(docsInF10)).isFalse();
		assertThat(searchServices.getResultsCount(bobDocs)).isEqualTo(docsCount);

		assertThat(searchServices.getResultsCount(aliceDocs)).isEqualTo(0);
		assertThat(searchServices.getResultsCount(bobDocs)).isEqualTo(docsCount);
		assertThat(searchServices.getResultsCount(charlesDocs)).isEqualTo(0);

		authServices.add(authorizationForUsers(users.charlesIn(zeCollection)).givingReadAccess().on(f20));
		authServices.add(authorizationForUsers(users.bobIn(zeCollection)).givingNegativeReadAccess().on(f10Sub));
		waitForBatchProcess();

		assertThat(searchServices.getResultsCount(aliceDocs)).isEqualTo(0);
		assertThat(searchServices.getResultsCount(bobDocs)).isEqualTo(0);
		assertThat(searchServices.getResultsCount(charlesDocs)).isEqualTo(docsCount);


	}


	Folder f(int category, int folder) {
		return rm.searchFolders(where(Schemas.TITLE).isEqualTo(category + "-" + folder)).get(0);
	}

	Document d(int category, int folder) {
		return rm.searchDocuments(where(Schemas.TITLE).isEqualTo(category + "-" + folder + "-doc")).get(0);
	}

}
