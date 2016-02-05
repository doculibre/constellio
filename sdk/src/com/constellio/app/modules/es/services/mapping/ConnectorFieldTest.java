package com.constellio.app.modules.es.services.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;

public class ConnectorFieldTest extends ConstellioTest {

	@Test
	public void whenModifyConnectorFieldThenBecomeDirty()
			throws Exception {

		ConnectorField connectorField = new ConnectorField("id", "label", MetadataValueType.STRING);
		assertThat(connectorField.isDirty()).isFalse();

		connectorField = new ConnectorField("id", "label", MetadataValueType.STRING);
		connectorField.setId("id2");
		assertThat(connectorField.isDirty()).isTrue();

		connectorField = new ConnectorField("id", "label", MetadataValueType.STRING);
		connectorField.setLabel("label2");
		assertThat(connectorField.isDirty()).isTrue();

		connectorField = new ConnectorField("id", "label", MetadataValueType.STRING);
		connectorField.setType(MetadataValueType.DATE);
		assertThat(connectorField.isDirty()).isTrue();
	}
}
