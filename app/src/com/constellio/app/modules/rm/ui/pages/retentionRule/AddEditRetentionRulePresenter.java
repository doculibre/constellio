package com.constellio.app.modules.rm.ui.pages.retentionRule;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.calculators.folder.FolderDecomDatesDynamicLocalDependency;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.RetentionRuleToVOBuilder;
import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleTablePresenter;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.constellio.app.modules.rm.model.calculators.document.DocumentDecomDatesDynamicLocalDependency.isMetadataUsableByCopyRetentionRules;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class AddEditRetentionRulePresenter extends SingleSchemaBasePresenter<AddEditRetentionRuleView>
		implements RetentionRuleTablePresenter {

	private static Logger LOGGER = LoggerFactory.getLogger(AddEditRetentionRulePresenter.class);

	private boolean addView;

	private RetentionRuleVO rule;

	private transient RMConfigs configs;

	private RetentionRuleToVOBuilder voBuilder;

	private boolean reloadingForm = false;

	public AddEditRetentionRulePresenter(AddEditRetentionRuleView view) {
		super(view, RetentionRule.DEFAULT_SCHEMA);
		reloadingForm = true;
	}

	public void forParams(String id) {
		Record record;
		if (StringUtils.isNotBlank(id)) {
			record = getRecord(id);
			addView = false;
		} else {
			record = newRecord();
			addView = true;
		}

		voBuilder = new RetentionRuleToVOBuilder(appLayerFactory, schema(Category.DEFAULT_SCHEMA),
				schema(UniformSubdivision.DEFAULT_SCHEMA));
		rule = voBuilder.build(record, VIEW_MODE.FORM, view.getSessionContext());
		view.setRetentionRule(rule);

		if (configs().areDocumentRetentionRulesEnabled()) {
			if (isScopeAvaliable() && rule.getScope() == null) {
				rule.setScope(RetentionRuleScope.DOCUMENTS_AND_FOLDER);
			}
		}

		boolean sortDisposal = false;


		if (!addView && isCopyRetentionRulesAvalible()) {
			List<CopyRetentionRule> copyRetentionRules = rule.getCopyRetentionRules();
			for (CopyRetentionRule copyRetentionRule : copyRetentionRules) {
				if (copyRetentionRule.canSort()) {
					sortDisposal = true;
					break;
				}
			}
		}
		updateDisposalTypeForDocumentTypes(sortDisposal);
	}

	private boolean isCopyRetentionRulesAvalible() {
		return rule.getMetadataOrNull(RetentionRule.COPY_RETENTION_RULES) != null;
	}

	public void viewAssembled() {
		reloadingForm = false;
	}

	public boolean isAddView() {
		return addView;
	}

	public boolean shouldDisplayDocumentTypeDetails() {
		return !configs().areDocumentRetentionRulesEnabled();
	}

	public List<VariableRetentionPeriodVO> getOpenPeriodsDDVList() {
		List<VariableRetentionPeriodVO> returnList = new ArrayList<>();
		LogicalSearchCondition condition = from(schemaType(VariableRetentionPeriod.SCHEMA_TYPE).getDefaultSchema()).returnAll();
		List<Record> records = searchServices().search(new LogicalSearchQuery(condition));
		for (Record record : records) {
			VariableRetentionPeriodVO variableRetentionPeriodVO = new VariableRetentionPeriodVO().setRecordId(record.getId())
					.setTitle((String) record.get(Schemas.TITLE, ConstellioUI.getCurrentSessionContext().getCurrentLocale())).setCode((String) record.get(Schemas.CODE));
			returnList.add(variableRetentionPeriodVO);
		}
		return returnList;
	}

	public void saveButtonClicked() {
		if (isScopeAvaliable() && rule.getScope() == RetentionRuleScope.DOCUMENTS && isCopyRetentionRulesAvalible()) {
			rule.getCopyRetentionRules().clear();
		}
		try {
			Record record = toRecord(rule);
			try {
				addOrUpdate(record);

				if (rule.getMetadataOrNull(RetentionRuleVO.CATEGORIES) != null) {
					saveCategories(record.getId(), rule.getCategories());
				}
				if (rule.getMetadataOrNull(RetentionRuleVO.UNIFORM_SUBDIVISIONS) != null) {
					saveUniformSubdivisions(record.getId(), rule.getUniformSubdivisions());
				}

				view.navigate().to(RMViews.class).listRetentionRules();
			} catch (ValidationRuntimeException e) {
				view.showErrorMessage($(e.getValidationErrors()));
			}
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}
	}

	public void cancelButtonClicked() {
		if (addView) {
			view.navigate().to(RMViews.class).listRetentionRules();
		} else {
			view.navigate().to(RMViews.class).displayRetentionRule(rule.getId());
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_RETENTIONRULE).onSomething();
	}

	private void saveCategories(String id, List<String> categories) {
		MetadataSchema schema = schema(Category.DEFAULT_SCHEMA);
		Metadata ruleMetadata = schema.getMetadata(Category.RETENTION_RULES);
		saveInvertedRelation(id, categories, schema, ruleMetadata);
	}

	private void saveUniformSubdivisions(String id, List<String> subdivisions) {
		MetadataSchema schema = schema(UniformSubdivision.DEFAULT_SCHEMA);
		Metadata ruleMetadata = schema.getMetadata(UniformSubdivision.RETENTION_RULE);
		saveInvertedRelation(id, subdivisions, schema, ruleMetadata);
	}

	private void saveInvertedRelation(String id, List<String> records, MetadataSchema schema, Metadata ruleMetadata) {
		Transaction transaction = new Transaction().setUser(getCurrentUser());

		LogicalSearchCondition condition = from(schema).where(ruleMetadata).isEqualTo(id)
				.andWhere(Schemas.IDENTIFIER).isNotIn(records);
		List<Record> removed = searchServices().search(new LogicalSearchQuery(condition));
		for (Record record : removed) {
			List<Object> rules = new ArrayList<>(record.getList(ruleMetadata));
			rules.remove(id);
			record.set(ruleMetadata, rules);
			transaction.add(record);
		}

		condition = from(schema).where(Schemas.IDENTIFIER).isIn(records).andWhere(ruleMetadata).isNotEqual(id);
		List<Record> added = searchServices().search(new LogicalSearchQuery(condition));
		for (Record record : added) {
			List<Object> rules = new ArrayList<>(record.getList(ruleMetadata));
			rules.add(id);
			record.set(ruleMetadata, rules);
			transaction.add(record);
		}

		try {
			recordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public void disposalTypeChanged(CopyRetentionRule copyRetentionRule) {
		boolean sortPossible;
		if (copyRetentionRule.canSort()) {
			sortPossible = true;
		} else {
			sortPossible = false;
			if(isCopyRetentionRulesAvalible()) {
				List<CopyRetentionRule> copyRetentionRules = rule.getCopyRetentionRules();
				for (CopyRetentionRule existingCopyRetentionRule : copyRetentionRules) {
					if (!existingCopyRetentionRule.equals(copyRetentionRule) && existingCopyRetentionRule.canSort()) {
						sortPossible = true;
						break;
					}
				}
			}
		}
		updateDisposalTypeForDocumentTypes(sortPossible);
	}

	private void updateDisposalTypeForDocumentTypes(boolean sortDisposal) {
		view.setDisposalTypeVisibleForDocumentTypes(sortDisposal);
	}

	private RMConfigs configs() {
		if (configs == null) {
			configs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		}
		return configs;
	}

	public boolean isFoldersCopyRetentionRulesVisible() {
		boolean foldersCopyRetentionRulesVisible = false;
		boolean documentRetentionRulesEnabled = configs().areDocumentRetentionRulesEnabled();

			if (documentRetentionRulesEnabled) {
				if(isScopeAvaliable()) {
					RetentionRuleScope scope = rule.getScope();
					foldersCopyRetentionRulesVisible = scope == RetentionRuleScope.DOCUMENTS_AND_FOLDER;
				}
			} else {
				foldersCopyRetentionRulesVisible = true;
		}

		return foldersCopyRetentionRulesVisible;
	}

	private boolean isScopeAvaliable() {
		return rule.getMetadataOrNull(RetentionRule.SCOPE) == null;
	}

	public boolean isDocumentsCopyRetentionRulesVisible() {
		boolean documentRetentionRulesEnabled = configs().areDocumentRetentionRulesEnabled();
		return documentRetentionRulesEnabled;
	}

	public boolean isDefaultDocumentsCopyRetentionRulesVisible() {
		boolean defaultDocumentsCopyRetentionRulesVisible = false;
		boolean documentRetentionRulesEnabled = configs().areDocumentRetentionRulesEnabled();
		if (documentRetentionRulesEnabled) {
			if(isScopeAvaliable()) {
				RetentionRuleScope scope = rule.getScope();
				defaultDocumentsCopyRetentionRulesVisible = scope == RetentionRuleScope.DOCUMENTS;
			}
		} else {
			defaultDocumentsCopyRetentionRulesVisible = false;
		}
		return defaultDocumentsCopyRetentionRulesVisible;
	}

	public boolean isScopeVisible() {
		boolean documentRetentionRulesEnabled = configs().areDocumentRetentionRulesEnabled();
		return documentRetentionRulesEnabled;
	}

	public void scopeChanged(RetentionRuleScope scope) {
		if (!reloadingForm) {
			reloadingForm = true;
			rule.setScope(scope);
			view.reloadForm();
			reloadingForm = false;
		}
	}

	public List<MetadataVO> getDateMetadataVOs(String documentTypeId) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		MetadataSchema schema = schema(Document.DEFAULT_SCHEMA);
		if (documentTypeId != null) {
			DocumentType documentType = rm.getDocumentType(documentTypeId);
			if (documentType.getLinkedSchema() != null) {
				schema = schema(documentType.getLinkedSchema());
			}
		}

		List<MetadataVO> dateMetadataVOs = new ArrayList<>();

		MetadataToVOBuilder metadataToVOBuilder = new MetadataToVOBuilder();
		SessionContext sessionContext = view.getSessionContext();

		for (Metadata metadata : schema.getMetadatas()) {
			if (isMetadataUsableByCopyRetentionRules(metadata) && !Schemas.isGlobalMetadata(metadata.getLocalCode())) {
				MetadataVO metadataVO = metadataToVOBuilder.build(metadata, sessionContext);
				dateMetadataVOs.add(metadataVO);
			}
		}

		Collections.sort(dateMetadataVOs, new Comparator<MetadataVO>() {
			@Override
			public int compare(MetadataVO o1, MetadataVO o2) {
				String label1 = AccentApostropheCleaner.cleanAll(o1.getLabel());
				String label2 = AccentApostropheCleaner.cleanAll(o2.getLabel());
				return label1.compareTo(label2);
			}
		});

		return dateMetadataVOs;
	}

	public List<MetadataVO> getFolderMetadataVOs() {
		MetadataSchemaType folder = schemaType(Folder.SCHEMA_TYPE);

		List<MetadataVO> dateMetadataVOs = new ArrayList<>();

		MetadataToVOBuilder metadataToVOBuilder = new MetadataToVOBuilder();
		SessionContext sessionContext = view.getSessionContext();

		for (Metadata metadata : folder.getAllMetadatas()) {
			if (FolderDecomDatesDynamicLocalDependency.isMetadataUsableByCopyRetentionRules(metadata)
				&& !Schemas.isGlobalMetadata(metadata.getLocalCode())) {
				MetadataVO metadataVO = metadataToVOBuilder.build(metadata, sessionContext);
				dateMetadataVOs.add(metadataVO);
			}
		}

		Collections.sort(dateMetadataVOs, new Comparator<MetadataVO>() {
			@Override
			public int compare(MetadataVO o1, MetadataVO o2) {
				String label1 = AccentApostropheCleaner.cleanAll(o1.getLabel());
				String label2 = AccentApostropheCleaner.cleanAll(o2.getLabel());
				if (label1 == null) {
					label1 = "";
				}
				if (label2 == null) {
					label2 = "";
				}
				return label1.compareTo(label2);
			}
		});

		return dateMetadataVOs;
	}

	@Override
	public CopyRetentionRule newDocumentCopyRetentionRule() {
		CopyRetentionRuleBuilder builder = CopyRetentionRuleBuilder.sequential(ConstellioFactories.getInstance());
		CopyRetentionRule newCopy = builder.newCopyRetentionRule();
		newCopy.setCopyType(CopyType.PRINCIPAL);
		newCopy.setActiveRetentionPeriod(RetentionPeriod.ZERO);
		newCopy.setSemiActiveRetentionPeriod(RetentionPeriod.ZERO);
		return newCopy;
	}

	@Override
	public CopyRetentionRule newFolderCopyRetentionRule(boolean principal) {
		CopyRetentionRuleBuilder builder = CopyRetentionRuleBuilder.sequential(ConstellioFactories.getInstance());
		CopyRetentionRule newCopy = builder.newCopyRetentionRule();
		if (principal) {
			newCopy.setCopyType(CopyType.PRINCIPAL);
		} else {
			newCopy.setCopyType(CopyType.SECONDARY);
			newCopy.setInactiveDisposalType(DisposalType.DESTRUCTION);
		}
		newCopy.setActiveRetentionPeriod(RetentionPeriod.ZERO);
		newCopy.setSemiActiveRetentionPeriod(RetentionPeriod.ZERO);
		return newCopy;
	}

	@Override
	public CopyRetentionRule newDocumentDefaultCopyRetentionRule(boolean principal) {
		CopyRetentionRuleBuilder builder = CopyRetentionRuleBuilder.sequential(ConstellioFactories.getInstance());
		CopyRetentionRule newCopy = builder.newCopyRetentionRule();
		if (principal) {
			newCopy.setCopyType(CopyType.PRINCIPAL);
		} else {
			newCopy.setCopyType(CopyType.SECONDARY);
			newCopy.setInactiveDisposalType(DisposalType.DESTRUCTION);
		}
		newCopy.setActiveRetentionPeriod(RetentionPeriod.ZERO);
		newCopy.setSemiActiveRetentionPeriod(RetentionPeriod.ZERO);
		return newCopy;
	}

	public boolean areSubdivisionUniformEnabled() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).areUniformSubdivisionEnabled();
	}
}
