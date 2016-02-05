package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.regex.Pattern;

import org.junit.Test;

import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.RegexConfig.RegexConfigType;

public class MetadataBuilder_MetadataPopulateConfigTest extends MetadataBuilderTest {

	@Test
	public void givenMetadataWithoutInheritanceBuilderWhenBuildThenPopuplatedConfigsNotnull()
			throws Exception {
		metadataWithoutInheritanceBuilder.setType(STRING);

		build();

		assertThat(metadataWithoutInheritanceBuilder.getPopulateConfigsBuilder()).isNotNull();
		assertThat(metadataWithoutInheritanceBuilder.getPopulateConfigsBuilder().getStyles()).isEmpty();
		assertThat(metadataWithoutInheritanceBuilder.getPopulateConfigsBuilder().getProperties()).isEmpty();
		assertThat(metadataWithoutInheritanceBuilder.getPopulateConfigsBuilder().getRegexes()).isEmpty();
	}

	@Test
	public void givenPopulateConfigsDefinedInMetadataAndInheritanceWhenBuildingThenMetadataWithInheritanceIsDetached()
			throws Exception {

		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder1 = createPopulateConfigsBuilder(1);
		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder2 = createPopulateConfigsBuilder(2);

		inheritedMetadataBuilder.setType(STRING).definePopulateConfigsBuilder(metadataPopulateConfigsBuilder1);
		metadataWithInheritanceBuilder.definePopulateConfigsBuilder(metadataPopulateConfigsBuilder2);

		build();

		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getStyles()).containsOnly("style1");
		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getProperties()).containsOnly("property1");
		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata1", "regex1", "value1"));

		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getStyles()).containsOnly("style2");
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getProperties()).containsOnly("property2");
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata2", "regex2", "value2"));
	}

	@Test
	public void givenPopulateConfigsDefinedInMetadataAndInheritanceWhenBuildingThenMetadataWithInheritanceHasSame()
			throws Exception {

		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder1 = createPopulateConfigsBuilder(1);

		inheritedMetadataBuilder.setType(STRING).definePopulateConfigsBuilder(metadataPopulateConfigsBuilder1);

		build();

		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getStyles()).containsOnly("style1");
		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getProperties()).containsOnly("property1");
		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata1", "regex1", "value1"));

		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getStyles()).containsOnly("style1");
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getProperties()).containsOnly("property1");
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata1", "regex1", "value1"));
	}

	@Test
	public void givenPopulateConfigsDefinedInMetadataAndInheritanceWhenModifyingThenMetadataWithInheritanceHasOnlyCustomPopulateConfigs()
			throws Exception {

		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder1 = createPopulateConfigsBuilder(1);
		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder2 = MetadataPopulateConfigsBuilder.modify(
				metadataPopulateConfigsBuilder1);

		inheritedMetadataBuilder.setType(STRING).definePopulateConfigsBuilder(metadataPopulateConfigsBuilder1);
		metadataWithInheritanceBuilder.definePopulateConfigsBuilder(metadataPopulateConfigsBuilder2).addProperty("property3")
				.addStyle("style3").addRegex(createRegexConfig(3));

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getStyles()).containsOnly("style1");
		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getProperties()).containsOnly("property1");
		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata1", "regex1", "value1"));
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getStyles()).containsOnly("style1", "style3");
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getProperties())
				.containsOnly("property1", "property3");
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata1", "regex1", "value1"),
						tuple("inputMetadata3", "regex3", "value3"));
	}

	@Test
	public void givenPopulateConfigsDefinedDuplicatelyInMetadataAndInheritanceWhenBuildingThenNoDuplicate()
			throws Exception {

		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder1 = createPopulateConfigsBuilder(1);
		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder2 = MetadataPopulateConfigsBuilder.modify(
				metadataPopulateConfigsBuilder1);

		inheritedMetadataBuilder.setType(STRING).definePopulateConfigsBuilder(metadataPopulateConfigsBuilder1);
		metadataWithInheritanceBuilder.definePopulateConfigsBuilder(metadataPopulateConfigsBuilder2).addProperty("property1")
				.addStyle("style1");

		build();

		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getStyles()).containsOnlyOnce("style1");
		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getProperties()).containsOnlyOnce("property1");
		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata1", "regex1", "value1"));
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getStyles()).containsOnlyOnce("style1");
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getProperties())
				.containsOnlyOnce("property1");
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata1", "regex1", "value1"));
	}

	@Test
	public void givenPopulateConfigsDefinedDuplicatelyInMetadataAndInheritanceWhenModifyingThenNoDuplicate()
			throws Exception {

		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder1 = createPopulateConfigsBuilder(1);
		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder2 = MetadataPopulateConfigsBuilder.modify(
				metadataPopulateConfigsBuilder1);

		inheritedMetadataBuilder.setType(STRING).definePopulateConfigsBuilder(metadataPopulateConfigsBuilder1);
		metadataWithInheritanceBuilder.definePopulateConfigsBuilder(metadataPopulateConfigsBuilder2).addProperty("property1")
				.addStyle("style1");

		buildAndModify();

		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getStyles()).containsOnlyOnce("style1");
		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getProperties()).containsOnlyOnce("property1");
		assertThat(inheritedMetadataBuilder.getPopulateConfigsBuilder().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata1", "regex1", "value1"));
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getStyles()).containsOnlyOnce("style1");
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getProperties())
				.containsOnlyOnce("property1");
		assertThat(metadataWithInheritanceBuilder.getPopulateConfigsBuilder().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata1", "regex1", "value1"));
	}

	//
	private MetadataPopulateConfigsBuilder createPopulateConfigsBuilder(int i) {
		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder = MetadataPopulateConfigsBuilder.create();
		metadataPopulateConfigsBuilder.setProperties(asList("property" + i));
		metadataPopulateConfigsBuilder.setStyles(asList("style" + i));
		metadataPopulateConfigsBuilder.setRegexes(asList(createRegexConfig(i)));
		return metadataPopulateConfigsBuilder;
	}

	private RegexConfig createRegexConfig(int i) {
		Pattern regex = Pattern.compile("regex" + i);
		return new RegexConfig("inputMetadata" + i, regex, "value" + i, RegexConfigType.SUBSTITUTION);
	}
}
