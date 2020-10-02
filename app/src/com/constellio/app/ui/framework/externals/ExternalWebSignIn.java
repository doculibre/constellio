package com.constellio.app.ui.framework.externals;

import com.constellio.app.ui.pages.base.SessionContext;
import lombok.Getter;

import javax.servlet.http.HttpServletRequest;

public interface ExternalWebSignIn {
	ExternalWebSignInResponse getExternalWebSignInResponse();
	void setExternalWebSignInResponse(ExternalWebSignInResponse response);

	class CreateExternalWebSignInCallbackURLParameters{

		@Getter
		private final SessionContext sessionContext;

		@Getter
		private final HttpServletRequest request;

		@Getter
		private final String username;

		public CreateExternalWebSignInCallbackURLParameters(
				SessionContext sessionContext, HttpServletRequest request, String username) {
			this.sessionContext = sessionContext;
			this.request = request;
			this.username = username;
		}


	}
	interface ExternalWebSignInResponse {
		String createCallbackURL(CreateExternalWebSignInCallbackURLParameters parameters);
	}
}
