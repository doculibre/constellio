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
package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;

@SuppressWarnings("serial")
public class DecommissioningEmailServiceException extends Exception {

	protected DecommissioningEmailServiceException(String message) {
		super(message);
	}

	protected DecommissioningEmailServiceException(String message, Exception e) {
		super(message, e);
	}

	public static class CannotFindManangerEmail extends DecommissioningEmailServiceException {

		public CannotFindManangerEmail(DecommissioningList list) {
			super("Cannot find a manager with an email for that list'" + list.getTitle() + "'");
		}

	}
}
