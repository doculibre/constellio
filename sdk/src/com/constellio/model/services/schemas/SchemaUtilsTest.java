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
package com.constellio.model.services.schemas;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.asSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchemaType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.ManualDataEntry;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.sdk.tests.ConstellioTest;

public class SchemaUtilsTest extends ConstellioTest {

	@Mock Metadata anotherSchemaTitle;
	@Mock Metadata anotherSchemaUnmodifiedField;
	@Mock Metadata notTheSameTypeSchemaTitle;

	@Mock Metadata calculatedMetadata;
	@Mock Metadata copiedTitleUsingReferenceToAnotherSchema;
	@Mock Metadata referenceToAnotherSchema;

	@Mock Metadata anotherReferenceToAnotherSchema;
	@Mock Metadata referenceToNotSameSchema;

	@Mock MetadataSchemaTypes types;

	List<Taxonomy> taxonomies;

	SchemaUtils schemaUtils = new SchemaUtils();

	@Before
	public void setUp()
			throws Exception {

		taxonomies = new ArrayList<>();

		when(anotherSchemaTitle.getCode()).thenReturn("title");
		when(anotherSchemaTitle.getCode()).thenReturn("anotherType_default_title");
		when(types.getMetadata("anotherType_default_title")).thenReturn(anotherSchemaTitle);

		when(anotherSchemaUnmodifiedField.getCode()).thenReturn("unmodifiedField");
		when(anotherSchemaUnmodifiedField.getCode()).thenReturn("anotherType_default_unmodifiedField");
		when(types.getMetadata("anotherType_default_unmodifiedField")).thenReturn(anotherSchemaUnmodifiedField);

		when(notTheSameTypeSchemaTitle.getCode()).thenReturn("title");
		when(notTheSameTypeSchemaTitle.getCode()).thenReturn("notTheSameType_default_title");
		when(types.getMetadata("notTheSameType_default_title")).thenReturn(notTheSameTypeSchemaTitle);

		when(referenceToAnotherSchema.getCode()).thenReturn("ref");
		when(referenceToAnotherSchema.getCode()).thenReturn("zeType_default_ref");
		when(referenceToAnotherSchema.getAllowedReferences()).thenReturn(new AllowedReferences("anotherType", null));
		when(types.getMetadata("zeType_default_ref")).thenReturn(referenceToAnotherSchema);

		when(anotherReferenceToAnotherSchema.getCode()).thenReturn("ref2");
		when(anotherReferenceToAnotherSchema.getCode()).thenReturn("zeType_default_ref2");
		when(anotherReferenceToAnotherSchema.getAllowedReferences())
				.thenReturn(new AllowedReferences(null, asSet("anotherType_default")));
		when(types.getMetadata("zeType_default_ref2")).thenReturn(anotherReferenceToAnotherSchema);

		when(referenceToNotSameSchema.getCode()).thenReturn("refToDifferentSchema");
		when(referenceToNotSameSchema.getCode()).thenReturn("zeType_default_refToDifferentSchema");
		when(referenceToNotSameSchema.getAllowedReferences()).thenReturn(new AllowedReferences("differentType", null));
		when(types.getMetadata("zeType_default_refToDifferentSchema")).thenReturn(referenceToNotSameSchema);

		when(copiedTitleUsingReferenceToAnotherSchema.getCode()).thenReturn("copiedTitle");
		when(copiedTitleUsingReferenceToAnotherSchema.getCode()).thenReturn("zeType_default_copiedTitle");
		when(copiedTitleUsingReferenceToAnotherSchema.getDataEntry())
				.thenReturn(new CopiedDataEntry("zeType_default_ref", "anotherType_default_title"));
		when(types.getMetadata("zeType_default_copiedTitle")).thenReturn(copiedTitleUsingReferenceToAnotherSchema);

	}

	@Test
	public void whenGetSimpleCodeWithNullValueThenReturnNull()
			throws Exception {
		String code = schemaUtils.toLocalMetadataCode(null);
		assertThat(code).isNull();

	}

	@Test
	public void whenGetSimpleCodeWithCompleteValueThenReturnSimpleCode()
			throws Exception {
		String code = schemaUtils.toLocalMetadataCode("a_b_c");
		assertThat(code).isEqualTo("c");

	}

