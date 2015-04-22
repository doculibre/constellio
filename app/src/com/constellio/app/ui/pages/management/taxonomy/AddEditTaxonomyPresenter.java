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

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;

public class AddEditTaxonomyPresenter extends BasePresenter<AddEditTaxonomyView> {

	transient UserServices userServices;
	private boolean actionEdit = false;
	private List<String> titles;
	private transient TaxonomiesManager taxonomiesManager;
	private transient MetadataSchemasManager schemasManager;
	private transient SchemasDisplayManager schemasDisplayManager;

	public AddEditTaxonomyPresenter(AddEditTaxonomyView view) {
		super(view);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		userServices = modelLayerFactory.newUserServices();
		taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
		schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
	}

	public void saveButtonClicked(TaxonomyVO taxonomyVO) {
		if (isActionEdit()) {
			Taxonomy taxonomy = fetchTaxonomy(taxonomyVO.getCode())
					.withTitle(taxonomyVO.getTitle())
					.withUserIds(taxonomyVO.getUserIds())
					.withGroupIds(taxonomyVO.getGroupIds())
					.withVisibleInHomeFlag(taxonomyVO.isVisibleInHomePage());

			createMetadatasInClassifiedObjects(taxonomy, taxonomyVO.getClassifiedObjects());
			taxonomiesManager.editTaxonomy(taxonomy);
			view.navigateTo().listTaxonomies();
		} else {
			boolean canCreate = canCreate(taxonomyVO.getTitle());
			if (canCreate) {
				Taxonomy taxonomy = valueListServices()
						.createTaxonomy(taxonomyVO.getTitle(), taxonomyVO.getUserIds(), taxonomyVO.getGroupIds(),
								taxonomyVO.isVisibleInHomePage());
				createMetadatasInClassifiedObjects(taxonomy, taxonomyVO.getClassifiedObjects());
				view.navigateTo().listTaxonomies();
				titles.add(taxonomyVO.getTitle());
			} else {
				view.showErrorMessage("Taxonomny already exists!");
				return;
			}
		}
	}

	void createMetadatasInClassifiedObjects(Taxonomy taxonomy, List<String> classifiedObjects) {

		if (classifiedObjects != null) {
			if (classifiedObjects.contains("folderObject")) {
				createMetadatasInDefaultSchemaIfInexistent(taxonomy, Folder.SCHEMA_TYPE);
			}

			if (classifiedObjects.contains("documentObject")) {
				createMetadatasInDefaultSchemaIfInexistent(taxonomy, Document.SCHEMA_TYPE);
			}
		}
	}

	void createMetadatasInDefaultSchemaIfInexistent(Taxonomy taxonomy, String schemaType) {

		if (!getClassifiedObjects(taxonomy).contains(schemaType)) {
			String groupLabel = $("classifiedInGroupLabel");
			valueListServices().createAMultivalueClassificationMetadataInGroup(taxonomy, schemaType, groupLabel);
		}
	}

	Taxonomy fetchTaxonomy(String taxonomyCode) {
		TaxonomiesManager taxonomiesManager = view.getConstellioFactories().getModelLayerFactory().getTaxonomiesManager();
		return taxonomiesManager.getEnabledTaxonomyWithCode(view.getSessionContext().getCurrentCollection(), taxonomyCode);
	}

	public TaxonomyVO newTaxonomyVO(Taxonomy taxonomy) {
		TaxonomyToVOBuilder voBuilder = new TaxonomyToVOBuilder();
		TaxonomyVO taxonomyVO = voBuilder.build(taxonomy);
		taxonomyVO.setClassifiedObjects(getClassifiedObjects(taxonomy));
		return taxonomyVO;

	}

	List<String> getClassifiedObjects(Taxonomy taxonomy) {
		List<String> classifiedObjects = new ArrayList<>();
		List<String> classifiedTypes = getClassifiedSchemaTypes(taxonomy);
		if (classifiedTypes.contains(Folder.SCHEMA_TYPE)) {
			classifiedObjects.add("folderObject");
		}
		if (classifiedTypes.contains(Document.SCHEMA_TYPE)) {
			classifiedObjects.add("documentObject");
		}
		return classifiedObjects;
	}

	List<String> getClassifiedSchemaTypes(Taxonomy taxonomy) {
		List<MetadataSchemaType> classifiedTypes = valueListServices().getClassifiedSchemaTypes(taxonomy);
		return new SchemaUtils().toSchemaTypeCodes(classifiedTypes);
	}

	ValueListServices valueListServices() {
		return new ValueListServices(appLayerFactory, view.getCollection());
	}

	public void cancelButtonClicked() {
		view.navigateTo().listTaxonomies();
	}

	boolean canCreate(String taxonomy) {
		taxonomy = taxonomy.trim();
		boolean canCreate = false;
		if (StringUtils.isNotBlank(taxonomy)) {
			boolean exist = verifyIfExists(taxonomy);
			canCreate = !exist;
		}
		return canCreate;
	}

	private boolean verifyIfExists(String taxonomy) {
		if (titles == null) {
			getTaxonomies();
		}
		boolean exits = false;
		for (String title : titles) {
			if (title.equals(taxonomy)) {
				exits = true;
			}
		}
		return exits;
	}

	private List<TaxonomyVO> getTaxonomies() {
		titles = new ArrayList<>();
		TaxonomyToVOBuilder builder = new TaxonomyToVOBuilder();
		List<TaxonomyVO> result = new ArrayList<>();
		for (Taxonomy taxonomy : valueListServices().getTaxonomies()) {
			result.add(builder.build(taxonomy));
			titles.add(taxonomy.getTitle());
		}
		return result;
	}

	public void setEditAction(boolean actionEdit) {
		this.actionEdit = actionEdit;
	}

	public boolean isActionEdit() {
		return actionEdit;
	}

	public boolean canEditClassifiedObjects(TaxonomyVO taxonomyVO) {
		return !actionEdit || taxonomyVO.getClassifiedObjects().isEmpty();
	}
}
