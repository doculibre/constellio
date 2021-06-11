package com.constellio.app.api.graphql;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.sdk.tests.ConstellioTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.vimalselvam.graphql.GraphqlTemplate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphqlServletAcceptanceTest extends ConstellioTest {

	private WebTarget client;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(new RMTestRecords(zeCollection))
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
				.withDocumentsDecommissioningList());

		GraphqlServlet.graphqlProvidersByCollection.set(null);

		client = newWebTarget("graphql", null, false).queryParam("collection", "zeCollection");
	}

	@Test
	public void givenRetentionRuleIdThenRetentionRuleReturned() throws Exception {
		Response response = post("retentionRuleById.graphql");
		JsonNode data = readData(response);
		assertThat(data.get("retentionRuleById").get("code").asText()).isEqualTo("1");
	}

	@Test
	public void givenRetentionRuleByIdWithCopyRetentionRuleThenRetentionRuleAndCopyRetentionRulesReturned()
			throws Exception {
		Response response = post("retentionRuleByIdWithCopyRetentionRule.graphql");
		JsonNode data = readData(response);
		assertThat(data.get("retentionRuleById").get("id").asText()).isEqualTo("ruleId_1");
		assertThat(data.get("retentionRuleById").get("copyRetentionRules").get(0).get("copyType").asText()).isEqualTo("PRINCIPAL");
		assertThat(data.get("retentionRuleById").get("copyRetentionRules").get(0).get("activeRetentionPeriod").asText()).isEqualTo("42");
		assertThat(data.get("retentionRuleById").get("copyRetentionRules").get(0).get("mediumTypes").get(0).get("code").asText()).isEqualTo("PA");
		assertThat(data.get("retentionRuleById").get("copyRetentionRules").get(0).get("mediumTypes").get(1).get("code").asText()).isEqualTo("DM");
		assertThat(data.get("retentionRuleById").get("copyRetentionRules").get(1).get("copyType").asText()).isEqualTo("SECONDARY");
		assertThat(data.get("retentionRuleById").get("copyRetentionRules").get(1).get("activeRetentionPeriod").asText()).isEqualTo("888");
		assertThat(data.get("retentionRuleById").get("copyRetentionRules").get(1).get("mediumTypes").get(0).get("code").asText()).isEqualTo("PA");
		assertThat(data.get("retentionRuleById").get("copyRetentionRules").get(1).get("mediumTypes").get(1).get("code").asText()).isEqualTo("DM");
	}

	@Test
	public void givenRetentionRuleByCodeThenRetentionRuleReturned() throws Exception {
		Response response = post("retentionRuleByCode.graphql");
		JsonNode data = readData(response);
		assertThat(data.get("retentionRuleByCode").get("administrativeUnits").get(0).get("id").asText()).isEqualTo("unitId_10");
		assertThat(data.get("retentionRuleByCode").get("administrativeUnits").get(1).get("code").asText()).isEqualTo("20");
	}

	@Test
	public void givenRetentionRuleByExpressionThenRetentionRulesReturned() throws Exception {
		Response response = post("retentionRuleByExpression.graphql");
		JsonNode data = readData(response);
		assertThat(data.get("searchRetentionRule").get(0).get("id").asText()).isEqualTo("ruleId_6");
		assertThat(data.get("searchRetentionRule").get(0).get("code").asText()).isEqualTo("6");
		assertThat(data.get("searchRetentionRule").get(1).get("id").asText()).isEqualTo("ruleId_1");
		assertThat(data.get("searchRetentionRule").get(1).get("code").asText()).isEqualTo("1");
	}

	private Response post(String graphqlFilename) throws Exception {
		File file = getTestResourceFile(graphqlFilename);
		String payload = GraphqlTemplate.parseGraphql(file, null);
		return client.request().post(Entity.json(payload));
	}

	private JsonNode readData(Response response) {
		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		JsonNode jsonNode = response.readEntity(JsonNode.class);
		return jsonNode.get("data");
	}

	private JsonNode readError(Response response) {
		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		JsonNode jsonNode = response.readEntity(JsonNode.class);
		return jsonNode.get("error");
	}
}
