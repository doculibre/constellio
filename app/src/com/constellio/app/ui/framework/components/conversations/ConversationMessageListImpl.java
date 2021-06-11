package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.ui.framework.components.conversations.ConversationMessageList.ListRefreshedArgs.ListRefreshedListener;
import com.constellio.app.ui.framework.components.conversations.ConversationMessageList.ListRefreshedArgs.ListRefreshedObservable;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.data.ConversationMessageDataProvider;
import com.constellio.app.ui.framework.data.ConversationMessageDataProviderEvents.DataRefreshed;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.Message;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConversationMessageListImpl extends CustomComponent implements ConversationMessageList {

	private final ListRefreshedObservable listRefreshedObservable;
	private final ConversationMessageListArgs args;

	private ConversationMessageListPresenter presenter;

	private VerticalLayout messageListLayout;
	private Component olderMessagesControls;
	private Component newerMessagesControls;

	private final ConversationMessageFactory conversationMessageFactory;

	public ConversationMessageListImpl(ConversationMessageListArgs args) {
		this.args = args;
		this.listRefreshedObservable = new ListRefreshedObservable();
		conversationMessageFactory = new ConversationMessageFactory(args.getSessionContext(), args.getConversationFacetsHandler());
		presenter = new ConversationMessageListPresenter(args.getCollection(), args.getSessionContext(), args.getConversationFacetsHandler(), args.getConversationId(), args.getParentMessageId());
	}

	@Override
	public void attach() {
		super.attach();


		presenter.addDataRefreshedListener(this);

		setCompositionRoot(buildComponent());
	}

	@Override
	public void detach() {
		super.detach();

		presenter.removeDataRefreshedListener(this);
	}

	private Component buildComponent() {

		Layout layout = new VerticalLayout();

		olderMessagesControls = buildTopControls(presenter::isOlderMessagesAvailable,
				loadOlderMessagesAndAddToBeginning());

		newerMessagesControls = buildBottomControls(presenter::isNewerMessagesAvailable,
				loadNewerMessagesAndAddToEnd());

		layout.addComponents(
				olderMessagesControls,
				buildConversationMessageList(),
				newerMessagesControls
		);

		return layout;
	}

	private Component buildConversationMessageList() {
		messageListLayout = new VerticalLayout();

		if (args.getTargetedMessageId() != null) {
			buildContextAroundTargetedMessage();
		} else {
			buildRepliesOfParent();
		}

		return messageListLayout;
	}

	public Component buildTopControls(Supplier<Boolean> hasOlderMessagesToLoad, Runnable loadOlderMessages) {
		return buildDefaultLoadingControls($("ConversationMessageList.load.older"), hasOlderMessagesToLoad, loadOlderMessages);
	}

	public Component buildBottomControls(Supplier<Boolean> hasNewerMessagesToLoad, Runnable loadNewerMessages) {
		return buildDefaultLoadingControls($("ConversationMessageList.load.newer"), hasNewerMessagesToLoad, loadNewerMessages);
	}

	public Component buildDefaultLoadingControls(String caption, Supplier<Boolean> hasMessagesToLoad,
												 Runnable loadMessages) {
		Layout layout = new I18NHorizontalLayout();
		Button loadMoreButton = new Button(caption);
		loadMoreButton.addStyleName(ValoTheme.BUTTON_LINK);

		loadMoreButton.addClickListener(event -> {
			if (hasMessagesToLoad.get()) {
				loadMessages.run();
			}
		});

		layout.addComponents(loadMoreButton);

		return layout;
	}

	private Runnable loadOlderMessagesAndAddToBeginning() {
		return () -> {
			if (presenter.isOlderMessagesAvailable()) {
				presenter.loadOlderMessagesThanThisMessage(presenter.getOldestMessage());
			}
		};
	}

	private Runnable loadNewerMessagesAndAddToEnd() {
		return () -> {
			if (presenter.isNewerMessagesAvailable()) {
				presenter.loadNewerMessagesThanThisMessage(presenter.getNewestMessage());
			}
		};
	}

	private void buildContextAroundTargetedMessage() {
		String targetMessageId = args.getTargetedMessageId();

		presenter.loadMessagesAroundTargetedMessage(targetMessageId);
	}

	private void buildRepliesOfParent() {

		boolean loadNewest = args.getWhatToLoadWhenNoTargetedMessage() == WhatToLoadWhenNoTargetedMessage.LOAD_NEWEST_MESSAGE;

		if (loadNewest) {
			presenter.loadNewestMessages();
		} else {
			presenter.loadOldestMessages();
		}
	}

	private ConversationMessage buildConversationMessage(Message message) {
		ConversationMessage conversationMessage = conversationMessageFactory.build(message);
		conversationMessage.setId(buildDomId(message.getId()));

		return conversationMessage;
	}

	private String buildDomId(String id) {
		return "conversationMessage_" + id;
	}


	private void removeNotUsefulMessages(boolean removeAtBeginning) {
		updatePresenterLimitMessages();
	}

	private void updatePresenterLimitMessages() {
		updatePresenterOldestMessage();
		updatePresenterNewestMessage();
	}

	private void updatePresenterOldestMessage() {
		Message message = null;

		Component itemLoadedOnClient = getFirstItemLoadedOnClient();
		if (itemLoadedOnClient != null) {
			message = ((ConversationMessage) itemLoadedOnClient).getMessage();
		}

		presenter.setOldestMessage(message);
	}

	private void updatePresenterNewestMessage() {
		Message message = null;

		Component itemLoadedOnClient = getLastItemLoadedOnClient();
		if (itemLoadedOnClient != null) {
			message = ((ConversationMessage) itemLoadedOnClient).getMessage();
		}

		presenter.setNewestMessage(message);
	}

	public void refreshControls() {
		if (olderMessagesControls != null) {
			olderMessagesControls.setVisible(presenter.isOlderMessagesAvailable());
		}

		if (newerMessagesControls != null) {
			newerMessagesControls.setVisible(presenter.isNewerMessagesAvailable());
		}
	}

	public Component getFirstItemLoadedOnClient() {
		return messageListLayout.getComponentCount() > 0 ? messageListLayout.getComponent(0) : null;
	}

	public Component getLastItemLoadedOnClient() {
		return messageListLayout.getComponentCount() > 0 ? messageListLayout.getComponent(messageListLayout.getComponentCount() - 1) : null;
	}

	private void showOnScreen(Component component) {
		showOnScreen(component, 0);
	}

	private void showOnScreen(Component component, int howLongToWaitInMilli) {
		if (component != null && component.getId() != null) {
			if (component.isAttached()) {
				JavaScript jsEngine = JavaScript.getCurrent();

				String parentId = args.getQuerySelectorForScrollablePanel() != null ? ", \"" + args.getQuerySelectorForScrollablePanel() + "\"" : "";

				String whatToExecute = "setElementAtCenterOfScreen(\"" + component.getId() + "\"" + parentId + ");";

				if (howLongToWaitInMilli > 0) {
					whatToExecute = "setTimeout(function(){" + whatToExecute + "}, " + howLongToWaitInMilli + ");";
				}

				jsEngine.execute(whatToExecute);
			} else {
				AtomicBoolean hasBeenAttachedOnce = new AtomicBoolean(false);

				component.addAttachListener(event -> {
					if (hasBeenAttachedOnce.compareAndSet(false, true)) {
						showOnScreen(component, howLongToWaitInMilli);
					}
				});
			}
		}
	}

	private void freezeComponentTopPositionBeforeExecuting(Component component, Runnable whatToExecute) {
		freezeComponentTopPositionBeforeExecuting(component, whatToExecute, null);
	}

	private void freezeComponentTopPositionBeforeExecuting(Component component, Runnable whatToExecute,
														   Runnable whatToExecuteAfterDefreeze) {
		JavaScript engine = JavaScript.getCurrent();
		String uuid = UUID.randomUUID().toString().replace("-", "_");

		String freezeComponentFuctionName = "freeze" + component.getId() + uuid;
		String afterDefreezeComponentFuctionName = "afterDefreeze" + component.getId() + uuid;

		String parentId = args.getQuerySelectorForScrollablePanel() != null ? ", \"" + args.getQuerySelectorForScrollablePanel() + "\"" : "";

		engine.addFunction(freezeComponentFuctionName, args -> {

			double componentRelativeTop = args.getNumber(0);

			if (whatToExecute != null) {
				whatToExecute.run();
			}

			engine.execute(
					"setTimeout(" +
					"  function(){" +
					"    setElementAtTopPositionRelativeToScreenAndScrollableElement(\"" + component.getId() + "\", " + componentRelativeTop + "" + parentId + ");" +
					" " + afterDefreezeComponentFuctionName + "();" +
					"  }, 100);"
			);
			engine.removeFunction(freezeComponentFuctionName);
		});

		engine.addFunction(afterDefreezeComponentFuctionName, args -> {
			if (whatToExecuteAfterDefreeze != null) {
				whatToExecuteAfterDefreeze.run();
			}

			engine.removeFunction(afterDefreezeComponentFuctionName);
		});


		engine.execute("getElementTopLocationRelativeToScreenAndScrollableElement(\"" + component.getId() + "\", " + freezeComponentFuctionName + "" + parentId + ");");
	}

	@Override
	public ConversationMessageDataProvider getDataProvider() {
		return presenter != null ? presenter.getDataProvider() : null;
	}

	@Override
	public SessionContext getSessionContext() {
		return args.getSessionContext();
	}

	@Override
	public final void addListRefreshedListener(ListRefreshedListener listener) {
		listRefreshedObservable.addListener(listener);
	}

	@Override
	public final void removeListRefreshedListener(ListRefreshedListener listener) {
		listRefreshedObservable.removeListener(listener);
	}

	@Override
	public void eventFired(DataRefreshed args) {
		Message targetedMessage = args.getTargetedMessage();
		String targetMessageId = targetedMessage != null ? targetedMessage.getId() : null;

		List<Message> messages = args.getMessages();
		if (!messages.isEmpty()) {
			if (args.isNewContext()) {
				messageListLayout.removeAllComponents();

				messages.forEach(message -> messageListLayout.addComponent(buildConversationMessage(message)));

				if (messageListLayout.getComponentCount() > 0) {
					Component messageToShowOnScreen = null;
					if (targetedMessage != null) {
						for (int i = 0; i < messageListLayout.getComponentCount(); i++) {
							ConversationMessage conversationMessage = (ConversationMessage) messageListLayout.getComponent(i);
							if (conversationMessage.getMessage().getId().equals(targetMessageId)) {
								conversationMessage.setHighlighted(true);
								messageToShowOnScreen = conversationMessage;

								break;
							}
						}
					} else {
						messageToShowOnScreen = args.isAddedAfterTargetedMessage() ? getLastItemLoadedOnClient() : getFirstItemLoadedOnClient();
					}

					removeNotUsefulMessages(true);
					refreshControls();

					showOnScreen(messageToShowOnScreen, 300);
					listRefreshedObservable.fire(new ListRefreshedArgs(this, args));
				}
			} else {
				if (args.isAddedAfterTargetedMessage()) {

					freezeComponentTopPositionBeforeExecuting(getLastItemLoadedOnClient(), () -> {
						messages.stream().map(this::buildConversationMessage).forEach(messageListLayout::addComponent);

						removeNotUsefulMessages(true);
						refreshControls();
					}, () -> listRefreshedObservable.fire(new ListRefreshedArgs(this, args)));

				} else {
					freezeComponentTopPositionBeforeExecuting(getFirstItemLoadedOnClient(), () -> {
						for (int i = 0; i < messages.size(); i++) {
							messageListLayout.addComponent(buildConversationMessage(messages.get(i)), i);
						}

						removeNotUsefulMessages(false);
						refreshControls();
					}, () -> listRefreshedObservable.fire(new ListRefreshedArgs(this, args)));
				}
			}
		} else {
			messageListLayout.removeAllComponents();
		}

		removeNotUsefulMessages(true);
		refreshControls();
	}
}
