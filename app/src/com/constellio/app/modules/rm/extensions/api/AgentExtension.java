package com.constellio.app.modules.rm.extensions.api;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.constellio.data.frameworks.extensions.SingleValueExtension;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.records.wrappers.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class AgentExtension {

	private SingleValueExtension<AgentUrlExtension> agentUrlExtension = new SingleValueExtension<>();

	private SingleValueExtension<AgentGetPassthroughPathExtension> agentGetPassthroughPathExtension = new SingleValueExtension<>();

	private SingleValueExtension<AgentGetCheckedOutDocumentsExtension> agentGetCheckedOutDocumentsExtension = new SingleValueExtension<>();

	private VaultBehaviorsList<AgentClientConfigExtension> agentClientConfigExtensions = new VaultBehaviorsList<>();

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

	public abstract static class AgentGetPassthroughPathExtension {

		public abstract String getPassthroughPath(GetPassthroughPathParams params);

		@AllArgsConstructor
		@Getter
		public static class GetPassthroughPathParams {
			private User user;
			private String recordId;
			private String metadataCode;
		}

	}

	public abstract static class AgentGetCheckedOutDocumentsExtension {

		public abstract List<Map<String, String>> getCheckedOutDocuments(GetCheckedOutDocumentsParams params);

		@AllArgsConstructor
		@Getter
		public static class GetCheckedOutDocumentsParams {
			private User user;
			private String collection;
		}

	}

	public abstract static class AgentClientConfigExtension {

		public abstract void addClientConfigs(AgentClientConfigParams params);

		@AllArgsConstructor
		@Getter
		public static class AgentClientConfigParams {
			private Properties agentConfigs;
		}

	}
}
