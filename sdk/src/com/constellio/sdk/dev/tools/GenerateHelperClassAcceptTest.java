package com.constellio.sdk.dev.tools;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.rm.model.ExternalLinkType;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.UserFunction;
import com.constellio.app.modules.rm.wrappers.triggers.Trigger;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerAction;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerActionType;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerType;
import com.constellio.app.modules.rm.wrappers.triggers.actions.MoveInFolderTriggerAction;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.type.StorageSpaceType;
import com.constellio.app.modules.rm.wrappers.type.YearType;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.model.wrappers.RobotLog;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflow;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowInstance;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.CapsuleLanguage;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.ThesaurusConfig;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@MainTest
public class GenerateHelperClassAcceptTest extends ConstellioTest {

	@Test
	public void generateRobotsSchemas()
			throws Exception {
		givenCollection(zeCollection).withRobotsModule();

		Map<String, Class<? extends RecordWrapper>> wrappers = new HashMap<>();

		wrappers.put(ActionParameters.DEFAULT_SCHEMA, ActionParameters.class);
		wrappers.put(Robot.DEFAULT_SCHEMA, Robot.class);
		wrappers.put(RobotLog.DEFAULT_SCHEMA, RobotLog.class);

		System.out.println(header());

		printGeneratedSchemas(wrappers, true);

		System.out.println(footer());
	}

	@Test
	public void generateCoreSchemas()
			throws Exception {
		givenCollection(zeCollection);

		Map<String, Class<? extends RecordWrapper>> wrappers = new HashMap<>();

		wrappers.put(User.DEFAULT_SCHEMA, User.class);
		wrappers.put(Group.DEFAULT_SCHEMA, Group.class);
		wrappers.put(UserCredential.DEFAULT_SCHEMA, UserCredential.class);
		wrappers.put(GlobalGroup.DEFAULT_SCHEMA, GlobalGroup.class);
		wrappers.put(Event.DEFAULT_SCHEMA, Event.class);
		wrappers.put(Collection.DEFAULT_SCHEMA, Collection.class);
		wrappers.put(EmailToSend.DEFAULT_SCHEMA, EmailToSend.class);
		wrappers.put(Facet.DEFAULT_SCHEMA, Facet.class);
		wrappers.put(UserDocument.DEFAULT_SCHEMA, UserDocument.class);
		wrappers.put(Authorization.DEFAULT_SCHEMA, Authorization.class);
		wrappers.put(Report.DEFAULT_SCHEMA, Report.class);
		wrappers.put(Printable.DEFAULT_SCHEMA, Printable.class);
		wrappers.put(UserFolder.DEFAULT_SCHEMA, UserFolder.class);
		wrappers.put(TemporaryRecord.DEFAULT_SCHEMA, TemporaryRecord.class);
		wrappers.put(ImportAudit.SCHEMA, ImportAudit.class);
		wrappers.put(ExportAudit.SCHEMA, ExportAudit.class);
		wrappers.put(Capsule.DEFAULT_SCHEMA, Capsule.class);
		wrappers.put(SearchEvent.DEFAULT_SCHEMA, SearchEvent.class);
		wrappers.put(ThesaurusConfig.DEFAULT_SCHEMA, ThesaurusConfig.class);
		wrappers.put(CapsuleLanguage.DEFAULT_SCHEMA, CapsuleLanguage.class);
		wrappers.put(SavedSearch.DEFAULT_SCHEMA, SavedSearch.class);

		System.out.println(header());

		printGeneratedSchemas(wrappers, false);

		System.out.println(footer());
	}

	@Test
	public void generateRMSchemas()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule();

		Map<String, Class<? extends RecordWrapper>> wrappers = new HashMap<>();

		wrappers.put(AdministrativeUnit.DEFAULT_SCHEMA, AdministrativeUnit.class);
		wrappers.put(Category.DEFAULT_SCHEMA, Category.class);
		wrappers.put(UniformSubdivision.DEFAULT_SCHEMA, UniformSubdivision.class);

