package com.constellio.app.api.search;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchWebServiceRMAcceptTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTest(users).withConstellioRMModule()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		getModelLayerFactory().getPasswordFileAuthenticationService().changePassword("admin", "youshallnotpass");
	}

	private QueryResponse query(SystemWideUserInfos userCredential, ModifiableSolrParams solrParams)
			throws SolrServerException, IOException {
		SolrClient solrServer = newSearchClient();

		UserServices userServices = getModelLayerFactory().newUserServices();
		String serviceKey = userServices.giveNewServiceToken(userCredential.getUsername());
		String token = userServices.getToken(serviceKey, userCredential.getUsername(), "youshallnotpass");
		solrParams.set("serviceKey", serviceKey);
		solrParams.set("token", token);
		return solrServer.query(solrParams);
	}

	@Test
	public void givenUserWithCollectionReadAccessWhenSearchingWithFreeTextUsingWildcardThenSeeAllRecordsOfTheCollection()
			throws Exception {

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("freeText", "abeille");
		QueryResponse queryResponse = query(users.admin(), params);
		assertThat(queryResponse.getHighlighting()).isNotEmpty();

	}

}
