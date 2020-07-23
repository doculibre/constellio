package com.constellio.app.services.menu.behavior;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.pages.user.DisplayUserCredentialViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.users.UserServices;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class UserCredentialMenuItemActionBehaviors {


	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private UserServices userServices;

	public UserCredentialMenuItemActionBehaviors(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.userServices = modelLayerFactory.newUserServices();
	}

	private Map<String, String> clone(Map<String, String> map) {
		if (map == null) {
			return null;
		}

		Map<String, String> newMap = new HashMap<>();

		newMap.putAll(map);

		return newMap;
	}
	// FIXME C'est 3 méthode ne devrais pas avoir a caster la vu pour obtenir des variable et le presenter.


	public void edit(MenuItemActionBehaviorParams params) {
		Map<String, String> viewParam = clone(params.getFormParams());
		UserCredentialVO userCredentialVO = (UserCredentialVO) params.getObjectRecordVO();

		viewParam.put("username", userCredentialVO.getUsername());
		String parameters = getParameters(NavigatorConfigurationService.USER_DISPLAY, viewParam, (DisplayUserCredentialViewImpl) params.getView());
		params.getView().navigate().to().editUserCredential(parameters);
	}

	private String getParameters(String viewName, Map<String, String> paramsMap,
								 DisplayUserCredentialViewImpl displayUserCredentialView) {
		Map<String, Object> newParamsMap = new HashMap<>();
		newParamsMap.putAll(paramsMap);
		if (!viewName.isEmpty()) {
			viewName = "/" + viewName;
		}
		String parameters = ParamUtils.addParams(displayUserCredentialView.getBreadCrumb() + viewName, newParamsMap);
		parameters = displayUserCredentialView.getPresenter().cleanParameters(parameters);
		return parameters;
	}

	public void generateToken(MenuItemActionBehaviorParams params) {
		WindowButton windowButton = new WindowButton($("DisplayUserCredentialView.generateTokenButton"),
				$("DisplayUserCredentialView.generateToken")) {
			@Override
			protected Component buildWindowContent() {
				UserCredentialVO userCredentialVO = (UserCredentialVO) params.getObjectRecordVO();
				//				final BaseIntegerField durationField = new BaseIntegerField($("DisplayUserCredentialView.Duration"));
				final TextField durationField = new TextField($("DisplayUserCredentialView.Duration"));

				final ComboBox unitTimeCombobox = new BaseComboBox();
				unitTimeCombobox.setNullSelectionAllowed(false);
				unitTimeCombobox.setCaption($("DisplayUserCredentialView.unitTime"));
				unitTimeCombobox.addItem("hours");
				unitTimeCombobox.setItemCaption("hours", $("DisplayUserCredentialView.hours"));
				unitTimeCombobox.setValue("hours");
				unitTimeCombobox.addItem("days");
				unitTimeCombobox.setItemCaption("days", $("DisplayUserCredentialView.days"));

				HorizontalLayout horizontalLayoutFields = new HorizontalLayout();
				horizontalLayoutFields.setSpacing(true);
				horizontalLayoutFields.addComponents(durationField, unitTimeCombobox);

				//
				final Label label = new Label($("DisplayUserCredentialView.serviceKey"));
				final Label labelValue = new Label(getServiceKey(userCredentialVO.getUsername()));
				final HorizontalLayout horizontalLayoutServiceKey = new HorizontalLayout();
				horizontalLayoutServiceKey.setSpacing(true);
				horizontalLayoutServiceKey.addComponents(label, labelValue);

				final Label tokenLabel = new Label($("DisplayUserCredentialView.token"));
				final Label tokenValue = new Label();
				final HorizontalLayout horizontalLayoutToken = new HorizontalLayout();
				horizontalLayoutToken.setSpacing(true);
				horizontalLayoutToken.addComponents(tokenLabel, tokenValue);

				final Link linkTest = new Link($("DisplayUserCredentialView.test"), new ExternalResource(""));
				linkTest.setTargetName("_blank");

				final VerticalLayout verticalLayoutGenerateValues = new VerticalLayout();
				verticalLayoutGenerateValues
						.addComponents(horizontalLayoutServiceKey, horizontalLayoutToken, linkTest);
				verticalLayoutGenerateValues.setSpacing(true);
				verticalLayoutGenerateValues.setVisible(false);

				final BaseButton generateTokenButton = new BaseButton($("DisplayUserCredentialView.generateToken")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						int durationValue;
						try {
							if (durationField.getValue() != null) {
								durationValue = Integer.valueOf(durationField.getValue());
								String serviceKey = getServiceKey(userCredentialVO.getUsername());
								labelValue.setValue(serviceKey);
								String token = generateToken(userCredentialVO.getUsername(), (String) unitTimeCombobox.getValue(),
										durationValue);
								tokenValue.setValue(token);
								String constellioUrl = getConstellioUrl();
								String linkValue = constellioUrl + "select?token=" + token + "&serviceKey=" + serviceKey
												   + "&fq=-type_s:index" + "&q=*:*";
								linkTest.setResource(new ExternalResource(linkValue));

								verticalLayoutGenerateValues.setVisible(true);
							}
						} catch (Exception e) {
						}
					}
				};
				generateTokenButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				generateTokenButton.setEnabled(false);

				durationField.addTextChangeListener(new TextChangeListener() {
					@Override
					public void textChange(TextChangeEvent event) {
						enableOrDisableButton(event.getText(), generateTokenButton);
					}
				});

				//
				VerticalLayout mainVerticalLayout = new VerticalLayout();
				mainVerticalLayout
						.addComponents(horizontalLayoutFields, generateTokenButton, verticalLayoutGenerateValues);
				mainVerticalLayout.setSpacing(true);

				return mainVerticalLayout;
			}
		};

		windowButton.click();
	}

	private void enableOrDisableButton(String value, BaseButton generateTokenButton) {
		boolean enable = false;
		if (value != null) {
			int durationValue;
			try {
				durationValue = Integer.valueOf(value);
				if (durationValue > 0) {
					enable = true;
				}
			} catch (NumberFormatException e) {
			}
		}
		generateTokenButton.setEnabled(enable);
	}

	public String generateToken(String username, String unitTime, int duration) {
		return userServices.generateToken(username, unitTime, duration);
	}

	public String getServiceKey(String username) {
		String serviceKey = userServices.getUserInfos(username).getServiceKey();
		if (serviceKey == null) {
			serviceKey = userServices.giveNewServiceToken(userServices.getUserInfos(username));
		}
		return serviceKey;
	}

	public String getConstellioUrl() {
		return new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager()).getConstellioUrl();
	}
}
