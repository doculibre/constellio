package com.constellio.model.services.schemas;

import com.constellio.model.entities.schemas.preparationSteps.CalculateMetadatasRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.RecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.SequenceRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.UpdateCreationModificationUsersAndDateRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.ValidateCyclicReferencesRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.ValidateMetadatasRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.ValidateUsingSchemaValidatorsRecordPreparationStep;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.testimpl.TestRecordValidator1;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsCalculatedUsingPattern;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RecordPreparationStepsAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();

	@Test
	public void givenTypicalSchemasWithoutValidatorsThenTypicalPreparationSteps()
			throws Exception {

		defineSchemasManager().using(setup.withAStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern("'Calculated : ' + stringMetadata")));

		List<RecordPreparationStep> steps = zeSchema.instance().getPreparationSteps();

		assertThat(steps).extracting("class.name").containsExactly(
				UpdateCreationModificationUsersAndDateRecordPreparationStep.class.getName(),
				ValidateMetadatasRecordPreparationStep.class.getName(),
				CalculateMetadatasRecordPreparationStep.class.getName(),
				ValidateCyclicReferencesRecordPreparationStep.class.getName(),
				ValidateMetadatasRecordPreparationStep.class.getName(),
				ValidateUsingSchemaValidatorsRecordPreparationStep.class.getName()
		);

		assertThat(((ValidateMetadatasRecordPreparationStep) steps.get(1)).getMetadatasCodes())
				.containsOnly(zeSchema.stringMetadata().getCode(), zeSchema.metadata("title").getCode());

		assertThat(((CalculateMetadatasRecordPreparationStep) steps.get(2)).getMetadatasCodes())
				.containsOnly(zeSchema.anotherStringMetadata().getCode(), "zeSchemaType_default_tokens",
						"zeSchemaType_default_pathParts", "zeSchemaType_default_path",
						"zeSchemaType_default_principalpath", "zeSchemaType_default_attachedAncestors",
						"zeSchemaType_default_allRemovedAuths", "zeSchemaType_default_autocomplete",
						"zeSchemaType_default_tokensHierarchy", "zeSchemaType_default_attachedPrincipalAncestorsIntIds",
						"zeSchemaType_default_secondaryConceptsIntIds", "zeSchemaType_default_principalAncestorsIntIds");

		assertThat(((ValidateMetadatasRecordPreparationStep) steps.get(4)).getMetadatasCodes())
				.containsOnly(zeSchema.anotherStringMetadata().getCode(), "zeSchemaType_default_tokens",
						"zeSchemaType_default_pathParts", "zeSchemaType_default_path",
						"zeSchemaType_default_principalpath", "zeSchemaType_default_attachedAncestors",
						"zeSchemaType_default_allRemovedAuths", "zeSchemaType_default_autocomplete",
						"zeSchemaType_default_tokensHierarchy", "zeSchemaType_default_attachedPrincipalAncestorsIntIds",
						"zeSchemaType_default_secondaryConceptsIntIds", "zeSchemaType_default_principalAncestorsIntIds");

		assertThat(((ValidateUsingSchemaValidatorsRecordPreparationStep) steps.get(5)).getValidators()).isEmpty();
	}

	@Test
	public void givenTypicalSchemasWithValidatorsThenTypicalPreparationSteps()
			throws Exception {
		defineSchemasManager().using(setup.withAStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern("'Calculated : ' + stringMetadata"))
				.withRecordValidator(TestRecordValidator1.class));

		List<RecordPreparationStep> steps = zeSchema.instance().getPreparationSteps();

		assertThat(steps).extracting("class.name").containsExactly(
				UpdateCreationModificationUsersAndDateRecordPreparationStep.class.getName(),
				ValidateMetadatasRecordPreparationStep.class.getName(),
				CalculateMetadatasRecordPreparationStep.class.getName(),
				ValidateCyclicReferencesRecordPreparationStep.class.getName(),
				ValidateMetadatasRecordPreparationStep.class.getName(),
				ValidateUsingSchemaValidatorsRecordPreparationStep.class.getName()
		);

		assertThat(((ValidateMetadatasRecordPreparationStep) steps.get(1)).getMetadatasCodes())
				.contains(zeSchema.stringMetadata().getCode());

		assertThat(((CalculateMetadatasRecordPreparationStep) steps.get(2)).getMetadatasCodes())
				.contains(zeSchema.anotherStringMetadata().getCode());

		assertThat(((ValidateMetadatasRecordPreparationStep) steps.get(4)).getMetadatasCodes())
				.contains(zeSchema.anotherStringMetadata().getCode());

		assertThat(((ValidateUsingSchemaValidatorsRecordPreparationStep) steps.get(5)).getValidators()).extracting("class.name")
				.contains(TestRecordValidator1.class.getName());

	}

	@Test
	public void givenDynamicSequenceMetadataWithoutMetadatasDependingOnItThenSequenceStepAtLast()
			throws Exception {

		defineSchemasManager().using(setup.withAStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern("'Calculated : ' + stringMetadata"))
				.withRecordValidator(TestRecordValidator1.class).withADynamicSequence());

		List<RecordPreparationStep> steps = zeSchema.instance().getPreparationSteps();

		assertThat(steps).extracting("class.name").isEqualTo(asList(
				UpdateCreationModificationUsersAndDateRecordPreparationStep.class.getName(),
				ValidateMetadatasRecordPreparationStep.class.getName(),
				CalculateMetadatasRecordPreparationStep.class.getName(),
				ValidateCyclicReferencesRecordPreparationStep.class.getName(),

				ValidateMetadatasRecordPreparationStep.class.getName(),
				ValidateUsingSchemaValidatorsRecordPreparationStep.class.getName(),

				SequenceRecordPreparationStep.class.getName()
		));

		assertThat(((ValidateMetadatasRecordPreparationStep) steps.get(1)).getMetadatasCodes())
				.contains(zeSchema.stringMetadata().getCode());

		assertThat(((CalculateMetadatasRecordPreparationStep) steps.get(2)).getMetadatasCodes())
				.contains(zeSchema.anotherStringMetadata().getCode())
				.doesNotContain("zeSchemaType_default_metaDependentOfSeq1", "zeSchemaType_default_metaDependentOfSeq4",
						"zeSchemaType_default_metaDependentOfSeq3", "zeSchemaType_default_metaDependentOfSeq2");

		assertThat(((ValidateMetadatasRecordPreparationStep) steps.get(4)).getMetadatasCodes())
				.contains(zeSchema.anotherStringMetadata().getCode())
				.doesNotContain("zeSchemaType_default_metaDependentOfSeq1", "zeSchemaType_default_metaDependentOfSeq4",
						"zeSchemaType_default_metaDependentOfSeq3", "zeSchemaType_default_metaDependentOfSeq2");

		assertThat(((ValidateUsingSchemaValidatorsRecordPreparationStep) steps.get(5)).getValidators()).extracting("class.name")
				.contains(TestRecordValidator1.class.getName());

		assertThat(((SequenceRecordPreparationStep) steps.get(6)).getMetadatasCodes())
				.containsExactly("zeSchemaType_default_dynamicSequenceMetadata");

	}

	@Test
	public void givenDynamicSequenceMetadataThenMetadataAndThoseDependingOnItAreCalculatedAndValidatedAtTheEnd()
			throws Exception {

		defineSchemasManager().using(setup.withAStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern("'Calculated : ' + stringMetadata"))
				.withRecordValidator(TestRecordValidator1.class)
				.withADynamicSequence().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						MetadataSchemaBuilder zeSchema = schemaTypes.getSchema("zeSchemaType_default");
						zeSchema.create("metaDependentOfSeq1").setType(STRING).defineDataEntry().asJexlScript(
								"dynamicSequenceMetadata + 'test'");

						zeSchema.create("metaDependentOfSeq2").setType(STRING).defineDataEntry().asJexlScript(
								"metaDependentOfSeq1 + metaDependentOfSeq3");

						zeSchema.create("metaDependentOfSeq3").setType(STRING).defineDataEntry().asJexlScript(
								"dynamicSequenceMetadata + metaDependentOfSeq4 + metaDependentOfSeq1");
						zeSchema.create("metaDependentOfSeq4").setType(STRING).defineDataEntry().asJexlScript(
								"dynamicSequenceMetadata + anotherStringMetadata");
						zeSchema.create("metaDependentOfSeq0").setType(STRING).defineDataEntry().asJexlScript(
								"'test' + metaDependentOfSeq2");
					}
				}));

		List<RecordPreparationStep> steps = zeSchema.instance().getPreparationSteps();

		assertThat(steps).extracting("class.name").containsExactly(
				UpdateCreationModificationUsersAndDateRecordPreparationStep.class.getName(),
				ValidateMetadatasRecordPreparationStep.class.getName(),
				CalculateMetadatasRecordPreparationStep.class.getName(),
				ValidateCyclicReferencesRecordPreparationStep.class.getName(),

				ValidateMetadatasRecordPreparationStep.class.getName(),
				ValidateUsingSchemaValidatorsRecordPreparationStep.class.getName(),

				SequenceRecordPreparationStep.class.getName(),
				CalculateMetadatasRecordPreparationStep.class.getName(),
				ValidateMetadatasRecordPreparationStep.class.getName(),
				ValidateUsingSchemaValidatorsRecordPreparationStep.class.getName()
		);

		assertThat(((ValidateMetadatasRecordPreparationStep) steps.get(1)).getMetadatasCodes())
				.contains(zeSchema.stringMetadata().getCode());

		assertThat(((CalculateMetadatasRecordPreparationStep) steps.get(2)).getMetadatasCodes())
				.contains(zeSchema.anotherStringMetadata().getCode())
				.doesNotContain("zeSchemaType_default_metaDependentOfSeq1", "zeSchemaType_default_metaDependentOfSeq4",
						"zeSchemaType_default_metaDependentOfSeq3", "zeSchemaType_default_metaDependentOfSeq2",
						"zeSchemaType_default_metaDependentOfSeq0");

		assertThat(((ValidateMetadatasRecordPreparationStep) steps.get(4)).getMetadatasCodes())
				.contains(zeSchema.anotherStringMetadata().getCode())
				.doesNotContain("zeSchemaType_default_metaDependentOfSeq1", "zeSchemaType_default_metaDependentOfSeq4",
						"zeSchemaType_default_metaDependentOfSeq3", "zeSchemaType_default_metaDependentOfSeq2",
						"zeSchemaType_default_metaDependentOfSeq0");

		assertThat(((ValidateUsingSchemaValidatorsRecordPreparationStep) steps.get(5)).getValidators()).extracting("class.name")
				.contains(TestRecordValidator1.class.getName());

		assertThat(((SequenceRecordPreparationStep) steps.get(6)).getMetadatasCodes())
				.containsExactly("zeSchemaType_default_dynamicSequenceMetadata");

		assertThat(((CalculateMetadatasRecordPreparationStep) steps.get(7)).getMetadatasCodes())
				.containsExactly("zeSchemaType_default_metaDependentOfSeq1", "zeSchemaType_default_metaDependentOfSeq4",
						"zeSchemaType_default_metaDependentOfSeq3", "zeSchemaType_default_metaDependentOfSeq2",
						"zeSchemaType_default_metaDependentOfSeq0");

		assertThat(((ValidateMetadatasRecordPreparationStep) steps.get(8)).getMetadatasCodes())
				.containsExactly("zeSchemaType_default_metaDependentOfSeq1", "zeSchemaType_default_metaDependentOfSeq4",
						"zeSchemaType_default_metaDependentOfSeq3", "zeSchemaType_default_metaDependentOfSeq2",
						"zeSchemaType_default_metaDependentOfSeq0");

		assertThat(((ValidateUsingSchemaValidatorsRecordPreparationStep) steps.get(9)).getValidators()).extracting("class.name")
				.contains(TestRecordValidator1.class.getName());

	}

	@Test
	public void givenFixedSequenceMetadataThenMetadataAndThoseDependingOnItAreCalculatedAndValidatedAtTheEnd()
			throws Exception {
		defineSchemasManager().using(setup.withAStringMetadata()
				.withAnotherStringMetadata(whichIsCalculatedUsingPattern("'Calculated : ' + stringMetadata"))
				.withRecordValidator(TestRecordValidator1.class)
				.withAFixedSequence().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						MetadataSchemaBuilder zeSchema = schemaTypes.getSchema("zeSchemaType_default");
						zeSchema.create("metaDependentOfSeq1").setType(STRING).defineDataEntry().asJexlScript(
								"fixedSequenceMetadata + 'test'");

						zeSchema.create("metaDependentOfSeq2").setType(STRING).defineDataEntry().asJexlScript(
								"metaDependentOfSeq1 + metaDependentOfSeq3");

						zeSchema.create("metaDependentOfSeq0").setType(STRING).defineDataEntry().asJexlScript(
								"'test' + metaDependentOfSeq2");

						zeSchema.create("metaDependentOfSeq3").setType(STRING).defineDataEntry().asJexlScript(
								"fixedSequenceMetadata + metaDependentOfSeq4 + metaDependentOfSeq1");
						zeSchema.create("metaDependentOfSeq4").setType(STRING).defineDataEntry().asJexlScript(
								"fixedSequenceMetadata + anotherStringMetadata");

					}
				}));

		List<RecordPreparationStep> steps = zeSchema.instance().getPreparationSteps();

		assertThat(steps).extracting("class.name").containsExactly(
				UpdateCreationModificationUsersAndDateRecordPreparationStep.class.getName(),
				ValidateMetadatasRecordPreparationStep.class.getName(),
				CalculateMetadatasRecordPreparationStep.class.getName(),
				ValidateCyclicReferencesRecordPreparationStep.class.getName(),

				ValidateMetadatasRecordPreparationStep.class.getName(),
				ValidateUsingSchemaValidatorsRecordPreparationStep.class.getName(),

				SequenceRecordPreparationStep.class.getName(),
				CalculateMetadatasRecordPreparationStep.class.getName(),
				ValidateMetadatasRecordPreparationStep.class.getName(),
				ValidateUsingSchemaValidatorsRecordPreparationStep.class.getName()
		);

		assertThat(((ValidateMetadatasRecordPreparationStep) steps.get(1)).getMetadatasCodes())
				.contains(zeSchema.stringMetadata().getCode());

		assertThat(((CalculateMetadatasRecordPreparationStep) steps.get(2)).getMetadatasCodes())
				.contains(zeSchema.anotherStringMetadata().getCode())
				.doesNotContain("zeSchemaType_default_metaDependentOfSeq1", "zeSchemaType_default_metaDependentOfSeq4",
						"zeSchemaType_default_metaDependentOfSeq3", "zeSchemaType_default_metaDependentOfSeq2",
						"zeSchemaType_default_metaDependentOfSeq0");

		assertThat(((ValidateMetadatasRecordPreparationStep) steps.get(4)).getMetadatasCodes())
				.contains(zeSchema.anotherStringMetadata().getCode())
				.doesNotContain("zeSchemaType_default_metaDependentOfSeq1", "zeSchemaType_default_metaDependentOfSeq4",
						"zeSchemaType_default_metaDependentOfSeq3", "zeSchemaType_default_metaDependentOfSeq2",
						"zeSchemaType_default_metaDependentOfSeq0");

		assertThat(((ValidateUsingSchemaValidatorsRecordPreparationStep) steps.get(5)).getValidators()).extracting("class.name")
				.contains(TestRecordValidator1.class.getName());

		assertThat(((SequenceRecordPreparationStep) steps.get(6)).getMetadatasCodes())
				.containsExactly("zeSchemaType_default_fixedSequenceMetadata");

		assertThat(((CalculateMetadatasRecordPreparationStep) steps.get(7)).getMetadatasCodes())
				.containsExactly("zeSchemaType_default_metaDependentOfSeq1", "zeSchemaType_default_metaDependentOfSeq4",
						"zeSchemaType_default_metaDependentOfSeq3", "zeSchemaType_default_metaDependentOfSeq2",
						"zeSchemaType_default_metaDependentOfSeq0");

		assertThat(((ValidateMetadatasRecordPreparationStep) steps.get(8)).getMetadatasCodes())
				.containsExactly("zeSchemaType_default_metaDependentOfSeq1", "zeSchemaType_default_metaDependentOfSeq4",
						"zeSchemaType_default_metaDependentOfSeq3", "zeSchemaType_default_metaDependentOfSeq2",
						"zeSchemaType_default_metaDependentOfSeq0");

		assertThat(((ValidateUsingSchemaValidatorsRecordPreparationStep) steps.get(9)).getValidators()).extracting("class.name")
				.contains(TestRecordValidator1.class.getName());

	}

}
