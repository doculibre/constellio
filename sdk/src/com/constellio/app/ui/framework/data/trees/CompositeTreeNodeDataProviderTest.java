package com.constellio.app.ui.framework.data.trees;

import com.constellio.app.ui.framework.data.TreeNode;
import com.constellio.app.ui.framework.data.trees.CompositeTreeNodeDataProvider.CompositeTreeNodeDataProviderFastContinuationInfos;
import com.constellio.app.ui.framework.data.trees.TreeNodesProvider.TreeNodesProviderResponse;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompositeTreeNodeDataProviderTest extends ConstellioTest {

	@Mock TreeNode provider1Node1, provider1Node2, provider1Node3, provider1Node4, provider1Node5, provider1Node6;
	@Mock TreeNode provider2Node1, provider2Node2, provider2Node3, provider2Node4, provider2Node5;
	@Mock TreeNode provider3Node1, provider3Node2, provider3Node3, provider3Node4, provider3Node5, provider3Node6, provider3Node7;

	@Mock TreeNode provider4MouhahahahaNode;

	@Mock TreeNodesProvider provider1, provider2, exceptionProvider, provider3, doomProvider;


	@Test
	public void whenGetNodesThenLazyConsumeEachChildNodeProviders() {

		CompositeTreeNodeDataProvider dataProvider = (CompositeTreeNodeDataProvider) CompositeTreeNodeDataProvider.forNodesProvider(
				asList(provider1, provider2, exceptionProvider, provider3, doomProvider));

		when(provider1.getNodes(eq(null), eq(0), eq(3), any())).thenReturn(
				new TreeNodesProviderResponse(true, asList(provider1Node1, provider1Node2, provider1Node3), "node4IsZeNext!"));

		when(provider1.getNodes(eq(null), eq(3), eq(3), any())).thenReturn(
				new TreeNodesProviderResponse(false, asList(provider1Node4, provider1Node5, provider1Node6), "pleaseStop!"));

		when(provider2.getNodes(eq(null), eq(0), eq(0), any())).thenReturn(
				new TreeNodesProviderResponse(true, new ArrayList<>(), "yessirThereIsSomeNodesBuIGiveYouNoOne"));

		when(provider2.getNodes(eq(null), eq(0), eq(3), any())).thenReturn(
				new TreeNodesProviderResponse(true, asList(provider2Node1, provider2Node2, provider2Node3), "plentyOfNodesHere"));

		when(provider2.getNodes(eq(null), eq(3), eq(3), any())).thenReturn(
				new TreeNodesProviderResponse(false, asList(provider2Node4, provider2Node5), "noMoreNodesTryAnotherProvider"));

		when(provider3.getNodes(eq(null), eq(0), eq(1), any())).thenReturn(
				new TreeNodesProviderResponse(true, asList(provider3Node1), "ContinueWithTheSecondNode"));

		when(provider3.getNodes(eq(null), eq(1), eq(3), any())).thenReturn(
				new TreeNodesProviderResponse(true, asList(provider3Node2, provider3Node3, provider3Node4), "stillALotOfNodes"));

		when(provider3.getNodes(eq(null), eq(4), eq(3), any())).thenReturn(
				new TreeNodesProviderResponse(false, asList(provider3Node5, provider3Node6, provider3Node7), "itsTheEnd"));


		when(provider1.areNodesPossibleIn(null)).thenReturn(true);
		when(provider2.areNodesPossibleIn(null)).thenReturn(true);
		when(provider3.areNodesPossibleIn(null)).thenReturn(true);

		//This provider tells you that he return no root nodes, but if you still call it, the mouhahahaha node is returned
		when(doomProvider.areNodesPossibleIn(null)).thenReturn(false);
		when(doomProvider.getNodes(eq(null), anyInt(), anyInt(), any())).thenReturn(
				new TreeNodesProviderResponse(false, asList(provider4MouhahahahaNode), "yourDoomHasArrived"));

		//This provider tells you that he return root nodes, but throw an exception
		when(exceptionProvider.areNodesPossibleIn(null)).thenReturn(true);
		when(exceptionProvider.getNodes(eq(null), anyInt(), anyInt(), any())).thenThrow(
				new RuntimeException("Your doom has arrived, Mouhahahaha!"));

		List<TreeNodesProviderResponse> responses = new ArrayList<>();
		CompositeTreeNodeDataProviderFastContinuationInfos infos = null;
		int index = 0;
		for (int i = 0; i < 6; i++) {
			TreeNodesProviderResponse<CompositeTreeNodeDataProviderFastContinuationInfos> response = dataProvider.getNodes(null, index, 3, infos);
			infos = response.getFastContinuationInfos();
			responses.add(response);
			index += 3;
			System.out.println(">" + infos.continueAtProvider + "-" + infos.continueAtProviderIndex + "-" + infos.currentFastContinuationInfos);
		}

		assertThat(responses.get(0)).isEqualToComparingFieldByField(new TreeNodesProviderResponse<>(
				true,
				asList(provider1Node1, provider1Node2, provider1Node3),
				new CompositeTreeNodeDataProviderFastContinuationInfos(0, 3, "node4IsZeNext!")));

		assertThat(responses.get(1)).isEqualToComparingFieldByField(new TreeNodesProviderResponse<>(
				true,
				asList(provider1Node4, provider1Node5, provider1Node6),
				new CompositeTreeNodeDataProviderFastContinuationInfos(1, 0, null)));

		assertThat(responses.get(2)).isEqualToComparingFieldByField(new TreeNodesProviderResponse<>(
				true,
				asList(provider2Node1, provider2Node2, provider2Node3),
				new CompositeTreeNodeDataProviderFastContinuationInfos(1, 3, "plentyOfNodesHere")));

		assertThat(responses.get(3)).isEqualToComparingFieldByField(new TreeNodesProviderResponse<>(
				true,
				asList(provider2Node4, provider2Node5, provider3Node1),
				new CompositeTreeNodeDataProviderFastContinuationInfos(3, 1, "ContinueWithTheSecondNode")));

		assertThat(responses.get(4)).isEqualToComparingFieldByField(new TreeNodesProviderResponse<>(
				true,
				asList(provider3Node2, provider3Node3, provider3Node4),
				new CompositeTreeNodeDataProviderFastContinuationInfos(3, 4, "stillALotOfNodes")));

		assertThat(responses.get(5)).isEqualToComparingFieldByField(new TreeNodesProviderResponse<>(
				false,
				asList(provider3Node5, provider3Node6, provider3Node7),
				new CompositeTreeNodeDataProviderFastContinuationInfos(5, 0, null)));


		InOrder inOrder = inOrder(provider1, provider2, provider3);
		inOrder.verify(provider1).getNodes(null, 0, 3, null);
		inOrder.verify(provider1).getNodes(null, 3, 3, "node4IsZeNext!");
		inOrder.verify(provider2).getNodes(null, 0, 0, null);
		inOrder.verify(provider2).getNodes(null, 0, 3, null);
		inOrder.verify(provider2).getNodes(null, 3, 3, "plentyOfNodesHere");
		inOrder.verify(provider3).getNodes(null, 0, 1, null);
		inOrder.verify(provider3).getNodes(null, 1, 3, "ContinueWithTheSecondNode");
		inOrder.verify(provider3).getNodes(null, 4, 3, "stillALotOfNodes");

		inOrder.verifyNoMoreInteractions();

		verify(doomProvider, never()).getNodes(eq(null), anyInt(), anyInt(), anyObject());
	}
}