	@Test
	public void whenGetLocalCodeOfMetadataInSameSchemaThenReturnTheValue() {
		assertThat(schemaUtils.getLocalCode("zeType_zeSchema_zeMetadata", "zeType_zeSchema")).isEqualTo("zeMetadata");
	}

	@Test
	public void whenGetLocalCodeOfMetadataOfDefaultSchemaThenReturnTheValue() {
		assertThat(schemaUtils.getLocalCode("zeType_default_zeMetadata", "zeType_zeSchema")).isEqualTo("zeMetadata");
	}

	@Test(expected = CannotGetMetadatasOfAnotherSchema.class)
	public void whenGetLocalCodeOfMetadataInOtherSchemaThenThrowException() {
		schemaUtils.getLocalCode("zeType_otherSchema_zeMetadata", "zeType_zeSchema");
	}

	@Test(expected = CannotGetMetadatasOfAnotherSchemaType.class)
	public void whenGetLocalCodeOfMetadataInOtherSchemaTypeThenThrowException() {
		schemaUtils.getLocalCode("otherType_zeSchema_zeMetadata", "zeType_zeSchema");
	}

	@Test
	public void whenGetLocalCodeOfGlobalMetadataThenSkipSchemaValidationAndReturnTheValue() {
		assertThat(schemaUtils.getLocalCode("global_zeSchema_zeMetadata", "zeType_zeSchema")).isEqualTo("zeMetadata");
	}

	@Test
	public void whenGetSimpleCodeWithSimpleValueThenReturnSameSimpleCode()
			throws Exception {
		String code = schemaUtils.toLocalMetadataCode("c");
		assertThat(code).isEqualTo("c");

	}

	@SuppressWarnings("rawtypes")
	@Test
	public void givenCopyMetadataThenHasLocalDependenceToReference()
			throws Exception {
		Metadata copyMetadata = mock(Metadata.class);
		when(copyMetadata.getLocalCode()).thenReturn("m1");
		when(copyMetadata.getType()).thenReturn(STRING);
		when(copyMetadata.getDataEntry()).thenReturn(new CopiedDataEntry("m1d1", "om1"));

		LocalDependency localDependency = LocalDependency.toADateTime("m2d1");
		ReferenceDependency referenceDependency = ReferenceDependency.toADateTime("m2d2", "om2");
		MetadataValueCalculator valueCalculator = mock(MetadataValueCalculator.class);
		when(valueCalculator.getDependencies()).thenReturn(asList(localDependency, referenceDependency));

		Metadata calculatedMetadata = mock(Metadata.class);
		when(calculatedMetadata.getLocalCode()).thenReturn("m2");
		when(calculatedMetadata.getType()).thenReturn(STRING);
		when(calculatedMetadata.getDataEntry()).thenReturn(new CalculatedDataEntry(valueCalculator));

		Metadata refMetadata = mock(Metadata.class);
		when(refMetadata.getCode()).thenReturn("m3");
		when(refMetadata.getType()).thenReturn(REFERENCE);
		when(refMetadata.getDataEntry()).thenReturn(new ManualDataEntry());

		Map<String, Set<String>> dependencies = schemaUtils
				.calculatedMetadataDependencies(asList(copyMetadata, calculatedMetadata,
						refMetadata));

		assertThat(dependencies).hasSize(2).containsEntry("m1", asSet("m1d1")).containsEntry("m2", asSet("m2d1", "m2d2"));
	}

	@Test
	public void whenGetSchemaCodeThenItIsReturned()
			throws Exception {
		Metadata metadata = mock(Metadata.class);
		when(metadata.getCode()).thenReturn("zeType_zeSchema_zeMetadata");

		assertThat(schemaUtils.getSchemaCode(metadata)).isEqualTo("zeType_zeSchema");
	}

	@Test
	public void whenGetSchemaCodeFromBuilderThenItIsReturned()
			throws Exception {
		MetadataBuilder metadataBuilder = mock(MetadataBuilder.class);
		when(metadataBuilder.getCode()).thenReturn("zeType_zeSchema_zeMetadata");

		assertThat(schemaUtils.getSchemaCode(metadataBuilder)).isEqualTo("zeType_zeSchema");
	}

}
