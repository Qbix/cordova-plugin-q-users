/*global cordova, module*/
// Q.Cordova.Users = {
//    Labels:  {
//       ///add: ..., 								addLabel(title, onSuccess, onError)
//       //remove: ..., 								removeLabel(labelId, onSuccess, onError)
//       //update: ... (instead of editLabel),		editLabel(labelId, info, onSuccess, onError)
//       //getAll: ... (instead of labels)			labels(onSuccess, onError)
//       //forContacts: ...							labelsForContacts(contactIdsArray, doUnion, onSuccess, onError)
//    },

//    Contacts: {
//       forLabels,								сontactsForLabels(labelIdsArray, doUnion, onSuccess, onError)
//       withoutLabel,								contactsNoLabel(onSuccess(contactsArray), onError)
//       withPhoto,								contactsWithPhoto(onSuccess(contactsArray), onError(error))
//       withEmail,								contactsWithEmail(onSuccess(contactsArray), onError(error))
//       withPhone,								contactsWithPhone(onSuccess(contactsArray), onError(error))
//       setLabels,								contactSetLabels(contactId, labelsIdsArray, onSuccess, onError)
//       setLabel 									contactsSetLabel(contactIds, labelId, onSuccess, onError)
//       noInLabels 								contactsNoInLabels(labelIdsArray,onSuccess(contactsArray), onError)
//    }

// }

var groupsFileName = "groups_labes.json";

    var contactsFieldsAll = [
        ContactFieldType.id,
        ContactFieldType.rawId,
        ContactFieldType.displayName,
        ContactFieldType.name,
        ContactFieldType.nickname,
        ContactFieldType.phoneNumbers,
        ContactFieldType.emails,
        ContactFieldType.addresses,
        ContactFieldType.ims,
        ContactFieldType.organizations,
        ContactFieldType.birthday,
        ContactFieldType.note,
        ContactFieldType.photos,
        ContactFieldType.categories,
        ContactFieldType.urls
    ];

    var contactsFieldsSimple = [
        ContactFieldType.id,
        //ContactFieldType.rawId,
        ContactFieldType.displayName,
        ContactFieldType.name,
        ContactFieldType.nickname,
        ContactFieldType.phoneNumbers,
        ContactFieldType.emails,
        ContactFieldType.addresses,
        ContactFieldType.birthday,
        ContactFieldType.photos
    ];

