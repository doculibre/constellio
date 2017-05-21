package com.constellio.app.services.schemas.bulkImport.extensions;

import static com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension.INVALID_DOCUMENT_TYPE_CODE;
import static com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension.INVALID_MEDIUM_TYPE_CODE;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.COPY_RETENTION_RULES;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DOCUMENT_TYPES_DETAILS;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.INVALID_ENUM_VALUE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.INVALID_NUMBER_VALUE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.REQUIRED_VALUE;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static com.constellio.sdk.tests.TestUtils.frenchMessages;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.sdk.tests.ConstellioTest;

public class RetentionRuleExtensionAcceptanceTest extends ConstellioTest {

	private RetentionRuleImportExtension retentionRuleExtension;
	private List<Object> copyRetentionRules;
	private List<Object> documentTypes;
	@Mock ImportData importData;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule()
		);

		retentionRuleExtension = new RetentionRuleImportExtension(zeCollection, getModelLayerFactory());
		copyRetentionRules = new ArrayList<>();
		documentTypes = new ArrayList<>();

		when(importData.getList(COPY_RETENTION_RULES)).thenReturn(copyRetentionRules);
		when(importData.getList(DOCUMENT_TYPES_DETAILS)).thenReturn(documentTypes);
	}

	@Test
	public void givenRetentionRuleThenInvalidIdValueOnDocumentType() {
		ValidationErrors validationErrors = new ValidationErrors();

		givenDocumentTypes(documentTypes).code("documentPapier").addField("archivisticStatus", "C").build();

		ImportDataOptions importDataOptions = new ImportDataOptions();

		retentionRuleExtension.validate(new ValidationParams(validationErrors, importData, importDataOptions));

		assertThat(extractingSimpleCodeAndParameters(validationErrors, "index", "value")).containsOnly(
				tuple("RetentionRuleImportExtension_invalidDocumentType", "0", "documentPapier")
		);

		assertThat(frenchMessages(validationErrors)).containsOnly(
				"Le code «documentPapier» du type de document à la position 0 ne correspond à aucun enregistrement."
		);
	}

	@Test
	public void givenRetentionRuleThenInvalidCopyTypeOnCopyRetentionRule() {
		ValidationErrors validationErrors = new ValidationErrors();

		givenCopyRetentionRule(copyRetentionRules).addField("copyType", "F'`,,,").addField("inactiveDisposalType", "S")
				.addField("mediumType", "PA,FI");

		retentionRuleExtension.prevalidate(new PrevalidationParams(validationErrors, importData));

		List<ValidationError> errors = validationErrors.getValidationErrors();

		assertThat(extractingSimpleCodeAndParameters(validationErrors, "index", "field", "value"))
				.containsOnly(
						tuple("RetentionRuleImportExtension_requiredCopyRuleField", "0", "mediumTypes", null),
						tuple("RetentionRuleImportExtension_invalidCopyRuleEnumField", "0", "inactiveDisposalType", "S"),
						tuple("RetentionRuleImportExtension_requiredCopyRuleField", "0", "activeRetentionPeriod", null),
						tuple("RetentionRuleImportExtension_requiredCopyRuleField", "0", "semiActiveRetentionPeriod", null),
						tuple("RetentionRuleImportExtension_invalidCopyRuleEnumField", "0", "copyType", "F'`,,,")
				);

		assertThat(frenchMessages(validationErrors)).containsOnly(
				"La valeur «F'`,,,» au champ «copyType» de l'exemplaire à la position 0 est invalide. Seules les valeurs «P, S» sont supportées.",
				"La valeur «S» au champ «inactiveDisposalType» de l'exemplaire à la position 0 est invalide. Seules les valeurs «T, D, C» sont supportées.",
				"Le champ «mediumTypes» est requis pour l'exemplaire à la position 0.",
				"Le champ «activeRetentionPeriod» est requis pour l'exemplaire à la position 0.",
				"Le champ «semiActiveRetentionPeriod» est requis pour l'exemplaire à la position 0."
		);

	}

	@Test
	public void givenALargeAmountOfScrapDataWhenValidateThenAFullStackOfError() {
		ValidationErrors validationErrors = new ValidationErrors();

		givenCopyRetentionRule(copyRetentionRules).code(String.valueOf(2)).addField("copyType", "S")
				.addField("inactiveDisposalType", "C")
				.addField("mediumType", "PA");
		givenCopyRetentionRule(copyRetentionRules).code(String.valueOf(3)).addField("copyType", "S")
				.addField("inactiveDisposalType", "T").addField("mediumTypes", "PA,MD")
				.addField("mediumType", "test").addField("activeRetentionPeriod", "-25");
		givenCopyRetentionRule(copyRetentionRules).code(String.valueOf(10)).addField("copyType", "P")
				.addField("activeRetentionPeriod", "999");
		givenCopyRetentionRule(copyRetentionRules).code(String.valueOf(1)).addField("copyType", "Principale").addField(
				"activeRetentionPeriod",
				"888").addField("contentTypesComment", "value unknown").addField("inactiveDisposalType", "D");
		givenCopyRetentionRule(copyRetentionRules).code(String.valueOf(1)).addField("copyType", "S")
				.addField("activeRetentionPeriod", "awer")
				.addField("contentTypesComment", "value unknown with full stack").addField("inactiveDisposalType", "T");

		givenDocumentTypes(documentTypes).code("emailDocumentType").addField("archivisticStatus", "Conservation");
		givenDocumentTypes(documentTypes).code("documentType").addField("archivisticStatus", "T");
		givenDocumentTypes(documentTypes).code(String.valueOf(3)).addField("archivisticStatus", "t");
		givenDocumentTypes(documentTypes).addField("archivisticStatus", "D");
		givenDocumentTypes(documentTypes).code("emailDocumentType").code(String.valueOf(1)).addField("archivisticStatus", "T");

		retentionRuleExtension.prevalidate(new PrevalidationParams(validationErrors, importData));

		assertThat(extractingSimpleCodeAndParameters(validationErrors, "index", "field", "value")).containsOnly(
				tuple("RetentionRuleImportExtension_invalidCopyRuleNumberField", "1", "activeRetentionPeriod", "-25"),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "2", "mediumTypes", null),
				tuple("RetentionRuleImportExtension_invalidCopyRuleEnumField", "3", "copyType", "PRINCIPALE"),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "3", "code", null),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "3", "mediumTypes", null),
				tuple("RetentionRuleImportExtension_invalidCopyRuleNumberField", "4", "activeRetentionPeriod", "awer"),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "4", "mediumTypes", null),
				tuple("RetentionRuleImportExtension_invalidDocumentTypeEnumField", "0", "archivisticStatus", "CONSERVATION"),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "3", "semiActiveRetentionPeriod", null),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "0", "semiActiveRetentionPeriod", null),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "1", "semiActiveRetentionPeriod", null),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "2", "semiActiveRetentionPeriod", null),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "0", "activeRetentionPeriod", null),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "0", "mediumTypes", null),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "4", "semiActiveRetentionPeriod", null)
		);

		assertThat(frenchMessages(validationErrors)).containsOnly(
				"Le champ «semiActiveRetentionPeriod» est requis pour l'exemplaire à la position 3.",
				"La valeur «CONSERVATION» au champ «archivisticStatus» du type de document à la position 0 est invalide. Seules les valeurs «a, s, d, v» sont supportées.",
				"La valeur «-25» au champ «activeRetentionPeriod» de l'exemplaire à la position 1 n'est pas un nombre valide.",
				"Le champ «semiActiveRetentionPeriod» est requis pour l'exemplaire à la position 2.",
				"La valeur «awer» au champ «activeRetentionPeriod» de l'exemplaire à la position 4 n'est pas un nombre valide.",
				"Le champ «semiActiveRetentionPeriod» est requis pour l'exemplaire à la position 4.",
				"Le champ «mediumTypes» est requis pour l'exemplaire à la position 4.",
				"Le champ «mediumTypes» est requis pour l'exemplaire à la position 3.",
				"Le champ «semiActiveRetentionPeriod» est requis pour l'exemplaire à la position 1.",
				"Le champ «semiActiveRetentionPeriod» est requis pour l'exemplaire à la position 0.",
				"Le champ «code» est requis pour l'exemplaire à la position 3.",
				"La valeur «PRINCIPALE» au champ «copyType» de l'exemplaire à la position 3 est invalide. Seules les valeurs «P, S» sont supportées.",
				"Le champ «mediumTypes» est requis pour l'exemplaire à la position 0.",
				"Le champ «activeRetentionPeriod» est requis pour l'exemplaire à la position 0.",
				"Le champ «mediumTypes» est requis pour l'exemplaire à la position 2."
		);

	}

	@Test
	public void givenEmptyMetadatasWhenPrevalidateThenInvalidStringValueError() {
		ValidationErrors validationErrors = new ValidationErrors();

		givenCopyRetentionRule(copyRetentionRules).code(String.valueOf(1)).addField("copyType", "")
				.addField("inactiveDisposalType", "")
				.addField("mediumTypes", "").addField("activeRetentionPeriod", "").addField("contentTypesComment", "")
				.addField("semiActiveRetentionPeriod", "");

		givenDocumentTypes(documentTypes).code("emailDocumentType").addField("archivisticStatus", "");

		retentionRuleExtension.prevalidate(new PrevalidationParams(validationErrors, importData));

		assertThat(extractingSimpleCodeAndParameters(validationErrors, "index", "field", "value", "acceptedValues")).containsOnly(
				tuple("RetentionRuleImportExtension_invalidCopyRuleEnumField", "0", "copyType", "", "P, S"),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "0", "mediumTypes", null, null),
				tuple("RetentionRuleImportExtension_invalidDocumentTypeEnumField", "0", "archivisticStatus", "", "a, s, d, v"),
				tuple("RetentionRuleImportExtension_requiredCopyRuleField", "0", "activeRetentionPeriod", null, null),
				tuple("RetentionRuleImportExtension_invalidCopyRuleEnumField", "0", "inactiveDisposalType", "", "T, D, C")
		);

		assertThat(frenchMessages(validationErrors)).containsOnly(
				"La valeur «» au champ «archivisticStatus» du type de document à la position 0 est invalide. Seules les valeurs «a, s, d, v» sont supportées.",
				"Le champ «mediumTypes» est requis pour l'exemplaire à la position 0.",
				"La valeur «» au champ «copyType» de l'exemplaire à la position 0 est invalide. Seules les valeurs «P, S» sont supportées.",
				"Le champ «activeRetentionPeriod» est requis pour l'exemplaire à la position 0.",
				"La valeur «» au champ «inactiveDisposalType» de l'exemplaire à la position 0 est invalide. Seules les valeurs «T, D, C» sont supportées."
		);

	}

	//----------------------------

	private class StructureMapBuilder {

		private Map<String, String> fields = new HashMap<>();

		public Map<String, String> build() {
			return fields;
		}

		public StructureMapBuilder addField(String key, String value) {
			fields.put(key, value);
			return this;
		}

		public StructureMapBuilder code(String code) {
			fields.put("code", code);
			return this;
		}

	}

	private StructureMapBuilder givenCopyRetentionRule(List<Object> copyRetentionRule) {
		StructureMapBuilder copyRetentionRuleMapBuilder = new StructureMapBuilder();
		copyRetentionRule.add(copyRetentionRuleMapBuilder.build());
		return copyRetentionRuleMapBuilder;
	}

	private StructureMapBuilder givenDocumentTypes(List<Object> documentTypes) {
		StructureMapBuilder documentTypeDetailMapBuilder = new StructureMapBuilder();
		documentTypes.add(documentTypeDetailMapBuilder.build());
		return documentTypeDetailMapBuilder;
	}

}
