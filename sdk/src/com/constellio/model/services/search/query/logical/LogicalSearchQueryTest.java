package com.constellio.model.services.search.query.logical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.sdk.tests.ConstellioTest;

public class LogicalSearchQueryTest extends ConstellioTest {
	@Mock User user;
	LogicalSearchQuery query;
	List<String> userTokens = Arrays.asList("rtokenA", "rtokenB", "wtokenC", "wtokenD", "dtokenE", "dtokenF");
	@Mock DataStoreField metadata;

	@Before
	public void setUp()
			throws Exception {
		query = new LogicalSearchQuery();
		when(user.getCollection()).thenReturn("ZeCollection");
	}

	//	@Test
	//	public void givenFilterByUserReadWith2TokensThenOK()
	//			throws Exception {
	//		query.filteredWithUser(user);
	//		assertThat(query.filterUser).isEqualTo("tokens_ss:A38 OR tokens_ss:rtokenA OR tokens_ss:rtokenB OR tokens_ss:__public__");
	//	}
	//
	//	@Test
	//	public void givenFilterByUserReadWith2TokensAndReadAccessThenOK()
	//			throws Exception {
	//		when(user.hasCollectionReadAccess()).thenReturn(true);
	//		query.filteredWithUser(user);
	//
	//		assertThat(query.filterUser).isEqualTo(
	//				"tokens_ss:A38 OR collection_s:ZeCollection OR tokens_ss:rtokenA OR tokens_ss:rtokenB OR tokens_ss:__public__");
	//	}
	//
	//	@Test
	//	public void givenFilterByUserReadWith2TokensAndWriteAccessThenOK()
	//			throws Exception {
	//		when(user.hasCollectionWriteAccess()).thenReturn(true);
	//		query.filteredWithUser(user);
	//
	//		assertThat(query.filterUser)
	//				.isEqualTo(
	//						"tokens_ss:A38 OR collection_s:ZeCollection OR tokens_ss:rtokenA OR tokens_ss:rtokenB OR tokens_ss:__public__");
	//	}
	//
	//	@Test
	//	public void givenFilterByUserReadWith2TokensAndDeleteAccessThenOK()
	//			throws Exception {
	//		when(user.getUserTokens()).thenReturn(Arrays.asList("rtokenA", "rtokenB", "wtokenC", "dtokenD"));
	//		when(user.hasCollectionDeleteAccess()).thenReturn(true);
	//		query.filteredWithUser(user);
	//
	//		assertThat(query.filterUser)
	//				.isEqualTo(
	//						"tokens_ss:A38 OR collection_s:ZeCollection OR tokens_ss:rtokenA OR tokens_ss:rtokenB OR tokens_ss:__public__");
	//	}
	//
	//	@Test
	//	public void givenFilterByUserWriteWith2TokensAndReadAccessThenOK()
	//			throws Exception {
	//		when(user.hasCollectionReadAccess()).thenReturn(true);
	//		query.filteredWithUserWrite(user);
	//
	//		assertThat(query.filterUser).isEqualTo("tokens_ss:A38 OR tokens_ss:wtokenC OR tokens_ss:wtokenD");
	//	}
	//
	//	@Test
	//	public void givenFilterByUserWriteWith2TokensAndWriteAccessThenOK()
	//			throws Exception {
	//		when(user.hasCollectionWriteAccess()).thenReturn(true);
	//		query.filteredWithUserWrite(user);
	//
	//		assertThat(query.filterUser)
	//				.isEqualTo("tokens_ss:A38 OR collection_s:ZeCollection OR tokens_ss:wtokenC OR tokens_ss:wtokenD");
	//	}
	//
	//	@Test
	//	public void givenFilterByUserWriteWith2TokensAndDeleteAccessThenOK()
	//			throws Exception {
	//		when(user.hasCollectionDeleteAccess()).thenReturn(true);
	//		query.filteredWithUserWrite(user);
	//
	//		assertThat(query.filterUser).isEqualTo("tokens_ss:A38 OR tokens_ss:wtokenC OR tokens_ss:wtokenD");
	//	}
	//
	//	@Test
	//	public void givenFilterByUserDeleteWith2TokensAndReadAccessThenOK()
	//			throws Exception {
	//		when(user.hasCollectionReadAccess()).thenReturn(true);
	//		query.filteredWithUserDelete(user);
	//
	//		assertThat(query.filterUser).isEqualTo("tokens_ss:A38 OR tokens_ss:dtokenE OR tokens_ss:dtokenF");
	//	}
	//
	//	@Test
	//	public void givenFilterByUserDeleteWith2TokensAndWriteAccessThenOK()
	//			throws Exception {
	//		when(user.hasCollectionWriteAccess()).thenReturn(true);
	//		query.filteredWithUserDelete(user);
	//
	//		assertThat(query.filterUser).isEqualTo("tokens_ss:A38 OR tokens_ss:dtokenE OR tokens_ss:dtokenF");
	//	}
	//
	//	@Test
	//	public void givenFilterByUserDeleteWith2TokensAndDeleteAccessThenOK()
	//			throws Exception {
	//		when(user.hasCollectionDeleteAccess()).thenReturn(true);
	//		query.filteredWithUserDelete(user);
	//
	//		assertThat(query.filterUser)
	//				.isEqualTo("tokens_ss:A38 OR collection_s:ZeCollection OR tokens_ss:dtokenE OR tokens_ss:dtokenF");
	//	}

	@Test
	public void givenFilterByStatusDeletedThenFiltersContainsDeleted()
			throws Exception {

		query.filteredByStatus(StatusFilter.DELETED);

		assertThat(query.filterStatus).isEqualTo("deleted_s:__TRUE__");
	}

	@Test
	public void givenFilterByStatusActiveThenFiltersContainsActive()
			throws Exception {

		query.filteredByStatus(StatusFilter.ACTIVES);

		assertThat(query.filterStatus).isEqualTo("(*:* -deleted_s:__TRUE__)");
	}

	@Test
	public void givenFilterByStatusAllThenNoFilterAdded()
			throws Exception {

		query.filteredByStatus(StatusFilter.ALL);

		assertThat(query.filterStatus).isNull();
	}

	@Test
	public void whenAddStatsOnFieldThenFilterAdded()
			throws Exception {
		query.computeStatsOnField(metadata);
		assertThat(query.getStatisticFields()).contains(metadata.getDataStoreCode());
	}
}
