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
import com.constellio.model.utils.ClassProvider;
import com.constellio.model.utils.DefaultClassProvider;
import com.constellio.sdk.tests.ConstellioTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClassListBuilderTest extends ConstellioTest {

	ClassListBuilder<RecordMetadataValidator<?>> builder, builderWithElement, builderWithAbstractImplementation,
			builderWithoutDefaultConstructor, builderWithInvalidImplementationClassname,
			builderWithImplementationNotImplementingInterface;

	@Before
	public void setUp() {
		ClassProvider classProvider = new DefaultClassProvider();
		builder = new ClassListBuilder<RecordMetadataValidator<?>>(classProvider, RecordMetadataValidator.class);

		Set<RecordMetadataValidator<?>> elements = new HashSet<>();
		elements.add(new TestRecordMetadataValidator1());
		builderWithElement = new ClassListBuilder<RecordMetadataValidator<?>>(classProvider, RecordMetadataValidator.class,
				elements);

		builderWithAbstractImplementation = new ClassListBuilder<RecordMetadataValidator<?>>(classProvider,
				RecordMetadataValidator.class);
		builderWithAbstractImplementation.add(AbstractTestMetadataValidator.class);

		builderWithoutDefaultConstructor = new ClassListBuilder<RecordMetadataValidator<?>>(classProvider,
				RecordMetadataValidator.class);
		builderWithoutDefaultConstructor.add(TestRecordMetadataValidatorWithoutDefaultConstructor.class);

		builderWithInvalidImplementationClassname = new ClassListBuilder<RecordMetadataValidator<?>>(
				classProvider, RecordMetadataValidator.class);
		builderWithInvalidImplementationClassname.add("toto");

		builderWithImplementationNotImplementingInterface = new ClassListBuilder<RecordMetadataValidator<?>>(
				classProvider, RecordMetadataValidator.class);
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
