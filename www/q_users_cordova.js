var Labels = {
    commandQueue: [],
    exec: function(name, params, successCallback, errorCallback) {
        this.commandQueue.push({
            name: name,
            params: params,
            successCallback: successCallback,
            errorCallback: errorCallback
        });
        if (this.commandQueue.length == 1) {
            this.execNext();
        }
    },
    execNext: function() {
        var scope = this;
        if (scope.commandQueue.length == 0) {
            return
        }

        var command = scope.commandQueue[0];

        var finishCommand = function() {
            console.log("Finish exec: " + command.name);
            scope.commandQueue.shift();
            scope.execNext();
        }

        var successFunction = function(data) {
            command.successCallback(data);
            finishCommand();
        };

        var errorFunction = function(err) {
            command.errorCallback(err);
            finishCommand();
        };

        console.log("Exec method: " + command.name);
        cordova.exec(successFunction, errorFunction, "QUsersCordova", command.name, command.params);
    },
    getAll: function(successCallback, errorCallback) {
        this.exec("getAll", [], successCallback, errorCallback);
    },
    /**
     * @param {String} name can be:
     *  "uncategorized", "byTimeAdded",
     *  "byCompany", "hasEmail", "hasPhone", "hasPhoto"
     * @param {Function} onSuccess receives info, where
     */
    smart: function(name, successCallback, errorCallback) {
        this.exec("smart", [name], successCallback, errorCallback);
    },
    /**
     * Gets native labels for contacts
     * @contactIdsArray array of cordova contact ids
     * @param {boolean} doUnion union by labelIds if true, otherwise intersection
     * @param {Function} onSuccess first parameter is object of {labelId: info}
     */
    forContacts: function(contactIdsArray, doUnion, successCallback, errorCallback) {
        this.exec("forContacts", [contactIdsArray, doUnion], successCallback, errorCallback);
    },
    /**
     * Gets one or more native labels
     * @param {array} labelIds an array of labels ids
     * @param onSuccess first parameter is hash of {labelId: info}
     *   and info is same as above in getAll, including contactIds
     */
    get: function(labelIds, successCallback, errorCallback) {
        this.exec("get", [labelIds], successCallback, errorCallback);
    },
    /**
     * Creates or edits label info (except for contacts)
     * @param {Object} info
     * @param {string} [info.labelId] - if specified, tries to edit existing label
     * @param {string} [info.title] - change the title of the label
     * @param {Function} onSuccess - gets new label info as param 1
     * @param {Function} onFailure - gets document error code as param 1
     */
    save: function(info, successCallback, errorCallback) {
        this.exec("save", [(info.labelId ? info.labelId : -1), info.title], successCallback, errorCallback);
    },
    /**
     * Remove an existing label
     */
    remove: function(labelId, successCallback, errorCallback) {
        this.exec("remove", [labelId], successCallback, errorCallback);
    },
    /**
     * Adds a contact to label
     * @param {string} labelId
     * @param {array} contactIds
     * @param onSuccess
     * @param onFailure - gets error code and list of contactIds which were not added
     */
    addContact: function(labelId, contactIds, successCallback, errorCallback) {
        this.exec("addContact", [labelId, contactIds], successCallback, errorCallback);
    },
    /**
     * Remove a contact from label
     * @param {string} labelId
     * @param {array} contactIds
     * @param onSuccess
     * @param onFailure - gets error code and list of contactIds which were not added
     */
    removeContact: function(labelId, contactIds, successCallback, errorCallback) {
        this.exec("removeContact", [labelId, contactIds], successCallback, errorCallback);
    },
    /**
     * Sets all the labels for the contact -- this can be used to add or remove labels since the list of
     * labelIds is supposed to be the total set of labels for the contact, eg [ ] removes all labels.
     * @param {string} contactId
     * @param {array} labelIdsArray
     * @param onSuccess
     * @param onFailure - gets error code and list of contactIds which were not added
     */
    setForContact: function(contactId, labelIds, successCallback, errorCallback) {
        this.exec("setForContact", [contactId, labelIds], successCallback, errorCallback);
    }
}


module.exports = {
    //        hello: function (name, successCallback, errorCallback) {
    //            cordova.exec(successCallback, errorCallback, "QUsersCordova", "hello", [name]);
    //        }
};

module.exports.Labels = Labels;