var Labels = {
        labels:function(onSuccess, onError) {
            utils.getGroupsLabelObj(function(obj){
                onSuccess(obj);
            }, onError);
        },
		add:function(title, onSuccess, onError) {
		    var context = this;
            utils.getGroupsLabelObj(function(obj){
                var hash = Q.Users.Cordova.md5.MD5(title);

                if(utils.isPresentInLabelArray(hash, obj)) {
                    onError(Q.Users.Cordova.QGroupsError.DUBLICATE_LABELS_NAME);
                } else {
                    var item =  {
                        id : String(hash),
                        title: title,
                        members: []
                    };
                    
                    obj.push(item);

                    utils.writeGroupsLabelObj(obj, function() {
                        onSuccess(item.id);
                    }, onError);
                }

            }, onError);
        },
        remove:function(labelId, onSuccess, onError) {
            var context = this;
            utils.getGroupsLabelObj(function(obj){
                if(!utils.isPresentInLabelArray(labelId, obj)) {
                    onError(Q.Users.Cordova.QGroupsError.NOT_FOUND_LABEL);
                } else {
                    
                    var newLabelsArray = utils.removeElementFromArrayByIdParam(labelId, obj);
                    if(newLabelsArray == Q.Users.Cordova.QGroupsError.NOT_FOUND_LABEL) {
                        onError(Q.Users.Cordova.QGroupsError.NOT_FOUND_LABEL);
                    } else {
                        utils.writeGroupsLabelObj(newLabelsArray, function() {
                            onSuccess(labelId);
                        }, onError);
                    }
                }

            }, onError);
        },
        update:function(labelId, info, onSuccess, onError) {
            var context = this;
            utils.getGroupsLabelObj(function(obj){
                if(!utils.isPresentInLabelArray(labelId, obj)) {
                    onError(Q.Users.Cordova.QGroupsError.NOT_FOUND_LABEL);
                } else {
                    for (var i=0; i<obj.length; i++) {
                        if(obj[i].id == labelId) {
                            obj[i].title = info;
                            break;
                        }
                    }
                    utils.writeGroupsLabelObj(obj, function() {
                        onSuccess(labelId);
                    }, onError);
                }

            }, onError);
        },
        forContacts:function(contactIdsArray, doUnion, onSuccess, onError) {
            utils.getGroupsLabelObj(function(obj){
                var labelsArray = [];
                if(doUnion) {
                    for(var i=0; i < contactIdsArray.length; i++) {
                        for (var j=0; j<obj.length; j++) {
                            if(utils.isPresentInArray(contactIdsArray[i], obj[j].members)) {
                                if(!utils.isPresentInArray(obj[j].id, labelsArray)) {
                                    labelsArray.push(obj[j]);
                                }
                                break;
                            }
                        }
                    }
                } else {
                    for(var i=0; i<obj.length; i++) {
                        var labelObject = JSON.parse(JSON.stringify(obj[i]));
                        labelObject.members = [];
                        for(var j=0; j < contactIdsArray.length; j++) {
                            if(utils.isPresentInArray(contactIdsArray[j], obj[i].members)) {
                                if(!utils.isPresentInArray(contactIdsArray[j], labelObject.members)) {
                                    labelObject.members.push(contactIdsArray[j]);
                                }
                            }
                        }

                        if(labelObject.members.length > 0) {
                            labelsArray.push(labelObject)
                        }
                    }
                }

                onSuccess(labelsArray);
            }, onError);
        },
        removeAll:function(onSuccess, onError) {
            utils.writeGroupsLabelObj([], onSuccess, onError);
        }
}

