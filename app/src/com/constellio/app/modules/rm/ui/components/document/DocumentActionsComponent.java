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
package com.constellio.app.modules.rm.ui.components.document;

import java.io.Serializable;

import com.constellio.app.modules.rm.ui.entities.ComponentState;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;

public interface DocumentActionsComponent extends Serializable {

	ConstellioNavigator navigateTo();

	void showMessage(String message);

	void showErrorMessage(String errorMessage);

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();

	void setRecordVO(RecordVO recordVO);
	
	void openUploadWindow(boolean checkingIn);
	
	void setEditDocumentButtonState(ComponentState state);
	
	void setDeleteDocumentButtonState(ComponentState state);
	
	void setAddAuthorizationButtonState(ComponentState state);
	
	void setUploadButtonState(ComponentState state);
	
	void setCheckInButtonState(ComponentState state);
	
	void setCheckOutButtonState(ComponentState state);
	
	void setFinalizeButtonVisible(boolean visible);
	
	void setBorrowedMessage(String borrowedMessageKey, String...args);

}
