package com.constellio.app.modules.es.services.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;

public class ConnectorFieldFactoryTest extends ConstellioTest {

	ConnectorFieldFactory factory;

	@Before
	public void setUp()
			throws Exception {

		factory = new ConnectorFieldFactory();
	}

	@Test
	public void whenConvertStructureToStringThenEqualled()
			throws Exception {

		ConnectorField object1 = new ConnectorField("zeId", "zeLabel", MetadataValueType.DATE);
		String strValue1 = factory.toString(object1);
		ConnectorField object2 = (ConnectorField) factory.build(strValue1);
		String strValue2 = factory.toString(object2);

		assertThat(object1).isEqualTo(object2);
		assertThat(strValue1).isEqualTo(strValue2);
		assertThat(object1.isDirty()).isFalse();
		assertThat(object2.isDirty()).isFalse();
	}
}
