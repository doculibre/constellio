/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.utils.ParametrizedInstanceUtilsRuntimeException.CannotInstanciate;
import com.constellio.model.utils.ParametrizedInstanceUtilsRuntimeException.NoSuchConstructor;
import com.constellio.model.utils.ParametrizedInstanceUtilsRuntimeException.UnsupportedArgument;
import com.constellio.model.utils.ParametrizedInstanceUtilsTestResources.CorrectParametrizedClass;
import com.constellio.model.utils.ParametrizedInstanceUtilsTestResources.PrivateConstructorClass;
import com.constellio.model.utils.ParametrizedInstanceUtilsTestResources.UnsupportedArgumentClass;
import com.constellio.model.utils.ParametrizedInstanceUtilsTestResources.UnsupportedChildArgumentClass;

public class ParametrizedInstanceUtilsTest {

	ParametrizedInstanceUtils utils;
	private CorrectParametrizedClass correctParametrizedClass;
	private CorrectParametrizedClass nullValueParametrizedClass;
	private CorrectParametrizedClass multipleNullValueParametrizedClass;
	private Element rootElement;

	@Before
	public void setUp()
			throws Exception {
		utils = new ParametrizedInstanceUtils();

		rootElement = Mockito.mock(Element.class);

		List<String> metadatas = new ArrayList<>();
		metadatas.add("metadata");
		metadatas.add("metadata");
		metadatas.add("metadata");

		List<Integer> mapValue = new ArrayList<>();
		mapValue.add(1);
		mapValue.add(1);

		HashMap<String, List<Integer>> values = new HashMap<>();
		values.put("entry1", mapValue);
		values.put("entry2", null);
		values.put("entry3", mapValue);

		LocalDateTime aDateTime = new LocalDateTime();
		LocalDate aDate = new LocalDate();
		double aDouble = 2.0;

		correctParametrizedClass = new CorrectParametrizedClass(metadatas, values, false, aDateTime, aDate, aDouble);
		nullValueParametrizedClass = new CorrectParametrizedClass(null, values, false, aDateTime, aDate, aDouble);
		multipleNullValueParametrizedClass = new CorrectParametrizedClass(null, values, null, aDateTime, aDate, null);
	}

	@Test
	public void givenCorrectParametrizedClassThenSaveAndLoadThenSameClassLoaded() {
		Element classElement = utils.toElement(correctParametrizedClass, "parameter");
		CorrectParametrizedClass loaded = utils.toObject(classElement, correctParametrizedClass.getClass());

		assertThat(loaded.getInstanceParameters()).isEqualTo(correctParametrizedClass.getInstanceParameters());
	}

	@Test
	public void givenNullValueParametrizedClassThenSaveAndLoadThenSameClassLoaded() {
		Element classElement = utils.toElement(nullValueParametrizedClass, "parameter");
		CorrectParametrizedClass loaded = utils.toObject(classElement, nullValueParametrizedClass.getClass());

		assertThat(loaded.getInstanceParameters()).isEqualTo(nullValueParametrizedClass.getInstanceParameters());
	}

	@Test
	public void givenDoubleNullValueParametrizedClassThenSaveAndLoadThenSameClassLoaded() {
		Element classElement = utils.toElement(multipleNullValueParametrizedClass, "parameter");
		CorrectParametrizedClass loaded = utils.toObject(classElement, multipleNullValueParametrizedClass.getClass());

		assertThat(loaded.getInstanceParameters()).isEqualTo(multipleNullValueParametrizedClass.getInstanceParameters());
	}

	@Test(expected = NoSuchConstructor.class)
	public void givenIncorrectParameterThenNoSuchConstructorThrown() {
		utils.toObject(rootElement, correctParametrizedClass.getClass());
	}

	@Test(expected = UnsupportedArgument.class)
	public void givenUnsupportedArgumentThenUnsupportedArgumentThrown() {
		Metadata metadata = Mockito.mock(Metadata.class);
		Element classElement = utils.toElement(new UnsupportedArgumentClass(metadata), "parameter");
		fail();
	}

	@Test(expected = UnsupportedArgument.class)
	public void givenUnsupportedChildArgumentThenUnsupportedArgumentThrown() {
		Metadata metadata = Mockito.mock(Metadata.class);
		List<Metadata> metadatas = new ArrayList<>();
		metadatas.add(metadata);

		Element classElement = utils.toElement(new UnsupportedChildArgumentClass(metadatas), "parameter");
		fail();
	}

	@Test(expected = CannotInstanciate.class)
	public void givenReflectionExceptionInParameterThenCannotInstanciateThrown() {
		List<Class> parameterClasses = Mockito.mock(List.class);
		List<Object> parameters = Mockito.mock(List.class);

		when(rootElement.getAttribute("name")).thenReturn(new Attribute("name",
				"com.constellio.model.utils.ParametrizedInstanceUtilsTestResources.CorrectParametrizedClass"));

		ParametrizedInstanceUtils parametrizedUtils = Mockito.mock(ParametrizedInstanceUtils.class);
		willThrow(InstantiationException.class).given(parametrizedUtils).getConstructorParameter(rootElement, parameters,
				parameterClasses);
		when(parametrizedUtils.toObject(rootElement, correctParametrizedClass.getClass())).thenCallRealMethod();

		parametrizedUtils.toObject(rootElement, correctParametrizedClass.getClass());
		fail();
	}

	@Test(expected = CannotInstanciate.class)
	public void givenReflectionExceptionInSubParameterThenCannotInstanciateThrown()
			throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

		when(rootElement.getAttribute("type")).thenReturn(new Attribute("type",
				"java.lang.String"));

		ParametrizedInstanceUtils parametrizedUtils = Mockito.mock(ParametrizedInstanceUtils.class);
		willThrow(InstantiationException.class).given(parametrizedUtils).getObject(rootElement, String.class);
		when(parametrizedUtils.toObject(rootElement)).thenCallRealMethod();

		parametrizedUtils.toObject(rootElement);
		fail();
	}

	@Test(expected = CannotInstanciate.class)
	public void givenPrivateConstructorClassThenNoSuchConstructorThrown() {
		Element privateConstructorClass = Mockito.mock(Element.class);
		when(privateConstructorClass.getAttribute("name")).thenReturn(new Attribute("name",
				"com.constellio.model.utils.ParametrizedInstanceUtilsTestResources.PrivateConstructorClass"));

		utils.toObject(privateConstructorClass, PrivateConstructorClass.class);
		fail();
	}

	@Test(expected = CannotInstanciate.class)
	public void givenIncorrectClassThenSaveAndLoadThenNoSuchClassThrown() {
		when(rootElement.getAttribute("name")).thenReturn(new Attribute("name", "com.constellio.model.utils.Nonexistent"));

		utils.toObject(rootElement, CorrectParametrizedClass.class);
		fail();
	}

}
