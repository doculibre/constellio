package com.constellio.app.entities.modules;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MigrationResourcesProviderRuntimeException.MigrationResourcesProviderRuntimeException_NoBundle;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public abstract class MetadataSchemasAlterationHelper {
	private static Logger LOGGER = LoggerFactory.getLogger(MetadataSchemasAlterationHelper.class);
	protected MetadataSchemaTypesBuilder typesBuilder;
	protected String collection;
	protected ModelLayerFactory modelLayerFactory;
	protected AppLayerFactory appLayerFactory;
	protected DataLayerFactory dataLayerFactory;
	protected MigrationResourcesProvider migrationResourcesProvider;

	protected MetadataSchemasAlterationHelper(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.appLayerFactory = appLayerFactory;
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.migrationResourcesProvider = migrationResourcesProvider;
	}

	public final void migrate() {
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();

		typesBuilder = metadataSchemasManager.modify(collection);
		migrate(typesBuilder);
		try {
			try {
				applyI18N(typesBuilder);
			} catch (MigrationResourcesProviderRuntimeException_NoBundle e) {
				//LOGGER.warn(e.getMessage());
			}
			metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimisticLocking e) {
			migrate();
		}
	}

	private void applyI18N(MetadataSchemaTypesBuilder typesBuilder) {
		for (MetadataSchemaTypeBuilder type : typesBuilder.getTypes()) {
			applyI18N(type);
		}
	}

	protected MetadataSchemaTypeBuilder schemaType(String schemaTypeCode) {
		return this.typesBuilder.getSchemaType(schemaTypeCode);
	}

	protected MetadataSchemaBuilder schema(String schemaCode) {
		return this.typesBuilder.getSchema(schemaCode);
	}

	private MetadataSchemaTypeBuilder applyI18N(MetadataSchemaTypeBuilder schemaType) {
		String schemaTypeKey = "init." + schemaType.getCode();

		List<String> collectionLanguages = modelLayerFactory.getCollectionsListManager().getCollectionLanguages(collection);
		for (String languageCode : collectionLanguages) {
			Language language = Language.withCode(languageCode);
			String schemaTypeLabel = migrationResourcesProvider.getString(schemaTypeKey, language.getLocale());
			addLabel(schemaType, schemaTypeLabel, true, language);

			for (MetadataSchemaBuilder schemaBuilder : schemaType.getAllSchemas()) {
				String schemaKey = schemaTypeKey + "." + schemaBuilder.getLocalCode();

				String schemaLabel = migrationResourcesProvider.getString(schemaKey, language.getLocale());
				addLabel(schemaBuilder, schemaLabel, true, language);
				for (MetadataBuilder metadataBuilder : schemaBuilder.getMetadatas()) {

					boolean overwrite = true;
					String specificKey = "init." + metadataBuilder.getCode().replace("_", ".");
					String label = migrationResourcesProvider.getString(specificKey, language.getLocale());
					if (label.equals(specificKey)) {
						overwrite = false;
						String globalKey = "init.allTypes.allSchemas." + metadataBuilder.getLocalCode();
						label = migrationResourcesProvider.getString(globalKey, language.getLocale());
						if (label.equals(globalKey)) {
							label = $(globalKey, language.getLocale());
						}
					}
					if (label.startsWith("init.")) {
						label = specificKey;
					}
					addLabel(metadataBuilder, label, overwrite, language);
				}
			}
		}
		return schemaType;
	}

	private void addLabel(MetadataBuilder metadataBuilder, String label, boolean overwrite, Language language) {
		String currentLabel = metadataBuilder.getLabel(language);
		boolean oldLabelIsHumanFriendly = currentLabel != null
				&& !currentLabel.equals(metadataBuilder.getLocalCode())
				&& !currentLabel.equals(metadataBuilder.getCode())
				&& !currentLabel.startsWith("init.");

		boolean labelDefined = metadataBuilder.getInheritance() != null || oldLabelIsHumanFriendly;
		boolean newLabelIsHumanFriendly = label != null && !label.startsWith("init.");
		if (!labelDefined || (newLabelIsHumanFriendly && overwrite)) {
			metadataBuilder.addLabel(language, label);
		}
	}

	private void addLabel(MetadataSchemaTypeBuilder schemaType, String label, boolean overwrite, Language language) {
		boolean labelDefined =
				schemaType.getLabel(language) != null && !schemaType.getLabel(language).equals(schemaType.getCode());
		boolean newLabelIsHumanFriendly = label != null && !label.startsWith("init.");
		if (!labelDefined || (newLabelIsHumanFriendly && overwrite)) {
			schemaType.addLabel(language, label);
		}

		addLabel(schemaType.getDefaultSchema(), label, overwrite, language);
	}

	private void addLabel(MetadataSchemaBuilder schema, String label, boolean overwrite, Language language) {
		boolean labelDefined = schema.getLabel(language) != null && !schema.getLabel(language).equals(schema.getLocalCode());
		boolean newLabelIsHumanFriendly = label != null && !label.startsWith("init.");

		if (!labelDefined || (newLabelIsHumanFriendly && overwrite)) {
			schema.addLabel(language, label);
		}
	}

	protected abstract void migrate(MetadataSchemaTypesBuilder typesBuilder);

	public MetadataSchemaTypesBuilder types() {
		return typesBuilder;
	}

	public MetadataSchemaTypeBuilder type(String code) {
		return types().getSchemaType(code);
	}

	public MetadataBuilder defaultMetadata(String typeCode, String metadataCode) {
		return type(typeCode).getMetadata(metadataCode);
	}
}