		wrappers.put(DecommissioningList.DEFAULT_SCHEMA, DecommissioningList.class);
		wrappers.put(Cart.DEFAULT_SCHEMA, Cart.class);
		wrappers.put(RetentionRule.DEFAULT_SCHEMA, RetentionRule.class);
		wrappers.put(Folder.DEFAULT_SCHEMA, Folder.class);
		wrappers.put(Document.DEFAULT_SCHEMA, Document.class);
		wrappers.put(Email.SCHEMA, Email.class);
		wrappers.put(DocumentType.DEFAULT_SCHEMA, DocumentType.class);
		wrappers.put(FolderType.DEFAULT_SCHEMA, FolderType.class);

		wrappers.put(ContainerRecord.DEFAULT_SCHEMA, ContainerRecord.class);
		wrappers.put(StorageSpace.DEFAULT_SCHEMA, StorageSpace.class);
		wrappers.put(StorageSpaceType.DEFAULT_SCHEMA, StorageSpaceType.class);
		wrappers.put(PrintableLabel.SCHEMA_NAME, PrintableLabel.class);
		wrappers.put(PrintableReport.SCHEMA_NAME, PrintableReport.class);

		wrappers.put(RMUserFolder.DEFAULT_SCHEMA, RMUserFolder.class);
		wrappers.put(RMTask.DEFAULT_SCHEMA, RMTask.class);
		wrappers.put(SIParchive.SCHEMA, SIParchive.class);
		wrappers.put(YearType.DEFAULT_SCHEMA, YearType.class);
		wrappers.put(UserFunction.DEFAULT_SCHEMA, UserFunction.class);

		wrappers.put(TriggerType.DEFAULT_SCHEMA, TriggerType.class);
		wrappers.put(TriggerActionType.DEFAULT_SCHEMA, TriggerActionType.class);
		wrappers.put(Trigger.DEFAULT_SCHEMA, Trigger.class);
		wrappers.put(TriggerAction.DEFAULT_SCHEMA, Trigger.class);
		wrappers.put(MoveInFolderTriggerAction.DEFAULT_SCHEMA, MoveInFolderTriggerAction.class);

		wrappers.put(ExternalLink.DEFAULT_SCHEMA, ExternalLink.class);
		wrappers.put(ExternalLinkType.DEFAULT_SCHEMA, ExternalLinkType.class);


		System.out.println(header());

		printGeneratedSchemas(wrappers, false);

