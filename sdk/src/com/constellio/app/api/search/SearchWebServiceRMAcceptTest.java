package com.constellio.app.api.search;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AuthorizationModificationRequest;
import com.constellio.model.services.schemas.builders.MetadataAccessRestrictionBuilder;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.setups.Users;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class SearchWebServiceRMAcceptTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	RMSchemasRecordsServices rm;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTest(users).withConstellioRMModule()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		getModelLayerFactory().getPasswordFileAuthenticationService().changePassword("admin", "youshallnotpass");
		getModelLayerFactory().getPasswordFileAuthenticationService().changePassword("bob", "youshallnotpass");
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		rm.executeTransaction(new Transaction(records.getFolder_A01().setDescription("Biz biz biz")));


		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, types -> {

			MetadataAccessRestriction metadataAccessRestriction = new MetadataAccessRestriction(Arrays.asList(RMRoles.RGD), new ArrayList<String>(),
					new ArrayList<String>(), new ArrayList<String>());
			types.getMetadata("folder_default_description").setAccessRestrictionBuilder(MetadataAccessRestrictionBuilder.modify(metadataAccessRestriction));
		});
	}

	private QueryResponse query(SystemWideUserInfos userCredential, ModifiableSolrParams solrParams)
			throws SolrServerException, IOException {
		SolrClient solrServer = newSearchClient();

		UserServices userServices = getModelLayerFactory().newUserServices();
		String serviceKey = userServices.giveNewServiceKey(userCredential.getUsername());
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

	@Test
	public void whenSearchingIncludingExtraReferencedMetadatasThenReturned()
			throws Exception {


		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("freeText", "abeille");
		params.add("fl.extraReferencedMetadatas", "administrativeUnit.title_s, category.title_s , folder.title_s, folder.description_t, folder.administrativeUnitId_s");
		QueryResponse queryResponse = query(users.admin(), params);
		List<Tuple> tuples = queryResponse.getResults().stream().map(d -> new Tuple(
				d.getFieldValue("title_s"),
				d.getFieldValue("schema_s"),
				d.getFieldValue("administrativeUnitId_s"),
				d.getFieldValue("administrativeUnit.title_s"),
				d.getFieldValue("categoryId_s"),
				d.getFieldValue("category.title_s"),
				d.getFieldValue("folderPId_s"),
				d.getFieldValue("folder.title_s"),
				d.getFieldValue("folder.description_t"),
				d.getFieldValue("folder.administrativeUnitId_s")
		)).collect(Collectors.toList());

		assertThat(tuples).containsOnly(
				tuple("Abeille", "folder_default", "unitId_10a", "Unité 10-A", "categoryId_X110", "X110", null, null, null, null),
				tuple("Abeille - Livre de recettes", "document_default", "unitId_10a", "Unité 10-A", "categoryId_X110", "X110", "A01", "Abeille", "Biz biz biz", "unitId_10a"),
				tuple("Abeille - Typologie", "document_default", "unitId_10a", "Unité 10-A", "categoryId_X110", "X110", "A01", "Abeille", "Biz biz biz", "unitId_10a"),
				tuple("Abeille - Petit guide", "document_default", "unitId_10a", "Unité 10-A", "categoryId_X110", "X110", "A01", "Abeille", "Biz biz biz", "unitId_10a"),
				tuple("Abeille - Histoire", "document_default", "unitId_10a", "Unité 10-A", "categoryId_X110", "X110", "A01", "Abeille", "Biz biz biz", "unitId_10a"));
	}


	@Test
	public void whenSearchingIncludingInvalidExtraReferencedMetadatasThenErrors()
			throws Exception {


		TestUtils.assertThatException(() -> query(users.admin(), new ModifiableSolrParams().set("freeText", "abeille")
				.add("fl.extraReferencedMetadatas", "folder.title_s, folder")))
				.isInstanceOf(RemoteSolrException.class);

		TestUtils.assertThatException(() -> query(users.admin(), new ModifiableSolrParams().set("freeText", "abeille")
				.add("fl.extraReferencedMetadatas", "folder.title_s, folder.pouet_s")))
				.isInstanceOf(RemoteSolrException.class);

		TestUtils.assertThatException(() -> query(users.admin(), new ModifiableSolrParams().set("freeText", "abeille")
				.add("fl.extraReferencedMetadatas", "folder.title_s, folder.title_s.pouet_s")))
				.isInstanceOf(RemoteSolrException.class);


	}

	@Test
	public void givenNoAccessToReferencedRecordWhenTryingToRetrieveMetadatasThenNotReturnedUnlessTitleOrCaption()
			throws Exception {

		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		String negativeAuthId = authorizationsServices.add(authorizationForUsers(users.bobIn(zeCollection)).on(records.folder_A01).givingNegativeReadWriteDeleteAccess());
		Document document = rm.searchDocuments(where(Schemas.TITLE).isEqualTo("Abeille - Livre de recettes")).get(0);
		waitForBatchProcess();
		authorizationsServices.execute(AuthorizationModificationRequest.modifyAuthorizationOnRecord(negativeAuthId, document).removingItOnRecord());

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("freeText", "abeille");
		params.add("fl.extraReferencedMetadatas", "administrativeUnit.title_s, folder.title_s, folder.caption_s, folder.description_t, folder.administrativeUnitId_s");
		QueryResponse queryResponse = query(users.bob(), params);

		List<Tuple> tuples = queryResponse.getResults().stream().map(d -> new Tuple(
				d.getFieldValue("title_s"),
				d.getFieldValue("schema_s"),
				d.getFieldValue("administrativeUnitId_s"),
				d.getFieldValue("administrativeUnit.title_s"),
				d.getFieldValue("folderPId_s"),
				d.getFieldValue("folder.title_s"),
				d.getFieldValue("folder.caption_s"),
				d.getFieldValue("folder.description_t"),
				d.getFieldValue("folder.administrativeUnitId_s")
		)).collect(Collectors.toList());

		assertThat(tuples).containsOnly(
				tuple("Abeille - Livre de recettes", "document_default", "unitId_10a", "Unité 10-A", "A01", "Abeille", "Abeille", null, null));
	}


	@Test
	public void givenNoAccessToProtectedMetadataWhenTryingToRetrieveMetadatasThenNotReturned()
			throws Exception {


		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("freeText", "abeille");
		params.add("fl.extraReferencedMetadatas", "administrativeUnit.title_s, folder.title_s, folder.caption_s, folder.description_t, folder.administrativeUnitId_s");
		QueryResponse queryResponse = query(users.bob(), params);

		List<Tuple> tuples = queryResponse.getResults().stream().map(d -> new Tuple(
				d.getFieldValue("title_s"),
				d.getFieldValue("schema_s"),
				d.getFieldValue("administrativeUnitId_s"),
				d.getFieldValue("administrativeUnit.title_s"),
				d.getFieldValue("folderPId_s"),
				d.getFieldValue("folder.title_s"),
				d.getFieldValue("folder.caption_s"),
				d.getFieldValue("folder.description_t"),
				d.getFieldValue("folder.administrativeUnitId_s")
		)).collect(Collectors.toList());

		assertThat(tuples).contains(
				tuple("Abeille - Livre de recettes", "document_default", "unitId_10a", "Unité 10-A", "A01", "Abeille", "Abeille", null, "unitId_10a"));
	}

}
