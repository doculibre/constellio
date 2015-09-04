/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.extensions;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.api.extensions.DownloadContentVersionLinkExtension;
import com.constellio.app.api.extensions.GenericRecordPageExtension;
import com.constellio.app.api.extensions.PageExtension;
import com.constellio.app.api.extensions.TaxonomyPageExtension;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyExtraFieldsParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyManagementClassifiedTypesParams;
import com.constellio.app.api.extensions.taxonomies.TaxonomyExtraField;
import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class AppLayerCollectionExtensions {

	//------------ Extension points -----------

	public VaultBehaviorsList<PageExtension> pageAccessExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<TaxonomyPageExtension> taxonomyAccessExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<GenericRecordPageExtension> schemaTypeAccessExtensions = new VaultBehaviorsList<>();

	public List<DownloadContentVersionLinkExtension> downloadContentVersionLinkExtensions = new ArrayList<>();

	//----------------- Callers ---------------

	public List<TaxonomyManagementClassifiedType> getClassifiedTypes(GetTaxonomyManagementClassifiedTypesParams params) {
		List<TaxonomyManagementClassifiedType> types = new ArrayList<>();
		for (TaxonomyPageExtension extension : taxonomyAccessExtensions) {
			types.addAll(extension.getClassifiedTypesFor(params));
		}
		return types;
	}

	public List<TaxonomyExtraField> getTaxonomyExtraFields(GetTaxonomyExtraFieldsParam params) {
		List<TaxonomyExtraField> fields = new ArrayList<>();
		for (TaxonomyPageExtension extension : taxonomyAccessExtensions) {
			fields.addAll(extension.getTaxonomyExtraFieldsFor(params));
		}
		return fields;
	}

	public boolean hasPageAccess(boolean defaultValue, final Class<? extends BasePresenter> presenterClass, final String params,
			final User user) {
		return pageAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<PageExtension>() {
			@Override
			public ExtensionBooleanResult call(PageExtension behavior) {
				return behavior.hasPageAccess(presenterClass, params, user);
			}
		});
	}

	public boolean hasRestrictedRecordAccess(boolean defaultValue, final Class<? extends BasePresenter> presenterClass,
			final String params, final User user, final Record restrictedRecord) {
		return pageAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<PageExtension>() {
			@Override
			public ExtensionBooleanResult call(PageExtension behavior) {
				return behavior.hasRestrictedRecordAccess(presenterClass, params, user, restrictedRecord);
			}
		});
	}

	public boolean canManageSchema(boolean defaultValue, final User user, final MetadataSchemaType schemaType) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.canManageSchema(user, schemaType);
			}
		});
	}

	public boolean canViewSchemaRecord(boolean defaultValue, final User user, final MetadataSchemaType schemaType,
			final Record restrictedRecord) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.canViewSchemaRecord(user, schemaType, restrictedRecord);
			}
		});
	}

	public boolean canModifySchemaRecord(boolean defaultValue, final User user, final MetadataSchemaType schemaType,
			final Record restrictedRecord) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.canModifySchemaRecord(user, schemaType, restrictedRecord);
			}
		});
	}

	public boolean canLogicallyDeleteSchemaRecord(boolean defaultValue, final User user, final MetadataSchemaType schemaType,
			final Record restrictedRecord) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.canLogicallyDeleteSchemaRecord(user, schemaType, restrictedRecord);
			}
		});
	}

	public boolean isSchemaTypeConfigurable(boolean defaultValue, final MetadataSchemaType schemaType) {
		return schemaTypeAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<GenericRecordPageExtension>() {
			@Override
			public ExtensionBooleanResult call(GenericRecordPageExtension behavior) {
				return behavior.isSchemaTypeConfigurable(schemaType);
			}
		});
	}

	public boolean canManageTaxonomy(boolean defaultValue, final User user, final Taxonomy taxonomy) {
		return taxonomyAccessExtensions.getBooleanValue(defaultValue, new BooleanCaller<TaxonomyPageExtension>() {
			@Override
			public ExtensionBooleanResult call(TaxonomyPageExtension behavior) {
				return behavior.canManageTaxonomy(user, taxonomy);
			}
		});
	}

}
