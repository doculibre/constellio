package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.extensions.records.params.GetRecordsToSaveInSameTransactionAsParentRecordParams;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.IOException;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

@SuppressWarnings("serial")
public class AddEditTaxonomyConceptPresenter extends SingleSchemaBasePresenter<AddEditTaxonomyConceptView> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AddEditTaxonomyConceptPresenter.class);

	public static final String EDIT = "EDIT";
	public static final String ADD = "ADD";

	public static final String STYLE_NAME = "window-button";
	public static final String WINDOW_STYLE_NAME = STYLE_NAME + "-window";
	public static final String WINDOW_CONTENT_STYLE_NAME = WINDOW_STYLE_NAME + "-content";

	private String taxonomyCode;
	private String conceptId;
	private String operation;
	private Record originalRecord;

	public AddEditTaxonomyConceptPresenter(AddEditTaxonomyConceptView view) {
		super(view);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
	}

	public RecordVO getRecordVO() {
		Record record;
		if (operation.equals(EDIT)) {
			record = recordServices().getDocumentById(conceptId);
			originalRecord = record;
		} else {
			record = newRecord();
			if (conceptId != null) {
				Record parentRecord = recordServices().getDocumentById(conceptId);
				for (Metadata metadata : schema().getParentReferences()) {
					if (metadata.getAllowedReferences().isAllowed(schema(parentRecord.getSchemaCode()))) {
						record.set(metadata, parentRecord.getId());
					}
				}
			}
		}
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		return voBuilder.build(record, VIEW_MODE.FORM, view.getSessionContext());
	}

	public void forElementInTaxonomy(String parameters) {
		String schemaCode = getSchemaCode();
		String[] splitParams = splitTaxonomyCodeAndElementId(parameters);
		operation = splitParams[0];
		taxonomyCode = splitParams[1];
		schemaCode = splitParams.length < 3 ? null : splitParams[2];
		conceptId = splitParams.length < 4 ? null : splitParams[3];
		setSchemaCode(schemaCode);
	}

	String[] splitTaxonomyCodeAndElementId(String parameters) {
		if (parameters == null || parameters.equals("")) {
			throw new RuntimeException("Replace with a 404 OR Invalid parameters");
		} else {
			return parameters.split("/", 4);
		}
	}

	public void saveButtonClicked(RecordVO recordVO, boolean isReindexationNeeded) {
		try {
			Record record = toRecord(recordVO);
			recordServices().recalculate(record);

			if (isReindexationNeeded) {
				Transaction transaction = new Transaction();
				transaction.setUser(getCurrentUser());
				recordServices().executeWithoutImpactHandling(transaction.update(record));
				appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
				view.navigate().to().taxonomyManagement(taxonomyCode, conceptId);
			} else {
				addOrUpdate(record, appCollectionExtentions.getRecordsToSaveInSameTransactionAsParentRecord(new GetRecordsToSaveInSameTransactionAsParentRecordParams(recordVO, view.getForm())));

				view.navigate().to().taxonomyManagement(taxonomyCode, conceptId);
			}
		} catch (Exception e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
			LOGGER.error(e.getMessage(), e);
		}
	}

	public void cancelButtonClicked(RecordVO recordVO) {
		view.navigate().to().taxonomyManagement(taxonomyCode, conceptId);
	}

	public String getConceptId() {
		return conceptId;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		forElementInTaxonomy(params);
		return new TaxonomyPresentersService(appLayerFactory).canManage(taxonomyCode, user);
	}

	public void confirmBeforeSave(final RecordVO recordVO) {
		Record record = null;
		try {
			record = toRecord(recordVO);
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}
		recordServices().recalculate(record);
		final boolean isReindexationNeeded;

		if (operation.equals(EDIT) &&
			(record.isModified(Schemas.PATH) || record.isModified(Schemas.CODE) || record.isModified(Schemas.TITLE))) {
			Long numberOfRecordsToChange = searchServices().getResultsCount(fromAllSchemasIn(collection).where(Schemas.PATH)
					.isStartingWithText(((List<String>) originalRecord.get(Schemas.PATH)).get(0)));
			String confirmationMessage;

			if (numberOfRecordsToChange < 10000) {
				confirmationMessage = $("AddEditTaxonomyConceptPresenter.confirmLowerThan10000");
				isReindexationNeeded = false;
			} else {
				confirmationMessage = $("AddEditTaxonomyConceptPresenter.confirmHigherOrEqualTo10000");
				isReindexationNeeded = true;
			}
			ConfirmDialog.show(
					UI.getCurrent(),
					$("AddEditTaxonomyConceptPresenter.confirmTitle"),
					confirmationMessage,
					$("confirm"),
					$("cancel"),
					new ConfirmDialog.Listener() {
						public void onClose(ConfirmDialog dialog) {
							if (dialog.isConfirmed()) {
								saveButtonClicked(recordVO, isReindexationNeeded);
							}
						}
					});
		} else {
			isReindexationNeeded = false;
			saveButtonClicked(recordVO, isReindexationNeeded);
		}
	}


	public void contentVersionUploadFieldChanged(ContentVersionUploadField field) {
		// TODO
	}
}