		System.out.println(footer());
	}

	@Test
	public void generateEnterpriseSearchSchemas()
			throws Exception {
		givenCollection(zeCollection).withConstellioESModule();

		Map<String, Class<? extends RecordWrapper>> wrappers = new HashMap<>();

		// Commons
		wrappers.put(ConnectorType.DEFAULT_SCHEMA, ConnectorType.class);
		wrappers.put(ConnectorInstance.DEFAULT_SCHEMA, ConnectorInstance.class);

		// HTTP
		wrappers.put(ConnectorHttpInstance.SCHEMA_CODE, ConnectorHttpInstance.class);
		wrappers.put(ConnectorHttpDocument.DEFAULT_SCHEMA, ConnectorHttpDocument.class);

		// SMB
		wrappers.put(ConnectorSmbInstance.SCHEMA_CODE, ConnectorSmbInstance.class);
		wrappers.put(ConnectorSmbDocument.DEFAULT_SCHEMA, ConnectorSmbDocument.class);
		wrappers.put(ConnectorSmbFolder.DEFAULT_SCHEMA, ConnectorSmbFolder.class);

		// LDAP
		wrappers.put(ConnectorLDAPInstance.SCHEMA_CODE, ConnectorLDAPInstance.class);
		wrappers.put(ConnectorLDAPUserDocument.DEFAULT_SCHEMA, ConnectorLDAPUserDocument.class);

		System.out.println(header());

		printGeneratedSchemas(wrappers, true);

		System.out.println(footer());
	}

	@Test
	public void generateTasksSchemas()
			throws Exception {
		givenCollection(zeCollection).withTaskModule();

		Map<String, Class<? extends RecordWrapper>> wrappers = new HashMap<>();

		// Task
		wrappers.put(Task.DEFAULT_SCHEMA, Task.class);
		wrappers.put(BetaWorkflowTask.DEFAULT_SCHEMA, BetaWorkflowTask.class);
		wrappers.put(TaskType.DEFAULT_SCHEMA, TaskType.class);
		wrappers.put(TaskStatus.DEFAULT_SCHEMA, TaskStatus.class);
		wrappers.put(BetaWorkflow.DEFAULT_SCHEMA, BetaWorkflow.class);
		wrappers.put(BetaWorkflowInstance.DEFAULT_SCHEMA, BetaWorkflowInstance.class);

		System.out.println(header());

		printGeneratedSchemas(wrappers, true);

		System.out.println(footer());
	}

	protected void printGeneratedSchemas(Map<String, Class<? extends RecordWrapper>> wrappers, boolean appLayer)
			throws Exception {

		StringBuilder stringBuilder = new StringBuilder();

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		for (MetadataSchemaType type : types.getSchemaTypes()) {

			MetadataSchema defaultSchema = type.getDefaultSchema();

			Class<? extends RecordWrapper> defaultSchemaWrapperClass = wrappers.get(defaultSchema.getCode());

			if (defaultSchemaWrapperClass != null) {
				boolean hasConstructorWithLocale = false;
				for (Constructor constructor : defaultSchemaWrapperClass.getConstructors()) {
					if (constructor.getParameterTypes().length > 0) {
						hasConstructorWithLocale |= constructor.getParameterTypes()
								[constructor.getParameterTypes().length - 1].equals(Locale.class);
					}
				}

				stringBuilder.append(generateSchemaHelperMethods(defaultSchema, defaultSchemaWrapperClass.getSimpleName(),
						hasConstructorWithLocale));
				stringBuilder.append(metadatasHelperMethod(type, defaultSchema, defaultSchemaWrapperClass));
			}

			for (MetadataSchema schema : type.getCustomSchemas()) {

				Class<? extends RecordWrapper> wrapperClass = wrappers.get(schema.getCode());

				if (wrapperClass != null) {
					boolean hasConstructorWithLocale = false;
					for (Constructor constructor : wrapperClass.getConstructors()) {
						if (constructor.getParameterTypes().length > 0) {
							hasConstructorWithLocale |= constructor.getParameterTypes()
									[constructor.getParameterTypes().length - 1].equals(Locale.class);
						}
					}

					stringBuilder
							.append(generateSchemaHelperMethods(schema, wrapperClass.getSimpleName(), hasConstructorWithLocale));
					stringBuilder.append(metadatasHelperMethod(type, schema, wrapperClass));
				}

			}
		}

		String code = stringBuilder.toString();

		if (!appLayer) {
			code = code.replace("appLayerFactory.getModelLayerFactory()", "modelLayerFactory");
		}

		System.out.println(code);
	}

	private String generateSchemaHelperMethods(MetadataSchema schema, String wrapperName, boolean withLocale) {
		StringBuilder stringBuilder = new StringBuilder();

		appendWrapElementHelperMethod(schema, wrapperName, withLocale, stringBuilder);
		appendWrapElementsHelperMethod(schema, wrapperName, withLocale, stringBuilder);
		appendSearchByQueryElementsHelperMethod(schema, wrapperName, stringBuilder);
		appendSearchByConditionElementsHelperMethod(schema, wrapperName, stringBuilder);

		CacheConfig cacheConfig = getModelLayerFactory().getRecordsCaches().getCache(schema.getCollection())
				.getCacheConfigOf(SchemaUtils.getSchemaTypeCode(schema.getCode()));
		if (cacheConfig != null && cacheConfig.isPermanent()) {
			appendIterateFromCacheHelperMethod(schema, wrapperName, stringBuilder);
			appendStreamFromCacheHelperMethod(schema, wrapperName, stringBuilder);
		} else {
			appendIterateFromConditionHelperMethod(schema, wrapperName, stringBuilder);
			appendStreamFromConditionHelperMethod(schema, wrapperName, stringBuilder);
			appendIterateFromQueryHelperMethod(schema, wrapperName, stringBuilder);
			appendStreamFromQueryHelperMethod(schema, wrapperName, stringBuilder);
		}
		appendGetByIdHelperMethod(schema, wrapperName, stringBuilder);
		appendGetByIdsHelperMethod(schema, wrapperName, stringBuilder);

		if (schema.hasMetadataWithCode("code")) {
			appendGetByCodeHelperMethod(schema, wrapperName, stringBuilder);
		}

		appendGetByLegacyIdHelperMethod(schema, wrapperName, stringBuilder);
		appendNewHelperMethod(schema, wrapperName, stringBuilder);
		appendNewWithIdHelperMethod(schema, wrapperName, stringBuilder);
		return stringBuilder.toString();
	}

	private void appendGetByCodeHelperMethod(MetadataSchema schema, String wrapperName, StringBuilder stringBuilder) {

		String schemaTypeCall = schemaTypeCallerFor(schema);

		stringBuilder.append("\n\tpublic " + wrapperName + " get" + wrapperName + "WithCode(String code) {");
		stringBuilder.append("\n\t\treturn wrap" + wrapperName + "(getByCode(" + schemaTypeCall + ", code));");
		stringBuilder.append("\n\t}\n");
	}

	private void appendNewHelperMethod(MetadataSchema schema, String wrapperName, StringBuilder stringBuilder) {

		String schemaCall = schemaCallerFor(schema);

		stringBuilder.append("\n\tpublic " + wrapperName + " new" + wrapperName + "() {");
		stringBuilder.append("\n\t\treturn wrap" + wrapperName + "(create(" + schemaCall + "));");
		stringBuilder.append("\n\t}\n");
	}

	private String schemaCallerFor(MetadataSchema schema) {
		if (schema.getLocalCode().equals("default")) {
			return new SchemaUtils().getSchemaTypeCode(schema.getCode()) + ".schema()";
		} else {
			return schema.getCode() + ".schema()";
		}
	}

	private String schemaTypeCallerFor(MetadataSchema schema) {
		return new SchemaUtils().getSchemaTypeCode(schema.getCode()) + ".schemaType()";
	}

	private void appendNewWithIdHelperMethod(MetadataSchema schema, String wrapperName, StringBuilder stringBuilder) {

		String schemaCall = schemaCallerFor(schema);

		stringBuilder.append("\n\tpublic " + wrapperName + " new" + wrapperName + "WithId(String id) {");
		stringBuilder.append("\n\t\treturn wrap" + wrapperName + "(create(" + schemaCall + ", id));");
		stringBuilder.append("\n\t}\n");
	}

	private void appendGetByLegacyIdHelperMethod(MetadataSchema schema, String wrapperName,
												 StringBuilder stringBuilder) {

		String schemaTypeCall = schemaTypeCallerFor(schema);

		stringBuilder.append("\n\tpublic " + wrapperName + " get" + wrapperName + "WithLegacyId(String legacyId) {");
		stringBuilder.append("\n\t\treturn wrap" + wrapperName + "(getByLegacyId(" + schemaTypeCall + ",  legacyId));");
		stringBuilder.append("\n\t}\n");
	}

	private void appendStreamFromCacheHelperMethod(MetadataSchema schema, String wrapperName,
												   StringBuilder stringBuilder) {
		String schemaTypeCall = schemaTypeCallerFor(schema);
		stringBuilder.append("\n\tpublic Stream<" + wrapperName + "> " + StringUtils.uncapitalize(wrapperName) + "Stream() {");
		stringBuilder.append("\n\t\treturn streamFromCache(" + schemaTypeCall + ",this::wrap" + wrapperName + ");");
		stringBuilder.append("\n\t}\n");
	}


	private void appendIterateFromCacheHelperMethod(MetadataSchema schema, String wrapperName,
													StringBuilder stringBuilder) {
		String schemaTypeCall = schemaTypeCallerFor(schema);
		stringBuilder.append("\n\tpublic Iterator<" + wrapperName + "> " + StringUtils.uncapitalize(wrapperName) + "Iterator() {");
		stringBuilder.append("\n\t\treturn iterateFromCache(" + schemaTypeCall + ",this::wrap" + wrapperName + ");");
		stringBuilder.append("\n\t}\n");
	}


	private void appendIterateFromConditionHelperMethod(MetadataSchema schema, String wrapperName,
														StringBuilder stringBuilder) {
		String schemaTypeCall = schemaTypeCallerFor(schema);
		stringBuilder.append("\n\tpublic Iterator<" + wrapperName + "> " + StringUtils.uncapitalize(wrapperName) + "Iterator(LogicalSearchCondition condition) {");
		stringBuilder.append("\n\t\treturn searchIterator(from(" + schemaTypeCall + ").whereAllConditions(asList(condition)), this::wrap" + wrapperName + ");");
		stringBuilder.append("\n\t}\n");
	}


	private void appendIterateFromQueryHelperMethod(MetadataSchema schema, String wrapperName,
													StringBuilder stringBuilder) {
		stringBuilder.append("\n\tpublic Iterator<" + wrapperName + "> " + StringUtils.uncapitalize(wrapperName) + "Iterator(LogicalSearchQuery query) {");
		stringBuilder.append("\n\t\treturn searchIterator(query, this::wrap" + wrapperName + ");");
		stringBuilder.append("\n\t}\n");
	}


	private void appendStreamFromConditionHelperMethod(MetadataSchema schema, String wrapperName,
													   StringBuilder stringBuilder) {
		String schemaTypeCall = schemaTypeCallerFor(schema);
		stringBuilder.append("\n\tpublic Stream<" + wrapperName + "> " + StringUtils.uncapitalize(wrapperName) + "Stream(LogicalSearchCondition condition) {");
		stringBuilder.append("\n\t\treturn searchIterator(from(" + schemaTypeCall + ").whereAllConditions(asList(condition)), this::wrap" + wrapperName + ").stream();");
		stringBuilder.append("\n\t}\n");
	}


	private void appendStreamFromQueryHelperMethod(MetadataSchema schema, String wrapperName,
												   StringBuilder stringBuilder) {
		stringBuilder.append("\n\tpublic Stream<" + wrapperName + "> " + StringUtils.uncapitalize(wrapperName) + "Stream(LogicalSearchQuery query) {");
		stringBuilder.append("\n\t\treturn searchIterator(query, this::wrap" + wrapperName + ").stream();");
		stringBuilder.append("\n\t}\n");
	}

	private void appendGetByIdHelperMethod(MetadataSchema schema, String wrapperName, StringBuilder stringBuilder) {
		String schemaTypeCall = schemaTypeCallerFor(schema);
		stringBuilder.append("\n\tpublic " + wrapperName + " get" + wrapperName + "(String id) {");
		stringBuilder.append("\n\t\treturn wrap" + wrapperName + "(get(" + schemaTypeCall + ",id));");
		stringBuilder.append("\n\t}\n");
	}

	private void appendGetByIdsHelperMethod(MetadataSchema schema, String wrapperName, StringBuilder stringBuilder) {
		String schemaTypeCall = schemaTypeCallerFor(schema);
		stringBuilder.append("\n\tpublic List<" + wrapperName + "> get" + wrapperName + "s(List<String> ids) {");
		stringBuilder.append("\n\t\treturn wrap" + wrapperName + "s(get(" + schemaTypeCall + ",ids));");
		stringBuilder.append("\n\t}\n");
	}

	private void appendWrapElementHelperMethod(MetadataSchema schema, String wrapperName, boolean withLocale,
											   StringBuilder stringBuilder) {

		if (schema.getCode().equals(User.DEFAULT_SCHEMA)) {
			stringBuilder.append("\n\tpublic abstract " + wrapperName + " wrap" + wrapperName + "(Record record);");
		} else {
			stringBuilder.append("\n\tpublic " + wrapperName + " wrap" + wrapperName + "(Record record) {");
			if (withLocale) {
				stringBuilder.append("\n\t\treturn record == null ? null : new " + wrapperName + "(record, getTypes(), locale);");
			} else {
				stringBuilder.append("\n\t\treturn record == null ? null : new " + wrapperName + "(record, getTypes());");
			}
			stringBuilder.append("\n\t}\n");
		}
	}

	private void appendWrapElementsHelperMethod(MetadataSchema schema, String wrapperName, boolean withLocale,
												StringBuilder stringBuilder) {
		if (schema.getCode().equals(User.DEFAULT_SCHEMA)) {
			stringBuilder.append("\n\tpublic abstract List<" + wrapperName + "> wrap" + wrapperName + "s(List<Record> records);");
		} else {
			stringBuilder.append("\n\tpublic List<" + wrapperName + "> wrap" + wrapperName + "s(List<Record> records) {");
			stringBuilder.append("\n\t\tList<" + wrapperName + "> wrapped = new ArrayList<>();");
			stringBuilder.append("\n\t\tfor (Record record : records) {");
			if (withLocale) {
				stringBuilder.append("\n\t\t\twrapped.add(new " + wrapperName + "(record, getTypes(), locale));");
			} else {
				stringBuilder.append("\n\t\t\twrapped.add(new " + wrapperName + "(record, getTypes()));");
			}
			stringBuilder.append("\n\t\t}\n");

			stringBuilder.append("\n\t\treturn wrapped;");
			stringBuilder.append("\n\t}\n");
		}
	}

	private void appendSearchByQueryElementsHelperMethod(MetadataSchema schema, String wrapperName,
														 StringBuilder stringBuilder) {
		stringBuilder.append("\n\tpublic List<" + wrapperName + "> search" + wrapperName + "s(LogicalSearchQuery query) {");
		stringBuilder.append("\n\t\treturn wrap" + wrapperName
							 + "s(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));");
		stringBuilder.append("\n\t}\n");
	}

	private void appendSearchByConditionElementsHelperMethod(MetadataSchema schema, String wrapperName,
															 StringBuilder stringBuilder) {
		stringBuilder
				.append("\n\tpublic List<" + wrapperName + "> search" + wrapperName + "s(LogicalSearchCondition condition) {");

		stringBuilder.append("\n\t\tMetadataSchemaType type = " + schemaTypeCallerFor(schema) + ";");
		stringBuilder
				.append("\n\t\tLogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));");
		stringBuilder.append("\n\t\treturn wrap" + wrapperName
							 + "s(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));");
		stringBuilder.append("\n\t}\n");
	}

	private String metadatasHelperMethod(MetadataSchemaType type, MetadataSchema schema,
										 Class<? extends RecordWrapper> recordWrapperClass)
			throws Exception {

		StringBuilder stringBuilder = new StringBuilder();
		Map<String, String> wrapperDeclaredFields = getDeclaredFields(recordWrapperClass);

		String shortcutClass, shortcutExtendsClass, variableName;
		boolean customSchema;
		if (schema.getLocalCode().equals("default")) {
			shortcutClass = "SchemaTypeShortcuts_" + schema.getCode();
			shortcutExtendsClass = "SchemaTypeShortcuts";
			variableName = type.getCode();
			customSchema = false;
		} else {
			shortcutClass = "SchemaTypeShortcuts_" + schema.getCode();
			shortcutExtendsClass = "SchemaTypeShortcuts_" + type.getCode() + "_default";
			variableName = schema.getCode();
			customSchema = true;
		}

		stringBuilder.append("\n\tpublic final " + shortcutClass + " " + variableName + "\n\t\t = new " + shortcutClass + "(\""
							 + schema.getCode() + "\");");
		stringBuilder.append("\n\tpublic class " + shortcutClass + " extends " + shortcutExtendsClass + " {");

		stringBuilder.append("\n\t\tprotected " + shortcutClass + "(String schemaCode) {");
		stringBuilder.append("\n\t\t\tsuper(schemaCode);");
		stringBuilder.append("\n\t}");

		for (Metadata metadata : schema.getMetadatas()) {
			if (metadata.getInheritance() == null && (customSchema || wrapperDeclaredFields
					.containsKey(metadata.getLocalCode()))) {
				stringBuilder.append("\n");
				stringBuilder.append("\n\t\tpublic Metadata " + metadata.getLocalCode() + "() {");
				stringBuilder.append("\n\t\t\treturn metadata(\"" + metadata.getLocalCode() + "\");");
				stringBuilder.append("\n\t\t}");
			}
		}

		stringBuilder.append("\n\t}");

		return stringBuilder.toString();

	}

	private Map<String, String> getDeclaredFields(Class<? extends RecordWrapper> recordWrapperClass)
			throws Exception {

		Map<String, String> declaredFields = new HashMap<>();
		for (Field field : recordWrapperClass.getDeclaredFields()) {
			if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && String.class
					.equals(field.getType())) {

				String value = (String) field.get(null);
				declaredFields.put(value, field.getName());

			}
		}

		return declaredFields;
	}

	protected String header() {
		String line = "/** " + StringUtils.repeat("** ", 25) + "**/";
		return line + "\n\t\t// Auto-generated methods by "
			   + "" + this.getClass().getSimpleName() + " -- start\n" + line + "\n\n";
	}

	protected String footer() {
		String line = "/** " + StringUtils.repeat("** ", 25) + "**/";
		return line + "\n\t\t// Auto-generated methods by " + this.getClass().getSimpleName() + " -- end\n" + line + "\n\n";
	}

	private interface SchemasFilter {

		boolean isGenerated(Metadata metadata);

	}

}
