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
			if (presentersService.canManage(taxonomy.getCode(), user)) {
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
		view.navigateTo().addTaxonomy();
	}

	public void editButtonClicked(String taxonomyCode) {
		view.navigateTo().editTaxonomy(taxonomyCode);
	}

	public void displayButtonClicked(TaxonomyVO taxonomy) {
		view.navigateTo().taxonomyManagement(taxonomy.getCode());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_TAXONOMIES).globally();
	}

}
