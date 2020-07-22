package com.constellio.app.ui.framework.data;

import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.taxonomies.FastContinueInfos;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.sdk.tests.ConstellioTest;
import com.vaadin.server.Resource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.records.Record.GetMetadataOption.NO_SUMMARY_METADATA_VALIDATION;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class RecordLookupTreeDataProviderTest extends ConstellioTest {

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
		when(treeNodesDataProvider.getRootNodes(0, 20, null)).thenReturn(nodeResponse);

		ObjectsResponse response = dataProvider.getRootObjects(0, 20);
		assertThat(response.getCount()).isEqualTo(19);
		assertThat(response.getObjects()).containsOnly(ids(0, 19));

		assertThat(dataProvider.getCaption(id(0))).isEqualTo("Caption of 'record0'");
		assertThat(dataProvider.getCaption(id(3))).isEqualTo("Caption of 'record3'");
		assertThat(dataProvider.getParent(id(0))).isNull();
		assertThat(dataProvider.getParent(id(3))).isNull();
		assertThat(dataProvider.getIcon(id(0), true)).isSameAs(expandedIcon);
		assertThat(dataProvider.getIcon(id(0), false)).isSameAs(collapsedIcon);
		assertThat(dataProvider.getIcon(id(3), true)).isSameAs(expandedIcon);
		assertThat(dataProvider.getIcon(id(3), false)).isSameAs(collapsedIcon);
		assertThat(dataProvider.getEstimatedRootNodesCount()).isEqualTo(19);
		assertThat(dataProvider.getEstimatedChildrenNodesCount(id(0))).isEqualTo(-1);

		verify(treeNodesDataProvider).getRootNodes(0, 20, null);
		verifyNoMoreInteractions(treeNodesDataProvider);

	}

	@Test
	public void given19ResultsWhenGetChildConceptsThenAllInformationsAvailableForFurtherCalls()
			throws Exception {

		LinkableTaxonomySearchResponse nodeResponse = new LinkableTaxonomySearchResponse(19, testRecords("parent1", "parent2"));
		when(treeNodesDataProvider.getRootNodes(0, 20, null)).thenReturn(nodeResponse);

		LinkableTaxonomySearchResponse parent1ChildrenResponse = new LinkableTaxonomySearchResponse(19, testRecords(0, 19));
		when(treeNodesDataProvider.getChildrenNodes("parent1", 0, 20, null)).thenReturn(parent1ChildrenResponse);

		LinkableTaxonomySearchResponse parent2ChildrenResponse = new LinkableTaxonomySearchResponse(21, testRecords(30, 50));
		when(treeNodesDataProvider.getChildrenNodes("parent2", 0, 20, null)).thenReturn(parent2ChildrenResponse);

		//Fetch root nodes
		ObjectsResponse response = dataProvider.getRootObjects(0, 20);

		response = dataProvider.getChildren("parent1", 0, 20);
		assertThat(response.getCount()).isEqualTo(19);
		assertThat(response.getObjects()).containsOnly(ids(0, 19));

		response = dataProvider.getChildren("parent2", 0, 20);
		assertThat(response.getCount()).isEqualTo(21);
		assertThat(response.getObjects()).containsOnly(ids(30, 50));

		assertThat(dataProvider.getCaption(id(0))).isEqualTo("Caption of 'record0'");
		assertThat(dataProvider.getCaption(id(3))).isEqualTo("Caption of 'record3'");
		assertThat(dataProvider.getParent(id(0))).isEqualTo("parent1");
		assertThat(dataProvider.getParent(id(3))).isEqualTo("parent1");
		assertThat(dataProvider.getIcon(id(0), true)).isSameAs(expandedIcon);
		assertThat(dataProvider.getIcon(id(0), false)).isSameAs(collapsedIcon);
		assertThat(dataProvider.getIcon(id(3), true)).isSameAs(expandedIcon);
		assertThat(dataProvider.getIcon(id(3), false)).isSameAs(collapsedIcon);

		assertThat(dataProvider.getCaption(id(35))).isEqualTo("Caption of 'record35'");
		assertThat(dataProvider.getCaption(id(42))).isEqualTo("Caption of 'record42'");
		assertThat(dataProvider.getParent(id(35))).isEqualTo("parent2");
		assertThat(dataProvider.getParent(id(42))).isEqualTo("parent2");
		assertThat(dataProvider.getIcon(id(35), true)).isSameAs(expandedIcon);
		assertThat(dataProvider.getIcon(id(35), false)).isSameAs(collapsedIcon);
		assertThat(dataProvider.getIcon(id(42), true)).isSameAs(expandedIcon);
		assertThat(dataProvider.getIcon(id(42), false)).isSameAs(collapsedIcon);

		assertThat(dataProvider.getEstimatedChildrenNodesCount("parent1")).isEqualTo(19);
		assertThat(dataProvider.getEstimatedChildrenNodesCount("parent2")).isEqualTo(21);

		verify(treeNodesDataProvider).getRootNodes(0, 20, null);
		verify(treeNodesDataProvider).getChildrenNodes("parent1", 0, 20, null);
		verify(treeNodesDataProvider).getChildrenNodes("parent2", 0, 20, null);
		verifyNoMoreInteractions(treeNodesDataProvider);

	}

	@Test
	public void givenNoDataRetrievedThenSomeInformationsNotAvailable()
			throws Exception {

		assertThat(dataProvider.getEstimatedRootNodesCount()).isEqualTo(-1);
		assertThat(dataProvider.getEstimatedChildrenNodesCount("parent3")).isEqualTo(-1);
		assertThat(dataProvider.getCaption(id(42))).isEqualTo("");
		assertThat(dataProvider.getDescription(id(42))).isNull();
		assertThat(dataProvider.getIcon(id(42), true)).isNull();
		assertThat(dataProvider.getIcon(id(42), false)).isNull();

	}

	@Test
	public void whenGetDataProviderTaxonomyCodeThenReturnNodesProvidersTaxonomy()
			throws Exception {

		assertThat(dataProvider.getTaxonomyCode()).isEqualTo(zeTaxonomy);
	}

	@Test
	public void whenReturnDataThenUseMultipleMetadatasAsNodeDescription()
			throws Exception {

		Record record1 = newRecord("record1");
		when(record1.get(Schemas.DESCRIPTION_STRING, NO_SUMMARY_METADATA_VALIDATION)).thenReturn("description 1");

		Record record2 = newRecord("record2");
		when(record2.get(Schemas.DESCRIPTION_TEXT, NO_SUMMARY_METADATA_VALIDATION)).thenReturn("text description 2");

		Record record3 = newRecord("record3");
		when(record3.get(Schemas.DESCRIPTION_TEXT, NO_SUMMARY_METADATA_VALIDATION)).thenReturn("text description 3");
		when(record3.get(Schemas.DESCRIPTION_STRING, NO_SUMMARY_METADATA_VALIDATION)).thenReturn("description 3");

		Record record4 = newRecord("record4");

		List<TaxonomySearchRecord> searchRecords = new ArrayList<>();
		searchRecords.add(new TaxonomySearchRecord(record1, true, true));
		searchRecords.add(new TaxonomySearchRecord(record2, true, true));
		searchRecords.add(new TaxonomySearchRecord(record3, true, true));
		searchRecords.add(new TaxonomySearchRecord(record4, true, true));

		LinkableTaxonomySearchResponse nodeResponse = new LinkableTaxonomySearchResponse(4, searchRecords);
		when(treeNodesDataProvider.getRootNodes(0, 20, null)).thenReturn(nodeResponse);

		assertThat(dataProvider.getRootObjects(0, 20));
		assertThat(dataProvider.getDescription("record1")).isEqualTo("description 1");
		assertThat(dataProvider.getDescription("record2")).isEqualTo("text description 2");
		assertThat(dataProvider.getDescription("record3")).isEqualTo("description 3");
		assertThat(dataProvider.getDescription("record4")).isNull();
	}

	@Test
	public void givenExpandedRootNodesThenMaximumUpdated()
			throws Exception {
		LinkableTaxonomySearchResponse nodeResponse1 = new LinkableTaxonomySearchResponse(25, testRecords(0, 20));
		when(treeNodesDataProvider.getRootNodes(0, 20, null)).thenReturn(nodeResponse1);

		LinkableTaxonomySearchResponse nodeResponse2 = new LinkableTaxonomySearchResponse(27, testRecords(20, 27));
		when(treeNodesDataProvider.getRootNodes(20, 27, null)).thenReturn(nodeResponse2);

		ObjectsResponse response = dataProvider.getRootObjects(0, 20);
		assertThat(dataProvider.getEstimatedRootNodesCount()).isEqualTo(25);

		response = dataProvider.getRootObjects(20, 27);
		assertThat(dataProvider.getEstimatedRootNodesCount()).isEqualTo(27);
	}

	@Test
	public void givenExpandedChildNodesThenMaximumUpdated()
			throws Exception {
		LinkableTaxonomySearchResponse nodeResponse1 = new LinkableTaxonomySearchResponse(1, testRecords("parent1"));
		when(treeNodesDataProvider.getRootNodes(0, 20, null)).thenReturn(nodeResponse1);

		LinkableTaxonomySearchResponse parent1ChildrenResponse1 = new LinkableTaxonomySearchResponse(24, testRecords(0, 20));
		when(treeNodesDataProvider.getChildrenNodes("parent1", 0, 20, null)).thenReturn(parent1ChildrenResponse1);

		LinkableTaxonomySearchResponse parent1ChildrenResponse2 = new LinkableTaxonomySearchResponse(45, testRecords(20, 40));
		when(treeNodesDataProvider.getChildrenNodes("parent1", 20, 40, null)).thenReturn(parent1ChildrenResponse2);

		LinkableTaxonomySearchResponse parent1ChildrenResponse3 = new LinkableTaxonomySearchResponse(46, testRecords(40, 46));
		when(treeNodesDataProvider.getChildrenNodes("parent1", 40, 60, null)).thenReturn(parent1ChildrenResponse3);

		ObjectsResponse response = dataProvider.getRootObjects(0, 20);
		assertThat(dataProvider.getEstimatedChildrenNodesCount("parent1")).isEqualTo(-1);

		response = dataProvider.getChildren("parent1", 0, 20);
		assertThat(dataProvider.getEstimatedChildrenNodesCount("parent1")).isEqualTo(24);

		response = dataProvider.getChildren("parent1", 20, 40);
		assertThat(dataProvider.getEstimatedChildrenNodesCount("parent1")).isEqualTo(45);

		response = dataProvider.getChildren("parent1", 40, 60);
		assertThat(dataProvider.getEstimatedChildrenNodesCount("parent1")).isEqualTo(46);
	}

	@Test
	public void givenExpandedChildNodesReturnFastContinuationInfo()
			throws Exception {

		LinkableTaxonomySearchResponse nodeResponse1, nodeResponse2, nodeResponse3, parent1ChildrenResponse1,
				parent1ChildrenResponse2, parent1ChildrenResponse3;

		FastContinueInfos firstRootCallContinuationInfos = mock(FastContinueInfos.class, "firstRootCallContinuationInfos");

		nodeResponse1 = new LinkableTaxonomySearchResponse(1, firstRootCallContinuationInfos, testRecords("parent1"));
		when(treeNodesDataProvider.getRootNodes(0, 1, null)).thenReturn(nodeResponse1);

		FastContinueInfos secondRootCallContinuationInfos = mock(FastContinueInfos.class, "secondRootCallContinuationInfos");
		nodeResponse2 = new LinkableTaxonomySearchResponse(1, secondRootCallContinuationInfos, testRecords("parent2"));
		when(treeNodesDataProvider.getRootNodes(1, 1, firstRootCallContinuationInfos)).thenReturn(nodeResponse2);

		FastContinueInfos thirdRootCallContinuationInfos = mock(FastContinueInfos.class, "thirdRootCallContinuationInfos");
		nodeResponse3 = new LinkableTaxonomySearchResponse(1, thirdRootCallContinuationInfos, testRecords("parent3"));
		when(treeNodesDataProvider.getRootNodes(2, 1, secondRootCallContinuationInfos)).thenReturn(nodeResponse3);

		FastContinueInfos firstCallContinuationInfos = mock(FastContinueInfos.class, "firstCallContinuationInfos");
		parent1ChildrenResponse1 = new LinkableTaxonomySearchResponse(24, firstCallContinuationInfos, testRecords(0, 20));
		when(treeNodesDataProvider.getChildrenNodes("parent1", 0, 20, null))
				.thenReturn(parent1ChildrenResponse1);

		FastContinueInfos secondCallContinuationInfos = mock(FastContinueInfos.class, "secondCallContinuationInfos");
		parent1ChildrenResponse2 = new LinkableTaxonomySearchResponse(45, secondCallContinuationInfos, testRecords(20, 40));
		when(treeNodesDataProvider.getChildrenNodes("parent1", 20, 20, firstCallContinuationInfos))
				.thenReturn(parent1ChildrenResponse2);

		FastContinueInfos thirdCallContinuationInfos = mock(FastContinueInfos.class, "thirdCallContinuationInfos");
		parent1ChildrenResponse3 = new LinkableTaxonomySearchResponse(46, thirdCallContinuationInfos, testRecords(40, 46));
		when(treeNodesDataProvider.getChildrenNodes("parent1", 40, 20, secondCallContinuationInfos))
				.thenReturn(parent1ChildrenResponse3);

		assertThat(dataProvider.getRootObjects(0, 1).getObjects()).isEqualTo(asList("parent1"));
		assertThat(dataProvider.getRootObjects(1, 1).getObjects()).isEqualTo(asList("parent2"));
		assertThat(dataProvider.getRootObjects(2, 1).getObjects()).isEqualTo(asList("parent3"));

		assertThat(dataProvider.getChildren("parent1", 0, 20).getObjects()).isEqualTo(asList(ids(0, 20)));
		assertThat(dataProvider.getChildren("parent1", 20, 20).getObjects()).isEqualTo(asList(ids(20, 40)));
		assertThat(dataProvider.getChildren("parent1", 40, 20).getObjects()).isEqualTo(asList(ids(40, 46)));

	}

	@Test
	public void givenLinkabilityIgnoredThenAllRecordsAllLinkable()
			throws Exception {

		List<TaxonomySearchRecord> searchRecords = new ArrayList<>();
		searchRecords.add(new TaxonomySearchRecord(newRecord("record1"), true, true));
		searchRecords.add(new TaxonomySearchRecord(newRecord("record2"), true, false));
		searchRecords.add(new TaxonomySearchRecord(newRecord("record3"), false, true));
		searchRecords.add(new TaxonomySearchRecord(newRecord("record4"), false, false));

		LinkableTaxonomySearchResponse nodeResponse = new LinkableTaxonomySearchResponse(4, searchRecords);
		when(treeNodesDataProvider.getRootNodes(0, 20, null)).thenReturn(nodeResponse);

		assertThat(dataProvider.getRootObjects(0, 20));
		assertThat(dataProvider.isSelectable("record1")).isTrue();
		assertThat(dataProvider.isLeaf("record1")).isFalse();
		assertThat(dataProvider.isSelectable("record2")).isTrue();
		assertThat(dataProvider.isLeaf("record2")).isTrue();
		assertThat(dataProvider.isSelectable("record3")).isFalse();
		assertThat(dataProvider.isLeaf("record3")).isFalse();
		assertThat(dataProvider.isSelectable("record4")).isFalse();
		assertThat(dataProvider.isLeaf("record4")).isTrue();

		dataProvider.setIgnoreLinkability(true);
		assertThat(dataProvider.getRootObjects(0, 20));
		assertThat(dataProvider.isSelectable("record1")).isTrue();
		assertThat(dataProvider.isLeaf("record1")).isFalse();
		assertThat(dataProvider.isSelectable("record2")).isTrue();
		assertThat(dataProvider.isLeaf("record2")).isTrue();
		assertThat(dataProvider.isSelectable("record3")).isTrue();
		assertThat(dataProvider.isLeaf("record3")).isFalse();
		assertThat(dataProvider.isSelectable("record4")).isTrue();
		assertThat(dataProvider.isLeaf("record4")).isTrue();
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
		return testRecords(ids(from, to));
	}

	private List<TaxonomySearchRecord> testRecords(String... ids) {

		List<TaxonomySearchRecord> records = new ArrayList<>();

		for (String id : ids) {
			Record record = newRecord(id);
			records.add(new TaxonomySearchRecord(record, true, true));
		}

		return records;
	}

	private Record newRecord(String id) {
		Record record = mock(Record.class);
		when(record.getId()).thenReturn(id);
		when(record.getSchemaCode()).thenReturn("aType_default");
		when(record.getTitle()).thenReturn("Ze " + id);
		return record;
	}

}
