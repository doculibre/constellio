package com.constellio.model.extensions;

import com.constellio.app.api.extensions.RecordDisplayFactoryExtension;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.sdk.tests.ConstellioTest;
import com.vaadin.ui.Component;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RecordDisplayFactoryExtensionAcceptanceTest extends ConstellioTest {
	@Mock ReferenceDisplay defaultDisplay;
	AppLayerCollectionExtensions extensions;

	@Before
	public void setup() {
		defaultDisplay = mock(ReferenceDisplay.class);
		extensions = spy(getAppLayerFactory().getExtensions().forCollection(zeCollection));
		doReturn(defaultDisplay).when(extensions).getDefaultDisplayForReference(anyString());
	}

	@Test
	public void givenNoExtensionsThenReturnDefaultDisplay() {
		Component displayForReference = (Component) extensions.getDisplayForReference(null, null);
		assertThat(displayForReference).isEqualTo(defaultDisplay);
	}

	@Test
	public void givenNoExtensionsForRecordThenReturnDefaultDisplay() {
		RecordDisplayFactoryExtension executedExtension = spy(new RecordDisplayFactoryExtension());
		extensions.recordDisplayFactoryExtensions.add(executedExtension);
		Component displayForReference = (Component) extensions.getDisplayForReference(null, null);
		verify(executedExtension, times(1)).getDisplayForReference(any(AllowedReferences.class), anyString());
		assertThat(displayForReference).isEqualTo(defaultDisplay);
	}

	@Test
	public void givenAnExtensionsForRecordExistsThenReturnSpecificDisplay() {
		final ReferenceDisplay specificDisplay = mock(ReferenceDisplay.class);
		RecordDisplayFactoryExtension wrongExtension = new RecordDisplayFactoryExtension();
		RecordDisplayFactoryExtension goodExtension = new RecordDisplayFactoryExtension() {
			@Override
			public ReferenceDisplay getDisplayForReference(AllowedReferences allowedReferences, String id) {
				return specificDisplay;
			}
		};
		extensions.recordDisplayFactoryExtensions.add(wrongExtension);
		extensions.recordDisplayFactoryExtensions.add(goodExtension);

		Component displayForReference = (Component) extensions.getDisplayForReference(null, null);
		assertThat(displayForReference).isEqualTo(specificDisplay);
	}
}
