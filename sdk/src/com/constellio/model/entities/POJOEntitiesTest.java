package com.constellio.model.entities;

import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.model.entities.schemas.MetadataNetwork;
import com.constellio.model.entities.schemas.MetadataPopulateConfigs;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.RegexConfig.RegexConfigType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.ManualDataEntry;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.extractions.DefaultMetadataPopulator;
import com.constellio.model.services.records.extractions.MetadataPopulator;
import com.constellio.model.services.records.extractions.MetadataToText;
import com.constellio.model.services.records.extractions.RegexExtractor;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.constellio.sdk.tests.TestUtils.assertThatToEqualsAndToStringThrowNoException;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class POJOEntitiesTest extends ConstellioTest {

	Map<Language, String> labels;

	@Before
	public void setUp()
			throws Exception {
		labels = new HashMap<>();
		labels.put(Language.French, "a");

	}

	@Test
	public void testThatRecordWrapperHasValidEqualsHashcodeAndToStringBehaviors() {
		Record record = mock(Record.class);
		when(record.getSchemaCode()).thenReturn("folder_default");
		MetadataSchemaTypes types1 = mock(MetadataSchemaTypes.class);
		MetadataSchemaTypes types2 = mock(MetadataSchemaTypes.class);
		List languageList = new ArrayList();
		languageList.add(Language.French);
		doReturn(languageList).when(types1).getLanguages();
		RecordWrapper o = new RecordWrapper(record, types1, "folder");
		RecordWrapper o2 = new RecordWrapper(record, types1, "folder");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
		assertThat(o).isInstanceOf(Serializable.class);
	}

	@Test
	public void testThatCalculatedDataEntryHasValidEqualsHashcodeAndToStringBehaviors() {
		MetadataValueCalculator calculator = mock(MetadataValueCalculator.class);
		CalculatedDataEntry o = new CalculatedDataEntry(calculator);
		CalculatedDataEntry o2 = new CalculatedDataEntry(calculator);
		assertThatToEqualsAndToStringThrowNoException(o, o2);

	}

	@Test
	public void testThatCopiedDataEntryHasValidEqualsHashcodeAndToStringBehaviors() {
		CopiedDataEntry o = new CopiedDataEntry("a", "b");
		CopiedDataEntry o2 = new CopiedDataEntry("a", "b");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatManualDataEntryHasValidEqualsHashcodeAndToStringBehaviors() {
		ManualDataEntry o = new ManualDataEntry();
		ManualDataEntry o2 = new ManualDataEntry();
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatLocalDependencyHasValidEqualsHashcodeAndToStringBehaviors() {
		LocalDependency o = LocalDependency.toABoolean("a");
		LocalDependency o2 = LocalDependency.toABoolean("a");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatReferenceDependencyHasValidEqualsHashcodeAndToStringBehaviors() {
		ReferenceDependency o = ReferenceDependency.toABoolean("a", "b");
		ReferenceDependency o2 = ReferenceDependency.toABoolean("a", "b");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatAllowedReferencesHasValidEqualsHashcodeAndToStringBehaviors() {
		AllowedReferences o = new AllowedReferences("type", null);
		AllowedReferences o2 = new AllowedReferences("type", null);
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatMetadataPopulateConfigsHasValidEqualsHashcodeAndToStringBehaviors() {
		Pattern regex = Pattern.compile("regex");
		RegexConfig regexConfig = new RegexConfig("inpuptMetadata", regex, "value", RegexConfigType.SUBSTITUTION);
		MetadataPopulator metadataPopulator = new DefaultMetadataPopulator(
				new RegexExtractor(regexConfig.getRegex().pattern(),
						regexConfig.getRegexConfigType() == RegexConfigType.TRANSFORMATION, regexConfig.getValue()),
				new MetadataToText(regexConfig.getInputMetadata()));
		MetadataPopulateConfigs o = new MetadataPopulateConfigs(asList("style"), asList("property"), asList(regexConfig),
				asList(metadataPopulator), null);
		MetadataPopulateConfigs o2 = new MetadataPopulateConfigs(asList("style"), asList("property"), asList(regexConfig),
				asList(metadataPopulator), null);
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatMetadataHasValidEqualsHashcodeAndToStringBehaviors() {
		Metadata o = Schemas.IDENTIFIER;
		Metadata o2 = Schemas.IDENTIFIER;
		assertThatToEqualsAndToStringThrowNoException(o, o2);
		assertThat(o).isInstanceOf(Serializable.class);
	}

	@Test
	public void testThatMetadataSchemaHasValidEqualsHashcodeAndToStringBehaviors() {
		CollectionInfo zeCollectionInfo = new CollectionInfo((byte) 0, zeCollection, "fr", Arrays.asList("fr"));
		MetadataSchema o = new MetadataSchema((short) 0, "a", "a", zeCollectionInfo, labels, new ArrayList<Metadata>(), true,
				true, new HashSet<RecordValidator>(), null, DataStore.RECORDS, true);
		MetadataSchema o2 = new MetadataSchema((short) 0, "a", "a", zeCollectionInfo, labels, new ArrayList<Metadata>(), true,
				true, new HashSet<RecordValidator>(), null, DataStore.RECORDS, true);
		assertThatToEqualsAndToStringThrowNoException(o, o2);
		assertThat(o).isInstanceOf(Serializable.class);
	}

	@Test
	public void testThatMetadataSchemaTypeHasValidEqualsHashcodeAndToStringBehaviors() {
		CollectionInfo zeCollectionInfo = new CollectionInfo((byte) 0, zeCollection, "fr", Arrays.asList("fr"));
		MetadataSchema defaultSchema = new MetadataSchema((short) 0, "a", "a", zeCollectionInfo, labels, new ArrayList<Metadata>(), true,
				true, new HashSet<RecordValidator>(), null, DataStore.RECORDS, true);
		MetadataSchema defaultSchema2 = new MetadataSchema((short) 0, "a", "a", zeCollectionInfo, labels, new ArrayList<Metadata>(), true,
				true, new HashSet<RecordValidator>(), null, DataStore.RECORDS, true);
		MetadataSchemaType o = new MetadataSchemaType((short) 0, "a", null, zeCollectionInfo, labels, new ArrayList<MetadataSchema>(),
				defaultSchema, true, true, RecordCacheType.NOT_CACHED, true, false, "records");
		MetadataSchemaType o2 = new MetadataSchemaType((short) 0, "a", null, zeCollectionInfo, labels, new ArrayList<MetadataSchema>(),
				defaultSchema2,
				true, true, RecordCacheType.NOT_CACHED, true, false, "records");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
		assertThat(o).isInstanceOf(Serializable.class);
	}

	@Test
	public void testThatMetadataSchemaTypesHasValidEqualsHashcodeAndToStringBehaviors() {
		CollectionInfo zeCollectionInfo = new CollectionInfo((byte) 0, zeCollection, "fr", Arrays.asList("fr"));
		MetadataSchemaTypes o = new MetadataSchemaTypes(zeCollectionInfo, 1, new ArrayList<MetadataSchemaType>(),
				new ArrayList<String>(), new ArrayList<String>(), Arrays.asList(Language.French), MetadataNetwork.EMPTY());
		MetadataSchemaTypes o2 = new MetadataSchemaTypes(zeCollectionInfo, 1, new ArrayList<MetadataSchemaType>(),
				new ArrayList<String>(), new ArrayList<String>(), Arrays.asList(Language.French), MetadataNetwork.EMPTY());
		assertThatToEqualsAndToStringThrowNoException(o, o2);
		assertThat(o).isInstanceOf(Serializable.class);
	}

	@Test
	public void testThatMetadataAccessRestrictionsHasValidEqualsHashcodeAndToStringBehaviors() {
		MetadataValueCalculator calculator = mock(MetadataValueCalculator.class);
		MetadataAccessRestriction o = new MetadataAccessRestriction(Arrays.asList("a"), Arrays.asList("b"), Arrays.asList("c"),
				Arrays.asList("d"));
		MetadataAccessRestriction o2 = new MetadataAccessRestriction(Arrays.asList("a"), Arrays.asList("b"), Arrays.asList("c"),
				Arrays.asList("d"));
		assertThatToEqualsAndToStringThrowNoException(o, o2);

	}

	@Test
	public void testThatBinaryConfigurationHasValidEqualsHashcodeAndToStringBehaviors() {
		BinaryConfiguration o = new BinaryConfiguration("a", null);
		BinaryConfiguration o2 = new BinaryConfiguration("a", null);
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatXMLConfigurationHasValidEqualsHashcodeAndToStringBehaviors() {
		XMLConfiguration o = new XMLConfiguration("a", null, null);
		XMLConfiguration o2 = new XMLConfiguration("a", null, null);
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatPropertiesConfigurationHasValidEqualsHashcodeAndToStringBehaviors() {
		PropertiesConfiguration o = new PropertiesConfiguration("a", null);
		PropertiesConfiguration o2 = new PropertiesConfiguration("a", null);
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatTaxonomiesHasValidEqualsHashcodeAndToStringBehaviors() {
		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "b");


		Taxonomy o = Taxonomy.createPublic("a", labelTitle1, "zeCollection", Arrays.asList("c"));
		Taxonomy o2 = Taxonomy.createPublic("a", labelTitle1, "zeCollection", Arrays.asList("c"));
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_AllConditions_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchCondition nested = mock(LogicalSearchCondition.class);
		LogicalSearchCondition o = LogicalSearchQueryOperators.allConditions(Arrays.asList(nested));
		LogicalSearchCondition o2 = LogicalSearchQueryOperators.allConditions(Arrays.asList(nested));
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_AnyConditions_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchCondition nested = mock(LogicalSearchCondition.class);
		LogicalSearchCondition o = LogicalSearchQueryOperators.anyConditions(Arrays.asList(nested));
		LogicalSearchCondition o2 = LogicalSearchQueryOperators.anyConditions(Arrays.asList(nested));
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_Is_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.is("4");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.is("4");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_IsNotEqual_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.isNotEqual("4");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.isNotEqual("4");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_In_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.in(Arrays.asList("4", "5"));
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.in(Arrays.asList("4", "5"));
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_NotIn_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.notIn(Arrays.asList("4", "5"));
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.notIn(Arrays.asList("4", "5"));
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_Containing_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.containing(Arrays.asList("4", "5"));
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.containing(Arrays.asList("4", "5"));
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_notContainingElements_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.notContainingElements(Arrays.asList("4", "5"));
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.notContainingElements(Arrays.asList("4", "5"));
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_isNull_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.isNull();
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.isNull();
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_isNotNull_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.isNotNull();
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.isNotNull();
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_containingText_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.containingText("a");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.containingText("a");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_startingWithText_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.startingWithText("a");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.startingWithText("a");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_endingWithText_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.endingWithText("a");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.endingWithText("a");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_all_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.all(new ArrayList<LogicalSearchValueCondition>());
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.all(new ArrayList<LogicalSearchValueCondition>());
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_any_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.any(new ArrayList<LogicalSearchValueCondition>());
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.any(new ArrayList<LogicalSearchValueCondition>());
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_not_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.not(LogicalSearchQueryOperators.startingWithText("text"));
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.not(LogicalSearchQueryOperators.startingWithText("text"));
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_isTrue_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.isTrue();
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.isTrue();
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_isTrueOrNull_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.isTrueOrNull();
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.isTrueOrNull();
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_isFalse_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.isFalse();
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.isFalse();
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_isFalseOrNull_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.isFalseOrNull();
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.isFalseOrNull();
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_valueInRange_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.valueInRange("1", "2");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.valueInRange("1", "2");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_lessThan_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.lessThan("1");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.lessThan("1");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_greaterThan_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.greaterThan("1");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.greaterThan("1");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_lessOrEqualThan_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.lessOrEqualThan("1");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.lessOrEqualThan("1");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_greaterOrEqualThan_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.greaterOrEqualThan("1");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.greaterOrEqualThan("1");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_equal_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.equal("1");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.equal("1");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_notEqual_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.notEqual("1");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.notEqual("1");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_query_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.query("1");
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.query("1");
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatMetadataSchemaTypesIsNotSerializable() {
	}

	@Test
	public void testThatSpeCriterion_newerThan_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.newerThan(1.0, MeasuringUnitTime.DAYS);
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.newerThan(1.0, MeasuringUnitTime.DAYS);
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_olderThan_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.olderThan(1.0, MeasuringUnitTime.DAYS);
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.olderThan(1.0, MeasuringUnitTime.DAYS);
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

	@Test
	public void testThatSpeCriterion_olderLike_HasValidEqualsHashcodeAndToStringBehaviors() {
		LogicalSearchValueCondition o = LogicalSearchQueryOperators.oldLike(1.0, MeasuringUnitTime.DAYS);
		LogicalSearchValueCondition o2 = LogicalSearchQueryOperators.oldLike(1.0, MeasuringUnitTime.DAYS);
		assertThatToEqualsAndToStringThrowNoException(o, o2);
	}

}
