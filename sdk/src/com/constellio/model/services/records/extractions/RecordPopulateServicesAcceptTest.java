package com.constellio.model.services.records.extractions;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.robots.model.actions.RunExtractorsActionExecutor;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.ui.pages.search.criteria.CriterionBuilder;
import com.constellio.model.entities.enums.MetadataPopulatePriority;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.enums.TitleMetadataPopulatePriority;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.RegexConfig.RegexConfigType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataBuilderConfigurator;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatasAdapter;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.METADATA_POPULATE_PRIORITY;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.REMOVE_EXTENSION_FROM_RECORD_TITLE;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.TITLE_METADATA_POPULATE_PRIORITY;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class RecordPopulateServicesAcceptTest extends ConstellioTest {

	private final String titleStyle = "titreofficiel";
	private final String clientNameStyle = "nomduclient";
	private final String clientAddressStyle = "adresseduclient";
	private final String companyNameStyle = "nomdelacompagnie";
	private final String companyAddressStyle = "adressedelacompagnie";

	private final String titleProperty = "title";
	private final String subjectProperty = "subject";
	private final String authorProperty = "author";
	private final String managerProperty = "manager";
	private final String companyProperty = "company";
	private final String categoryProperty = "category";
	private final String keywordsProperty = "keywords";
	private final String commentsProperty = "comments";

	private static final boolean andTitleIsFileName = true;
	private static final boolean andTitleIsNotFileName = false;

	RMSchemasRecordsServices rm;
	MetadataSchemaTypes types;

	RMTestRecords records = new RMTestRecords(zeCollection);

	User admin;
	ContentVersionDataSummary documentWithStylesAndProperties1, documentWithStylesAndProperties2,
			documentWithStylesAndProperties3, documentWithStylesAndProperties4, withoutStylesAndProperties,
			documentWithEmptyStylesAndNoProperties, documentWithEmptyStylesAndProperties, documentWithStylesAndNoProperties,
			mappedToACustomSchema, onlyWithRegex;
	ContentManager contentManager;
	RecordServices recordServices;
	RobotSchemaRecordServices robotsSchemas;
	RecordPopulateServices services;
	Users users = new Users();
	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	RecordPopulateServicesAcceptTest_ZeSchemaMetadatas zeSchemas = new RecordPopulateServicesAcceptTest_ZeSchemaMetadatas(
			schemas.new ZeSchemaMetadatas());
	AnotherSchemaMetadatas anotherSchemas = schemas.new AnotherSchemaMetadatas();

	//	// @Test
	public void whenSavingARecordWithoutMetadatasSettedToDifferentValuesWhenSavingThenValuesPopulated()
			throws Exception {
		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnStyles(titleStyle))
				.withStringMeta(mappedOnStyles(titleStyle))
				.withTextMeta(mappedOnStyles(clientAddressStyle, companyAddressStyle))
				.withStringsMeta(mappedOnStyles(clientNameStyle, companyNameStyle))));

		Content recordContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties1);
		Record record = recordServices.newRecordWithSchema(zeSchemas.instance()).set(zeSchemas.requiredContent(), recordContent);

		recordServices.add(record);

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Mon premier contrat")
				.hasMetadataValue(zeSchemas.stringMeta(), "Mon premier contrat")
				.hasMetadataValue(zeSchemas.textMeta(), "42, rue Ultimate, Québec")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Édouard Lechat", "ACME"))
				.hasNoMetadataValue(zeSchemas.textsMeta());

		record.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties2));
		recordServices.update(record);

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "The ring contract")
				.hasMetadataValue(zeSchemas.stringMeta(), "The ring contract")
				.hasMetadataValue(zeSchemas.textMeta(), "Somewhere, Terre du Milieu")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Gandalf Leblanc", "Frodon"))
				.hasNoMetadataValue(zeSchemas.textsMeta());
	}

	//	// @Test
	public void whenRunningRepopulateActionThenRepopulate()
			throws Exception {
		// TODO Correct action to force rerun of extractors
		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle()
				.withStringMeta()
				.withTextMeta()
				.withStringsMeta()
				.withTextsMeta()));

		Content recordContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties1);
		Record record = recordServices.newRecordWithSchema(zeSchemas.instance()).set(zeSchemas.requiredContent(), recordContent);

		recordServices.add(record);

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "contract.docx")
				.hasNoMetadataValue(zeSchemas.stringMeta());

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {

				MetadataSchemaBuilder defaultSchema = types.getSchema(zeSchemas.code());
				defaultSchema.get(zeSchemas.stringMeta().getLocalCode()).getPopulateConfigsBuilder()
						.setStyles(asList(titleStyle));
				defaultSchema.get(Schemas.TITLE_CODE).getPopulateConfigsBuilder()
						.setStyles(asList(titleStyle));

			}
		});

		//run action
		recordServices.add(robotsSchemas.newRobot().setActionParameters((ActionParameters) null)
				.setSchemaFilter(zeSchemas.typeCode()).setSearchCriteria(asList(
						new CriterionBuilder(zeSchemas.typeCode()).where(Schemas.TITLE).isEqualTo("contract").build()
				)).setAction(RunExtractorsActionExecutor.ID).setCode("robocop").setTitle("robocop"));
		robotsSchemas.getRobotsManager().startAllRobotsExecution();
		waitForBatchProcess();

		recordServices.refresh(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "contract.docx")
				.hasMetadataValue(zeSchemas.stringMeta(), "Mon premier contrat");
	}

	//	// @Test
	public void whenSavingARecordWithMetadatasSettedToDefaultValuesWhenSavingThenValuesPopulatedUsingStyles()
			throws Exception {
		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnStyles(titleStyle), defaultValue("defaultTitle"))
				.withStringMeta(mappedOnStyles(titleStyle), defaultValue("defaultStringMeta"))
				.withTextMeta(mappedOnStyles(clientAddressStyle, companyAddressStyle), defaultValue("defaultTextMeta"))
				.withStringsMeta(mappedOnStyles(clientNameStyle, companyNameStyle), defaultValue(asList("value1", "value2")))
				.withTextsMeta(defaultValue(asList("value3", "value4")))));

		Content recordContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties1);
		Record record = recordServices.newRecordWithSchema(zeSchemas.instance()).set(zeSchemas.requiredContent(), recordContent);

		recordServices.add(record);

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Mon premier contrat")
				.hasMetadataValue(zeSchemas.stringMeta(), "Mon premier contrat")
				.hasMetadataValue(zeSchemas.textMeta(), "42, rue Ultimate, Québec")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Édouard Lechat", "ACME"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("value3", "value4"));

		record.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties2));
		recordServices.update(record);

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "The ring contract")
				.hasMetadataValue(zeSchemas.stringMeta(), "The ring contract")
				.hasMetadataValue(zeSchemas.textMeta(), "Somewhere, Terre du Milieu")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Gandalf Leblanc", "Frodon"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("value3", "value4"));
	}

	//	// @Test
	public void whenSavingARecordWithMetadatasSettedToDefaultValuesWhenSavingThenValuesPopulatedUsingProperties()
			throws Exception {

		givenConfig(TITLE_METADATA_POPULATE_PRIORITY, TitleMetadataPopulatePriority.STYLES_PROPERTIES_FILENAME);

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnProperties(titleProperty), defaultValue("defaultTitle"))
				.withStringMeta(mappedOnProperties(categoryProperty), defaultValue("defaultStringMeta"))
				.withTextMeta(mappedOnProperties(keywordsProperty), defaultValue("defaultTextMeta"))
				.withStringsMeta(mappedOnProperties(authorProperty), defaultValue(asList("value1", "value2")))
				.withTextsMeta(defaultValue(asList("value3", "value4")))));

		Content recordContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties1);
		Record record = recordServices.newRecordWithSchema(zeSchemas.instance()).set(zeSchemas.requiredContent(), recordContent);

		recordServices.add(record);

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "title1")
				.hasMetadataValue(zeSchemas.stringMeta(), "category1")
				.hasMetadataValue(zeSchemas.textMeta(), "zeKeyword1")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("author1"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("value3", "value4"));

		record.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties2));
		recordServices.update(record);

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "title2")
				.hasMetadataValue(zeSchemas.stringMeta(), "category2")
				.hasMetadataValue(zeSchemas.textMeta(), "zeKeyword2")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("author2"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("value3", "value4"));
	}

	//	// @Test
	public void whenSavingARecordWithMetadatasSettedToDifferentValuesWhenSavingThenValuesNotPopulated()
			throws Exception {
		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnStyles(titleStyle))
				.withStringMeta(mappedOnStyles(titleStyle))
				.withTextMeta(mappedOnStyles(clientAddressStyle, companyAddressStyle))
				.withStringsMeta(mappedOnStyles(clientNameStyle, companyNameStyle))));

		Content recordContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties1);
		Record record = new TestRecord(zeSchemas).set(zeSchemas.requiredContent(), recordContent);
		record.set(Schemas.TITLE, "customTitle");
		record.set(zeSchemas.stringMeta(), "customStringValue");
		record.set(zeSchemas.textMeta(), "customTextValue");
		record.set(zeSchemas.stringsMeta(), asList("customStringValue1", "customStringValue2"));
		record.set(zeSchemas.textsMeta(), asList("customTextValue1", "customTextValue2"));

		recordServices.add(record);

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "customTitle")
				.hasMetadataValue(zeSchemas.stringMeta(), "customStringValue")
				.hasMetadataValue(zeSchemas.textMeta(), "customTextValue")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("customStringValue1", "customStringValue2"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("customTextValue1", "customTextValue2"));

		record.set(zeSchemas.textMeta(), "newCustomTextValue");
		recordServices.update(record);

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "customTitle")
				.hasMetadataValue(zeSchemas.stringMeta(), "customStringValue")
				.hasMetadataValue(zeSchemas.textMeta(), "newCustomTextValue")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("customStringValue1", "customStringValue2"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("customTextValue1", "customTextValue2"));
	}

	//	// @Test
	public void givenMetadatasWithMappedStylesWhenPopulatingNewRecordWithoutValuesThenValuesAreUpdated()
			throws Exception {

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnStyles(titleStyle))
				.withStringMeta(mappedOnStyles(titleStyle))
				.withTextMeta(mappedOnStyles(clientAddressStyle, companyAddressStyle))
				.withStringsMeta(mappedOnStyles(clientNameStyle, companyNameStyle))));

		Content recordContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties1);
		Record record = new TestRecord(zeSchemas).set(zeSchemas.requiredContent(), recordContent);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Mon premier contrat")
				.hasMetadataValue(zeSchemas.stringMeta(), "Mon premier contrat")
				.hasMetadataValue(zeSchemas.textMeta(), "42, rue Ultimate, Québec")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Édouard Lechat", "ACME"))
				.hasNoMetadataValue(zeSchemas.textsMeta());
		recordServices.add(record);

		recordContent = record.get(zeSchemas.requiredContent());
		recordContent.updateContent(users.adminIn(zeCollection), documentWithStylesAndProperties2, true);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "The ring contract")
				.hasMetadataValue(zeSchemas.stringMeta(), "The ring contract")
				.hasMetadataValue(zeSchemas.textMeta(), "Somewhere, Terre du Milieu")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Gandalf Leblanc", "Frodon"))
				.hasNoMetadataValue(zeSchemas.textsMeta());
		recordServices.update(record);

		recordContent = record.get(zeSchemas.requiredContent());
		recordContent.updateContent(users.adminIn(zeCollection), withoutStylesAndProperties, true);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "contract.docx")
				.hasNoMetadataValue(zeSchemas.stringMeta())
				.hasNoMetadataValue(zeSchemas.textMeta())
				.hasNoMetadataValue(zeSchemas.stringsMeta())
				.hasNoMetadataValue(zeSchemas.textsMeta());
	}

	//	// @Test
	public void givenMetadatasWithMappedStylesWhenPopulatingSavedRecordWithoutValuesThenValuesAreUpdated()
			throws Exception {

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnStyles(titleStyle))
				.withStringMeta(mappedOnStyles(titleStyle))
				.withTextMeta(mappedOnStyles(clientAddressStyle, companyAddressStyle))
				.withStringsMeta(mappedOnStyles(clientNameStyle, companyNameStyle))));

		Content recordContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties1);
		Record record = new TestRecord(zeSchemas);
		getModelLayerFactory().newRecordServices().add(record);

		record.set(zeSchemas.requiredContent(), recordContent);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Mon premier contrat")
				.hasMetadataValue(zeSchemas.stringMeta(), "Mon premier contrat")
				.hasMetadataValue(zeSchemas.textMeta(), "42, rue Ultimate, Québec")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Édouard Lechat", "ACME"))
				.hasNoMetadataValue(zeSchemas.textsMeta());
		recordServices.add(record);

		recordContent = record.get(zeSchemas.requiredContent());
		recordContent.updateContent(users.adminIn(zeCollection), documentWithStylesAndProperties2, true);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "The ring contract")
				.hasMetadataValue(zeSchemas.stringMeta(), "The ring contract")
				.hasMetadataValue(zeSchemas.textMeta(), "Somewhere, Terre du Milieu")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Gandalf Leblanc", "Frodon"))
				.hasNoMetadataValue(zeSchemas.textsMeta());
		recordServices.update(record);

		recordContent = record.get(zeSchemas.requiredContent());
		recordContent.updateContent(users.adminIn(zeCollection), withoutStylesAndProperties, true);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "contract.docx")
				.hasNoMetadataValue(zeSchemas.stringMeta())
				.hasNoMetadataValue(zeSchemas.textMeta())
				.hasNoMetadataValue(zeSchemas.stringsMeta())
				.hasNoMetadataValue(zeSchemas.textsMeta());
	}

	//	// @Test
	public void givenRecordWithAManuallySettedValueWhenPopulateThenDoesNotOverwriteWithStyles()
			throws Exception {

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnStyles(titleStyle))
				.withStringMeta(mappedOnStyles(titleStyle))
				.withTextMeta(mappedOnStyles(clientAddressStyle, companyAddressStyle))
				.withStringsMeta(mappedOnStyles(clientNameStyle, companyNameStyle))));

		Record record = new TestRecord(zeSchemas)
				.set(Schemas.TITLE, "My custom title")
				.set(zeSchemas.stringMeta(), "My custom title")
				.set(zeSchemas.textMeta(), "My custom text")
				.set(zeSchemas.stringsMeta(), asList("first value", "second value"));
		recordServices.add(record);

		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "My custom title")
				.hasMetadataValue(zeSchemas.stringMeta(), "My custom title")
				.hasMetadataValue(zeSchemas.textMeta(), "My custom text")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("first value", "second value"));
		recordServices.add(record);
		services.populate(record);

		Content newContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties2);
		record.set(zeSchemas.requiredContent(), newContent);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "My custom title")
				.hasMetadataValue(zeSchemas.stringMeta(), "My custom title")
				.hasMetadataValue(zeSchemas.textMeta(), "My custom text")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("first value", "second value"));
	}

	//	// @Test
	public void givenRecordWithAManuallyRemovedValueWhenPopulateThenDoesOverwriteWithStyles()
			throws Exception {

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnStyles(titleStyle))
				.withStringMeta(mappedOnStyles(titleStyle))
				.withTextMeta(mappedOnStyles(clientAddressStyle, companyAddressStyle))
				.withStringsMeta(mappedOnStyles(clientNameStyle, companyNameStyle))
				.withTextsMeta(mappedOnStyles(companyNameStyle, clientNameStyle, titleStyle))));

		Content recordContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties1);
		Record record = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), recordContent)
				.set(zeSchemas.stringMeta(), "My custom title");
		services.populate(record);
		record.set(Schemas.TITLE, null);
		record.set(zeSchemas.stringMeta(), null);
		record.set(zeSchemas.stringsMeta(), null);
		record.set(zeSchemas.textMeta(), null);
		assertThatRecord(record)
				.hasNoMetadataValue(Schemas.TITLE)
				.hasNoMetadataValue(zeSchemas.stringMeta())
				.hasNoMetadataValue(zeSchemas.stringsMeta())
				.hasNoMetadataValue(zeSchemas.textMeta())
				.hasMetadataValue(zeSchemas.textsMeta(), asList("ACME", "Édouard Lechat", "Mon premier contrat"));
		recordServices.add(record);

		Content newContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties2);
		record.set(zeSchemas.requiredContent(), newContent);
		services.populate(record);

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "The ring contract")
				.hasMetadataValue(zeSchemas.stringMeta(), "The ring contract")
				.hasMetadataValue(zeSchemas.textMeta(), "Somewhere, Terre du Milieu")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Gandalf Leblanc", "Frodon"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("Frodon", "Gandalf Leblanc", "The ring contract"));
	}

	//	// @Test
	public void givenMetadatasWithMappedPropertiesWhenPopulatingNewRecordWithoutValuesThenValuesAreUpdated()
			throws Exception {

		givenConfig(TITLE_METADATA_POPULATE_PRIORITY, TitleMetadataPopulatePriority.PROPERTIES_STYLES_FILENAME);

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnProperties(titleProperty))
				.withStringMeta(mappedOnProperties(titleProperty))
				.withTextMeta(mappedOnProperties(subjectProperty, authorProperty))
				.withStringsMeta(mappedOnProperties(managerProperty, companyProperty, keywordsProperty))
				.withTextsMeta(mappedOnProperties(commentsProperty, authorProperty))));

		Content recordContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties1);
		Record record = new TestRecord(zeSchemas).set(zeSchemas.requiredContent(), recordContent);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "title1")
				.hasMetadataValue(zeSchemas.stringMeta(), "title1")
				.hasMetadataValue(zeSchemas.textMeta(), "subject1")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("manager1", "company1", "zeKeyword1", "anotherKeyword1"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("comments1", "author1"));
		recordServices.add(record);

		recordContent = record.get(zeSchemas.requiredContent());
		recordContent.updateContent(users.adminIn(zeCollection), documentWithStylesAndProperties2, true);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "title2")
				.hasMetadataValue(zeSchemas.stringMeta(), "title2")
				.hasMetadataValue(zeSchemas.textMeta(), "subject2")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("manager2", "company2", "zeKeyword2", "anotherKeyword2"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("comments2", "author2"));
		recordServices.update(record);

		recordContent = record.get(zeSchemas.requiredContent());
		recordContent.updateContent(users.adminIn(zeCollection), withoutStylesAndProperties, true);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "contract.docx")
				.hasNoMetadataValue(zeSchemas.stringMeta())
				.hasNoMetadataValue(zeSchemas.textMeta())
				.hasNoMetadataValue(zeSchemas.stringsMeta())
				.hasNoMetadataValue(zeSchemas.textsMeta());
	}

	// @Test
	public void givenMetadatasWithMappedPropertiesWhenPopulatingSavedRecordWithoutValuesThenValuesAreUpdated()
			throws Exception {

		givenConfig(TITLE_METADATA_POPULATE_PRIORITY, TitleMetadataPopulatePriority.PROPERTIES_STYLES_FILENAME);

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnProperties(titleProperty))
				.withStringMeta(mappedOnProperties(titleProperty))
				.withTextMeta(mappedOnProperties(subjectProperty, authorProperty))
				.withStringsMeta(mappedOnProperties(managerProperty, companyProperty, keywordsProperty))
				.withTextsMeta(mappedOnProperties(commentsProperty, authorProperty))));

		Content recordContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties1);
		Record record = new TestRecord(zeSchemas);
		getModelLayerFactory().newRecordServices().add(record);

		record.set(zeSchemas.requiredContent(), recordContent);
		services.populate(record);

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "title1")
				.hasMetadataValue(zeSchemas.stringMeta(), "title1")
				.hasMetadataValue(zeSchemas.textMeta(), "subject1")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("manager1", "company1", "zeKeyword1", "anotherKeyword1"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("comments1", "author1"));
		recordServices.add(record);

		recordContent = record.get(zeSchemas.requiredContent());
		recordContent.updateContent(users.adminIn(zeCollection), documentWithStylesAndProperties2, true);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "title2")
				.hasMetadataValue(zeSchemas.stringMeta(), "title2")
				.hasMetadataValue(zeSchemas.textMeta(), "subject2")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("manager2", "company2", "zeKeyword2", "anotherKeyword2"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("comments2", "author2"));
		recordServices.update(record);

		recordContent = record.get(zeSchemas.requiredContent());
		recordContent.updateContent(users.adminIn(zeCollection), withoutStylesAndProperties, true);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "contract.docx")
				.hasNoMetadataValue(zeSchemas.stringMeta())
				.hasNoMetadataValue(zeSchemas.textMeta())
				.hasNoMetadataValue(zeSchemas.stringsMeta())
				.hasNoMetadataValue(zeSchemas.textsMeta());
	}

	// @Test
	public void givenRecordWithAManuallySettedValueWhenPopulateThenDoesNotOverwriteWithProperties()
			throws Exception {

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnProperties(titleProperty))
				.withStringMeta(mappedOnProperties(titleProperty))
				.withTextMeta(mappedOnProperties(subjectProperty, authorProperty))
				.withStringsMeta(mappedOnProperties(managerProperty, companyProperty, keywordsProperty))
				.withTextsMeta(mappedOnProperties(commentsProperty, authorProperty))));

		Content recordContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties1);
		Record record = new TestRecord(zeSchemas).set(zeSchemas.requiredContent(), recordContent)
				.set(Schemas.TITLE, "My custom title")
				.set(zeSchemas.stringMeta(), "My custom title")
				.set(zeSchemas.textMeta(), "My custom text")
				.set(zeSchemas.stringsMeta(), asList("first value", "second value"))
				.set(zeSchemas.textsMeta(), asList("first value", "second value"));
		recordServices.add(record);

		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "My custom title")
				.hasMetadataValue(zeSchemas.stringMeta(), "My custom title")
				.hasMetadataValue(zeSchemas.textMeta(), "My custom text")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("first value", "second value"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("first value", "second value"));

		Content newContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties2);
		record.set(zeSchemas.requiredContent(), newContent);
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "My custom title")
				.hasMetadataValue(zeSchemas.stringMeta(), "My custom title")
				.hasMetadataValue(zeSchemas.textMeta(), "My custom text")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("first value", "second value"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("first value", "second value"));
	}

	// @Test
	public void givenRecordWithAManuallyRemovedValueWhenPopulateThenDoesOverwriteWithProperties()
			throws Exception {

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnProperties(titleProperty))
				.withStringMeta(mappedOnProperties(titleProperty))
				.withTextMeta(mappedOnProperties(subjectProperty, authorProperty))
				.withStringsMeta(mappedOnProperties(managerProperty, companyProperty, keywordsProperty))
				.withTextsMeta(mappedOnProperties(commentsProperty, authorProperty))));

		Content recordContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties1);
		Record record = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), recordContent)
				.set(zeSchemas.stringMeta(), "My custom title");
		services.populate(record);
		record.set(Schemas.TITLE, null);
		record.set(zeSchemas.stringMeta(), null);
		record.set(zeSchemas.stringsMeta(), null);
		record.set(zeSchemas.textMeta(), null);

		assertThatRecord(record)
				.hasNoMetadataValue(Schemas.TITLE)
				.hasNoMetadataValue(zeSchemas.stringMeta())
				.hasNoMetadataValue(zeSchemas.stringsMeta())
				.hasNoMetadataValue(zeSchemas.textMeta())
				.hasMetadataValue(zeSchemas.textsMeta(), asList("comments1", "author1"));
		recordServices.update(record);

		Content newContent = contentManager.createMajor(admin, "contract.docx", documentWithStylesAndProperties2);
		record.set(zeSchemas.requiredContent(), newContent);
		services.populate(record);

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "contract.docx")
				.hasMetadataValue(zeSchemas.stringMeta(), "title2")
				.hasMetadataValue(zeSchemas.textMeta(), "subject2")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("manager2", "company2", "zeKeyword2", "anotherKeyword2"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("comments2", "author2"));
	}

	// @Test
	public void givenRecordHasMultipleContentsThenPropertiesanExtractedOnAllOfThemPriorizingRequired()
			throws Exception {

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnStyles(titleStyle))
				.withStringMeta(mappedOnStyles(titleStyle))
				.withTextMeta(mappedOnProperties(titleProperty))));

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(zeSchemas.requiredContent().getCode()).setDefaultRequirement(true);
				types.getMetadata(zeSchemas.requiredContents().getCode()).setDefaultRequirement(true);
			}
		});
		schemas.refresh();

		assertThat(zeSchemas.type().getAllMetadatas().onlyWithType(MetadataValueType.CONTENT)
				.sortedUsing(new MetadataSchemaBuilder.ContentsComparator()).toLocalCodesList()).isEqualTo(asList(
				zeSchemas.requiredContent().getLocalCode(), zeSchemas.requiredContents().getLocalCode(),
				zeSchemas.facultativeContent().getLocalCode(), zeSchemas.facultativeContents().getLocalCode()
		));

		Record record = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties1))
				.set(zeSchemas.facultativeContent(), createContent(documentWithStylesAndProperties2))
				.set(zeSchemas.requiredContents(), asList(createContent(documentWithStylesAndProperties3)))
				.set(zeSchemas.facultativeContents(), asList(createContent(documentWithStylesAndProperties4)));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Mon premier contrat")
				.hasMetadataValue(zeSchemas.stringMeta(), "Mon premier contrat")
				.hasMetadataValue(zeSchemas.textMeta(), "title1");

		record = new TestRecord(zeSchemas)
				.set(zeSchemas.facultativeContent(), createContent(documentWithStylesAndProperties2))
				.set(zeSchemas.requiredContents(), asList(createContent(documentWithStylesAndProperties3)))
				.set(zeSchemas.facultativeContents(), asList(createContent(documentWithStylesAndProperties4)));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Title in style 3")
				.hasMetadataValue(zeSchemas.stringMeta(), "Title in style 3")
				.hasMetadataValue(zeSchemas.textMeta(), "title3");

		record = new TestRecord(zeSchemas)
				.set(zeSchemas.facultativeContent(), createContent(documentWithStylesAndProperties2))
				.set(zeSchemas.facultativeContents(), asList(createContent(documentWithStylesAndProperties4)));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "The ring contract")
				.hasMetadataValue(zeSchemas.stringMeta(), "The ring contract")
				.hasMetadataValue(zeSchemas.textMeta(), "title2");

		record = new TestRecord(zeSchemas)
				.set(zeSchemas.facultativeContents(), asList(createContent(documentWithStylesAndProperties4)));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Title in style 4")
				.hasMetadataValue(zeSchemas.stringMeta(), "Title in style 4")
				.hasMetadataValue(zeSchemas.textMeta(), "title4");

	}

	// @Test
	public void givenPropertiesConfigsAreModifiedThenMetadataValuesAreNotRepopulatedWithProperties()
			throws Exception {

		givenConfig(TITLE_METADATA_POPULATE_PRIORITY, TitleMetadataPopulatePriority.PROPERTIES_STYLES_FILENAME);

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withTitle(mappedOnStyles(titleStyle))
				.withStringMeta(mappedOnStyles(titleStyle))
				.withTextMeta(mappedOnProperties(titleProperty))));

		Record record = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties1));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Mon premier contrat")
				.hasMetadataValue(zeSchemas.stringMeta(), "Mon premier contrat")
				.hasMetadataValue(zeSchemas.textMeta(), "title1");
		recordServices.add(record);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(zeSchemas.code() + "_" + Schemas.TITLE_CODE).getPopulateConfigsBuilder()
						.setProperties(asList(authorProperty))
						.setStyles(new ArrayList<String>());
				types.getMetadata(zeSchemas.stringMeta().getCode()).getPopulateConfigsBuilder()
						.setProperties(asList(authorProperty))
						.setStyles(new ArrayList<String>());
				types.getMetadata(zeSchemas.textMeta().getCode()).getPopulateConfigsBuilder()
						.setProperties(new ArrayList<String>())
						.setStyles(asList(clientNameStyle));
			}
		});

		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Mon premier contrat")
				.hasMetadataValue(zeSchemas.stringMeta(), "Mon premier contrat")
				.hasMetadataValue(zeSchemas.textMeta(), "title1");

		Record newRecord = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties1));
		services.populate(newRecord);
		assertThatRecord(newRecord)
				.hasMetadataValue(Schemas.TITLE, "author1")
				.hasMetadataValue(zeSchemas.stringMeta(), "author1")
				.hasMetadataValue(zeSchemas.textMeta(), "Édouard Lechat");
	}

	// @Test
	public void givenStylesArePriorizedOverPropertiesAndPropertiesOverRegexWhenPopulatingMetadatasThenPopulatedValuesBasedOnPriorities()
			throws Exception {

		givenConfig(TITLE_METADATA_POPULATE_PRIORITY, TitleMetadataPopulatePriority.STYLES_PROPERTIES_FILENAME);
		givenConfig(METADATA_POPULATE_PRIORITY, MetadataPopulatePriority.STYLES_PROPERTIES_REGEX);

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withStringMeta(
						mappedOnProperties(titleProperty),
						mappedOnStyles(titleStyle),
						populatedByRegex("Édouard").onMetadata("requiredContent").settingValue(
								"Édouard Lechat").settingRegexConfigType(RegexConfigType.SUBSTITUTION),
						populatedByRegex("Gandalf").onMetadata("requiredContent").settingValue(
								"Gandalf Leblanc").settingRegexConfigType(RegexConfigType.SUBSTITUTION)
				)
				.withTitle(mappedOnProperties(titleProperty), mappedOnStyles(titleStyle))));

		Record record = new TestRecord(zeSchemas).set(zeSchemas.requiredContent(),
				createContent(documentWithStylesAndProperties1));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "Mon premier contrat")
				.hasMetadataValue(Schemas.TITLE, "Mon premier contrat");
		recordServices.add(record);

		record.set(zeSchemas.requiredContent(), contentManager.createMajor(admin, "ze.docx", documentWithStylesAndProperties2));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "The ring contract")
				.hasMetadataValue(Schemas.TITLE, "The ring contract");

		validateThatARecordWithAContentWithStylesAndNoPropertiesAndNoRegexWillPopulateUsingStyles(andTitleIsNotFileName);
		validateThatARecordWithAContentWithPropertiesAndEmptyStylesAndNoRegexWillPopulateUsingProperties(andTitleIsNotFileName);
		validateThatARecordWithAContentWithRegexAndNoPropertiesAndNoStylesWillPopulateUsingRegex();
		validateThatARecordWithAContentWithoutRegexPropertiesAndStylesWillNotBePopulated();

	}

	// @Test
	public void givenStylesArePriorizedOverRegexAndRegexOverPropertiesWhenPopulatingMetadatasThenPopulatedValuesBasedOnPriorities()
			throws Exception {

		givenConfig(TITLE_METADATA_POPULATE_PRIORITY, TitleMetadataPopulatePriority.STYLES_FILENAME_PROPERTIES);
		givenConfig(METADATA_POPULATE_PRIORITY, MetadataPopulatePriority.STYLES_REGEX_PROPERTIES);

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withStringMeta(
						mappedOnProperties(titleProperty),
						mappedOnStyles(titleStyle),
						populatedByRegex("Édouard").onMetadata("requiredContent").settingValue(
								"Édouard Lechat").settingRegexConfigType(RegexConfigType.SUBSTITUTION),
						populatedByRegex("Gandalf").onMetadata("requiredContent").settingValue(
								"Gandalf Leblanc").settingRegexConfigType(RegexConfigType.SUBSTITUTION)
				)
				.withTitle(mappedOnProperties(titleProperty), mappedOnStyles(titleStyle))));

		Record record = new TestRecord(zeSchemas).set(zeSchemas.requiredContent(),
				createContent(documentWithStylesAndProperties1));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "Mon premier contrat")
				.hasMetadataValue(Schemas.TITLE, "Mon premier contrat");
		recordServices.add(record);

		record.set(zeSchemas.requiredContent(), contentManager.createMajor(admin, "ze.docx", documentWithStylesAndProperties2));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "The ring contract")
				.hasMetadataValue(Schemas.TITLE, "The ring contract");

		validateThatARecordWithAContentWithStylesAndNoPropertiesAndNoRegexWillPopulateUsingStyles(andTitleIsNotFileName);
		validateThatARecordWithAContentWithPropertiesAndEmptyStylesAndNoRegexWillPopulateUsingProperties(andTitleIsFileName);
		validateThatARecordWithAContentWithRegexAndNoPropertiesAndNoStylesWillPopulateUsingRegex();
		validateThatARecordWithAContentWithoutRegexPropertiesAndStylesWillNotBePopulated();
	}

	// @Test
	public void givenPropertiesArePriorizedOverRegexAndRegexOverStylesWhenPopulatingMetadatasThenPopulatedValuesBasedOnPriorities()
			throws Exception {

		givenConfig(TITLE_METADATA_POPULATE_PRIORITY, TitleMetadataPopulatePriority.PROPERTIES_FILENAME_STYLES);
		givenConfig(METADATA_POPULATE_PRIORITY, MetadataPopulatePriority.PROPERTIES_REGEX_STYLES);

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withStringMeta(
						mappedOnProperties(titleProperty),
						mappedOnStyles(titleStyle),
						populatedByRegex("Édouard").onMetadata("requiredContent").settingValue(
								"Édouard Lechat").settingRegexConfigType(RegexConfigType.SUBSTITUTION),
						populatedByRegex("Gandalf").onMetadata("requiredContent").settingValue(
								"Gandalf Leblanc").settingRegexConfigType(RegexConfigType.SUBSTITUTION)
				)
				.withTitle(mappedOnProperties(titleProperty), mappedOnStyles(titleStyle))));

		Record record = new TestRecord(zeSchemas).set(zeSchemas.requiredContent(),
				createContent(documentWithStylesAndProperties1));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "title1")
				.hasMetadataValue(Schemas.TITLE, "title1");
		recordServices.add(record);

		record.set(zeSchemas.requiredContent(), contentManager.createMajor(admin, "ze.docx", documentWithStylesAndProperties2));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "title2")
				.hasMetadataValue(Schemas.TITLE, "title2");

		validateThatARecordWithAContentWithStylesAndNoPropertiesAndNoRegexWillPopulateUsingStyles(andTitleIsFileName);
		validateThatARecordWithAContentWithPropertiesAndEmptyStylesAndNoRegexWillPopulateUsingProperties(andTitleIsNotFileName);
		validateThatARecordWithAContentWithRegexAndNoPropertiesAndNoStylesWillPopulateUsingRegex();
		validateThatARecordWithAContentWithoutRegexPropertiesAndStylesWillNotBePopulated();
	}

	// @Test
	public void givenPropertiesArePriorizedOverStylesAndStylesOverRegexWhenPopulatingMetadatasThenPopulatedValuesBasedOnPriorities()
			throws Exception {

		givenConfig(TITLE_METADATA_POPULATE_PRIORITY, TitleMetadataPopulatePriority.PROPERTIES_STYLES_FILENAME);
		givenConfig(METADATA_POPULATE_PRIORITY, MetadataPopulatePriority.PROPERTIES_STYLES_REGEX);

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withStringMeta(
						mappedOnProperties(titleProperty),
						mappedOnStyles(titleStyle),
						populatedByRegex("Édouard").onMetadata("requiredContent").settingValue(
								"Édouard Lechat").settingRegexConfigType(RegexConfigType.SUBSTITUTION),
						populatedByRegex("Gandalf").onMetadata("requiredContent").settingValue(
								"Gandalf Leblanc").settingRegexConfigType(RegexConfigType.SUBSTITUTION)
				)
				.withTitle(mappedOnProperties(titleProperty), mappedOnStyles(titleStyle))));

		Record record = new TestRecord(zeSchemas).set(zeSchemas.requiredContent(),
				createContent(documentWithStylesAndProperties1));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "title1")
				.hasMetadataValue(Schemas.TITLE, "title1");
		recordServices.add(record);

		record.set(zeSchemas.requiredContent(), contentManager.createMajor(admin, "ze.docx", documentWithStylesAndProperties2));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "title2")
				.hasMetadataValue(Schemas.TITLE, "title2");

		validateThatARecordWithAContentWithStylesAndNoPropertiesAndNoRegexWillPopulateUsingStyles(andTitleIsNotFileName);
		validateThatARecordWithAContentWithPropertiesAndEmptyStylesAndNoRegexWillPopulateUsingProperties(andTitleIsNotFileName);
		validateThatARecordWithAContentWithRegexAndNoPropertiesAndNoStylesWillPopulateUsingRegex();
		validateThatARecordWithAContentWithoutRegexPropertiesAndStylesWillNotBePopulated();
	}

	// @Test
	public void givenRegexArePriorizedOverPropertiesAndPropertiesOverStylesWhenPopulatingMetadatasThenPopulatedValuesBasedOnPriorities()
			throws Exception {

		givenConfig(TITLE_METADATA_POPULATE_PRIORITY, TitleMetadataPopulatePriority.FILENAME);
		givenConfig(METADATA_POPULATE_PRIORITY, MetadataPopulatePriority.REGEX_PROPERTIES_STYLES);

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withStringMeta(
						mappedOnProperties(titleProperty),
						mappedOnStyles(titleStyle),
						populatedByRegex("Édouard").onMetadata("requiredContent").settingValue(
								"Édouard Lechat").settingRegexConfigType(RegexConfigType.SUBSTITUTION),
						populatedByRegex("Gandalf").onMetadata("requiredContent").settingValue(
								"Gandalf Leblanc").settingRegexConfigType(RegexConfigType.SUBSTITUTION)
				)
				.withTitle(mappedOnProperties(titleProperty), mappedOnStyles(titleStyle))));

		Record record = new TestRecord(zeSchemas).set(zeSchemas.requiredContent(),
				createContent(documentWithStylesAndProperties1));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "Édouard Lechat")
				.hasMetadataValue(Schemas.TITLE, "file.docx");
		recordServices.add(record);

		record.set(zeSchemas.requiredContent(), contentManager.createMajor(admin, "ze.docx", documentWithStylesAndProperties2));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "Gandalf Leblanc")
				.hasMetadataValue(Schemas.TITLE, "ze.docx");

		validateThatARecordWithAContentWithStylesAndNoPropertiesAndNoRegexWillPopulateUsingStyles(andTitleIsFileName);
		validateThatARecordWithAContentWithPropertiesAndEmptyStylesAndNoRegexWillPopulateUsingProperties(andTitleIsFileName);
		validateThatARecordWithAContentWithRegexAndNoPropertiesAndNoStylesWillPopulateUsingRegex();
		validateThatARecordWithAContentWithoutRegexPropertiesAndStylesWillNotBePopulated();
	}

	// @Test
	public void givenRegexArePriorizedOverStylesAndStylesOverPropertiesWhenPopulatingMetadatasThenPopulatedValuesBasedOnPriorities()
			throws Exception {

		givenConfig(TITLE_METADATA_POPULATE_PRIORITY, TitleMetadataPopulatePriority.FILENAME);
		givenConfig(METADATA_POPULATE_PRIORITY, MetadataPopulatePriority.REGEX_STYLES_PROPERTIES);
		//TODO

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withStringMeta(
						mappedOnProperties(titleProperty),
						mappedOnStyles(titleStyle),
						populatedByRegex("Édouard").onMetadata("requiredContent").settingValue(
								"Édouard Lechat").settingRegexConfigType(RegexConfigType.SUBSTITUTION),
						populatedByRegex("Gandalf").onMetadata("requiredContent").settingValue(
								"Gandalf Leblanc").settingRegexConfigType(RegexConfigType.SUBSTITUTION)
				)
				.withTitle(mappedOnProperties(titleProperty), mappedOnStyles(titleStyle))));

		Record record = new TestRecord(zeSchemas).set(zeSchemas.requiredContent(),
				createContent(documentWithStylesAndProperties1));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "Édouard Lechat")
				.hasMetadataValue(Schemas.TITLE, "file.docx");
		recordServices.add(record);

		record.set(zeSchemas.requiredContent(), contentManager.createMajor(admin, "ze.docx", documentWithStylesAndProperties2));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "Gandalf Leblanc")
				.hasMetadataValue(Schemas.TITLE, "ze.docx");

		validateThatARecordWithAContentWithStylesAndNoPropertiesAndNoRegexWillPopulateUsingStyles(andTitleIsFileName);
		validateThatARecordWithAContentWithPropertiesAndEmptyStylesAndNoRegexWillPopulateUsingProperties(andTitleIsFileName);
		validateThatARecordWithAContentWithRegexAndNoPropertiesAndNoStylesWillPopulateUsingRegex();
		validateThatARecordWithAContentWithoutRegexPropertiesAndStylesWillNotBePopulated();
	}

	// @Test
	public void givenRemoveExtensionFromDocumentIsSelected()
			throws Exception {

		givenConfig(TITLE_METADATA_POPULATE_PRIORITY, TitleMetadataPopulatePriority.FILENAME);
		givenConfig(METADATA_POPULATE_PRIORITY, MetadataPopulatePriority.REGEX_STYLES_PROPERTIES);
		givenConfig(REMOVE_EXTENSION_FROM_RECORD_TITLE, true);
		//TODO

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withStringMeta(
						mappedOnProperties(titleProperty),
						mappedOnStyles(titleStyle),
						populatedByRegex("Édouard").onMetadata("requiredContent").settingValue(
								"Édouard Lechat").settingRegexConfigType(RegexConfigType.SUBSTITUTION),
						populatedByRegex("Gandalf").onMetadata("requiredContent").settingValue(
								"Gandalf Leblanc").settingRegexConfigType(RegexConfigType.SUBSTITUTION)
				)
				.withTitle(mappedOnProperties(titleProperty), mappedOnStyles(titleStyle))));

		Record record = new TestRecord(zeSchemas).set(zeSchemas.requiredContent(),
				createContent(documentWithStylesAndProperties1));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "Édouard Lechat")
				.hasMetadataValue(Schemas.TITLE, "file");
		recordServices.add(record);

		record.set(zeSchemas.requiredContent(), contentManager.createMajor(admin, "ze.docx", documentWithStylesAndProperties2));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "Gandalf Leblanc")
				.hasMetadataValue(Schemas.TITLE, "ze");

		validateThatARecordWithAContentWithStylesAndNoPropertiesAndNoRegexAndNoExtensionWillPopulateUsingStyles(
				andTitleIsFileName);
		validateThatARecordWithAContentWithPropertiesAndEmptyStylesAndNoRegexAndNoExtensionWillPopulateUsingProperties(
				andTitleIsFileName);
		validateThatARecordWithAContentWithRegexAndNoPropertiesAndNoStylesAndNoExtensionWillPopulateUsingRegex();
		validateThatARecordWithAContentWithoutRegexPropertiesAndStylesAndNoExtensionWillNotBePopulated();
	}

	// @Test
	public void whenCreatingADocumentWithACategoryMappedOnADocumentTypeThenSetToCustomSchema()
			throws Exception {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {

				MetadataSchemaBuilder defaultSchema = types.getSchema(Document.DEFAULT_SCHEMA);
				MetadataBuilder typeRef = defaultSchema.get(Document.TYPE);
				MetadataBuilder typeName = types.getSchema(DocumentType.DEFAULT_SCHEMA).get(DocumentType.TITLE);
				defaultSchema.get(Schemas.TITLE.getCode()).getPopulateConfigsBuilder().setStyles(asList(titleStyle));
				defaultSchema.create("typeName").setType(STRING).defineDataEntry().asCopied(typeRef, typeName);

				MetadataSchemaBuilder schema = types.getSchemaType(Document.SCHEMA_TYPE).createCustomSchema("zeUltimateSchema");
				schema.create("aCustomMetadata").setType(STRING).getPopulateConfigsBuilder()
						.setStyles(asList(clientNameStyle));
				schema.get(Schemas.TITLE.getCode()).getPopulateConfigsBuilder().setStyles(asList(companyNameStyle));
			}
		});

		Transaction transaction = new Transaction();
		transaction.add(rm.newDocumentTypeWithId("type1").setTitle("Ze ultimate document type")
				.setCode("ultimateDocument").setLinkedSchema("zeUltimateSchema"));
		transaction.add(rm.newDocumentTypeWithId("type2").setTitle("An other document type").setCode("anotherDocument"));
		recordServices.execute(transaction);

		MetadataSchema documentSchema = rm.defaultDocumentSchema();
		MetadataSchema ultimateDocumentSchema = rm.documentSchemaFor("type1");

		Record record = recordServices.newRecordWithSchema(documentSchema).set(rm.documentFolder(), records.folder_A04);
		record.set(rm.documentContent(), createContent(mappedToACustomSchema));
		recordServices.add(record);

		assertThat(record.getSchemaCode()).isEqualTo(ultimateDocumentSchema.getCode());
		assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "Organisation4");
		assertThatRecord(record).hasMetadataValue(ultimateDocumentSchema.get("aCustomMetadata"), "Client4");
		assertThatRecord(record).hasMetadataValue(ultimateDocumentSchema.get(Document.TYPE), "type1");
		assertThatRecord(record).hasMetadataValue(ultimateDocumentSchema.get("typeName"), "Ze ultimate document type");
		verify(contentManager).getParsedContent(mappedToACustomSchema.getHash());
	}

	// @Test
	public void givenAMetadataPopulatorConfiguredWithRegexWhenAddUpdateRecordThenUpdateValues()
			throws RecordServicesException {
		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withStringMeta(
						populatedByRegex("Édouard").onMetadata("requiredContent")
								.settingValue("Édouard Lechat").settingType(RegexConfigType.SUBSTITUTION).build(),
						populatedByRegex("Gandalf").onMetadata("requiredContent")
								.settingValue("Gandalf Leblanc").settingType(RegexConfigType.SUBSTITUTION).build()
				)
				.withTextMeta(
						populatedByRegex("(A-[0-9]+)").onMetadata("title").settingValue(
								"Formulaire $1").settingType(RegexConfigType.TRANSFORMATION).build()
				)
				.withStringsMeta(
						populatedByRegex("Édouard").onMetadata("requiredContent")
								.settingValue("Édouard Lechat").settingType(RegexConfigType.SUBSTITUTION).build(),
						populatedByRegex("Gandalf").onMetadata("requiredContent")
								.settingValue("Gandalf Leblanc").settingType(RegexConfigType.SUBSTITUTION).build()
				)
				.withTextsMeta(
						populatedByRegex("(A-[0-9]+)").onMetadata("title").settingValue(
								"Formulaire $1").settingType(RegexConfigType.TRANSFORMATION).build()
				)
		));

		Record record = new TestRecord(zeSchemas)
				.set(Schemas.TITLE, "Ze A-39!")
				.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties1));
		MetadataList populated = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(record.getCollection())
				.getSchema(record.getSchemaCode()).getMetadatas().onlyPopulated();
		assertThat(populated).isNotEmpty();
		services.populate(record);

		recordServices.add(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Ze A-39!")
				.hasMetadataValue(zeSchemas.stringMeta(), "Édouard Lechat")
				.hasMetadataValue(zeSchemas.textMeta(), "Formulaire A-39")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Édouard Lechat"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("Formulaire A-39"));

		services.populate(record
				.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties2)));
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Ze A-39!")
				.hasMetadataValue(zeSchemas.stringMeta(), "Gandalf Leblanc")
				.hasMetadataValue(zeSchemas.textMeta(), "Formulaire A-39")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Gandalf Leblanc"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("Formulaire A-39"));

		services.populate(record.set(Schemas.TITLE, "Ze A-38!"));
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Ze A-38!")
				.hasMetadataValue(zeSchemas.stringMeta(), "Gandalf Leblanc")
				.hasMetadataValue(zeSchemas.textMeta(), "Formulaire A-38")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Gandalf Leblanc"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("Formulaire A-38"));
	}

	// @Test
	public void givenARecordWithRegexExtractorsWhenAddUpdateRecordsThenUpdateValues()
			throws Exception {

		givenConfig(METADATA_POPULATE_PRIORITY, MetadataPopulatePriority.REGEX_STYLES_PROPERTIES);

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withStringMeta(
						populatedByRegex("Édouard").onMetadata("requiredContent")
								.settingValue("Édouard Lechat").settingRegexConfigType(RegexConfigType.SUBSTITUTION),
						populatedByRegex("Gandalf").onMetadata("requiredContent")
								.settingValue("Gandalf Leblanc").settingRegexConfigType(RegexConfigType.SUBSTITUTION)
				)
				.withTextMeta(
						populatedByRegex("(A-[0-9]+)").onMetadata("title").settingValue(
								"Formulaire $1").settingRegexConfigType(RegexConfigType.TRANSFORMATION)
				)
				.withStringsMeta(
						populatedByRegex("Édouard").onMetadata("requiredContent")
								.settingValue("Édouard Lechat").settingRegexConfigType(RegexConfigType.SUBSTITUTION),
						populatedByRegex("Gandalf").onMetadata("requiredContent")
								.settingValue("Gandalf Leblanc").settingRegexConfigType(RegexConfigType.SUBSTITUTION)
				)
				.withTextsMeta(
						populatedByRegex("(A-[0-9]+)").onMetadata("title").settingValue(
								"Formulaire $1").settingRegexConfigType(RegexConfigType.TRANSFORMATION)
				)
		));

		Record record = new TestRecord(zeSchemas)
				.set(Schemas.TITLE, "Ze A-39!")
				.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties1));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Ze A-39!")
				.hasMetadataValue(zeSchemas.stringMeta(), "Édouard Lechat")
				.hasMetadataValue(zeSchemas.textMeta(), "Formulaire A-39")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Édouard Lechat"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("Formulaire A-39"));
		recordServices.add(record);

		services.populate(record
				.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties2)));
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Ze A-39!")
				.hasMetadataValue(zeSchemas.stringMeta(), "Gandalf Leblanc")
				.hasMetadataValue(zeSchemas.textMeta(), "Formulaire A-39")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Gandalf Leblanc"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("Formulaire A-39"));

		services.populate(record.set(Schemas.TITLE, "Ze A-38!"));
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Ze A-38!")
				.hasMetadataValue(zeSchemas.stringMeta(), "Gandalf Leblanc")
				.hasMetadataValue(zeSchemas.textMeta(), "Formulaire A-38")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Gandalf Leblanc"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("Formulaire A-38"));
	}

	// @Test
	public void givenARecordHasACustomValuesToMetadatasPopulatedByRegexesWhenUpdatedThenCustomValuesKept()
			throws Exception {

		givenConfig(METADATA_POPULATE_PRIORITY, MetadataPopulatePriority.REGEX_STYLES_PROPERTIES);

		defineSchemasManager().using(schemas.with(fourMetadatas()
				.withStringMeta(
						populatedByRegex("Édouard").onMetadata("requiredContent")
								.settingValue("Édouard Lechat").settingRegexConfigType(RegexConfigType.SUBSTITUTION),
						populatedByRegex("Gandalf").onMetadata("requiredContent")
								.settingValue("Gandalf Leblanc").settingRegexConfigType(RegexConfigType.SUBSTITUTION)
				)
				.withTextMeta(
						populatedByRegex("(A-[0-9]+)").onMetadata("title").settingValue(
								"Formulaire $1").settingRegexConfigType(RegexConfigType.TRANSFORMATION)
				)
				.withStringsMeta(
						populatedByRegex("Édouard").onMetadata("requiredContent")
								.settingValue("Édouard Lechat").settingRegexConfigType(RegexConfigType.SUBSTITUTION),
						populatedByRegex("Gandalf").onMetadata("requiredContent")
								.settingValue("Gandalf Leblanc").settingRegexConfigType(RegexConfigType.SUBSTITUTION)
				)
				.withTextsMeta(
						populatedByRegex("(A-[0-9]+)").onMetadata("title").settingValue(
								"Formulaire $1").settingRegexConfigType(RegexConfigType.TRANSFORMATION)
				)
		));

		Record record = new TestRecord(zeSchemas)
				.set(Schemas.TITLE, "Ze A-39!")
				.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties1));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Ze A-39!")
				.hasMetadataValue(zeSchemas.stringMeta(), "Édouard Lechat")
				.hasMetadataValue(zeSchemas.textMeta(), "Formulaire A-39")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("Édouard Lechat"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("Formulaire A-39"));
		recordServices.add(record);

		services.populate(record
				.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties2))
				.set(zeSchemas.stringMeta(), "customStringValue")
				.set(zeSchemas.textMeta(), "customTextValue")
				.set(zeSchemas.stringsMeta(), asList("customStringsValue"))
				.set(zeSchemas.textsMeta(), asList("customTextsValue")));

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Ze A-39!")
				.hasMetadataValue(zeSchemas.stringMeta(), "customStringValue")
				.hasMetadataValue(zeSchemas.textMeta(), "customTextValue")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("customStringsValue"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("customTextsValue"));

		services.populate(record
				.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndProperties1)));

		assertThatRecord(record)
				.hasMetadataValue(Schemas.TITLE, "Ze A-39!")
				.hasMetadataValue(zeSchemas.stringMeta(), "customStringValue")
				.hasMetadataValue(zeSchemas.textMeta(), "customTextValue")
				.hasMetadataValue(zeSchemas.stringsMeta(), asList("customStringsValue"))
				.hasMetadataValue(zeSchemas.textsMeta(), asList("customTextsValue"))
		;
	}

	// @Test
	public void givenAOriginalRecordWithAContentAndPopulatedValuesAndARecordWithAContentThenValuesWillBeOverwritten() {
		Record originalRecord = recordServices.newRecordWithSchema(types.getDefaultSchema(Document.SCHEMA_TYPE));
		originalRecord.set(rm.document.content(), createContent(documentWithStylesAndProperties1));
		originalRecord.set(rm.document.company(), "customCompany");
		services.populate(originalRecord);

		assertThatRecord(originalRecord)
				.hasMetadataValue(rm.document.author(), "author1")
				.hasMetadataValue(rm.document.keywords(), asList("zeKeyword1", "anotherKeyword1"))
				.hasMetadataValue(rm.document.company(), "customCompany")
				.hasMetadataValue(rm.document.subject(), "subject1");

		Record record = (Record) SerializationUtils.clone(originalRecord);
		record.set(rm.document.content(), createContent(documentWithStylesAndProperties2));
		services.populate(record, originalRecord);

		assertThatRecord(record)
				.hasMetadataValue(rm.document.author(), "author2")
				.hasMetadataValue(rm.document.keywords(), asList("zeKeyword2", "anotherKeyword2"))
				.hasMetadataValue(rm.document.company(), "customCompany")
				.hasMetadataValue(rm.document.subject(), "subject2");
	}

	// ---------------------------------------------------------------------

	private void validateThatARecordWithAContentWithRegexAndNoPropertiesAndNoStylesWillPopulateUsingRegex() {
		Record record = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), createContent(onlyWithRegex));
		services.populate(record);
		assertThatRecord(record).hasMetadataValue(zeSchemas.stringMeta(), "Édouard Lechat");
		assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "file.docx");
	}

	private void validateThatARecordWithAContentWithRegexAndNoPropertiesAndNoStylesAndNoExtensionWillPopulateUsingRegex() {
		Record record = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), createContent(onlyWithRegex));
		services.populate(record);
		assertThatRecord(record).hasMetadataValue(zeSchemas.stringMeta(), "Édouard Lechat");
		assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "file");
	}

	private void validateThatARecordWithAContentWithoutRegexPropertiesAndStylesWillNotBePopulated() {
		Record record = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), createContent(documentWithEmptyStylesAndNoProperties));
		services.populate(record);
		assertThatRecord(record).hasNoMetadataValue(zeSchemas.stringMeta());
		assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "file.docx");
	}

	private void validateThatARecordWithAContentWithoutRegexPropertiesAndStylesAndNoExtensionWillNotBePopulated() {
		Record record = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), createContent(documentWithEmptyStylesAndNoProperties));
		services.populate(record);
		assertThatRecord(record).hasNoMetadataValue(zeSchemas.stringMeta());
		assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "file");
	}

	private void validateThatARecordWithAContentWithStylesAndNoPropertiesAndNoRegexWillPopulateUsingStyles(
			boolean titleIsFileName) {
		Record record = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndNoProperties));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "Mon premier contrat");

		if (titleIsFileName) {
			assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "file.docx");
		} else {
			assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "Mon premier contrat");
		}
	}

	private void validateThatARecordWithAContentWithStylesAndNoPropertiesAndNoRegexAndNoExtensionWillPopulateUsingStyles(
			boolean titleIsFileName) {
		Record record = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), createContent(documentWithStylesAndNoProperties));
		services.populate(record);
		assertThatRecord(record)
				.hasMetadataValue(zeSchemas.stringMeta(), "Mon premier contrat");

		if (titleIsFileName) {
			assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "file");
		} else {
			assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "Mon premier contrat");
		}
	}

	private void validateThatARecordWithAContentWithPropertiesAndEmptyStylesAndNoRegexAndNoExtensionWillPopulateUsingProperties(
			boolean titleIsFileName) {
		Record record = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), createContent(documentWithEmptyStylesAndProperties));
		services.populate(record);
		assertThatRecord(record).hasMetadataValue(zeSchemas.stringMeta(), "zeTitle");

		if (titleIsFileName) {
			assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "file");
		} else {
			assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "zeTitle");
		}
	}

	private void validateThatARecordWithAContentWithPropertiesAndEmptyStylesAndNoRegexWillPopulateUsingProperties(
			boolean titleIsFileName) {
		Record record = new TestRecord(zeSchemas)
				.set(zeSchemas.requiredContent(), createContent(documentWithEmptyStylesAndProperties));
		services.populate(record);
		assertThatRecord(record).hasMetadataValue(zeSchemas.stringMeta(), "zeTitle");

		if (titleIsFileName) {
			assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "file.docx");
		} else {
			assertThatRecord(record).hasMetadataValue(Schemas.TITLE, "zeTitle");
		}
	}

	private Content createContent(ContentVersionDataSummary dataSummary) {
		return contentManager.createMajor(admin, "file.docx", dataSummary);
	}

	@Before
	public void setUp()
			throws Exception {

		withSpiedServices(ContentManager.class);
		prepareSystem(withZeCollection().withConstellioRMModule().withRobotsModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus()
				.withAllTest(users));
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);
		admin = users.adminIn(zeCollection);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		services = getModelLayerFactory().newRecordPopulateServices();
		recordServices = getModelLayerFactory().newRecordServices();
		contentManager = getModelLayerFactory().getContentManager();
		robotsSchemas = new RobotSchemaRecordServices(zeCollection, getAppLayerFactory());

		documentWithStylesAndProperties1 = contentManager
				.upload(getTestResourceInputStream("DocumentWithStylesAndProperties1.docx"),
						new UploadOptions().setHandleDeletionOfUnreferencedHashes(false)
								.setFileName("DocumentWithStylesAndProperties1.docx")).getContentVersionDataSummary();
		documentWithStylesAndProperties2 = contentManager
				.upload(getTestResourceInputStream("DocumentWithStylesAndProperties2.docx"),
						new UploadOptions().setHandleDeletionOfUnreferencedHashes(false)
								.setFileName("DocumentWithStylesAndProperties2.docx")).getContentVersionDataSummary();
		documentWithStylesAndProperties3 = contentManager
				.upload(getTestResourceInputStream("DocumentWithStylesAndProperties3.docx"),
						new UploadOptions().setHandleDeletionOfUnreferencedHashes(false)
								.setFileName("DocumentWithStylesAndProperties3.docx")).getContentVersionDataSummary();
		documentWithStylesAndProperties4 = contentManager
				.upload(getTestResourceInputStream("DocumentWithStylesAndProperties4.docx"),
						new UploadOptions().setHandleDeletionOfUnreferencedHashes(false)
								.setFileName("DocumentWithStylesAndProperties4.docx")).getContentVersionDataSummary();
		documentWithEmptyStylesAndNoProperties = contentManager
				.upload(getTestResourceInputStream("DocumentWithEmptyStylesAndNoProperties.docx"),
						new UploadOptions().setHandleDeletionOfUnreferencedHashes(false)
								.setFileName("DocumentWithEmptyStylesAndNoProperties.docx"))
				.getContentVersionDataSummary();
		documentWithEmptyStylesAndProperties = contentManager
				.upload(getTestResourceInputStream("DocumentWithEmptyStylesAndWithProperties.docx"),
						new UploadOptions().setHandleDeletionOfUnreferencedHashes(false)
								.setFileName("DocumentWithEmptyStylesAndWithProperties.docx"))
				.getContentVersionDataSummary();
		onlyWithRegex = contentManager.upload(getTestResourceInputStream("onlyWithRegex.docx"),
				new UploadOptions().setHandleDeletionOfUnreferencedHashes(false).setFileName("OnlyWithRegex.docx"))
				.getContentVersionDataSummary();
		documentWithStylesAndNoProperties = contentManager
				.upload(getTestResourceInputStream("DocumentWithStylesAndNoProperties.docx"));
		withoutStylesAndProperties = contentManager.upload(getTestResourceInputStream("withoutStylesAndProperties.docx"));
		mappedToACustomSchema = contentManager.upload(getTestResourceInputStream("DocumentMappedToACustomSchema.docx"));
		Mockito.reset(contentManager);
	}

	private MetadataBuilderConfigurator defaultValue(final Object value) {
		return new MetadataBuilderConfigurator() {
			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setDefaultValue(value);
			}
		};
	}

	private MetadataBuilderConfigurator mappedOnStyles(final String... styles) {
		return new MetadataBuilderConfigurator() {
			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.getPopulateConfigsBuilder().setStyles(asList(styles));
			}
		};
	}

	private OngoingRegexConfig populatedByRegex(String regex) {
		return new OngoingRegexConfig(regex);

	}

	private MetadataBuilderConfigurator mappedOnProperties(final String... properties) {
		return new MetadataBuilderConfigurator() {
			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.getPopulateConfigsBuilder().setProperties(asList(properties));
			}
		};
	}

	private RecordPopulateServicesAcceptTestMetadataSchemaTypesConfigurator fourMetadatas() {
		return new RecordPopulateServicesAcceptTestMetadataSchemaTypesConfigurator();
	}

	private class RecordPopulateServicesAcceptTestMetadataSchemaTypesConfigurator
			implements MetadataSchemaTypesConfigurator {

		private List<MetadataBuilderConfigurator> titleConfigurators = new ArrayList<>();
		private List<MetadataBuilderConfigurator> stringMetaConfigurators = new ArrayList<>();
		private List<MetadataBuilderConfigurator> textMetaConfigurators = new ArrayList<>();
		private List<MetadataBuilderConfigurator> stringsMetaConfigurators = new ArrayList<>();
		private List<MetadataBuilderConfigurator> textsMetaConfigurators = new ArrayList<>();

		@Override
		public void configure(MetadataSchemaTypesBuilder schemaTypes) {
			MetadataSchemaBuilder zeSchemaBuilder = schemaTypes.getSchema(zeSchemas.code());

			MetadataBuilder title = zeSchemaBuilder.get(Schemas.TITLE_CODE);
			for (MetadataBuilderConfigurator configurator : titleConfigurators) {
				configurator.configure(title, schemaTypes);
			}

			MetadataBuilder stringMeta = zeSchemaBuilder.create("stringMeta").setType(STRING);
			for (MetadataBuilderConfigurator configurator : stringMetaConfigurators) {
				configurator.configure(stringMeta, schemaTypes);
			}

			MetadataBuilder textMeta = zeSchemaBuilder.create("textMeta").setType(MetadataValueType.TEXT);
			for (MetadataBuilderConfigurator configurator : textMetaConfigurators) {
				configurator.configure(textMeta, schemaTypes);
			}

			MetadataBuilder stringsMeta = zeSchemaBuilder.create("stringsMeta").setType(STRING)
					.setMultivalue(true);
			for (MetadataBuilderConfigurator configurator : stringsMetaConfigurators) {
				configurator.configure(stringsMeta, schemaTypes);
			}

			MetadataBuilder textsMeta = zeSchemaBuilder.create("textsMeta").setType(MetadataValueType.TEXT).setMultivalue(true);
			for (MetadataBuilderConfigurator configurator : textsMetaConfigurators) {
				configurator.configure(textsMeta, schemaTypes);
			}

			zeSchemaBuilder.create("requiredContent").setType(MetadataValueType.CONTENT);
			zeSchemaBuilder.create("requiredContents").setType(MetadataValueType.CONTENT).setMultivalue(true);
			zeSchemaBuilder.create("facultativeContent").setType(MetadataValueType.CONTENT);
			zeSchemaBuilder.create("facultativeContents").setType(MetadataValueType.CONTENT).setMultivalue(true);
		}

		public RecordPopulateServicesAcceptTestMetadataSchemaTypesConfigurator withTitle(
				MetadataBuilderConfigurator... configs) {
			titleConfigurators = asList(configs);
			return this;
		}

		public RecordPopulateServicesAcceptTestMetadataSchemaTypesConfigurator withStringMeta(
				MetadataBuilderConfigurator... configs) {
			stringMetaConfigurators = asList(configs);
			return this;
		}

		public RecordPopulateServicesAcceptTestMetadataSchemaTypesConfigurator withTextMeta(
				MetadataBuilderConfigurator... configs) {
			textMetaConfigurators = asList(configs);
			return this;
		}

		public RecordPopulateServicesAcceptTestMetadataSchemaTypesConfigurator withStringsMeta(
				MetadataBuilderConfigurator... configs) {
			stringsMetaConfigurators = asList(configs);
			return this;
		}

		public RecordPopulateServicesAcceptTestMetadataSchemaTypesConfigurator withTextsMeta(
				MetadataBuilderConfigurator... configs) {
			textsMetaConfigurators = asList(configs);
			return this;
		}
	}

	public static class OngoingRegexConfig {
		String regex;
		String metadata;
		String value;
		RegexConfigType type;

		public OngoingRegexConfig(String regex) {
			this.regex = regex;
		}

		public OngoingRegexConfig onMetadata(String metadata) {
			this.metadata = metadata;
			return this;
		}

		public OngoingRegexConfig onMetadata(Metadata metadata) {
			return onMetadata(metadata.getLocalCode());
		}

		public OngoingRegexConfig settingValue(String value) {
			this.value = value;
			return this;
		}

		public OngoingRegexConfig withReplacement() {
			this.type = RegexConfigType.TRANSFORMATION;
			return this;
		}

		public OngoingRegexConfig settingType(RegexConfigType regexConfigType) {
			this.type = regexConfigType;
			return this;
		}

		public MetadataBuilderConfigurator settingRegexConfigType(RegexConfigType regexConfigType) {
			this.type = regexConfigType;
			return build();
		}

		private MetadataBuilderConfigurator build() {
			final RegexConfig config = new RegexConfig(metadata, Pattern.compile(regex), value, type);

			return new MetadataBuilderConfigurator() {
				@Override
				public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
					builder.getPopulateConfigsBuilder().getRegexes().add(config);
				}
			};
		}

		public MetadataBuilderConfigurator convertToMetatdataPopulator() {
			final RegexConfig config = new RegexConfig(metadata, Pattern.compile(regex), value, type);
			final DefaultMetadataPopulator metadataPopulator = new DefaultMetadataPopulator(
					new RegexExtractor(config.getRegex().pattern(), config.getRegexConfigType() == RegexConfigType.TRANSFORMATION,
							config.getValue()),
					new MetadataToText(metadata));
			return new MetadataBuilderConfigurator() {
				@Override
				public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
					builder.getPopulateConfigsBuilder().getMetadataPopulators().add(metadataPopulator);
				}
			};
		}

	}

	private static class RecordPopulateServicesAcceptTest_ZeSchemaMetadatas extends ZeSchemaMetadatasAdapter {

		private RecordPopulateServicesAcceptTest_ZeSchemaMetadatas(ZeSchemaMetadatas zeSchemaMetadatas) {
			super(zeSchemaMetadatas);
		}

		public Metadata requiredContent() {
			return metadataWithCode(code() + "_" + "requiredContent");
		}

		public Metadata requiredContents() {
			return metadataWithCode(code() + "_" + "requiredContents");
		}

		public Metadata facultativeContent() {
			return metadataWithCode(code() + "_" + "facultativeContent");
		}

		public Metadata facultativeContents() {
			return metadataWithCode(code() + "_" + "facultativeContents");
		}

		public Metadata stringMeta() {
			return metadataWithCode(code() + "_" + "stringMeta");
		}

		public Metadata textMeta() {
			return metadataWithCode(code() + "_" + "textMeta");
		}

		public Metadata stringsMeta() {
			return metadataWithCode(code() + "_" + "stringsMeta");
		}

		public Metadata textsMeta() {
			return metadataWithCode(code() + "_" + "textsMeta");
		}

	}

}
