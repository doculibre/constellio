package com.constellio.model.services.schemas.builders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.schemas.MetadataPopulateConfigs;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.RegexConfig.RegexConfigType;
import com.constellio.sdk.tests.ConstellioTest;

public class MetadataPopulatedConfigsBuilderTest extends ConstellioTest {

	MetadataPopulateConfigsBuilder populateConfigsBuilder;
	MetadataPopulateConfigs populateConfigs;

	List<String> styles = new ArrayList<>();
	List<String> properties = new ArrayList<>();
	List<RegexConfig> regexes = new ArrayList<>();

	@Before
	public void setUp()
			throws Exception {
		configureStylesList();
		configurePropertiesList();
		configureRegexList();

		populateConfigsBuilder = MetadataPopulateConfigsBuilder.create();
	}

	@Test
	public void whenBuildThenConfigsAreNotNull()
			throws Exception {

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getRegexes()).isNotNull().isEmpty();
		assertThat(populateConfigs.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigs.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigs.getRegexes()).isNotNull().isEmpty();
	}

	@Test
	public void givenAEmptyMetadataPopulatedConfigsWhenBuildWithPopulateConfigThenConfigsAreNotNull()
			throws Exception {

		populateConfigs = new MetadataPopulateConfigs();
		populateConfigsBuilder = MetadataPopulateConfigsBuilder.modify(populateConfigs);
		MetadataPopulateConfigs newMetadataPopulateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getRegexes()).isNotNull().isEmpty();
		assertThat(newMetadataPopulateConfigs.getStyles()).isNotNull().isEmpty();
		assertThat(newMetadataPopulateConfigs.getProperties()).isNotNull().isEmpty();
		assertThat(newMetadataPopulateConfigs.getRegexes()).isNotNull().isEmpty();
	}

	@Test
	public void whenSettingStylesThenStylesSetted() {

		populateConfigsBuilder.setStyles(styles);

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).isEqualTo(styles);
		assertThat(populateConfigsBuilder.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getRegexes()).isNotNull().isEmpty();
		assertThat(populateConfigs.getStyles()).isEqualTo(styles);
		assertThat(populateConfigs.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigs.getRegexes()).isNotNull().isEmpty();
	}

	@Test
	public void whenSettingPropertiesThenPropertiesSetted() {

		populateConfigsBuilder.setProperties(properties);

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getProperties()).isEqualTo(properties);
		assertThat(populateConfigsBuilder.getRegexes()).isNotNull().isEmpty();
		assertThat(populateConfigs.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigs.getProperties()).isEqualTo(properties);
		assertThat(populateConfigs.getRegexes()).isNotNull().isEmpty();
	}

	@Test
	public void whenSettingRegexesThenRegexesSetted() {

		populateConfigsBuilder.setRegexes(regexes);

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getRegexes()).isEqualTo(regexes);
		assertThat(populateConfigs.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigs.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigs.getRegexes()).isEqualTo(regexes);
	}

	@Test
	public void whenAddStyleThenStyleAdded() {
		populateConfigsBuilder.addStyle("style5");

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).containsOnly("style5");
		assertThat(populateConfigsBuilder.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getRegexes()).isNotNull().isEmpty();
		assertThat(populateConfigs.getStyles()).containsOnly("style5");
		assertThat(populateConfigs.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigs.getRegexes()).isNotNull().isEmpty();
	}

	@Test
	public void whenAddPropertyThenPropertyAdded() {
		populateConfigsBuilder.addProperty("property5");

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getProperties()).containsOnly("property5");
		assertThat(populateConfigsBuilder.getRegexes()).isNotNull().isEmpty();
		assertThat(populateConfigs.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigs.getProperties()).containsOnly("property5");
		assertThat(populateConfigs.getRegexes()).isNotNull().isEmpty();
	}

	@Test
	public void whenAddRegexThenRegexAdded() {
		populateConfigsBuilder.addRegex(createRegexConfig(5));

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getRegexes()).extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata5", "regex5", "value5"));
		assertThat(populateConfigs.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigs.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigs.getRegexes()).extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata5", "regex5", "value5"));
	}

	@Test
	public void whenAddSameStyleThenDoNotAdd() {
		populateConfigsBuilder.addStyle("style5");
		populateConfigsBuilder.addStyle("style5");

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).containsOnlyOnce("style5");
		assertThat(populateConfigsBuilder.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getRegexes()).isNotNull().isEmpty();
		assertThat(populateConfigs.getStyles()).containsOnlyOnce("style5");
		assertThat(populateConfigs.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigs.getRegexes()).isNotNull().isEmpty();
	}

	@Test
	public void whenAddSamePropertyThenDoNotAdd() {
		populateConfigsBuilder.addProperty("property5");
		populateConfigsBuilder.addProperty("property5");

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getProperties()).containsOnlyOnce("property5");
		assertThat(populateConfigs.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigs.getProperties()).containsOnlyOnce("property5");
	}

	@Test
	public void givenStylesWhenRemoveStyleThenOk() {

		populateConfigsBuilder.setStyles(styles);
		populateConfigsBuilder.removeStyle("style1");

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).containsOnly("style2", "style3", "style4");
		assertThat(populateConfigsBuilder.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigs.getStyles()).containsOnly("style2", "style3", "style4");
		assertThat(populateConfigs.getProperties()).isNotNull().isEmpty();
	}

	@Test
	public void givenPropertyWhenRemovePropertyThenOk() {

		populateConfigsBuilder.setProperties(properties);
		populateConfigsBuilder.removeProperty("property1");

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getProperties()).containsOnly("property2", "property3", "property4");
		assertThat(populateConfigs.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigs.getProperties()).containsOnly("property2", "property3", "property4");
	}

	@Test
	public void whenSettingStylesWithDuplicatesThenStylesSettedWithoutDuplicates() {

		List<String> stylesWithDuplicates = new ArrayList<>();
		stylesWithDuplicates.addAll(styles);
		stylesWithDuplicates.add(styles.get(3));
		populateConfigsBuilder.setStyles(stylesWithDuplicates);

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles())
				.containsOnlyOnce(styles.get(0), styles.get(1), styles.get(2), styles.get(3));
		assertThat(populateConfigsBuilder.getProperties()).isNotNull().isEmpty();
		assertThat(populateConfigs.getStyles()).containsOnlyOnce(styles.get(0), styles.get(1), styles.get(2),
				styles.get(3));
		assertThat(populateConfigs.getProperties()).isNotNull().isEmpty();
	}

	@Test
	public void whenSettingPropertiesWithDuplicatesThenPropertiesSettedWithoutDuplicates() {

		List<String> propertiesWithDuplicates = new ArrayList<>();
		propertiesWithDuplicates.addAll(properties);
		propertiesWithDuplicates.add(properties.get(3));
		populateConfigsBuilder.setProperties(propertiesWithDuplicates);

		populateConfigs = populateConfigsBuilder.build();

		assertThat(populateConfigsBuilder.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigsBuilder.getProperties())
				.containsOnlyOnce(properties.get(0), properties.get(1), properties.get(2), properties.get(3));
		assertThat(populateConfigs.getStyles()).isNotNull().isEmpty();
		assertThat(populateConfigs.getProperties())
				.containsOnlyOnce(properties.get(0), properties.get(1), properties.get(2), properties.get(3));
	}

	//

	private void configurePropertiesList() {
		properties.add("property1");
		properties.add("property2");
		properties.add("property3");
		properties.add("property4");
	}

	private void configureStylesList() {
		styles.add("style1");
		styles.add("style2");
		styles.add("style3");
		styles.add("style4");
	}

	private void configureRegexList() {
		regexes.add(createRegexConfig(1));
		regexes.add(createRegexConfig(2));
		regexes.add(createRegexConfig(3));
		regexes.add(createRegexConfig(4));
	}

	private RegexConfig createRegexConfig(int i) {
		Pattern regex = Pattern.compile("regex" + i);
		return new RegexConfig("inputMetadata" + i, regex, "value" + i, RegexConfigType.SUBSTITUTION);
	}

}