var Contacts = {
        forLabels:function(labelIdsArray, doUnion, onSuccess, onError) {
            var context = this;
            utils.getGroupsLabelObj(function(obj){
                var contactsArray = [];
                for(var i=0; i < labelIdsArray.length; i++) {
                    for (var j=0; j<obj.length; j++) {
                        if(obj[j].id == labelIdsArray[i]) {
                            var labelResult = {}
                            labelResult.id = labelIdsArray[i];
                            labelResult.membersIds = obj[j].members;
                            contactsArray.push(labelResult)
                        }
                    }
                }

                var mergeContacts = [];
                for (var i = 0; i < contactsArray.length; i++) {
                    for (var j = 0; j < contactsArray[i].membersIds.length; j++) {
                        if(!utils.isPresentInArray(contactsArray[i].membersIds[j], mergeContacts)) {
                            mergeContacts.push(contactsArray[i].membersIds[j]);
                        }
                    }
                }

                context.contacts(function(contacts) {
                    console.log(contacts);
                    if(doUnion) {
                        onSuccess(contacts);
                    } else {
                       for (var i = 0; i < contactsArray.length; i++) {
                            contactsArray[i].members = [];
                            for(var j = 0; j < contactsArray[i].membersIds.length; j++) {
                                for(var k = 0; k < contacts.length; k++) {
                                    if(contactsArray[i].membersIds[j] === contacts[k].id) {
                                        contactsArray[i].members.push(contacts[k]);
                                    }
                                }
                            }
                        }

                        onSuccess(contactsArray);
                    }
                }, onError, {
                    multiple: true,
                    filterByIds: mergeContacts
                });

            }, onError);
        },
        withoutLabel:function(onSuccess, onError) {
            this.contacts(function(contactsArray){
                utils.getGroupsLabelObj(function(obj){
                    var contactsNoLabelArray = [];
                    for(var i=0; i < contactsArray.length; i++) {
                        var searchedContacts = contactsArray[i];
                        var isPresentContactInLabel = false;
                        for(var j=0; j < obj.length; j++) {
                            if(isPresentContactInLabel) {
                                break;
                            }
                            for(var k=0; k < obj[j].members.length; k++) {
                                if(utils.isPresentInArray(searchedContacts.id, obj[j].members)) {
                                    isPresentContactInLabel = true;
                                    break;
                                }
                            }
                        }
                        if(!isPresentContactInLabel) {
                            contactsNoLabelArray.push(searchedContacts);
                        }
                    }

                    onSuccess(contactsNoLabelArray);
                });
            }, onError);
        },
        noInLabels:function(labelIdsArray,onSuccess, onError) {
            var context = this;
            utils.getGroupsLabelObj(function(obj){
                var contactsNoLabelArray = [];
                for(var i=0; i < obj.length; i++) {
                    if(utils.isPresentInArray(obj[i].id, labelIdsArray)) {
                        for(var j=0; j < obj[i].members.length; j++) {
                            if(!utils.isPresentInArray(obj[i].members[j], contactsNoLabelArray)) {
                                contactsNoLabelArray.push(obj[i].members[j]);
                            }
                        }
                    }
                }

                context.contacts(function(contactsArray) {
                    onSuccess(contactsArray);
                }, onError, {
                    multiple: true,
                    filterSkipIds: contactsNoLabelArray
                });

            },onError);
        }, //*
        setLabels:function(contactId, labelsIdsArray, onSuccess, onError) {
            var context = this;
            utils.getGroupsLabelObj(function(obj){
                for (var i=0; i<obj.length; i++) {
                    obj[i].members = utils.removeMemberFromArrayByIdParam(contactId, obj[i].members);
                }
                for (var j=0; j<labelsIdsArray.length; j++) {
                   for (var i=0; i<obj.length; i++) {
                      if(obj[i].id == labelsIdsArray[j]) {
                         obj[i].members.push(contactId);
                      }
                   }
                }
                utils.writeGroupsLabelObj(obj, function() {
                   onSuccess();
                }, onError);
            }, onError);
        },
        setLabel:function(contactIds, labelsId, onSuccess, onError) {
            var context = this;
            utils.getGroupsLabelObj(function(obj){
                for (var i=0; i<obj.length; i++) {
                   if(obj[i].id == labelsId) {
                      for(var j=0; j < contactIds.length; j++) {
                         if(!utils.isPresentInArray(contactIds[j], obj[i].members)) {
                            obj[i].members.push(contactIds[j]);
                         }
                      }
                   }
                }
                utils.writeGroupsLabelObj(obj, function() {
                   onSuccess();
                }, onError);
            }, onError);
        },//+
        contacts:function(onSuccess, onError, options) {
            var optionsFinal = {};
            if(typeof options != undefined) {
                optionsFinal = options
            } else {
                  optionsFinal = {
                    filter: '',
                    multiple: true
                  };
            }

            navigator.contacts.find(contactsFieldsSimple, onSuccess, onError, optionsFinal);
        },
        withPhoto:function(onSuccess, onError) {
         var options = {
                        filter: '',
                        multiple: true,
                        hasPhoto: true
                    };
            this.contacts(function(contacts) {
                var contactsWithPhotosArray = [];
                for(var i=0; i < contacts.length; i++) {
                    if(contacts[i].photos != null && contacts[i].photos.length !=0) {
                        contactsWithPhotosArray.push(contacts[i]);
                    }
                }

                onSuccess(contactsWithPhotosArray);
            },
            onError, options);
        },
        withEmail:function(onSuccess, onError) {
            var options = {
                filter: '',
                multiple: true,
                hasEmail: true
            };
            this.contacts(function(contacts) {
                var contactsWithEmailsArray = [];
                for(var i=0; i < contacts.length; i++) {
                    if(contacts[i].emails != null && contacts[i].emails.length !=0) {
                        contactsWithEmailsArray.push(contacts[i]);
                    }
                }

                onSuccess(contactsWithEmailsArray);
            },
            onError, options);
        },
        withPhone:function(onSuccess, onError) {
            var options = {
                filter: '',
                multiple: true,
                hasPhoneNumber: true
            };
            this.contacts(function(contacts) {
                var contactsWithphoneNumbersArray = [];
                for(var i=0; i < contacts.length; i++) {
                    if(contacts[i].phoneNumbers != null && contacts[i].phoneNumbers.length !=0) {
                        contactsWithphoneNumbersArray.push(contacts[i]);
                    }
                }

                onSuccess(contactsWithphoneNumbersArray);
            },
            onError, options);
        },
        openNativeContactManager: function(onSuccess, onError) {
            navigator.contacts.pickContact(function(contact){
                console.log('The following contact has been selected:' + JSON.stringify(contact));
                onSuccess(contact);
            },function(err){
                console.log('Error: ' + err);
                onError(err);
            });
        }
}

