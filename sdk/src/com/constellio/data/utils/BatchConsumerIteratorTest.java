package com.constellio.data.utils;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.data.utils.LangUtils.stream;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class BatchConsumerIteratorTest extends ConstellioTest {

	@Test
	public void whenIteratingBatchIteratorThenOk() {

		List<Integer> batch1 = asList(1, 3, 4);
		List<Integer> batch2 = asList(2, 4, 4);
		List<Integer> batch3 = new ArrayList<>();
		List<Integer> batch4 = asList(7);

		List<Integer> consumed = stream(new BatchConsumerIterator<>(asList(batch1, batch2, batch3, batch4).iterator()))
				.collect(Collectors.toList());

		assertThat(consumed).isEqualTo(asList(1, 3, 4, 2, 4, 4, 7));
	}

}
