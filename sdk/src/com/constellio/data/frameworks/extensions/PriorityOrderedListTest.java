package com.constellio.data.frameworks.extensions;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PriorityOrderedListTest extends ConstellioTest {

	@Test
	public void whenIteratingThenBasedOnPriority() {

		PriorityOrderedList<String> list = new PriorityOrderedList<>();
		list.add(1234, "Combine de ma valise");
		list.add(3, "Le trois");
		list.add(1234, "Mot de passe à la bibliothèque");
		list.add(42, "Ze answer");


		assertThat(list.iterator())
				.containsExactly("Le trois", "Ze answer", "Combine de ma valise", "Mot de passe à la bibliothèque");

	}

	@Test
	public void whenIteratingWithNestedPriorityOrderedListThenBasedOnPriority() {

		PriorityOrderedList<String> innerList = new PriorityOrderedList<>();
		PriorityOrderedList<String> outerList = new PriorityOrderedList<>(innerList);

		innerList.add(1234, "Combine de ma valise");
		innerList.add(3, "Le trois");
		innerList.add(1234, "Mot de passe à la bibliothèque");
		innerList.add(42, "Ze answer");

		outerList.add(1234, "Cadena de vélo");
		outerList.add(4, "Le S'quatre novembre au soir");
		outerList.add(1234, "NIP");
		outerList.add(666, "The number of the beast");


		assertThat(innerList.iterator())
				.containsExactly("Le trois", "Ze answer", "Combine de ma valise", "Mot de passe à la bibliothèque");


		assertThat(outerList.iterator())
				.containsExactly("Le trois", "Le S'quatre novembre au soir", "Ze answer", "The number of the beast",
						"Combine de ma valise", "Mot de passe à la bibliothèque", "Cadena de vélo", "NIP");

	}
}
