package com.constellio.app.modules.tasks.extensions;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.api.extensions.MetadataThatDontSupportRoleAccessExtension;
import com.constellio.app.api.extensions.MetadataThatDontSupportRoleAccessRetValue;
import com.constellio.app.modules.rm.wrappers.RMTask;

public class TaskMetadataThatDontSupportRoleAccessExtension implements MetadataThatDontSupportRoleAccessExtension {
	@Override
	public List<MetadataThatDontSupportRoleAccessRetValue> getMetadataThatDontSupportRoleAccess() {
		return Arrays.asList(new MetadataThatDontSupportRoleAccessRetValue(RMTask.SCHEMA_TYPE, null, RMTask.PARENT_TASK));
	}
}
