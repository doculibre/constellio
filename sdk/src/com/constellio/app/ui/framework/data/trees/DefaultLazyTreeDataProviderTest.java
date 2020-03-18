package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.ui.framework.data.ObjectsResponse;
import com.constellio.app.ui.framework.data.TreeNode;
import com.constellio.app.ui.framework.data.trees.TreeNodesProvider.TreeNodesProviderResponse;
import com.constellio.sdk.tests.ConstellioTest;
import com.vaadin.server.Resource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DefaultLazyTreeDataProviderTest extends ConstellioTest {

	TreeNode node1, node2, node3, node4, node5, node6, node7, node8;
	@Mock Resource collapsedIcon1, expandedIcon1;
	@Mock Resource collapsedIcon2, expandedIcon2;
	@Mock Resource collapsedIcon3, expandedIcon3;
	@Mock Resource collapsedIcon4, expandedIcon4;
	@Mock Resource collapsedIcon5, expandedIcon5;
	@Mock Resource collapsedIcon6, expandedIcon6;
	@Mock Resource collapsedIcon7, expandedIcon7;
	@Mock Resource collapsedIcon8, expandedIcon8;


	@Mock TreeNodesProvider<String> nodesProvider;
	DefaultLazyTreeDataProvider dataProvider;

	@Before
	public void setUp() throws Exception {
		node1 = new TreeNode("id1", "type1", "caption1", "description1", collapsedIcon1, expandedIcon1, true);
		node2 = new TreeNode("id2", "type2", "caption2", "description2", collapsedIcon2, expandedIcon2, false);
		node3 = new TreeNode("id3", "type3", "caption3", "description3", collapsedIcon3, expandedIcon3, true);
		node4 = new TreeNode("id4", "type4", "caption4", "description4", collapsedIcon4, expandedIcon4, false);
		node5 = new TreeNode("id5", "type5", "caption5", "description5", collapsedIcon5, expandedIcon5, true);
		node6 = new TreeNode("id6", "type6", "caption6", "description6", collapsedIcon6, expandedIcon6, false);
		node7 = new TreeNode("id7", "type7", "caption7", "description7", collapsedIcon7, expandedIcon7, true);
		node8 = new TreeNode("id8", "type8", "caption8", "description8", collapsedIcon8, expandedIcon8, true);

		dataProvider = new DefaultLazyTreeDataProvider(nodesProvider, "zeTaxonomy");

		assertThat(dataProvider.getCaption("id1")).isEqualTo("");
		assertThat(dataProvider.getDescription("id1")).isNull();
		assertThat(dataProvider.getEstimatedChildrenNodesCount("id1")).isEqualTo(-1);
		assertThat(dataProvider.getParent("id1")).isNull();
		assertThat(dataProvider.getIcon("id1", true)).isNull();
		assertThat(dataProvider.getIcon("id1", false)).isNull();
	}

	@Test
	public void whenIteratingRootNodesThenConsumeNodeProviderOnlyOnceReturningItemsInSameOrder() {
		when(nodesProvider.getNodes(eq(null), eq(0), eq(3), any())).thenReturn(
				new TreeNodesProviderResponse<>(true, asList(node1, node2, node3), "continueAtNode4!"));

		when(nodesProvider.getNodes(eq(null), eq(3), eq(3), any())).thenReturn(
				new TreeNodesProviderResponse<>(true, asList(node4, node5, node6), "continueAtNode7!"));

		when(nodesProvider.getNodes(eq(null), eq(6), eq(3), any())).thenReturn(
				new TreeNodesProviderResponse<>(false, asList(node7, node8), "youShallNotContinue!"));

		assertThat(dataProvider.getRootObjects(0, 3)).isEqualTo(
				new ObjectsResponse<String>(asList("id1", "id2", "id3"), 4L));
		verify(nodesProvider).getNodes(null, 0, 3, null);

		assertThat(dataProvider.getRootObjects(3, 3)).isEqualTo(
				new ObjectsResponse<String>(asList("id4", "id5", "id6"), 7L));
		verify(nodesProvider).getNodes(null, 3, 3, "continueAtNode4!");

		assertThat(dataProvider.getRootObjects(6, 3)).isEqualTo(
				new ObjectsResponse<String>(asList("id7", "id8"), 8L));
		verify(nodesProvider).getNodes(null, 6, 3, "continueAtNode7!");
		verifyNoMoreInteractions(nodesProvider);

		assertThat(dataProvider.getCaption("id1")).isEqualTo("caption1");
		assertThat(dataProvider.getDescription("id1")).isEqualTo("description1");
		assertThat(dataProvider.getEstimatedChildrenNodesCount("id1")).isEqualTo(-1);
		assertThat(dataProvider.getParent("id1")).isNull();
		assertThat(dataProvider.getIcon("id1", true)).isEqualTo(expandedIcon1);
		assertThat(dataProvider.getIcon("id1", false)).isEqualTo(collapsedIcon1);

		assertThat(dataProvider.getCaption("id2")).isEqualTo("caption2");
		assertThat(dataProvider.getDescription("id2")).isEqualTo("description2");
		assertThat(dataProvider.getEstimatedChildrenNodesCount("id2")).isEqualTo(-1);
		assertThat(dataProvider.getParent("id2")).isNull();
		assertThat(dataProvider.getIcon("id2", true)).isEqualTo(expandedIcon2);
		assertThat(dataProvider.getIcon("id2", false)).isEqualTo(collapsedIcon2);

		assertThat(dataProvider.getCaption("id7")).isEqualTo("caption7");
		assertThat(dataProvider.getDescription("id7")).isEqualTo("description7");
		assertThat(dataProvider.getEstimatedChildrenNodesCount("id7")).isEqualTo(-1);
		assertThat(dataProvider.getParent("id7")).isNull();
		assertThat(dataProvider.getIcon("id7", true)).isEqualTo(expandedIcon7);
		assertThat(dataProvider.getIcon("id7", false)).isEqualTo(collapsedIcon7);


	}

	@Test
	public void whenIteratingChildNodesThenConsumeNodeProviderOnlyOnceReturningItemsInSameOrder() {
		when(nodesProvider.getNodes(eq("zeParent"), eq(0), eq(3), any())).thenReturn(
				new TreeNodesProviderResponse<>(true, asList(node1, node2, node3), "continueAtNode4!"));

		when(nodesProvider.getNodes(eq("zeParent"), eq(3), eq(3), any())).thenReturn(
				new TreeNodesProviderResponse<>(true, asList(node4, node5, node6), "continueAtNode7!"));

		when(nodesProvider.getNodes(eq("zeParent"), eq(6), eq(3), any())).thenReturn(
				new TreeNodesProviderResponse<>(false, asList(node7, node8), "youShallNotContinue!"));

		assertThat(dataProvider.getChildren("zeParent", 0, 3)).isEqualTo(
				new ObjectsResponse<String>(asList("zeParent|id1", "zeParent|id2", "zeParent|id3"), 4L));
		verify(nodesProvider).getNodes("zeParent", 0, 3, null);

		assertThat(dataProvider.getChildren("zeParent", 3, 3)).isEqualTo(
				new ObjectsResponse<String>(asList("zeParent|id4", "zeParent|id5", "zeParent|id6"), 7L));
		verify(nodesProvider).getNodes("zeParent", 3, 3, "continueAtNode4!");

		assertThat(dataProvider.getChildren("zeParent", 6, 3)).isEqualTo(
				new ObjectsResponse<String>(asList("zeParent|id7", "zeParent|id8"), 8L));
		verify(nodesProvider).getNodes("zeParent", 6, 3, "continueAtNode7!");
		verifyNoMoreInteractions(nodesProvider);

		assertThat(dataProvider.getCaption("zeParent|id1")).isEqualTo("caption1");
		assertThat(dataProvider.getDescription("zeParent|id1")).isEqualTo("description1");
		assertThat(dataProvider.getEstimatedChildrenNodesCount("zeParent|id1")).isEqualTo(-1);
		assertThat(dataProvider.getParent("zeParent|id1")).isEqualTo("zeParent");
		assertThat(dataProvider.getIcon("zeParent|id1", true)).isEqualTo(expandedIcon1);
		assertThat(dataProvider.getIcon("zeParent|id1", false)).isEqualTo(collapsedIcon1);

		assertThat(dataProvider.getCaption("zeParent|id2")).isEqualTo("caption2");
		assertThat(dataProvider.getDescription("zeParent|id2")).isEqualTo("description2");
		assertThat(dataProvider.getEstimatedChildrenNodesCount("zeParent|id2")).isEqualTo(-1);
		assertThat(dataProvider.getParent("zeParent|id2")).isEqualTo("zeParent");
		assertThat(dataProvider.getIcon("zeParent|id2", true)).isEqualTo(expandedIcon2);
		assertThat(dataProvider.getIcon("zeParent|id2", false)).isEqualTo(collapsedIcon2);

		assertThat(dataProvider.getCaption("zeParent|id7")).isEqualTo("caption7");
		assertThat(dataProvider.getDescription("zeParent|id7")).isEqualTo("description7");
		assertThat(dataProvider.getEstimatedChildrenNodesCount("zeParent|id7")).isEqualTo(-1);
		assertThat(dataProvider.getParent("zeParent|id7")).isEqualTo("zeParent");
		assertThat(dataProvider.getIcon("zeParent|id7", true)).isEqualTo(expandedIcon7);
		assertThat(dataProvider.getIcon("zeParent|id7", false)).isEqualTo(collapsedIcon7);


	}

}
