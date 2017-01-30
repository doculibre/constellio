package com.constellio.app.ui.framework.data.trees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.constellio.app.ui.framework.data.ObjectsResponse;
import com.constellio.app.ui.framework.data.RecordLookupTreeDataProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.sdk.tests.ConstellioTest;
import com.vaadin.server.Resource;

public class RecordTreeDataProviderTest extends ConstellioTest {

	@Mock Resource collapsedIcon;
	@Mock Resource expandedIcon;
	String zeTaxonomy = "zeTaxonomy";
	String zeSchemaType = "zeSchemaType";
	@Mock RecordTreeNodesDataProvider treeNodesDataProvider;
	RecordLookupTreeDataProvider dataProvider;

	@Before
	public void setUp()
			throws Exception {
		when(treeNodesDataProvider.getTaxonomyCode()).thenReturn(zeTaxonomy);
		dataProvider = spy(new RecordLookupTreeDataProvider(zeSchemaType, true, treeNodesDataProvider));

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				return "Caption of '" + ((Record) invocation.getArguments()[0]).getId() + "'";
			}
		}).when(dataProvider).getCaptionOf(any(Record.class));

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				return expandedIcon;
			}
		}).when(dataProvider).getExpandedIconOf(any(Record.class));

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				return collapsedIcon;
			}
		}).when(dataProvider).getCollapsedIconOf(any(Record.class));
	}

	@Test
	public void given19ResultsWhenGetRootConceptsThenAllInformationsAvailableForFurtherCalls()
			throws Exception {

		LinkableTaxonomySearchResponse nodeResponse = new LinkableTaxonomySearchResponse(19, testRecords(0, 19));
		when(treeNodesDataProvider.getRootNodes(0, 20)).thenReturn(nodeResponse);

		ObjectsResponse response = dataProvider.getRootObjects(0, 20);
		assertThat(response.getCount()).isEqualTo(19);
		assertThat(response.getObjects()).containsOnly(ids(0, 19));

		assertThat(dataProvider.getCaption(id(0))).isEqualTo("Caption of 'record0'");
		assertThat(dataProvider.getCaption(id(3))).isEqualTo("Caption of 'record3'");
		assertThat(dataProvider.getIcon(id(0), true)).isSameAs(expandedIcon);
		assertThat(dataProvider.getIcon(id(0), false)).isSameAs(collapsedIcon);
		assertThat(dataProvider.getIcon(id(3), true)).isSameAs(expandedIcon);
		assertThat(dataProvider.getIcon(id(3), false)).isSameAs(collapsedIcon);
		assertThat(dataProvider.getEstimatedRootNodesCount()).isEqualTo(19);
		assertThat(dataProvider.getEstimatedChildrenNodesCount(id(0))).isEqualTo(-1);

		verify(treeNodesDataProvider).getRootNodes(0, 20);
		verifyNoMoreInteractions(treeNodesDataProvider);

	}

	@Test
	public void given19ResultsWhenGetChildConceptsThenAllInformationsAvailableForFurtherCalls()
			throws Exception {

		LinkableTaxonomySearchResponse nodeResponse = new LinkableTaxonomySearchResponse(19, testRecords(0, 19));
		when(treeNodesDataProvider.getRootNodes(0, 20)).thenReturn(nodeResponse);

		ObjectsResponse response = dataProvider.getRootObjects(0, 20);
		assertThat(response.getCount()).isEqualTo(19);
		assertThat(response.getObjects()).containsOnly(ids(0, 19));

		assertThat(dataProvider.getCaption(id(0))).isEqualTo("Caption of 'record0'");
		assertThat(dataProvider.getCaption(id(3))).isEqualTo("Caption of 'record3'");
		assertThat(dataProvider.getIcon(id(0), true)).isSameAs(expandedIcon);
		assertThat(dataProvider.getIcon(id(0), false)).isSameAs(collapsedIcon);
		assertThat(dataProvider.getIcon(id(3), true)).isSameAs(expandedIcon);
		assertThat(dataProvider.getIcon(id(3), false)).isSameAs(collapsedIcon);
		assertThat(dataProvider.getEstimatedRootNodesCount()).isEqualTo(19);
		assertThat(dataProvider.getEstimatedChildrenNodesCount(id(0))).isEqualTo(-1);

		verify(treeNodesDataProvider).getRootNodes(0, 20);
		verifyNoMoreInteractions(treeNodesDataProvider);

	}

	@Test
	public void whenGetDataProviderTaxonomyCodeThenReturnNodesProvidersTaxonomy()
			throws Exception {

		assertThat(dataProvider.getTaxonomyCode()).isEqualTo(zeTaxonomy);
	}

	private String id(int i) {
		return "record" + i;
	}

	private String[] ids(int from, int to) {
		List<String> records = new ArrayList<>();

		for (int i = from; i < to; i++) {
			records.add("record" + i);
		}

		return records.toArray(new String[0]);
	}

	private List<TaxonomySearchRecord> testRecords(int from, int to) {

		List<TaxonomySearchRecord> records = new ArrayList<>();

		for (int i = from; i < to; i++) {
			Record record = mock(Record.class);
			when(record.getId()).thenReturn("record" + i);
			when(record.getSchemaCode()).thenReturn("aType_default");
			when(record.getTitle()).thenReturn("Ze record " + i);
			records.add(new TaxonomySearchRecord(record, true, true));
		}

		return records;
	}

}
