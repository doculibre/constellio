package com.constellio.data.dao.services.bigVault;

import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.LazyIteratorRuntimeException.LazyIteratorRuntimeException_IncorrectUsage;
import com.constellio.data.utils.LazyIteratorRuntimeException.LazyIteratorRuntimeException_RemoveNotAvailable;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LazyResultsIteratorTest extends ConstellioTest {

	@Mock RecordDao recordDao;

	ModifiableSolrParams modifiableSolrParams;

	LazyResultsIterator iterator;

	QueryResponseDTO noResultsResponse;

	@Mock RecordDTO record1;
	@Mock RecordDTO record2;
	@Mock RecordDTO record3;
	@Mock RecordDTO record4;
	@Mock RecordDTO record5;
	@Mock RecordDTO record6;

	@Before
	public void setUp()
			throws Exception {

		modifiableSolrParams = new ModifiableSolrParams();
		modifiableSolrParams.set("q", "zeQ");
		modifiableSolrParams.set("fq", "zeFQ");

		iterator = spy(new LazyResultsIterator<String>(recordDao, modifiableSolrParams, 3, true, "test") {
			@Override
			public String convert(RecordDTO recordDTO) {
				return "converted_" + recordDTO.getId();
			}
		});

		when(record1.getId()).thenReturn("1");
		when(record2.getId()).thenReturn("2");
		when(record3.getId()).thenReturn("3");
		when(record4.getId()).thenReturn("4");
		when(record5.getId()).thenReturn("5");
		when(record6.getId()).thenReturn("6");

		noResultsResponse = responseWithElements();
	}

	@Test
	public void givenNoResultsThenHasNextFalse()
			throws Exception {

		ArgumentCaptor<SolrParams> solrParams = ArgumentCaptor.forClass(SolrParams.class);

		when(recordDao.query(anyString(), solrParams.capture())).thenReturn(noResultsResponse);

		assertThat(iterator.hasNext()).isFalse();
		verify(iterator).loadNextBatch();

	}

	@Test
	public void givenFirstBatchOf3ResultsAndAnotherBatchOf1ResultsWhenIteratingThenCorrect()
			throws Exception {

		QueryResponseDTO batch1 = responseWithElements(record1, record2, record3);
		QueryResponseDTO batch2 = responseWithElements(record4);
		when(recordDao.query(anyString(), any(SolrParams.class))).thenReturn(batch1).thenReturn(batch2);

		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_1");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_2");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_3");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_4");
		assertThat(iterator.hasNext()).isFalse();

	}

	@Test
	public void givenResultsThenCanCallHasNextMultipleTimesWithoutImpacts()
			throws Exception {

		QueryResponseDTO batch1 = responseWithElements(record1, record2, record3);
		QueryResponseDTO batch2 = responseWithElements(record4);
		when(recordDao.query(anyString(), any(SolrParams.class))).thenReturn(batch1).thenReturn(batch2);

		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_1");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_2");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_3");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_4");
		assertThat(iterator.hasNext()).isFalse();
		assertThat(iterator.hasNext()).isFalse();
		assertThat(iterator.hasNext()).isFalse();

	}

	@Test
	public void givenIteratorUsedInHeroicModeThenOk()
			throws Exception {

		QueryResponseDTO batch1 = responseWithElements(record1, record2, record3);
		QueryResponseDTO batch2 = responseWithElements(record4);
		when(recordDao.query(anyString(), any(SolrParams.class))).thenReturn(batch1).thenReturn(batch2);

		assertThat(iterator.next()).isEqualTo("converted_1");
		assertThat(iterator.next()).isEqualTo("converted_2");
		assertThat(iterator.next()).isEqualTo("converted_3");
		assertThat(iterator.next()).isEqualTo("converted_4");

	}

	@Test
	public void givenFirstBatchOf3ResultsAndEmptyBatchResultsWhenIteratingThenCorrect()
			throws Exception {

		QueryResponseDTO batch1 = responseWithElements(record1, record2, record3);
		QueryResponseDTO batch2 = noResultsResponse;
		when(recordDao.query(anyString(), any(SolrParams.class))).thenReturn(batch1).thenReturn(batch2);

		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_1");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_2");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_3");
		assertThat(iterator.hasNext()).isFalse();

	}

	@Test
	public void givenRecordsSentMultipleTimesWhileIteratingThenNoProblem()
			throws Exception {

		QueryResponseDTO batch1 = responseWithElements(record1, record2, record3);
		QueryResponseDTO batch2 = responseWithElements(record3, record1, record4);
		QueryResponseDTO batch3 = noResultsResponse;
		when(recordDao.query(anyString(), any(SolrParams.class))).thenReturn(batch1).thenReturn(batch2).thenReturn(batch3);

		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_1");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_2");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_3");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_3");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_1");
		assertThat(iterator.hasNext()).isTrue();
		assertThat(iterator.next()).isEqualTo("converted_4");
		assertThat(iterator.hasNext()).isFalse();

	}

	@Test
	public void whenLoadNextBatchThenUseSameParamsWithDifferentRowsAndStart()
			throws Exception {

		QueryResponseDTO batch1 = responseWithElements(record1, record2, record3);
		QueryResponseDTO batch2 = responseWithElements(record3, record1, record4);
		QueryResponseDTO batch3 = noResultsResponse;
		ArgumentCaptor<SolrParams> solrParams = ArgumentCaptor.forClass(SolrParams.class);
		when(recordDao.query(anyString(), solrParams.capture())).thenReturn(batch1).thenReturn(batch2).thenReturn(batch3);

		while (iterator.hasNext()) {
			iterator.next();
		}

		assertThat(solrParams.getAllValues()).hasSize(3);
		assertThat(solrParams.getAllValues().get(0).get("q")).isEqualTo("zeQ");
		assertThat(solrParams.getAllValues().get(0).getParams("fq")).isEqualTo(new String[]{"zeFQ"});
		assertThat(solrParams.getAllValues().get(0).get("rows")).isEqualTo("3");
		assertThat(solrParams.getAllValues().get(0).get("sort")).isEqualTo("id asc");

		assertThat(solrParams.getAllValues().get(1).get("q")).isEqualTo("zeQ");
		assertThat(solrParams.getAllValues().get(1).getParams("fq")).isEqualTo(new String[]{"zeFQ", "id:{3 TO *}"});
		assertThat(solrParams.getAllValues().get(1).get("rows")).isEqualTo("3");
		assertThat(solrParams.getAllValues().get(1).get("sort")).isEqualTo("id asc");

		assertThat(solrParams.getAllValues().get(2).get("q")).isEqualTo("zeQ");
		assertThat(solrParams.getAllValues().get(2).getParams("fq")).isEqualTo(new String[]{"zeFQ", "id:{4 TO *}"});
		assertThat(solrParams.getAllValues().get(2).get("rows")).isEqualTo("3");
		assertThat(solrParams.getAllValues().get(2).get("sort")).isEqualTo("id asc");

	}

	@Test(expected = LazyIteratorRuntimeException_IncorrectUsage.class)
	public void givenNoElementWhenNextCalledThenException()
			throws Exception {

		ArgumentCaptor<SolrParams> solrParams = ArgumentCaptor.forClass(SolrParams.class);

		when(recordDao.query(anyString(), solrParams.capture())).thenReturn(noResultsResponse);

		iterator.next();

	}

	@Test()
	public void givenOneElementWhenNextCalledTwiceThenException()
			throws Exception {

		ArgumentCaptor<SolrParams> solrParams = ArgumentCaptor.forClass(SolrParams.class);
		QueryResponseDTO batch1 = responseWithElements(record1);
		when(recordDao.query(anyString(), solrParams.capture())).thenReturn(batch1);

		iterator.next();
		try {
			iterator.next();
			fail("IncorrectUsage_LazyResultsIteratorRuntimeException expected");
		} catch (LazyIteratorRuntimeException_IncorrectUsage e) {
			//OK
		}

	}

	@Test(expected = LazyIteratorRuntimeException_RemoveNotAvailable.class)
	public void whenRemoveCallThenThrowUnsupportedOperationException()
			throws Exception {

		iterator.remove();

	}

	private QueryResponseDTO responseWithElements(RecordDTO... records) {
		QueryResponseDTO response = mock(QueryResponseDTO.class);
		when(response.getNumFound()).thenReturn(0L);
		when(response.getResults()).thenReturn(Arrays.asList(records));
		return response;
	}

}
