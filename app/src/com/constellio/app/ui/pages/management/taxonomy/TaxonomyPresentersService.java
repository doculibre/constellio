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
package com.constellio.app.ui.pages.management.taxonomy;

import static com.constellio.data.frameworks.extensions.ExtensionUtils.getBooleanValue;

import com.constellio.app.api.extensions.TaxonomyAccessExtension;
import com.constellio.app.extensions.AppLayerCollectionEventsListeners;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BehaviorCaller;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;

public class TaxonomyPresentersService {

	AppLayerFactory appLayerFactory;

	public TaxonomyPresentersService(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	protected boolean canManage(String taxonomyCode, final User user) {

		final Taxonomy taxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
				.getEnabledTaxonomyWithCode(user.getCollection(), taxonomyCode);

		AppLayerCollectionEventsListeners extensions = appLayerFactory.getExtensions()
				.getCollectionListeners(user.getCollection());
		boolean defaultValue = user.has(CorePermissions.MANAGE_TAXONOMIES).globally();

		return getBooleanValue(extensions.taxonomyAccessExtensions, defaultValue,
				new BehaviorCaller<TaxonomyAccessExtension, ExtensionBooleanResult>() {
					@Override
					public ExtensionBooleanResult call(TaxonomyAccessExtension behavior) {
						return behavior.canManageTaxonomy(user, taxonomy);
					}
				});

	}

}
