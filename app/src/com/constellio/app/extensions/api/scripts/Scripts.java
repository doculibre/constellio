package com.constellio.app.extensions.api.scripts;

import com.constellio.data.services.tenant.TenantLocal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scripts {

	private static TenantLocal<List<Script>> scripts = new TenantLocal<>();

	public static void registerScript(Script script) {
		if (scripts.get() == null) {
			scripts.set(new ArrayList<Script>());
		}
		scripts.get().add(script);
	}

	public static List<Script> getScripts() {
		if (scripts.get() == null) {
			scripts.set(new ArrayList<Script>());
		}
		return Collections.unmodifiableList(scripts.get());
	}

	public static void removeScripts() {
		if (scripts.get() != null) {
			scripts.get().clear();
		}
	}

}