var utils = {
        getGroupsLabelObj:function(onSuccess, onError) {
            window.resolveLocalFileSystemURL(cordova.file.dataDirectory, function(dir) {
                dir.getFile(groupsFileName, {create:true}, function(fileEntry) {
                    fileEntry.file(function(file) {
                        var reader = new FileReader();
                        reader.onloadend = function(e) {
                            var jsonObj = null;
                            if(this.result !== "") {
                                jsonObj = JSON.parse(this.result);
                            } else {
                                jsonObj = [];
                            }

                            console.log(JSON.stringify(jsonObj));
                            onSuccess(jsonObj);
                        };

                       reader.readAsText(file);
                       }, onError);
                }, onError);
            }, onError);
        },
        writeGroupsLabelObj:function(obj, onSuccess, onError) {
            window.resolveLocalFileSystemURL(cordova.file.dataDirectory, function(dir) {
                dir.getFile(groupsFileName, {create:true}, function(fileEntry) {
                    fileEntry.createWriter(function(fileWriter) {
                        fileWriter.onwriteend = function(e) {
                            console.log('Write completed.');
                            onSuccess();
                        };

                        fileWriter.onerror = function(e) {
                            console.log('Write failed: ' + e.toString());
                            onError(e);
                        };

                        fileWriter.write(JSON.stringify(obj));
                    }, onError);
                }, onError);
            }, onError);
        },
        isPresentInLabelArray:function(hash, obj) {
            for(var i=0; i < obj.length; i++) {
                if(obj[i].id == hash) {
                    return true;
                }
            }
            return false;
        },
        removeElementFromArrayByIdParam:function(hash, obj) {
            var indexOfRemoveElement = -1;
            for(var i=0; i < obj.length; i++) {
                if(obj[i].id == hash) {
                    indexOfRemoveElement = i;
                    break;
                }
            }
            if(indexOfRemoveElement == -1) {
                return Q.Users.Cordova.QGroupsError.NOT_FOUND_LABEL;
            } else {
                obj.splice(indexOfRemoveElement, 1);
            }

            return obj
        },
        isPresentInArray:function(id, members) {
            for(var i=0; i < members.length; i++) {
                if(members[i] == id) {
                    return true;
                }
            }
            return false;
        },
        removeMemberFromArrayByIdParam:function(id, members) {
            var indexOfRemoveElement = -1;
            for(var i=0; i < members.length; i++) {
                if(members[i] == id) {
                    indexOfRemoveElement = i;
                    break;
                }
            }
            if(indexOfRemoveElement != -1) {
                members.splice(indexOfRemoveElement, 1);
            }

            return members
        }
    }

module.exports = {
    hello: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "QUsersCordova", "hello", [name]);
    },
    setAddressBookChangeListener: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "QUsersCordova", "setAddressBookChangeListener", []);
    }
};
module.exports.Labels = Labels;
module.exports.Contacts = Contacts;


