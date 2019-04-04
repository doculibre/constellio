package com.constellio.app.modules.rm.reports.model.administration.plan;

import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.reports.model.administration.plan.ClassificationPlanReportModel.ClassificationPlanReportModel_Category;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClassificationPlanReportPresenter {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClassificationPlanReportPresenter.class);

	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private MetadataSchemaTypes types;
	private TaxonomiesSearchOptions searchOptions;
	private TaxonomiesSearchServices taxonomiesSearchServices;
	private SearchServices searchServices;
	private boolean detailed;
	private String administrativeUnitId;
	private RMSchemasRecordsServices rm;
	private Locale locale;
	private List<String> categoryList;
	private boolean showDeactivated = true;

	public ClassificationPlanReportPresenter(String collection, ModelLayerFactory modelLayerFactory, Locale locale) {
		this(collection, modelLayerFactory, false, locale);
	}

	public ClassificationPlanReportPresenter(String collection, ModelLayerFactory modelLayerFactory, boolean detailed,
			Locale locale) {
		this(collection, modelLayerFactory, detailed, null, locale, null, true);
	}

	public ClassificationPlanReportPresenter(String collection, ModelLayerFactory modelLayerFactory, boolean detailed,
			String administrativeUnitId, Locale locale, List<String> categoryList, boolean showDeactivated) {

		this.showDeactivated = showDeactivated;
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.detailed = (detailed || StringUtils.isNotBlank(administrativeUnitId) ? true : false);
		this.administrativeUnitId = administrativeUnitId;
		this.locale = locale;
		this.categoryList = categoryList;
	}

	public ClassificationPlanReportModel build() {
		init();

		ClassificationPlanReportModel model = new ClassificationPlanReportModel();
		model.setDetailed(detailed);
		model.setByAdministrativeUnit(StringUtils.isNotBlank(administrativeUnitId));

		if (StringUtils.isNotBlank(administrativeUnitId)) {

			List<ClassificationPlanReportModel_Category> classificationPlanReportModel_categories = new ArrayList<>();

			Map<AdministrativeUnit, List<ClassificationPlanReportModel_Category>> categoriesByAdmUnit = model
					.getCategoriesByAdministrativeUnitMap();

			MetadataSchemaType retentionRuleSchemaType = rm.retentionRule.schemaType();
			AdministrativeUnit administrativeUnit = rm.getAdministrativeUnit(administrativeUnitId);

			LogicalSearchQuery retentionRulesQuery = new LogicalSearchQuery()
					.setCondition(LogicalSearchQueryOperators.from(retentionRuleSchemaType)
							.where(rm.retentionRule.administrativeUnits())
							.isContaining(Arrays.asList(administrativeUnit.getId()))).sortAsc(Schemas.CODE);
			List<String> retentionRulesIds = searchServices.searchRecordIds(retentionRulesQuery);

			for (String retentionRulesId : retentionRulesIds) {

				MetadataSchemaType categorySchemaType = rm.category.schemaType();
				LogicalSearchQuery categoriesQuery = new LogicalSearchQuery()
						.setCondition(LogicalSearchQueryOperators.from(categorySchemaType)
								.where(rm.category.retentionRules())
								.isContaining(Arrays.asList(retentionRulesId)))
						.sortAsc(Schemas.CODE);

				List<Record> categoryRecords = searchServices.search(categoriesQuery);

				for (Record categoryRecord : categoryRecords) {
					Category recordCategory = new Category(categoryRecord, types, locale);
					if (recordCategory != null && isCategoryShown(recordCategory)) {
						ClassificationPlanReportModel_Category modelCategory = new ClassificationPlanReportModel_Category();

						String code = StringUtils.defaultString(recordCategory.getCode());
						modelCategory.setCode(code);

						String title = StringUtils.defaultString(recordCategory.getTitle());
						modelCategory.setLabel(title);

						String description = StringUtils.defaultString(recordCategory.getDescription());
						modelCategory.setDescription(description);

						modelCategory.setKeywords(recordCategory.getKeywords());

						List<String> retentionRules = new ArrayList<>();
						for (String retentionRuleId : recordCategory.getRententionRules()) {
							retentionRules.add(rm.getRetentionRule(retentionRuleId).getCode());
						}
						modelCategory.setRetentionRules(retentionRules);

						classificationPlanReportModel_categories.add(modelCategory);
					}
				}
			}
			categoriesByAdmUnit.put(administrativeUnit, classificationPlanReportModel_categories);
		} else {
			List<ClassificationPlanReportModel_Category> rootCategories = model.getRootCategories();

			List<TaxonomySearchRecord> taxonomySearchRecords = taxonomiesSearchServices
					.getLinkableRootConcept(User.GOD, collection,
							RMTaxonomies.CLASSIFICATION_PLAN, Category.SCHEMA_TYPE, searchOptions);

			if (taxonomySearchRecords != null) {
				for (TaxonomySearchRecord taxonomyRecord : taxonomySearchRecords) {

					if (taxonomyRecord != null) {
						Record record = taxonomyRecord.getRecord();
						if (record != null && ((categoryList != null && categoryList.contains(record.getId())) || categoryList == null || categoryList.isEmpty())) {
							Category recordCategory = new Category(record, types, locale);

							boolean isCategoryShown = isCategoryShown(recordCategory);
							if (!isCategoryShown) {

								List<ClassificationPlanReportModel_Category> categoryList = getCategoriesForRecord(record);

								if(categoryList != null && categoryList.size() > 0)  {
									ClassificationPlanReportModel_Category modelCategory = new ClassificationPlanReportModel_Category();

									String description = StringUtils.defaultString(recordCategory.getDescription());
									String code = StringUtils.defaultString(recordCategory.getCode());
									String title = StringUtils.defaultString(recordCategory.getTitle());

									modelCategory.setCategories(categoryList);
									modelCategory.setDescription(description);
									modelCategory.setDeactivated(true);
									modelCategory.setCode(code);
									modelCategory.setLabel(i18n.$("RMReportsViewImpl.deactivated", title));

									rootCategories.add(modelCategory);
								}
							} else if (recordCategory != null) {
								ClassificationPlanReportModel_Category modelCategory = new ClassificationPlanReportModel_Category();

								String code = StringUtils.defaultString(recordCategory.getCode());
								modelCategory.setCode(code);

								String title = StringUtils.defaultString(recordCategory.getTitle());
								modelCategory.setLabel(title);

								String description = StringUtils.defaultString(recordCategory.getDescription());
								modelCategory.setDescription(description);

								modelCategory.setCategories(getCategoriesForRecord(record));

								rootCategories.add(modelCategory);
							}
						}
					}
				}
			}
		}

		return model;
	}


	private boolean isCategoryShown(Category category) {
		Boolean deactivated = category.get(Category.DEACTIVATE);

		return showDeactivated || !Boolean.TRUE.equals(deactivated);
	}

	private void init() {
		types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		searchOptions = new TaxonomiesSearchOptions().setReturnedMetadatasFilter(ReturnedMetadatasFilter.all())
				.setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(true);
		taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		rm = new RMSchemasRecordsServices(collection, modelLayerFactory, locale);
		searchServices = modelLayerFactory.newSearchServices();
	}

	private List<ClassificationPlanReportModel_Category> getCategoriesForRecord(Record record) {
		List<ClassificationPlanReportModel_Category> modelCategories = new ArrayList<>();

		List<TaxonomySearchRecord> children = taxonomiesSearchServices.getLinkableChildConcept(User.GOD, record,
				RMTaxonomies.CLASSIFICATION_PLAN, Category.SCHEMA_TYPE, searchOptions);

		if (children != null) {
			for (TaxonomySearchRecord child : children) {
				if (child != null) {
					try {
						Record childRecord = child.getRecord();
						if (childRecord != null) {
							Category recordCategory = new Category(childRecord, types, locale);

							boolean isCategoryShown = isCategoryShown(recordCategory);

							if(!isCategoryShown) {
								List<ClassificationPlanReportModel_Category> categoryList = getCategoriesForRecord(childRecord);

								if(categoryList != null && categoryList.size() > 0)  {
									String description = StringUtils.defaultString(recordCategory.getDescription());
									String code = StringUtils.defaultString(recordCategory.getCode());
									String title = StringUtils.defaultString(recordCategory.getTitle());

									ClassificationPlanReportModel_Category modelCategory = new ClassificationPlanReportModel_Category();
									modelCategory.setCategories(categoryList);
									modelCategory.setDescription(description);
									modelCategory.setDeactivated(true);
									modelCategory.setCode(code);
									modelCategory.setLabel(i18n.$("RMReportsViewImpl.deactivated", title));

									modelCategories.add(modelCategory);
								}
							} else if (recordCategory != null) {
								ClassificationPlanReportModel_Category modelCategory = new ClassificationPlanReportModel_Category();

								String categoryCode = StringUtils.defaultString(recordCategory.getCode());
								modelCategory.setCode(categoryCode);

								String categoryTitle = StringUtils.defaultString(recordCategory.getTitle());
								modelCategory.setLabel(categoryTitle);

								String categoryDescription = StringUtils.defaultString(recordCategory.getDescription());
								modelCategory.setDescription(categoryDescription);

								Record childChildRecord = child.getRecord();

								modelCategory.setCategories(getCategoriesForRecord(childChildRecord));

								modelCategories.add(modelCategory);
							}
						}
					} catch (Exception e) {
						// throw new RuntimeException(e);
						LOGGER.info("This is not a category. It's a " + child.getRecord().getSchemaCode());
					}
				}
			}
		}
		return modelCategories;
	}

	public FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}
}