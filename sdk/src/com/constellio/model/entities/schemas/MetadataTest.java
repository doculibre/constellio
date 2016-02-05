package com.constellio.model.entities.schemas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mock;

import com.constellio.sdk.tests.ConstellioTest;

public class MetadataTest extends ConstellioTest {

	@Mock InheritedMetadataBehaviors inheritedMetadataBehaviors;
	@Mock Metadata mockedMetadata;

	@Test
	public void whenGetMultivalueFlagThenReturnInheritedBehaviorValue()
			throws Exception {
		when(inheritedMetadataBehaviors.isMultivalue()).thenReturn(true);
		when(mockedMetadata.getInheritedMetadataBehaviors()).thenReturn(inheritedMetadataBehaviors);
		when(mockedMetadata.isMultivalue()).thenCallRealMethod();

		assertThat(mockedMetadata.isMultivalue()).isTrue();

		verify(inheritedMetadataBehaviors).isMultivalue();
	}

	@Test
	public void whenGetUndeletableuFlagThenReturnInheritedBehaviorValue()
			throws Exception {
		when(inheritedMetadataBehaviors.isUndeletable()).thenReturn(true);
		when(mockedMetadata.getInheritedMetadataBehaviors()).thenReturn(inheritedMetadataBehaviors);
		when(mockedMetadata.isUndeletable()).thenCallRealMethod();

		assertThat(mockedMetadata.isUndeletable()).isTrue();

		verify(inheritedMetadataBehaviors).isUndeletable();
	}
}
