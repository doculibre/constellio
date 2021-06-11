package com.constellio.app.ui.pages.search.criteria;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.AbstractMapBasedSeparatedStructureFactory;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.StructureInstanciationParams;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.sdk.tests.schemas.SchemasSetup;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

import java.util.Arrays;
import java.util.List;

public class SearchCriterionTestSetup extends SchemasSetup {
	public static final String SCHEMA_TYPE = "criterionTestRecord";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String A_STRING = "aString";
	public static final String A_BOOLEAN = "aBoolean";
	public static final String AN_INT = "anInt";
	public static final String A_DOUBLE = "aDouble";
	public static final String A_DATE = "aDate";
	public static final String A_DATE_TIME = "aDateTime";
	public static final String AN_ENUM = "anEnum";
	public static final String A_SEPARATED_STRUCTURE = "aSeparatedStructure";

	public enum TestEnum implements EnumWithSmallCode {
		VALUE1("a"), VALUE2("b"), VALUE3("c");

		private final String code;

		TestEnum(String code) {
			this.code = code;
		}

		@Override
		public String getCode() {
			return code;
		}
	}

	public static class TestCalculatedSeparatedStructureCalculator implements MetadataValueCalculator<TestCalculatedSeparatedStructure> {

		LocalDependency<String> titleDependency = LocalDependency.toAString("title");

		@Override
		public TestCalculatedSeparatedStructure calculate(CalculatorParameters parameters) {
			String title = parameters.get(titleDependency);

			TestCalculatedSeparatedStructure structure = new TestCalculatedSeparatedStructure();

			structure.setAnalysisName("Analysis of title '" + title + "'");
			if (title != null) {
				structure.setTitleFirstLetter("" + title.charAt(0));
				structure.setTitleLength("" + title.length());
			}
			return structure;
		}

		@Override
		public TestCalculatedSeparatedStructure getDefaultValue() {
			return null;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(titleDependency);
		}
	}

	public static class TestCalculatedSeparatedStructure extends AbstractMapBasedSeparatedStructureFactory.MapBasedStructure {

		public String getAnalysisName() {
			return get("analysisName");
		}

		public TestCalculatedSeparatedStructure setAnalysisName(String value) {
			set("analysisName", value);
			return this;
		}

		public String getTitleLength() {
			return get("titleLength");
		}

		public TestCalculatedSeparatedStructure setTitleLength(String value) {
			set("titleLength", value);
			return this;
		}

		public String getTitleFirstLetter() {
			return get("titleFirstLetter");
		}

		public TestCalculatedSeparatedStructure setTitleFirstLetter(String value) {
			set("titleFirstLetter", value);
			return this;
		}

	}

	public static class TestCalculatedSeparatedStructureFactory extends AbstractMapBasedSeparatedStructureFactory {

		@Override
		protected MapBasedStructure newEmptyStructure(StructureInstanciationParams params) {
			return new TestCalculatedSeparatedStructure();
		}

		@Override
		public String getMainValueFieldName() {
			return "analysisName";
		}
	}

	public SearchCriterionTestSetup(String collection) {
		super(collection);
	}

	@Override
	public void setUp() {
		MetadataSchemaBuilder builder = typesBuilder.createNewSchemaTypeWithSecurity(SCHEMA_TYPE).getDefaultSchema();
		builder.create(A_STRING).setType(MetadataValueType.STRING);
		builder.create(A_BOOLEAN).setType(MetadataValueType.BOOLEAN);
		// We cannot have integers yet
		//builder.create(AN_INT).setType(MetadataValueType.INTEGER);
		builder.create(A_DOUBLE).setType(MetadataValueType.NUMBER);
		builder.create(A_DATE).setType(MetadataValueType.DATE);
		builder.create(A_DATE_TIME).setType(MetadataValueType.DATE_TIME);
		builder.create(AN_ENUM).defineAsEnum(TestEnum.class);
		builder.create(A_SEPARATED_STRUCTURE).defineStructureFactory(TestCalculatedSeparatedStructureFactory.class);
	}

	public CriterionTestRecord getShortcuts() {
		return new CriterionTestRecord();
	}

	public class CriterionTestRecord implements SchemaShortcuts {
		@Override
		public String code() {
			return SCHEMA_TYPE;
		}

		@Override
		public String collection() {
			return collection;
		}

		@Override
		public MetadataSchema instance() {
			return getSchema(code());
		}

		public Metadata aString() {
			return getMetadata(DEFAULT_SCHEMA + "_" + A_STRING);
		}

		public Metadata aBoolean() {
			return getMetadata(DEFAULT_SCHEMA + "_" + A_BOOLEAN);
		}

		public Metadata anInt() {
			return getMetadata(DEFAULT_SCHEMA + "_" + AN_INT);
		}

		public Metadata aDouble() {
			return getMetadata(DEFAULT_SCHEMA + "_" + A_DOUBLE);
		}

		public Metadata aDate() {
			return getMetadata(DEFAULT_SCHEMA + "_" + A_DATE);
		}

		public Metadata aDateTime() {
			return getMetadata(DEFAULT_SCHEMA + "_" + A_DATE_TIME);
		}

		public Metadata anEnum() {
			return getMetadata(DEFAULT_SCHEMA + "_" + AN_ENUM);
		}

		public Metadata aSeparatedStructure() {
			return getMetadata(DEFAULT_SCHEMA + "_" + A_SEPARATED_STRUCTURE);
		}
	}
}