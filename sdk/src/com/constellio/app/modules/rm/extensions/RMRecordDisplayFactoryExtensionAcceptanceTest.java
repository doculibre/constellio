package com.constellio.app.modules.rm.extensions;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.extensions.app.RMRecordDisplayFactoryExtension;
import com.constellio.app.modules.rm.ui.components.retentionRule.AdministrativeUnitReferenceDisplay;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.sdk.tests.ConstellioTest;
import com.vaadin.ui.Component;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class RMRecordDisplayFactoryExtensionAcceptanceTest extends ConstellioTest {
	@Mock ReferenceDisplay defaultDisplay;
	AppLayerCollectionExtensions extensions;
	RMRecordDisplayFactoryExtension rmExtension;

	@Before
	public void setup() {
		defaultDisplay = mock(ReferenceDisplay.class);
		rmExtension = spy(new RMRecordDisplayFactoryExtension(getAppLayerFactory(), zeCollection));
		extensions = spy(getAppLayerFactory().getExtensions().forCollection(zeCollection));
		extensions.recordDisplayFactoryExtensions.add(rmExtension);
		doReturn(defaultDisplay).when(extensions).getDefaultDisplayForReference(anyString());
	}

	@Test
	public void givenNotAdministrativeUnitThenReturnDefaultDisplay() {
		Component displayForReference = (Component) extensions.getDisplayForReference(new AllowedReferences("wrongSchemaType", null), null);
		assertThat(displayForReference).isEqualTo(defaultDisplay);
		verify(rmExtension, times(1)).getDisplayForReference(any(AllowedReferences.class), anyString());
	}

	@Test
	public void givenIsAdministrativeUnitThenReturnSpecificDisplay() {
		AdministrativeUnitReferenceDisplay specificDisplay = mock(AdministrativeUnitReferenceDisplay.class);
		doReturn(specificDisplay).when(rmExtension).getReferenceDisplayForAdministrativeUnit(anyString());
		Component displayForReference = (Component) extensions.getDisplayForReference(new AllowedReferences(AdministrativeUnit.SCHEMA_TYPE, null), null);
		assertThat(displayForReference).isEqualTo(specificDisplay);
	}
}
