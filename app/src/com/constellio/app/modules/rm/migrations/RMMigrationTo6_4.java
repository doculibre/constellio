package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.document.DocumentVersionCalculator;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo6_4 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new SchemaAlterationsFor6_4(collection, provider, factory).migrate();
	}

	public static class SchemaAlterationsFor6_4 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor6_4(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			updateCartSchema(typesBuilder.getSchemaType(Cart.SCHEMA_TYPE).getDefaultSchema());
		}

		private void updateCartSchema(MetadataSchemaBuilder cart) {
			cart.getMetadata(CommonMetadataBuilder.TITLE).defineDataEntry().asManual();
			cart.getMetadata(Cart.OWNER).setUniqueValue(false);
		}
	}
}
