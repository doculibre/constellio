package com.constellio.app.ui.pages.breadcrumb;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.params.ParamUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class BreadcrumbTrailUtil {
	public static IntermediateBreadCrumbTailItem getPilotIntermediateBreadcrumb() {
		return new IntermediateBreadCrumbTailItem() {
			@Override
			public String getTitle() {
				return $("ViewGroup.AdminViewGroup");
			}

			@Override
			public void activate(Navigation navigate) {
				navigate.to(CoreViews.class).adminModule();
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		};
	}

	public static IntermediateBreadCrumbTailItem listSchemaTypeIntermediateBreadcrumb() {
		return new IntermediateBreadCrumbTailItem() {
			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public String getTitle() {
				return $("ListSchemaTypeView.viewTitle");
			}

			@Override
			public void activate(Navigation navigate) {
				navigate.to(CoreViews.class).listSchemaTypes();
			}
		};
	}

	public static IntermediateBreadCrumbTailItem listSchemaIntermediateBreadcrumb(final String schemaCode) {

		return new IntermediateBreadCrumbTailItem() {
			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public String getTitle() {
				return $("ListSchemaView.viewTitle");
			}

			@Override
			public void activate(Navigation navigate) {
				String schemaTypeCode = schemaCode.substring(0, schemaCode.indexOf("_"));

				Map<String, String> paramsMap = new HashMap<>();
				paramsMap.put("schemaTypeCode", schemaTypeCode);
				String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, paramsMap);

				navigate.to(CoreViews.class).listSchema(params);
			}
		};
	}

	public static List<IntermediateBreadCrumbTailItem> llistSchemaTypeSchemaList(String schemaCode) {
		List<IntermediateBreadCrumbTailItem> intermediateBreadCrumbTailItemList = new ArrayList<>();
		intermediateBreadCrumbTailItemList.addAll(Arrays.asList(
				BreadcrumbTrailUtil.listSchemaTypeIntermediateBreadcrumb(),
				BreadcrumbTrailUtil.listSchemaIntermediateBreadcrumb(schemaCode)));

		return intermediateBreadCrumbTailItemList;
	}

	public static IntermediateBreadCrumbTailItem editSchemaMetadata(final String schemaCode, final String schemaTypeCode, final String schemaLabel) {


		return new IntermediateBreadCrumbTailItem() {
			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public String getTitle() {
				return $("AddEditSchemaMetadataView.viewTitle", schemaLabel);
			}

			@Override
			public void activate(Navigation navigate) {
				Map<String,String> parameters = new HashMap<>();
				parameters.put("schemaCode", schemaCode);
				parameters.put("schemaTypeCode", schemaTypeCode);
				String params = ParamUtils.addParams(NavigatorConfigurationService.ADD_EDIT_SCHEMA_METADATA, parameters);
				navigate.to().listSchemaMetadata(params);
			}
		};
	}

	public static IntermediateBreadCrumbTailItem valueDomain() {
		return new IntermediateBreadCrumbTailItem() {
			@Override
			public String getTitle() {
				return $("ListValueDomainView.viewTitle");
			}

			@Override
			public void activate(Navigation navigate) {
				navigate.to().listValueDomains();
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		};
	}

	public static IntermediateBreadCrumbTailItem listSchemaRecord(final String schemaCode) {
		return new IntermediateBreadCrumbTailItem() {
			@Override
			public String getTitle() {
				return $("ListSchemaRecordsView.viewTitle");
			}

			@Override
			public void activate(Navigation navigate) {
				navigate.to().listSchemaRecords(schemaCode);
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		};
	}

	public static IntermediateBreadCrumbTailItem containterByAdministrativeUnit(final String type) {
		return new IntermediateBreadCrumbTailItem() {
			@Override
			public String getTitle() {
				return $("ContainersByAdministrativeUnitsView.viewTitle");
			}

			@Override
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).containersByAdministrativeUnits(type);
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		};
	}
}
