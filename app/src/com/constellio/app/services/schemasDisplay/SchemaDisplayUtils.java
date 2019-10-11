package com.constellio.app.services.schemasDisplay;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataListFilter;
import com.constellio.model.services.schemas.SchemaUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static java.util.Arrays.asList;

public class SchemaDisplayUtils {

	public static MetadataList getRequiredMetadatasInSchemaForm(MetadataSchema schema) {
		return getAvailableMetadatasInSchemaForm(schema).onlyEssentialMetadatasAndCodeTitle()
				.onlyManualsAndCalculatedWithEvaluator().onlyNonSystemReserved();
	}

	public static MetadataList getAvailableMetadatasInSchemaForm(MetadataSchema schema) {

		MetadataListFilter filter = new MetadataListFilter() {
			@Override
			public boolean isReturned(Metadata metadata) {
				return notAComment(metadata) && notIdentifier(metadata) && notSystemReserved(metadata);
			}

			private boolean notIdentifier(Metadata metadata) {
				return !"recordIdentifier".equals(metadata.getLocalCode());
			}

			private boolean notAComment(Metadata metadata) {
				return metadata.getStructureFactory() == null
					   || !CommentFactory.class.equals(metadata.getStructureFactory().getClass());
			}

			private boolean notSystemReserved(Metadata metadata) {
				return !metadata.isSystemReserved() || metadata.hasSameCode(Schemas.LEGACY_ID);
			}
		};

		return schema.getMetadatas().onlyManualsAndCalculatedWithEvaluator().onlyEnabled().only(filter)
				.sortedUsing(new FormMetadatasComparator());
	}

	private static class FormMetadatasComparator implements Comparator<Metadata> {

		@Override
		public int compare(Metadata metadata1, Metadata metadata2) {

			Integer priority1 = getPriority(metadata1);
			Integer priority2 = getPriority(metadata2);

			if (priority1.equals(priority2)) {
				return metadata1.getCode().compareTo(metadata2.getCode());
			} else {
				return priority1.compareTo(priority2);
			}

		}

		private Integer getPriority(Metadata metadata) {

			if (metadata.getLocalCode().equals(Schemas.CODE.getLocalCode())) {
				return 0;
			}

			if (metadata.getLocalCode().equals(Schemas.TITLE.getLocalCode())) {
				return 1;
			}

			if (metadata.getLocalCode().equals("type")) {
				return 2;
			}

			if (asList(STRING, NUMBER, INTEGER, REFERENCE, ENUM).contains(metadata.getType())) {
				return 3;
			}

			if (asList(DATE, DATE_TIME).contains(metadata.getType())) {
				return 4;
			}

			if (asList(BOOLEAN).contains(metadata.getType())) {
				return 5;
			}

			if (asList(STRUCTURE, TEXT, CONTENT).contains(metadata.getType())) {
				return 6;
			}

			return 999;
		}
	}

	private static class DisplayMetadatasComparator implements Comparator<Metadata> {

		@Override
		public int compare(Metadata metadata1, Metadata metadata2) {

			Integer priority1 = getPriority(metadata1);
			Integer priority2 = getPriority(metadata2);

			if (priority1.equals(priority2)) {
				return metadata1.getCode().compareTo(metadata2.getCode());
			} else {
				return priority1.compareTo(priority2);
			}

		}

		private Integer getPriority(Metadata metadata) {

			String localCode = metadata.getLocalCode();

			if (localCode.equals(Schemas.CODE.getLocalCode())) {
				return 0;
			}

			if (localCode.equals(Schemas.TITLE.getLocalCode())) {
				return 1;
			}

			if (localCode.equals("type")) {
				return 2;
			}

			if (localCode.equals(Schemas.CREATED_BY.getLocalCode())) {
				return 3;
			}

			if (localCode.equals(Schemas.CREATED_ON.getLocalCode())) {
				return 4;
			}

			if (localCode.equals(Schemas.MODIFIED_BY.getLocalCode())) {
				return 5;
			}

			if (localCode.equals(Schemas.MODIFIED_ON.getLocalCode())) {
				return 6;
			}

			if (asList(STRING, NUMBER, INTEGER, REFERENCE, ENUM, DATE, DATE_TIME, BOOLEAN).contains(metadata.getType())) {
				return 7;
			}

			if (asList(TEXT, CONTENT).contains(metadata.getType())) {
				return 8;
			}

			if (asList(STRUCTURE).contains(metadata.getType())) {
				return 9;
			}

			return 999;
		}
	}

	public static List<String> getDefaultDisplayedMetadatas(String schema, List<Metadata> allMetadatas) {

		List<Metadata> filteredMetadatas = new ArrayList<>();
		filteredMetadatas.add(Schemas.TITLE);

		boolean hasCreatedAndModifiedBy = !schema.startsWith(User.SCHEMA_TYPE) && !schema.startsWith(Group.SCHEMA_TYPE) && !schema
				.startsWith(Collection.SCHEMA_TYPE);

		if (hasCreatedAndModifiedBy) {
			filteredMetadatas.add(Schemas.CREATED_BY);
		}
		filteredMetadatas.add(Schemas.CREATED_ON);
		if (hasCreatedAndModifiedBy) {
			filteredMetadatas.add(Schemas.MODIFIED_BY);
		}
		filteredMetadatas.add(Schemas.MODIFIED_ON);

		for (Metadata metadata : allMetadatas) {
			if (!Schemas.isGlobalMetadata(metadata.getLocalCode()) && isDisplayedMetadata(metadata) && !metadata
					.isSystemReserved()) {
				filteredMetadatas.add(metadata);
			}
		}

		Collections.sort(filteredMetadatas, new DisplayMetadatasComparator());

		List<String> codes = new ArrayList<>();
		for (Metadata metadata : filteredMetadatas) {
			codes.add(schema + "_" + metadata.getLocalCode());
		}
		return codes;
	}

