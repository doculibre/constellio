package com.constellio.app.modules.rm.ui.builders;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.comparators.AbstractTextComparator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_CACHE;

public class RetentionRuleToVOBuilder extends RecordToVOBuilder {

	private final RMSchemasRecordsServices rm;
	private final SchemasDisplayManager schemasDisplayManager;
	private final SearchServices searchServices;
	private final MetadataSchema categorySchema;
	private final MetadataSchema subdivisionSchema;
	private final SchemaUtils schemaUtils;
	private SessionContext sessionContext;

	public RetentionRuleToVOBuilder(AppLayerFactory appLayerFactory, MetadataSchema categorySchema,
									MetadataSchema subdivisionSchema) {
		this.categorySchema = categorySchema;
		this.subdivisionSchema = subdivisionSchema;
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		rm = new RMSchemasRecordsServices(categorySchema.getCollection(), appLayerFactory);
		schemaUtils = new SchemaUtils();
	}

	public RetentionRuleToVOBuilder(SessionContext sessionContext, AppLayerFactory appLayerFactory,
									MetadataSchema categorySchema,
									MetadataSchema subdivisionSchema) {
		this.categorySchema = categorySchema;
		this.subdivisionSchema = subdivisionSchema;
		this.sessionContext = sessionContext;
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		rm = new RMSchemasRecordsServices(categorySchema.getCollection(), appLayerFactory);
		schemaUtils = new SchemaUtils();
	}

	@Override
	public RetentionRuleVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		this.sessionContext = sessionContext;
		return (RetentionRuleVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	protected RetentionRuleVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode,
										  List<String> excludedMetdataCode) {
		MetadataSchemaVO schema = metadataValueVOs.get(0).getMetadata().getSchema();

		MetadataValueVO categoriesMetadataValueVO = new MetadataValueVO(getCategoriesMetadata(schema), getCategories(id));
		MetadataValueVO uniformSubdivisionsMetadataValueVO = new MetadataValueVO(getUniformSubdivisionsMetadata(schema),
				getUniformSubdivisions(id));

		int indexOfAdministrativeUnits = getIndexOfMetadataCode(RetentionRule.ADMINISTRATIVE_UNITS, metadataValueVOs);
		if (indexOfAdministrativeUnits != -1) {
			metadataValueVOs.add(indexOfAdministrativeUnits, categoriesMetadataValueVO);
			metadataValueVOs.add(indexOfAdministrativeUnits + 1, uniformSubdivisionsMetadataValueVO);
		} else {
			metadataValueVOs.add(categoriesMetadataValueVO);
			metadataValueVOs.add(uniformSubdivisionsMetadataValueVO);
		}
		return new RetentionRuleVO(id, metadataValueVOs, viewMode);
	}

	private List<String> getCategories(String id) {
		LogicalSearchCondition condition = from(rm.category.schemaType()).where(rm.category.retentionRules())
				.isEqualTo(id);
		List<Record> categoryRecords = searchServices.cachedSearch(new LogicalSearchQuery(condition));
		List<Category> categories = new ArrayList<>();
		for (Record categoryRecord : categoryRecords) {
			categories.add(rm.wrapCategory(categoryRecord));
		}
		Collections.sort(categories, new AbstractTextComparator<Category>() {
			@Override
			protected String getText(Category object) {
				return object.getCode();
			}
		});
		List<String> categoryIds = new ArrayList<>();
		for (Category category : categories) {
			categoryIds.add(category.getId());
		}
		return categoryIds;
	}

	private MetadataVO getCategoriesMetadata(MetadataSchemaVO schema) {
		return getSynteticMetadata(schema, RetentionRuleVO.CATEGORIES, Category.SCHEMA_TYPE, Category.DEFAULT_SCHEMA);
	}

	private List<String> getUniformSubdivisions(String id) {
		LogicalSearchCondition condition = from(rm.uniformSubdivision.schemaType())
				.where(rm.uniformSubdivision.retentionRule()).isEqualTo(id);
		return searchServices.cachedSearchRecordIds(new LogicalSearchQuery(condition).setQueryExecutionMethod(USE_CACHE));
	}

	private MetadataVO getUniformSubdivisionsMetadata(MetadataSchemaVO schema) {
		return getSynteticMetadata(schema, RetentionRuleVO.UNIFORM_SUBDIVISIONS, UniformSubdivision.SCHEMA_TYPE,
				UniformSubdivision.DEFAULT_SCHEMA);
	}

	private MetadataVO getSynteticMetadata(MetadataSchemaVO schema, String label, String referencedSchemaType,
										   String referencedSchema) {
		Map<Locale, String> labels = new HashMap<>();
		labels.put(sessionContext.getCurrentLocale(), $("RetentionRules." + label));

		String[] taxoCodes = new String[0];

		Set<String> references = new HashSet<>();
		references.add(referencedSchema);

		String typeCode = SchemaUtils.getSchemaTypeCode(schema.getCode());

		Map<String, Map<Language, String>> groups = schemasDisplayManager.getType(schema.getCollection(), typeCode)
				.getMetadataGroup();
		Language language = Language.withCode(sessionContext.getCurrentLocale().getLanguage());
		String groupLabel = groups.keySet().isEmpty() ? null : groups.entrySet().iterator().next().getValue().get(language);

		putMetadataAtTheEnd(label, schema.getDisplayMetadataCodes());
		insertMetadataCodeBefore(label, RetentionRule.COPY_RETENTION_RULES, schema.getFormMetadataCodes());

		return new MetadataVO(label, MetadataVO.getCodeWithoutPrefix(label), MetadataValueType.REFERENCE, schema.getCollection(), schema, false, true, false,
				labels, null, taxoCodes, referencedSchemaType, MetadataInputType.LOOKUP, MetadataDisplayType.VERTICAL,
				new AllowedReferences(referencedSchemaType, references), groupLabel, null, false, new HashSet<String>(), false, null,
				new HashMap<String, Object>(), schema.getCollectionInfoVO(), false);
	}

	private void insertMetadataCodeBefore(String codeToInsert, String codeToSearch, List<String> codes) {
		int index = codes.indexOf(RetentionRule.DEFAULT_SCHEMA + "_" + codeToSearch);
		codes.add(index, codeToInsert);
	}

	private void putMetadataAtTheEnd(String codeToInsert, List<String> codes) {
		codes.add(codeToInsert);
	}
}
