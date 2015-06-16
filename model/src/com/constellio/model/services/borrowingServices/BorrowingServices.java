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
package com.constellio.model.services.borrowingServices;

import java.util.Date;

import org.joda.time.LocalDate;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_CannotBorrowActiveFolder;
import com.constellio.model.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed;
import com.constellio.model.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_FolderIsNotBorrowed;
import com.constellio.model.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder;
import com.constellio.model.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

//TODO Thiago - move to app
public class BorrowingServices {
	public static final String FOLDER_DEFAULT_BORROWED = "folder_default_borrowed";
	public static final String FOLDER_DEFAULT_BORROW_DATE = "folder_default_borrowDate";
	public static final String FOLDER_DEFAULT_BORROW_PREVIEW_RETURN_DATE = "folder_default_borrowPreviewReturnDate";
	public static final String FOLDER_DEFAULT_BORROW_USER = "folder_default_borrowUser";
	public static final String FOLDER_DEFAULT_BORROW_USER_ENTERED = "folder_default_borrowUserEntered";
	public static final String FOLDER_DEFAULT_ARCHIVISTIC_STATUS = "folder_default_archivisticStatus";
	//	public static final String FOLDER_DEFAULT_RECORD_ID = "folder_default_recordIdentifier";
	public static final String RGD = "RGD";
	public static final String ACTIVE = "ACTIVE";
	private final String collection;
	private final ModelLayerFactory modelLayerFactory;
	private final RecordServices recordServices;
	private final LoggingServices loggingServices;

	public BorrowingServices(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.recordServices = modelLayerFactory.newRecordServices();
		this.loggingServices = modelLayerFactory.newLoggingServices();
	}

	public void borrowFolder(String folderId, Date previewReturnDate, User currentUser, User borrowerEntered)
			throws RecordServicesException {

		Record folder = recordServices.getDocumentById(folderId);
		validateCanBorrow(currentUser, folder);
		setBorrowedMetadatasToFolder(folder, previewReturnDate, currentUser.getId(), borrowerEntered.getId());
		recordServices.update(folder);
		loggingServices.borrowRecord(folder, borrowerEntered);
	}

	public void returnFolder(String folderId, User currentUser)
			throws RecordServicesException {

		Record folder = recordServices.getDocumentById(folderId);
		validateCanReturnFolder(currentUser, folder);
		folder = setReturnedMetadatasToFolder(folder);
		recordServices.update(folder);
		loggingServices.returnRecord(folder, currentUser);
	}

	public void validateCanReturnFolder(User currentUser, Record folder) {
		MetadataSchemaTypes types = getMetadataSchemaTypes();

		if (currentUser.hasReadAccess().on(folder)) {
			if (folder.get(types.getMetadata(FOLDER_DEFAULT_BORROWED)) == null) {
				throw new BorrowingServicesRunTimeException_FolderIsNotBorrowed(folder.getId());
			} else if (!currentUser.getUserRoles().contains(RGD) && !currentUser.getId()
					.equals(folder.get(types.getMetadata(FOLDER_DEFAULT_BORROW_USER_ENTERED)))) {
				throw new BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder(currentUser.getUsername());
			}
		} else {
			throw new BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder(currentUser.getUsername(), folder.getId());
		}

	}

	public void validateCanBorrow(User currentUser, Record folder) {

		MetadataSchemaTypes types = getMetadataSchemaTypes();
		if (currentUser.hasReadAccess().on(folder)) {
			if (ACTIVE.equals(folder.get(types.getMetadata(FOLDER_DEFAULT_ARCHIVISTIC_STATUS)).toString())) {
				throw new BorrowingServicesRunTimeException_CannotBorrowActiveFolder(folder.getId());
			} else if (folder.get(types.getMetadata(FOLDER_DEFAULT_BORROWED)) != null) {
				throw new BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed(folder.getId());
			}
		} else {
			throw new BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder(currentUser.getUsername(), folder.getId());
		}
	}

	//

	private void setBorrowedMetadatasToFolder(Record folder, Date previewReturnDate, String userId, String borrowerEnteredId) {
		MetadataSchemaTypes types = getMetadataSchemaTypes();
		LocalDate newPreviewReturnDate = LocalDate.fromDateFields(previewReturnDate);
		folder.set(types.getMetadata(FOLDER_DEFAULT_BORROWED), true);
		folder.set(types.getMetadata(FOLDER_DEFAULT_BORROW_DATE), TimeProvider.getLocalDateTime());
		folder.set(types.getMetadata(FOLDER_DEFAULT_BORROW_PREVIEW_RETURN_DATE), newPreviewReturnDate);
		folder.set(types.getMetadata(FOLDER_DEFAULT_BORROW_USER), userId);
		folder.set(types.getMetadata(FOLDER_DEFAULT_BORROW_USER_ENTERED), borrowerEnteredId);
		//		folder.set(types.getMetadata(FOLDER_DEFAULT_RECORD_ID), folder.getId());
	}

	private Record setReturnedMetadatasToFolder(Record folder) {
		MetadataSchemaTypes types = getMetadataSchemaTypes();

		folder.set(types.getMetadata(FOLDER_DEFAULT_BORROWED), null);
		folder.set(types.getMetadata(FOLDER_DEFAULT_BORROW_DATE), null);
		folder.set(types.getMetadata(FOLDER_DEFAULT_BORROW_PREVIEW_RETURN_DATE), null);
		folder.set(types.getMetadata(FOLDER_DEFAULT_BORROW_USER), null);
		folder.set(types.getMetadata(FOLDER_DEFAULT_BORROW_USER_ENTERED), null);
		return folder;
	}

	private MetadataSchemaTypes getMetadataSchemaTypes() {
		return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
	}

}