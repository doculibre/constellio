package com.constellio.app.services.actionDisplayManager;

import com.constellio.app.services.actionDisplayManager.MenuPositionActionOptions.Position;
import com.constellio.app.services.actionDisplayManager.MenusDisplayTransaction.Action;
import com.constellio.app.services.actionDisplayManager.MenusDisplayTransaction.TransactionElement;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MenusDisplayManagerWriter {
	private Document document;
	private static final String MENU_DISPLAY = "menuDisplay";

	MenusDisplayManagerWriter(Document document) {
		this.document = document;
	}

	public void addUpdateActionDisplay(String schemaType, final MenuDisplayItem actionDisplay,
									   final MenuPositionActionOptions actionDisplayIO) {
		Element schemaTypeElement = this.createOrGetSchemaTypeElement(schemaType);

		Position position = actionDisplayIO.getPosition();
		Element actionDisplayElement = createActionDisplayElement(actionDisplay);

		if (position == Position.AFTER || position == position.BEFORE) {
			this.removeDisplayAction(schemaTypeElement, actionDisplay.getCode());
			int relativeItemPosition = getCodePosition(schemaTypeElement, actionDisplayIO.getRelativeActionCode());

			relativeItemPosition += (position == Position.AFTER ? 1 : 0);

			schemaTypeElement.addContent(relativeItemPosition, actionDisplayElement);
		} else {
			if (position == Position.AT_BEGINNING) {
				schemaTypeElement.addContent(0, actionDisplayElement);
			} else if (position == Position.AT_END) {
				schemaTypeElement.addContent(schemaTypeElement.getChildren().size(), actionDisplayElement);
			}
		}
	}

	public void removeTextContent(Element schemaTypeElement) {

		for (Iterator it = schemaTypeElement.getContent().iterator(); it.hasNext(); ) {
			Content content = (Content) it.next();
			if (content instanceof Text) {
				it.remove();
			}
		}
	}

	public void execute(MenusDisplayTransaction transaction) {
		for (TransactionElement actionsDisplayTransactionElement : transaction.getTransactionElements()) {
			if (actionsDisplayTransactionElement.getAction() == Action.ADD_UPDATE) {
				this.addUpdateActionDisplay(actionsDisplayTransactionElement.getSchemaType(), actionsDisplayTransactionElement.getMenuDisplayItem(), actionsDisplayTransactionElement.getMenuPositionActionOptions());
			} else if (actionsDisplayTransactionElement.getAction() == Action.REMOVE) {
				this.removeDisplayAction(actionsDisplayTransactionElement.getSchemaType(), actionsDisplayTransactionElement.getMenuDisplayItem().getCode());
			}
		}
	}

	public void createEmptyMenusDisplay() {
		this.document.setRootElement(new Element(MENU_DISPLAY));
	}

	private void validateUnicity(List<MenuDisplayItem> actionDisplay) {

		List<String> codes = new ArrayList<>();

		for (MenuDisplayItem currentActionDisplay : actionDisplay) {
			if (codes.contains(currentActionDisplay.getCode())) {
				throw new IllegalArgumentException("Codes have to unique in a schematype");
			}
		}
	}

	public void withActionsDisplay(String schemaType, List<MenuDisplayItem> actionDisplayList) {
		validateUnicity(actionDisplayList);

		if (actionDisplayList.size() > 0) {
			Element schemaTypeElement = this.createOrGetSchemaTypeElement(schemaType);
			schemaTypeElement.removeContent();
		} else {
			removeSchemaType(schemaType);
		}

		for (MenuDisplayItem currentActionDisplay : actionDisplayList) {
			this.addUpdateActionDisplay(schemaType, currentActionDisplay, MenuPositionActionOptions.displayActionAtEnd());
		}
	}

	public void updateActionDisplay(String schemaType, final MenuDisplayItem actionDisplay) {
		Element schemaTypeElement = this.createOrGetSchemaTypeElement(schemaType);
		int indexWithElement = getCodePosition(schemaTypeElement, actionDisplay.getCode());

		if (indexWithElement < 0) {
			throw new IllegalArgumentException("Action display code is not there so it cannot be updated : " + actionDisplay.getCode());
		}

		removeDisplayAction(schemaTypeElement, actionDisplay.getCode());

		schemaTypeElement.addContent(indexWithElement, createActionDisplayElement(actionDisplay));
	}

	private Element removeDisplayAction(String schemaTypeCode, String actionKey) {
		Element schemaTypeElement = this.createOrGetSchemaTypeElement(schemaTypeCode);

		return removeDisplayAction(schemaTypeElement, actionKey);
	}

	private Element removeDisplayAction(Element schemaTypeElement, String actionKey) {
		int indexToRemove = getCodePosition(schemaTypeElement, actionKey);

		if (indexToRemove < 0) {
			return null;
		}

		Element removedElement = (Element) schemaTypeElement.removeContent(indexToRemove);

		if (schemaTypeElement.getChildren().size() == 0) {
			removeSchemaType(schemaTypeElement.getAttributeValue(IMenuDisplayIO.SCHEMA_TYPE));
		}

		return removedElement;
	}

	private void removeSchemaType(String schemaType) {
		Element rootElement = getRootElement();

		for (Iterator itr = rootElement.getChildren().iterator(); itr.hasNext(); ) {
			Element currentSchemaTypeElement = (Element) itr.next();
			if (schemaType.equals(currentSchemaTypeElement.getAttributeValue(IMenuDisplayIO.SCHEMA_TYPE))) {
				itr.remove();
			}
		}
	}

	protected Element createOrGetSchemaTypeElement(String schemaType) {
		Element rootElement = getRootElement();

		for (Element currentSchemaTypeElement : rootElement.getChildren()) {
			if (schemaType.equals(currentSchemaTypeElement.getAttributeValue(IMenuDisplayIO.SCHEMA_TYPE))) {
				removeTextContent(currentSchemaTypeElement);
				return currentSchemaTypeElement;
			}
		}

		Element schemaTypeElement = new Element("schemaType");
		schemaTypeElement.setAttribute(IMenuDisplayIO.SCHEMA_TYPE, schemaType);

		rootElement.addContent(schemaTypeElement);

		return schemaTypeElement;
	}

	private Element getRootElement() {
		Element rootElement = document.getRootElement();
		removeTextContent(rootElement);
		return rootElement;
	}

	private int getCodePosition(Element schemaTypeElement, String codeToFind) {
		int i = 0;
		for (Content content : schemaTypeElement.getContent()) {
			if (content instanceof Element) {
				Element element = (Element) content;
				String code = element.getAttributeValue(IMenuDisplayIO.CODE);
				if (codeToFind.equals(code)) {
					return i;
				}
			}
			i++;
		}

		return -1;
	}

	protected Element createActionDisplayElement(MenuDisplayItem actionDisplay) {

		Element actionDisplayElement = new Element("actionDisplayElement");
		if (actionDisplay instanceof MenuDisplayContainer) {
			MenuDisplayContainer menuDisplayContainer = (MenuDisplayContainer) actionDisplay;
			for (Locale language : menuDisplayContainer.getLabels().keySet()) {
				actionDisplayElement.setAttribute(IMenuDisplayIO.LABELS + "_" + language.getLanguage(), menuDisplayContainer.getLabels().get(language));
			}
		}
		actionDisplayElement.setAttribute(IMenuDisplayIO.CODE, actionDisplay.getCode());

		if (actionDisplay.getParentCode() != null) {
			actionDisplayElement.setAttribute(IMenuDisplayIO.PARENT_CODE, actionDisplay.getParentCode());
		}

		actionDisplayElement.setAttribute(IMenuDisplayIO.TYPE, actionDisplay.getType().getCode());
		actionDisplayElement.setAttribute(IMenuDisplayIO.ACTIVE, actionDisplay.isOfficiallyActive() + "");
		actionDisplayElement.setAttribute(IMenuDisplayIO.ALWAYS_ACTIVE, actionDisplay.isAlwaysActive() + "");
		if (actionDisplay.getIcon() != null) {
			actionDisplayElement.setAttribute(IMenuDisplayIO.ICON, actionDisplay.getIcon());
		}

		if (actionDisplay.getI18nKey() != null) {
			actionDisplayElement.setAttribute(IMenuDisplayIO.I18N_KEY, actionDisplay.getI18nKey());
		}

		return actionDisplayElement;
	}

	public void deleteActionDisplay(String schemaType, String code) {
		Element schemaTypeElement = this.createOrGetSchemaTypeElement(schemaType);
		Iterator<Element> iterator = schemaTypeElement.getChildren().listIterator();

		while (iterator.hasNext()) {
			Element child = iterator.next();
			if (child.getAttribute(IMenuDisplayIO.CODE).getValue().equals(code)) {
				iterator.remove();
				break;
			}
		}
	}
}
