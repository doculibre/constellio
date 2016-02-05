package com.constellio.app.api.benchmarks;

public class BenchmarkWebServiceConfigurationRuntimeException extends RuntimeException {

	public BenchmarkWebServiceConfigurationRuntimeException(String message) {
		super(message);
	}

	public static class BenchmarkWebServiceRuntimeException_MissingParameter
			extends BenchmarkWebServiceConfigurationRuntimeException {

		public BenchmarkWebServiceRuntimeException_MissingParameter(String parameter) {
			super("Parameter '" + parameter + "' is required.");
		}

	}

	public static class BenchmarkWebServiceRuntimeException_BadParameter
			extends BenchmarkWebServiceConfigurationRuntimeException {

		public BenchmarkWebServiceRuntimeException_BadParameter(String parameter, String value) {
			super("Parameter '" + parameter + "' has a bad value : '" + value + "'");
		}
	}

}