	private static boolean isDisplayedMetadata(Metadata metadata) {
		if (metadata.getType() == MetadataValueType.STRUCTURE) {
			return metadata.getStructureFactory() instanceof ContentFactory || metadata
					.getStructureFactory() instanceof CommentFactory;
		}

		return true;
	}

	public static SchemaDisplayConfig getCustomSchemaDefaultDisplay(SchemaDisplayConfig defaultSchemaConfig,
																	String schemaCode,
																	MetadataSchemaTypes types) {
		String defaultSchema = defaultSchemaConfig.getSchemaCode();
		MetadataSchema schema = types.getSchema(schemaCode);

		List<String> displayMetadataCodes = toCustomMetadataCodes(schemaCode, defaultSchema,
				defaultSchemaConfig.getDisplayMetadataCodes());
		List<String> formMetadataCodes = toCustomMetadataCodes(schemaCode, defaultSchema,
				defaultSchemaConfig.getFormMetadataCodes());
		List<String> formHiddenMetadataCodes = toCustomMetadataCodes(schemaCode, defaultSchema,
				defaultSchemaConfig.getFormHiddenMetadataCodes());
		List<String> searchMetadatasCodes = toCustomMetadataCodes(schemaCode, defaultSchema,
				defaultSchemaConfig.getSearchResultsMetadataCodes());
		List<String> tableMetadatasCodes = toCustomMetadataCodes(schemaCode, defaultSchema,
				defaultSchemaConfig.getTableMetadataCodes());

		int commentIndex = displayMetadataCodes.indexOf(schemaCode + "_comments");

		for (Metadata metadata : schema.getMetadatas().onlyWithoutInheritance()) {
			if (commentIndex == -1) {
				displayMetadataCodes.add(metadata.getCode());
			} else {
				displayMetadataCodes.add(commentIndex, metadata.getCode());
			}
		}

		for (Metadata metadata : getAvailableMetadatasInSchemaForm(schema).onlyWithoutInheritance()) {
			formMetadataCodes.add(metadata.getCode());
		}

		return new SchemaDisplayConfig(types.getCollection(), schemaCode, displayMetadataCodes, formMetadataCodes,
				formHiddenMetadataCodes, searchMetadatasCodes, tableMetadatasCodes);
	}

	private static List<String> toCustomMetadataCodes(String schemaCode, String defaultSchema,
													  List<String> displayMetadataCodes) {

		List<String> metadataCodes = new ArrayList<>();

		for (String metadataCode : displayMetadataCodes) {
			metadataCodes.add(metadataCode.replace(defaultSchema + "_", schemaCode + "_"));
		}

		return metadataCodes;
	}

	public static SchemaDisplayConfig getDefaultSchemaDefaultDisplay(String schemaCode, MetadataSchemaTypes types) {
		SchemaUtils schemaUtils = new SchemaUtils();
		MetadataSchema schema = types.getSchema(schemaCode);
		List<String> displayMetadataCodes = getDefaultDisplayedMetadatas(schemaCode, schema.getMetadatas());
		List<String> formMetadataCodes = schemaUtils
				.toMetadataCodes(SchemaDisplayUtils.getAvailableMetadatasInSchemaForm(schema));
		List<String> formHiddenMetadataCodes = Collections.emptyList();

		formMetadataCodes.remove(schemaCode + "_" + Schemas.LEGACY_ID);
		String title = schema.getCode() + "_" + Schemas.TITLE.getLocalCode();
		String lastModificationDate = schema.getCode() + "_" + Schemas.MODIFIED_ON.getLocalCode();

		List<String> searchMetadatasCodes = asList(title, lastModificationDate);
		List<String> tableMetadatasCodes = asList(title, lastModificationDate);

		return new SchemaDisplayConfig(types.getCollection(), schemaCode, displayMetadataCodes, formMetadataCodes,
				formHiddenMetadataCodes, searchMetadatasCodes, tableMetadatasCodes);
	}

	public static Comparator<Metadata> getMetadataLabelComparator(final SessionContext sessionContext) {
		return new Comparator<Metadata>() {
			@Override
			public int compare(Metadata o1, Metadata o2) {

				Language language = Language.withLocale(sessionContext.getCurrentLocale());
				String firstValue = o1.getLabel(language).toLowerCase();
				String secondValue = o2.getLabel(language).toLowerCase();

				String firstValueWithoutAccents = AccentApostropheCleaner.removeAccents(firstValue);
				String secondValueWithoutAccents = AccentApostropheCleaner.removeAccents(secondValue);

				return firstValueWithoutAccents.compareTo(secondValueWithoutAccents);
			}
		};
	}
}