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
package com.constellio.model.services.schemas.builders;

import static com.constellio.sdk.tests.TestUtils.getElementsClasses;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.schemas.testimpl.TestRecordMetadataValidator1;
import com.constellio.model.services.schemas.testimpl.TestRecordMetadataValidator2;
import com.constellio.model.services.schemas.testimpl.problems.AbstractTestMetadataValidator;
import com.constellio.model.services.schemas.testimpl.problems.TestRecordMetadataValidatorWithoutDefaultConstructor;
import com.constellio.sdk.tests.ConstellioTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClassListBuilderTest extends ConstellioTest {

	ClassListBuilder<RecordMetadataValidator<?>> builder, builderWithElement, builderWithAbstractImplementation,
			builderWithoutDefaultConstructor, builderWithInvalidImplementationClassname,
			builderWithImplementationNotImplementingInterface;

	@Before
	public void setUp() {
		builder = new ClassListBuilder<RecordMetadataValidator<?>>(RecordMetadataValidator.class);

		Set<RecordMetadataValidator<?>> elements = new HashSet<>();
		elements.add(new TestRecordMetadataValidator1());
		builderWithElement = new ClassListBuilder<RecordMetadataValidator<?>>(RecordMetadataValidator.class, elements);

		builderWithAbstractImplementation = new ClassListBuilder<RecordMetadataValidator<?>>(RecordMetadataValidator.class);
		builderWithAbstractImplementation.add(AbstractTestMetadataValidator.class);

		builderWithoutDefaultConstructor = new ClassListBuilder<RecordMetadataValidator<?>>(RecordMetadataValidator.class);
		builderWithoutDefaultConstructor.add(TestRecordMetadataValidatorWithoutDefaultConstructor.class);

		builderWithInvalidImplementationClassname = new ClassListBuilder<RecordMetadataValidator<?>>(
				RecordMetadataValidator.class);
		builderWithInvalidImplementationClassname.add("toto");

		builderWithImplementationNotImplementingInterface = new ClassListBuilder<RecordMetadataValidator<?>>(
				RecordMetadataValidator.class);
		builderWithImplementationNotImplementingInterface.add((Class<?>) ConstellioTest.class);

		assertThat(builderWithElement.implementationsClassname.size()).isEqualTo(1);
	}

	@Test
	public void givenEmptyListWhenAddImplementationClassAndBuildThenListHasOneImplementation()
			throws Exception {
		builder.add(TestRecordMetadataValidator1.class);

		Set<RecordMetadataValidator<?>> implementations = builder.build();

		assertThat(getElementsClasses(implementations)).containsOnly(TestRecordMetadataValidator1.class);
	}

	@Test
	public void givenEmptyListWhenAddImplementationClassnameAndBuildThenListHasOneImplementation()
			throws Exception {
		builder.add(TestRecordMetadataValidator1.class.getName());

		Set<RecordMetadataValidator<?>> implementations = builder.build();

		assertThat(getElementsClasses(implementations)).containsOnly(TestRecordMetadataValidator1.class);
	}

	@Test
	public void givenListWithOneImplementationClassnameAndBuildIncludingAnOtherThenListHasTwoImplementation()
			throws Exception {
		builder.add(TestRecordMetadataValidator1.class.getName());

		Set<RecordMetadataValidator<?>> otherImplementations = new HashSet<>();
		otherImplementations.add(new TestRecordMetadataValidator2());

		Set<RecordMetadataValidator<?>> implementations = builder.build(otherImplementations);

		assertThat(getElementsClasses(implementations)).containsOnly(TestRecordMetadataValidator1.class,
				TestRecordMetadataValidator2.class);
	}

	@Test
	public void givenListWithOneImplementationClassnameAndBuildIncludingTheSameThenListHasOneImplementation()
			throws Exception {
		builder.add(TestRecordMetadataValidator1.class.getName());

		Set<RecordMetadataValidator<?>> otherImplementations = new HashSet<>();
		otherImplementations.add(new TestRecordMetadataValidator1());

		Set<RecordMetadataValidator<?>> implementations = builder.build(otherImplementations);

		assertThat(getElementsClasses(implementations)).containsOnly(TestRecordMetadataValidator1.class);
	}

	@Test
	public void givenListWithOneImplementationWhenRemovingImplClassAndBuildingThenListHasZeroImplementation()
			throws Exception {
		builderWithElement.remove(TestRecordMetadataValidator1.class);
		Set<RecordMetadataValidator<?>> implementations = builderWithElement.build();

		assertThat(getElementsClasses(implementations)).isEmpty();
	}

	@Test
	public void givenListWithOneImplementationWhenRemovingImplClassnameAndBuildingThenListHasZeroImplementation()
			throws Exception {
		builderWithElement.remove(TestRecordMetadataValidator1.class.getName());
		Set<RecordMetadataValidator<?>> implementations = builderWithElement.build();

		assertThat(getElementsClasses(implementations)).isEmpty();
	}

	@Test(expected = ClassListBuilderRuntimeException.CannotInstanciate.class)
	public void givenBuilderWithAbstractImplementationWhenBuildingThenException() {
		builderWithAbstractImplementation.build();
	}

	@Test(expected = ClassListBuilderRuntimeException.CannotInstanciate.class)
	public void givenBuilderWithoutDefaultConstructorWhenBuildingThenException() {
		builderWithoutDefaultConstructor.build();
	}

	@Test(expected = ClassListBuilderRuntimeException.ClassNotFound.class)
	public void givenBuilderWithInvalidClassNameWhenBuildingThenException() {
		builderWithInvalidImplementationClassname.build();
	}

	@Test(expected = ClassListBuilderRuntimeException.ClassDoesntImplementInterface.class)
	public void givenBuilderWithImplementationNotImplementingInterfaceWhenBuildingThenException() {
		builderWithImplementationNotImplementingInterface.build();
	}

}
