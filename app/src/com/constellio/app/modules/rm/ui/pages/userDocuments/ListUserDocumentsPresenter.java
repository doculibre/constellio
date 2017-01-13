package com.constellio.app.modules.rm.ui.pages.userDocuments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.constellio.model.services.contents.icap.IcapException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.UserDocumentToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.users.UserServices;

public class ListUserDocumentsPresenter extends SingleSchemaBasePresenter<ListUserDocumentsView> {

	private static Logger LOGGER = LoggerFactory.getLogger(ListUserDocumentsPresenter.class);

	private UserDocumentToVOBuilder voBuilder = new UserDocumentToVOBuilder();

	public ListUserDocumentsPresenter(ListUserDocumentsView view) {
		super(view, UserDocument.DEFAULT_SCHEMA);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void viewAssembled() {
		List<UserDocumentVO> currentUserUploadVOs = getCurrentUserDocumentVOs();
		try {
			view.setUserDocuments(currentUserUploadVOs);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private List<UserDocumentVO> getCurrentUserDocumentVOs() {
		SessionContext sessionContext = view.getSessionContext();

		UserVO currentUserVO = sessionContext.getCurrentUser();
		String collection = sessionContext.getCurrentCollection();

		UserServices userServices = modelLayerFactory.newUserServices();
		SearchServices searchServices = modelLayerFactory.newSearchServices();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), collection);

		MetadataSchema schema = schema();
		Metadata userMetadata = schema.getMetadata(UserDocument.USER);

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(LogicalSearchQueryOperators.from(schema).where(userMetadata).is(currentUser.getWrappedRecord()));
		query.sortAsc(Schemas.IDENTIFIER);

		List<UserDocumentVO> userDocumentVOs = new ArrayList<UserDocumentVO>();
		List<Record> matches = searchServices.search(query);
		for (Record match : matches) {
			UserDocumentVO userDocumentVO = (UserDocumentVO) voBuilder.build(match, VIEW_MODE.FORM, view.getSessionContext());
			userDocumentVOs.add(userDocumentVO);
		}
		return userDocumentVOs;
	}

	public void handleFile(final File file, String fileName, String mimeType, long length) {
		Record newRecord = newRecord();

		SessionContext sessionContext = view.getSessionContext();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		String collection = sessionContext.getCurrentCollection();

		UserServices userServices = modelLayerFactory.newUserServices();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), collection);

		InputStreamProvider inputStreamProvider = new InputStreamProvider() {
			@Override
			public InputStream getInputStream(String streamName) {
				IOServices ioServices = ConstellioFactories.getInstance().getIoServicesFactory().newIOServices();
				try {
					return ioServices.newFileInputStream(file, streamName);
				} catch (FileNotFoundException e) {
					return null;
				}
			}

			@Override
			public void deleteTemp() {
				FileUtils.deleteQuietly(file);
				file.deleteOnExit();
			}
		};
		UserDocumentVO newUserDocumentVO = (UserDocumentVO) voBuilder.build(newRecord, VIEW_MODE.FORM, view.getSessionContext());
		ContentVersionVO contentVersionVO = new ContentVersionVO(null, null, fileName, mimeType, length, null, null, null,
				null, null, null, inputStreamProvider);
		contentVersionVO.setMajorVersion(true);
		newUserDocumentVO.set(UserDocument.USER, currentUser.getWrappedRecord());
		newUserDocumentVO.set(UserDocument.CONTENT, contentVersionVO);

		try {
			// TODO More elegant way to achieve this
			newRecord = toRecord(newUserDocumentVO);

			addOrUpdate(newRecord);
			contentVersionVO.getInputStreamProvider().deleteTemp();

			newUserDocumentVO = (UserDocumentVO) voBuilder.build(newRecord, VIEW_MODE.FORM, view.getSessionContext());
			view.addUserDocument(newUserDocumentVO);
		} catch (final IcapException e) {
			view.showErrorMessage(e.getMessage());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			Throwable cause = e.getCause();
			if (cause != null && StringUtils.isNotBlank(cause.getMessage()) && cause instanceof ValidationException) {
				view.showErrorMessage(cause.getMessage());
			} else {
				view.showErrorMessage(MessageUtils.toMessage(e));
			}
		}
	}

	public void deleteButtonClicked(UserDocumentVO userUploadVO) {
		Record record = toRecord(userUploadVO);
		try {
			delete(record);
			view.removeUserDocument(userUploadVO);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			view.showErrorMessage(MessageUtils.toMessage(e));
		}

	}

	public void setFolderButtonClicked() {
		String folderId = view.getFolderId();
		List<UserDocumentVO> selectedUserDocumentVOs = view.getSelectedUserDocuments();
		for (UserDocumentVO selectedUserDocumentVO : selectedUserDocumentVOs) {
			selectedUserDocumentVO.setFolder(folderId);
			Record userDocumentRecord = toRecord(selectedUserDocumentVO);
			addOrUpdate(userDocumentRecord);
		}
		List<UserDocumentVO> currentUserUploadVOs = getCurrentUserDocumentVOs();
		view.setUserDocuments(currentUserUploadVOs);
	}

}
