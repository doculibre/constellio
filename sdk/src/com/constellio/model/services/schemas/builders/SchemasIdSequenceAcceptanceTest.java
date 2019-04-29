package com.constellio.model.services.schemas.builders;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemasIdSequenceAcceptanceTest extends ConstellioTest {

	SchemasIdSequence idSequence = new SchemasIdSequence();

	@Test
	public void givenOnUnusedDigitsInFirst2000ThenReused() {
		idSequence.markAsAssigned((short) 1);
		idSequence.markAsAssigned((short) 2);
		idSequence.markAsAssigned((short) 4);
		idSequence.markAsAssigned((short) 5);
		idSequence.markAsAssigned((short) 8);
		idSequence.markAsAssigned((short) 7);

		assertThat(idSequence.getNewId()).isEqualTo((short) 3);
		assertThat(idSequence.getNewId()).isEqualTo((short) 6);
		assertThat(idSequence.getNewId()).isEqualTo((short) 9);
		assertThat(idSequence.getNewId()).isEqualTo((short) 10);

	}

	@Test
	public void whenGettingNewIdsThenAllUniques() {
		for (int i = 0; i < Short.MAX_VALUE - 5; i++) {
			assertThat(idSequence.getNewId()).isEqualTo((short) (i + 1));
		}
		assertThat(idSequence.getNewId()).isEqualTo((short) 32763);
		assertThat(idSequence.getNewId()).isEqualTo((short) 32764);
		assertThat(idSequence.getNewId()).isEqualTo((short) 32765);
		assertThat(idSequence.getNewId()).isEqualTo((short) 32766);
		assertThat(idSequence.getNewId()).isEqualTo((short) 32767);
		assertThat(idSequence.getNewId()).isEqualTo((short) -1000);
		assertThat(idSequence.getNewId()).isEqualTo((short) -1001);
		assertThat(idSequence.getNewId()).isEqualTo((short) -1002);
		assertThat(idSequence.getNewId()).isEqualTo((short) -1003);
		assertThat(idSequence.getNewId()).isEqualTo((short) -1004);

	}

}
