package com.constellio.app.ui.pages.management.taxonomy;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;

public class ListTaxonomyPresenter extends BasePresenter<ListTaxonomyView> {

	private List<String> titles;

	public ListTaxonomyPresenter(ListTaxonomyView view) {
		super(view);
	}

	public List<TaxonomyVO> getTaxonomies() {
		titles = new ArrayList<>();
		TaxonomyToVOBuilder builder = new TaxonomyToVOBuilder();
		User user = getCurrentUser();
		TaxonomyPresentersService presentersService = new TaxonomyPresentersService(appLayerFactory);
		List<TaxonomyVO> result = new ArrayList<>();
		for (Taxonomy taxonomy : valueListServices().getTaxonomies()) {
			if (presentersService.canManage(taxonomy.getCode(), user) && presentersService.displayTaxonomy(taxonomy.getCode(),
					user)) {
				result.add(builder.build(taxonomy));
				titles.add(taxonomy.getTitle());
			}
		}
		return result;
	}

	ValueListServices valueListServices() {
		return new ValueListServices(appLayerFactory, view.getCollection());
	}

	public void addButtonClicked() {
		view.navigate().to().addTaxonomy();
	}

	public void editButtonClicked(String taxonomyCode) {
		view.navigate().to().editTaxonomy(taxonomyCode);
	}

	public void displayButtonClicked(TaxonomyVO taxonomy) {
		view.navigate().to().taxonomyManagement(taxonomy.getCode());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_TAXONOMIES).globally();
	}

}
