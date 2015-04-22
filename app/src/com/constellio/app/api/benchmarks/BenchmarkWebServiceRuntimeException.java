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
package com.constellio.app.api.benchmarks;

public class BenchmarkWebServiceRuntimeException extends RuntimeException {

	public BenchmarkWebServiceRuntimeException(String message) {
		super(message);
	}

	public BenchmarkWebServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BenchmarkWebServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class BenchmarkWebServiceRuntimeException_BenchmarkServiceMustBeEnabled
			extends BenchmarkWebServiceRuntimeException {
		public BenchmarkWebServiceRuntimeException_BenchmarkServiceMustBeEnabled() {
			super("Benchmark service must be enabled");
		}
	}

	public static class BenchmarkWebServiceRuntimeException_ParameterRequired extends BenchmarkWebServiceRuntimeException {
		public BenchmarkWebServiceRuntimeException_ParameterRequired(String parameter) {
			super("Parameter '" + parameter + "' is required");
		}
	}

	public static class BenchmarkWebServiceRuntimeException_ParameterInvalid extends BenchmarkWebServiceRuntimeException {
		public BenchmarkWebServiceRuntimeException_ParameterInvalid(String parameter) {
			super("Parameter '" + parameter + "' is invalid");
		}
	}

	public static class BenchmarkWebServiceRuntimeException_ConfigRequired extends BenchmarkWebServiceRuntimeException {
		public BenchmarkWebServiceRuntimeException_ConfigRequired(String parameter) {
			super("Servlet config '" + parameter + "' is required. Add it in web.xml.");
		}
	}

	public static class BenchmarkWebServiceRuntimeException_ConfigInvalid extends BenchmarkWebServiceRuntimeException {
		public BenchmarkWebServiceRuntimeException_ConfigInvalid(String parameter) {
			super("Servlet config '" + parameter + "' is invalid. Configure it in web.xml.");
		}
	}

}
