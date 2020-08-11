package com.constellio.app.ui.pages.management.capsule.addEdit;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.management.bagInfo.AddEditBagInfo.AddEditBagInfoPresenter;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServicesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;
import static com.constellio.model.entities.schemas.Schemas.TOKENS;

public class AddEditCapsulePresenter extends BasePresenter<AddEditCapsuleView> {

	private SchemaPresenterUtils utils;

	private static Logger LOGGER = LoggerFactory.getLogger(AddEditBagInfoPresenter.class);

	public AddEditCapsulePresenter(AddEditCapsuleView view) {
		super(view);
		utils = new SchemaPresenterUtils(Capsule.DEFAULT_SCHEMA, view.getConstellioFactories(), view.getSessionContext());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.ACCESS_SEARCH_CAPSULE).globally();
	}

	public RecordVO getRecordVO(String id) {
		return new RecordToVOBuilder().build(utils.getRecord(id), RecordVO.VIEW_MODE.FORM, view.getSessionContext());

	}

	public RecordVO newRecordVO() {
		return new RecordToVOBuilder().build(utils.newRecord(), RecordVO.VIEW_MODE.FORM, view.getSessionContext());
	}

	public void saveButtonClicked(RecordVO recordVO) throws RecordServicesException {
		try {
			Record record = utils.toRecord(recordVO);
			record.set(TOKENS, Arrays.asList(PUBLIC_TOKEN));
			Transaction trans = new Transaction();
			trans.update(record);
			utils.recordServices().execute(trans);
			view.navigate().to().previousView();
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}
	}

	public void cancelButtonClicked() {
		view.navigate().to().listCapsule();
	}

	public String getHash(ContentVersionVO contentVersionVO) {
		String hash = contentVersionVO.getHash();
		if (hash == null) {
			ContentManager contentManager = modelLayerFactory.getContentManager();
			HashingService hashingService = contentManager.getHashingService();
			try (InputStream in = contentVersionVO.getInputStreamProvider().getInputStream(getClass().getSimpleName() + ".getHash")) {
				hash = hashingService.getHashFromStream(in);
			} catch (IOException e) {
				view.showErrorMessage(e.getMessage());
				throw new RuntimeException(e);
			} catch (HashingServiceException e) {
				view.showErrorMessage(e.getMessage());
				throw new RuntimeException(e);
			}
		}
		return hash;
	}
}
