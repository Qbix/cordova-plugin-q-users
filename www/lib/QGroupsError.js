var QGroupsError = function(err) {
    this.code = (typeof err != 'undefined' ? err : null);
};

/**
 * Error codes
 */
QGroupsError.DUBLICATE_LABELS_NAME = {
	"code":0,
	"message":"Dublicate labels"
};
QGroupsError.NOT_FOUND_LABEL = {
	"code":1,
	"message":"Not found label"
};
QGroupsError.NOT_FOUND_CONTACT = {
	"code":2,
	"message":"Not found contact id"
};
QGroupsError.EMAIL_SENT_NOT_AVAILABLE = {
	"code":3,
	"message":"Email sent function is not available"
};
QGroupsError.SMS_CANCEL_SENT = {
	"code":4,
	"message":"SMS cancel sent"
};
QGroupsError.INVALID_ARGUMENT_ERROR = 1;
QGroupsError.TIMEOUT_ERROR = 2;
QGroupsError.PENDING_OPERATION_ERROR = 3;
QGroupsError.IO_ERROR = 4;
QGroupsError.NOT_SUPPORTED_ERROR = 5;
QGroupsError.PERMISSION_DENIED_ERROR = 20;

module.exports = QGroupsError;
