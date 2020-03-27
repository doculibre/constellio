package com.constellio.app.modules.rm.extensions.api;

import com.constellio.data.frameworks.extensions.SingleValueExtension;
import com.constellio.model.entities.records.wrappers.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.servlet.http.HttpServletRequest;

@Getter
public class AgentExtension {

	private SingleValueExtension<AgentUrlExtension> agentUrlExtension = new SingleValueExtension<>();

	public abstract static class AgentUrlExtension {

		public abstract String getAgentUrl(GetAgentUrlParams params);

		@AllArgsConstructor
		@Getter
		public static class GetAgentUrlParams {
			private User user;
			private String recordId;
			private HttpServletRequest httpRequest;
		}

	}
}
