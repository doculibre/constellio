package com.constellio.model.entities.records.wrappers;

public class EventType {

	public static final String DELETE = "delete";

	public static final String CREATE = "create";

	public static final String MODIFY = "modify";

	public static final String VIEW = "view";

	public static final String BORROW = "borrow";

	public static final String CONSULTATION = "consultation";

	public static final String RETURN = "return";

	public static final String OPEN_SESSION = "open_session";

	public static final String CLOSE_SESSION = "close_session";

	public static final String VIEW_FOLDER = "view_folder";

	public static final String VIEW_DOCUMENT = "view_document";

	public static final String CREATE_FOLDER = "create_folder";

	public static final String MODIFY_FOLDER = "modify_folder";

	public static final String DELETE_FOLDER = "delete_folder";

	public static final String BORROW_FOLDER = "borrow_folder";

	public static final String CONSULTATION_FOLDER = "consultation_folder";

	public static final String RETURN_FOLDER = "return_folder";

	public static final String CREATE_DOCUMENT = "create_document";

	public static final String MODIFY_DOCUMENT = "modify_document";

	public static final String DELETE_DOCUMENT = "delete_document";

	public static final String BORROW_DOCUMENT = "borrow_document";

	public static final String CURRENT_BORROW_DOCUMENT = "current_borrow_document";

	public static final String CURRENTLY_BORROWED_FOLDERS = "currently_borrowed_folders";

	public static final String LATE_BORROWED_FOLDERS = "late_borrowed_folders";

	public static final String RETURN_DOCUMENT = "return_document";

	public static final String BORROW_CONTAINER = "borrow_containerRecord";

	public static final String RETURN_CONTAINER = "return_containerRecord";

	public static final String CREATE_USER = CREATE + "_" + User.SCHEMA_TYPE;

	public static final String MODIFY_USER = MODIFY + "_" + User.SCHEMA_TYPE;

	public static final String DELETE_USER = DELETE + "_" + User.SCHEMA_TYPE;

	public static final String CREATE_GROUP = CREATE + "_" + Group.SCHEMA_TYPE;

	public static final String DELETE_GROUP = DELETE + "_" + Group.SCHEMA_TYPE;

	public static final String GRANT_PERMISSION = "grant_permission";

	public static final String MODIFY_PERMISSION = "modify_permission";

	public static final String DELETE_PERMISSION = "delete_permission";

	public static final String GRANT_PERMISSION_FOLDER = "grant_permission_folder";

	public static final String MODIFY_PERMISSION_FOLDER = "modify_permission_folder";

	public static final String DELETE_PERMISSION_FOLDER = "delete_permission_folder";

	public static final String GRANT_PERMISSION_DOCUMENT = "grant_permission_document";

	public static final String MODIFY_PERMISSION_DOCUMENT = "modify_permission_document";

	public static final String DELETE_PERMISSION_DOCUMENT = "delete_permission_document";

	//decommissiong
	public static final String DECOMMISSIONING_LIST = "decommissioningList";

	public static final String FOLDER_RELOCATION = "Relocate_folder_" + DECOMMISSIONING_LIST;

	public static final String FOLDER_DEPOSIT = "Deposit_folder_" + DECOMMISSIONING_LIST;

	public static final String FOLDER_DESTRUCTION = "destruct_folder_" + DECOMMISSIONING_LIST;

	public static final String PDF_A_GENERATION = "pdfAGeneration";

	public static final String RECEIVE_FOLDER = "receive_folder_" + DECOMMISSIONING_LIST;

	public static final String RECEIVE_CONTAINER = "receive_containerRecord" + DECOMMISSIONING_LIST;

	//tasks
	public static final String CREATE_TASK = "create_userTask";

	public static final String MODIFY_TASK = "modify_userTask";

	public static final String DELETE_TASK = "delete_userTask";

	//workflows
	public static final String WORKFLOW_STARTED = "start_workflow";
}
