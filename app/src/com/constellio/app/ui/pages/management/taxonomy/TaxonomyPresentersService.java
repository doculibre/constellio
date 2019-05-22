package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class TaxonomyPresentersService {

	AppLayerFactory appLayerFactory;

	public TaxonomyPresentersService(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public boolean canManage(String taxonomyCode, final User user) {

		final Taxonomy taxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
				.getEnabledTaxonomyWithCode(user.getCollection(), taxonomyCode);

		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollectionOf(user);
		boolean defaultValue = user.has(CorePermissions.MANAGE_TAXONOMIES).globally();

		return extensions.canManageTaxonomy(defaultValue, user, taxonomy);

	}

	public boolean canConsult(String taxonomyCode, final User user) {
		final Taxonomy taxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
				.getEnabledTaxonomyWithCode(user.getCollection(), taxonomyCode);

		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollectionOf(user);

		return extensions.canConsultTaxonomy(false, user, taxonomy);
	}

	public boolean displayTaxonomy(String taxonomyCode, final User user) {

		final Taxonomy taxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
				.getEnabledTaxonomyWithCode(user.getCollection(), taxonomyCode);

		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollectionOf(user);
		boolean defaultValue = user.has(CorePermissions.MANAGE_TAXONOMIES).globally();

		return extensions.displayTaxonomy(defaultValue, user, taxonomy);

	}

	public ValidationErrors validateDeletable(String taxonomyCode, User user) {
		final Taxonomy taxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
				.getEnabledTaxonomyWithCode(user.getCollection(), taxonomyCode);

		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollectionOf(user);
		return extensions.validateTaxonomyDeletable(taxonomy);
	}
}
