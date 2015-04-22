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
package com.constellio.data.dao.services.factories;

@SuppressWarnings("serial")
public class BigVaultDaosFactoryRuntimeException extends RuntimeException {

	public BigVaultDaosFactoryRuntimeException() {
	}

	public BigVaultDaosFactoryRuntimeException(String message) {
		super(message);
	}

	public BigVaultDaosFactoryRuntimeException(Throwable cause) {
		super(cause);
	}

	public BigVaultDaosFactoryRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotCreateContentsDAO extends BigVaultDaosFactoryRuntimeException {

		public CannotCreateContentsDAO(Throwable t) {
			super("Cannot create contents DAO", t);
		}
	}

}
