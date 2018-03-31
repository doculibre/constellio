package com.constellio.sdk.tests.schemas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetwork;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.utils.DefaultClassProvider;

public abstract class SchemasSetup {

	private static MetadataSchemasManager manager;
	private static List<SchemasSetup> setups = new ArrayList<>();
	protected final String collection;
	protected final List<String> languages;
	protected MetadataSchemaTypesBuilder typesBuilder;
	MetadataSchemaTypes types;
	private boolean wasSetUp;

	protected SchemasSetup(String collection) {
		this(collection, Arrays.asList("fr"));
	}

	protected SchemasSetup(String collection, List<String> languages) {
		this.collection = collection;
		this.languages = languages;
		setups.add(this);
	}

	public static void clearSetupList() {
		setups.clear();
	}

	public static void prepareSetups(MetadataSchemasManager manager, CollectionsManager collectionsManager) {
		SchemasSetup.manager = manager;
		for (SchemasSetup setup : setups) {

			if (collectionsManager != null && !collectionsManager.getCollectionCodes().contains(setup.collection)) {
				collectionsManager.createCollectionInCurrentVersion(setup.collection, setup.languages);
			}

			if (!setup.wasSetUp) {
				CollectionInfo collectionInfo = collectionsManager.getCollectionInfo(setup.collection);
				MetadataSchemaTypes types = manager.getSchemaTypes(setup.collection);
				if (collectionsManager == null && types == null) {
					types = new MetadataSchemaTypes(collectionInfo, 0, new ArrayList<MetadataSchemaType>(),
							new ArrayList<String>(), new ArrayList<String>(), Arrays.asList(Language.French),
							MetadataNetwork.EMPTY());
				}

				setup.typesBuilder = MetadataSchemaTypesBuilder.modify(types, new DefaultClassProvider());
				setup.setUp();
				setup.wasSetUp = true;
			}
		}
	}

	public abstract void setUp();

	public final MetadataSchemaTypesBuilder getTypesBuilder() {
		return typesBuilder;
	}

	public final void onSchemaBuilt(MetadataSchemaTypes types) {
		this.types = types;
	}

	public Metadata get(String type, String schema, String metadata) {
		return get(type, schema).getMetadata(metadata);
	}

	public MetadataSchema get(String type, String schema) {
		return get(type).getSchema(schema);
	}

	public MetadataSchemaType get(String type) {
		return types.getSchemaType(type);
	}

	public MetadataSchema getSchema(String code) {
		return types.getSchema(code);
	}

	public Metadata getMetadata(String code) {
		return types.getMetadata(code);
	}

	protected void configureMetadataBuilder(MetadataBuilder metadataBuilder, MetadataSchemaTypesBuilder typesBuilder,
			com.constellio.sdk.tests.schemas.MetadataBuilderConfigurator... builderConfigurators) {
		for (com.constellio.sdk.tests.schemas.MetadataBuilderConfigurator builderConfigurator : builderConfigurators) {
			builderConfigurator.configure(metadataBuilder, typesBuilder);
		}
	}

	protected void configureMetadataBuilder(MetadataSchemaTypeBuilder metadataSchemaTypeBuilder,
			MetadataSchemaTypesBuilder typesBuilder,
			com.constellio.sdk.tests.schemas.MetadataSchemaTypeConfigurator... builderConfigurators) {
		for (com.constellio.sdk.tests.schemas.MetadataSchemaTypeConfigurator builderConfigurator : builderConfigurators) {
			builderConfigurator.configure(metadataSchemaTypeBuilder, typesBuilder);
		}
	}

	public MetadataSchemaTypes getTypes() {
		return types;
	}

	public SchemasSetup with(MetadataSchemaTypesConfigurator metadataSchemaTypesConfigurator) {
		metadataSchemaTypesConfigurator.configure(typesBuilder);
		return this;
	}

	public void modify(MetadataSchemaTypesAlteration alteration) {
		manager.modify(collection, alteration);
		types = manager.getSchemaTypes(collection);
	}

	public void refresh(MetadataSchemasManager manager) {
		SchemasSetup.manager = manager;
		types = manager.getSchemaTypes(collection);
	}

	public void refresh() {
		types = manager.getSchemaTypes(collection);
	}

}
