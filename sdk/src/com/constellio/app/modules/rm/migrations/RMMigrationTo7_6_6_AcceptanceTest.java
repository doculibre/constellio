package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.calculators.TaskTokensCalculator;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.services.schemas.calculators.TokensCalculator2;
import com.constellio.model.services.schemas.calculators.TokensCalculator4;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class RMMigrationTo7_6_6_AcceptanceTest extends ConstellioTest {

	@Test
	public void givenSystemIn7_6_5_thenMigrated()
			throws Exception {
		givenTimeIs(date(2017, 11, 16));
		givenSystemIn7_6_5();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		List<Record> auths = searchServices
				.search(new LogicalSearchQuery(from(rm.authorizationDetails.schemaType()).returnAll()));

		assertThatRecords(auths).extractingMetadatas(IDENTIFIER, rm.authorizationDetails.targetSchemaType(),
				rm.authorizationDetails.lastTokenRecalculate()).containsOnly(
				tuple("00000000409", "folder", date(2017, 11, 16)),
				tuple("00000000088", "administrativeUnit", null),
				tuple("00000000087", "administrativeUnit", null),
				tuple("00000000089", "administrativeUnit", null),
				tuple("00000000404", "document", date(2017, 11, 16)),
				tuple("00000000084", "administrativeUnit", null),
				tuple("00000000095", "administrativeUnit", null),
				tuple("00000000083", "administrativeUnit", null),
				tuple("00000000094", "administrativeUnit", null),
				tuple("00000000086", "administrativeUnit", null),
				tuple("00000000085", "administrativeUnit", null),
				tuple("00000000080", "administrativeUnit", null),
				tuple("00000000091", "administrativeUnit", null),
				tuple("00000000090", "administrativeUnit", null),
				tuple("00000000082", "administrativeUnit", null),
				tuple("00000000093", "administrativeUnit", null),
				tuple("00000000081", "administrativeUnit", null),
				tuple("00000000092", "administrativeUnit", null)
		);

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		assertThat(calculatorOf(types.getSchema(Folder.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TokensCalculator4.class);
		assertThat(calculatorOf(types.getSchema(Document.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TokensCalculator4.class);
		assertThat(calculatorOf(types.getSchema(Task.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TaskTokensCalculator.class);
		assertThat(calculatorOf(types.getSchema(ContainerRecord.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TokensCalculator2.class);
		assertThat(calculatorOf(types.getSchema(Facet.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TokensCalculator2.class);
		assertThat(calculatorOf(types.getSchema(User.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TokensCalculator2.class);
		assertThat(calculatorOf(types.getSchema(Group.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TokensCalculator2.class);
		assertThat(calculatorOf(types.getSchema(Collection.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TokensCalculator2.class);
		assertThat(calculatorOf(types.getSchema(AdministrativeUnit.DEFAULT_SCHEMA).get(TOKENS)))
				.isEqualTo(TokensCalculator2.class);

	}

	private Class<?> calculatorOf(Metadata metadata) {
		return ((CalculatedDataEntry) metadata.getDataEntry()).getCalculator().getClass();
	}

	private void givenSystemIn7_6_5() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_7.6.5_withSomeAuths.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
