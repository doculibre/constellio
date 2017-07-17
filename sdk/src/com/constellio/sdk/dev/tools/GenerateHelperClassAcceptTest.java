package com.constellio.sdk.dev.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.rm.wrappers.type.StorageSpaceType;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.model.wrappers.RobotLog;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.global.SolrGlobalGroup;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;

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
		wrappers.put(SolrUserCredential.DEFAULT_SCHEMA, SolrUserCredential.class);
		wrappers.put(SolrGlobalGroup.DEFAULT_SCHEMA, SolrGlobalGroup.class);
		wrappers.put(Event.DEFAULT_SCHEMA, Event.class);
		wrappers.put(Collection.DEFAULT_SCHEMA, Collection.class);
		wrappers.put(EmailToSend.DEFAULT_SCHEMA, EmailToSend.class);
		wrappers.put(Facet.DEFAULT_SCHEMA, Facet.class);
		wrappers.put(UserDocument.DEFAULT_SCHEMA, UserDocument.class);
		wrappers.put(SolrAuthorizationDetails.DEFAULT_SCHEMA, SolrAuthorizationDetails.class);
		wrappers.put(Printable.DEFAULT_SCHEMA, Printable.class);
		wrappers.put(UserFolder.DEFAULT_SCHEMA, UserFolder.class);

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
		wrappers.put(TaskStatus.DEFAULT_SCHEMA, TaskStatus.class);

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
				stringBuilder.append(generateSchemaHelperMethods(defaultSchema, defaultSchemaWrapperClass.getSimpleName()));
				stringBuilder.append(metadatasHelperMethod(type, defaultSchema, defaultSchemaWrapperClass));
			}

			for (MetadataSchema schema : type.getCustomSchemas()) {

				Class<? extends RecordWrapper> wrapperClass = wrappers.get(schema.getCode());

				if (wrapperClass != null) {
					stringBuilder.append(generateSchemaHelperMethods(schema, wrapperClass.getSimpleName()));
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

	private String generateSchemaHelperMethods(MetadataSchema schema, String wrapperName) {
		StringBuilder stringBuilder = new StringBuilder();

		appendWrapElementHelperMethod(schema, wrapperName, stringBuilder);
		appendWrapElementsHelperMethod(schema, wrapperName, stringBuilder);
		appendSearchByQueryElementsHelperMethod(schema, wrapperName, stringBuilder);
		appendSearchByConditionElementsHelperMethod(schema, wrapperName, stringBuilder);
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

	private void appendGetByLegacyIdHelperMethod(MetadataSchema schema, String wrapperName, StringBuilder stringBuilder) {

		String schemaTypeCall = schemaTypeCallerFor(schema);

		stringBuilder.append("\n\tpublic " + wrapperName + " get" + wrapperName + "WithLegacyId(String legacyId) {");
		stringBuilder.append("\n\t\treturn wrap" + wrapperName + "(getByLegacyId(" + schemaTypeCall + ",  legacyId));");
		stringBuilder.append("\n\t}\n");
	}

	private void appendGetByIdHelperMethod(MetadataSchema schema, String wrapperName, StringBuilder stringBuilder) {

		stringBuilder.append("\n\tpublic " + wrapperName + " get" + wrapperName + "(String id) {");
		stringBuilder.append("\n\t\treturn wrap" + wrapperName + "(get(id));");
		stringBuilder.append("\n\t}\n");
	}

	private void appendGetByIdsHelperMethod(MetadataSchema schema, String wrapperName, StringBuilder stringBuilder) {

		stringBuilder.append("\n\tpublic List<" + wrapperName + "> get" + wrapperName + "s(List<String> ids) {");
		stringBuilder.append("\n\t\treturn wrap" + wrapperName + "s(get(ids));");
		stringBuilder.append("\n\t}\n");
	}

	private void appendWrapElementHelperMethod(MetadataSchema schema, String wrapperName, StringBuilder stringBuilder) {

		stringBuilder.append("\n\tpublic " + wrapperName + " wrap" + wrapperName + "(Record record) {");
		stringBuilder.append("\n\t\treturn record == null ? null : new " + wrapperName + "(record, getTypes());");
		stringBuilder.append("\n\t}\n");
	}

	private void appendWrapElementsHelperMethod(MetadataSchema schema, String wrapperName, StringBuilder stringBuilder) {
		stringBuilder.append("\n\tpublic List<" + wrapperName + "> wrap" + wrapperName + "s(List<Record> records) {");
		stringBuilder.append("\n\t\tList<" + wrapperName + "> wrapped = new ArrayList<>();");
		stringBuilder.append("\n\t\tfor (Record record : records) {");
		stringBuilder.append("\n\t\t\twrapped.add(new " + wrapperName + "(record, getTypes()));");
		stringBuilder.append("\n\t\t}\n");

		stringBuilder.append("\n\t\treturn wrapped;");
		stringBuilder.append("\n\t}\n");
	}

	private void appendSearchByQueryElementsHelperMethod(MetadataSchema schema, String wrapperName, StringBuilder stringBuilder) {
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
