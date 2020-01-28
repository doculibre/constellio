package com.constellio.app.modules.rm.migrations;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.calculators.TaskTokensCalculator2;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.calculators.TokensCalculator2;
import com.constellio.model.services.schemas.calculators.TokensCalculator4;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

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
		assertThat(calculatorOf(types.getSchema(Task.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TaskTokensCalculator2.class);
		assertThat(calculatorOf(types.getSchema(ContainerRecord.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TokensCalculator2.class);
		assertThat(calculatorOf(types.getSchema(Facet.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TokensCalculator2.class);
		assertThat(calculatorOf(types.getSchema(User.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TokensCalculator2.class);
		assertThat(calculatorOf(types.getSchema(Group.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TokensCalculator2.class);
		assertThat(calculatorOf(types.getSchema(Collection.DEFAULT_SCHEMA).get(TOKENS))).isEqualTo(TokensCalculator2.class);
		assertThat(calculatorOf(types.getSchema(AdministrativeUnit.DEFAULT_SCHEMA).get(TOKENS)))
				.isEqualTo(TokensCalculator2.class);

		assertThat(types.getSchemaType(Folder.SCHEMA_TYPE).getSmallCode()).isEqualTo("f");
		assertThat(types.getSchemaType(Document.SCHEMA_TYPE).getSmallCode()).isEqualTo("d");
		assertThat(types.getSchemaType(Task.SCHEMA_TYPE).getSmallCode()).isEqualTo("t");
		assertThat(types.getSchemaType(ContainerRecord.SCHEMA_TYPE).getSmallCode()).isEqualTo("c");
		assertThat(types.getSchemaType(Facet.SCHEMA_TYPE).getSmallCode()).isNull();

		ReindexingServices reindexingServices = getModelLayerFactory().newReindexingServices();
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		Record unit10 = record("unitId_10");

		List<String> usersWithReadAccess = new ArrayList<>();
		User gandalfUser = rm.getModelLayerFactory().newUserServices().getUserInCollection("gandalf", zeCollection);
		assertThat(gandalfUser.hasReadAccess().on(unit10)).isTrue();

		//Administrative units
		assertThat(getUsersWithReadAccess("unitId_10")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("unitId_20")).containsOnly("admin", "chuck", "alice");
		assertThat(getUsersWithReadAccess("unitId_30")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("unitId_10a")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("unitId_11")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("unitId_12")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("unitId_20d")).containsOnly("admin", "chuck", "alice");
		assertThat(getUsersWithReadAccess("unitId_20e")).containsOnly("admin", "chuck", "alice");
		assertThat(getUsersWithReadAccess("unitId_30c")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("unitId_11b")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("unitId_12b")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("unitId_12c")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		//Administrative units
		assertThat(getUsersWithReadAccess("A01")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A02")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A03")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A04")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A05")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A06")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A07")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A08")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A09")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A10")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A11")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A12")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A13")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A14")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A15")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A16")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A17")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A18")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A19")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A20")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A21")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A22")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A23")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A24")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A25")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A26")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A27")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A42")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A43")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A44")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A45")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A46")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A47")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A48")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A49")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A50")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A51")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A52")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A53")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A54")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A55")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A56")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A57")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A58")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A59")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A79")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A80")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A81")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A82")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A83")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A84")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A85")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A86")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A87")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A88")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A89")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A90")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A91")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A92")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A93")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A94")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A95")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("A96")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "gandalf");
		assertThat(getUsersWithReadAccess("B01")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B02")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B03")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B04")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B05")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B06")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B07")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B08")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B09")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B30")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B31")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B32")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B33")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B34")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B35")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B50")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B51")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B52")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B53")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B54")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("B55")).containsOnly("admin", "bob", "charles", "chuck", "dakota", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C01")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C02")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C03")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C04")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C05")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C06")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C07")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C08")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C09")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C30")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C31")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C32")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C33")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C34")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C35")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C50")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C51")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C52")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C53")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C54")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("C55")).containsOnly("admin", "bob", "chuck", "alice", "edouard", "gandalf");
		assertThat(getUsersWithReadAccess("00000000394")).containsOnly("admin", "chuck", "alice");
		assertThat(getUsersWithReadAccess("00000000397")).containsOnly("admin", "chuck", "alice", "edouard", "gandalf", "sasquatch");

		SchemasRecordsServices schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		MetadataSchema userSchema = schemas.user.schema();
		assertThat(userSchema.hasMetadataWithCode("authorizations")).isFalse();
		assertThat(userSchema.hasMetadataWithCode("allauthorizations")).isFalse();
		assertThat(userSchema.hasMetadataWithCode("alluserauthorizations")).isFalse();
		assertThat(userSchema.hasMetadataWithCode("groupsauthorizations")).isFalse();


		MetadataSchema groupSchema = schemas.group.schema();
		assertThat(groupSchema.hasMetadataWithCode("authorizations")).isFalse();

	}


	private List<String> getUsersWithReadAccess(String id) {

		Record record = record(id);

		List<String> usersWithReadAccess = new ArrayList<>();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		for (User user : rm.getAllUsers()) {
			if (user.hasReadAccess().on(record)) {
				usersWithReadAccess.add(user.getUsername());
			}
		}

		return usersWithReadAccess;
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
