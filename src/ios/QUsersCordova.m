#import "QUsersCordova.h"

static const NSString* QIdField = @"id";
static const NSString* QTitleField = @"title";
static const NSString* QContactIdsField = @"contactIds";
static const NSString* QTimeAddedField = @"timeAdded";

@implementation QUsersCordova

-(void) resolvePermissionAccess:(NSString*) callbackId andCallback:(void (^)(QABAdressBook*)) callback {
    [QABAdressBook requestPermission:^(BOOL granted) {
        if(granted) {
            callback([QABAdressBook sharedInstance]);
        } else {
            [self sentErrorCallback:callbackId andError:@"PERMISSION_DENIED_ERROR"];
        }
    }];
}

-(void) sentErrorCallback:(NSString*) callbackId andError:(NSString*) error {
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_ERROR
                               messageAsString:error];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

#pragma mark - Mapping Functions
-(NSDictionary*) mappingGroup:(QABGroup*) group {
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    [dictionary setObject:[NSNumber numberWithInt:[group groupIdentifier]] forKey:QIdField];
    [dictionary setObject:[group name] forKey:QTitleField];
    
    NSMutableArray *contactIds = [NSMutableArray array];
    NSArray<QABContact*> *members = [group getMembers];
    for(QABContact *member in members) {
        [contactIds addObject:[NSNumber numberWithInt:[member identifier]]];
    }
    [dictionary setObject:contactIds forKey:QContactIdsField];
    
    return [dictionary copy];
}

-(NSDictionary*) mappingFilteredGroup:(QABGroup*) group {
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    [dictionary setObject:[group name] forKey:QTitleField];
    
    NSMutableArray *contactIds = [NSMutableArray array];
    NSArray<QABContact*> *members = [group getMembers];
    for(QABContact *member in members) {
        [contactIds addObject:[NSNumber numberWithInt:[member identifier]]];
    }
    [dictionary setObject:contactIds forKey:QContactIdsField];
    
    return [dictionary copy];
}

//- (void)hello:(CDVInvokedUrlCommand*)command
//{
//    NSString* callbackId = [command callbackId];
//    NSString* name = [[command arguments] objectAtIndex:0];
//    NSString* msg = [NSString stringWithFormat: @"Hello, %@", name];
//
//    CDVPluginResult* result = [CDVPluginResult
//                               resultWithStatus:CDVCommandStatus_OK
//                               messageAsString:msg];
//
//    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
//}

- (void) getAll:(CDVInvokedUrlCommand*)command {
    [self resolvePermissionAccess:[command callbackId] andCallback:^(QABAdressBook *addressBook) {
        NSMutableArray<NSDictionary*> *mappedGroups = [NSMutableArray array];
        NSArray<QABGroup*> *groups = [addressBook getGroups];
        for(QABGroup *group in groups) {
            [mappedGroups addObject:[self mappingGroup:group]];
        }
        
        CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsArray:mappedGroups];
        
        [self.commandDelegate sendPluginResult:result callbackId:[command callbackId]];
    }];
}

- (void) smart:(CDVInvokedUrlCommand*)command {
    NSString *smartFilter = [[command arguments] objectAtIndex:0];
    [self resolvePermissionAccess:[command callbackId] andCallback:^(QABAdressBook *addressBook) {
        CDVPluginResult* result = nil;
        if ([smartFilter isEqualToString:@"byTimeAdded"]) {
            QABGroup *group = [addressBook getGroupAllContacts];
            
            NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
            [dictionary setObject:@"By Time Added" forKey:QTitleField];
            
            NSArray<QABContact*> *members = [group getMembers];
            
            NSArray<QABContact*> *sortedMembers = [members sortedArrayUsingComparator:^NSComparisonResult(id  _Nonnull obj1, id  _Nonnull obj2) {
                return [[(QABContact*)obj2 timeAdded] compare:[(QABContact*)obj1 timeAdded]];
            }];
            
            NSMutableArray *timeAdded = [NSMutableArray array];
            NSMutableArray *contactIds = [NSMutableArray array];
            for(QABContact *member in sortedMembers) {
                [contactIds addObject:[NSNumber numberWithInt:[member identifier]]];
                [timeAdded addObject:[NSNumber numberWithLong:[[member timeAdded] timeIntervalSince1970]]];
            }
            [dictionary setObject:contactIds forKey:QContactIdsField];
            [dictionary setObject:timeAdded forKey:QTimeAddedField];
            
            
            result = [CDVPluginResult
                      resultWithStatus:CDVCommandStatus_OK
                      messageAsDictionary:dictionary];
            
        } else {
            QABGroup *filterdGroup = nil;
            
            if([smartFilter isEqualToString:@"uncategorized"]) {
                filterdGroup = [addressBook getGroupFiltered:QABGroupMemberFilterNotInGroup];
            } else if ([smartFilter isEqualToString:@"byCompany"]) {
                filterdGroup = [addressBook getGroupFiltered:QABGroupMemberFilterHasCompany];
            } else if ([smartFilter isEqualToString:@"hasEmail"]) {
                filterdGroup = [addressBook getGroupFiltered:QABGroupMemberFilterHasEmail];
            } else if ([smartFilter isEqualToString:@"hasPhone"]) {
                filterdGroup = [addressBook getGroupFiltered:QABGroupMemberFilterHasPhone];
            } else if ([smartFilter isEqualToString:@"hasPhoto"]) {
                filterdGroup = [addressBook getGroupFiltered:QABGroupMemberFilterHasPhoto];
            }
            
            if(filterdGroup == nil) {
                return [self sentErrorCallback:[command callbackId] andError:@"Group not exist"];
            }
            
            
            result = [CDVPluginResult
                                       resultWithStatus:CDVCommandStatus_OK
                                       messageAsDictionary:[self mappingFilteredGroup:filterdGroup]];
            
            
        }
        
        [self.commandDelegate sendPluginResult:result callbackId:[command callbackId]];
    }];
}

- (void) get:(CDVInvokedUrlCommand*)command {
    NSArray *groupsIds = [[command arguments] objectAtIndex:0];
    [self resolvePermissionAccess:[command callbackId] andCallback:^(QABAdressBook *addressBook) {
        NSMutableArray<NSDictionary*> *mappedGroups = [NSMutableArray array];
        NSArray<QABGroup*> *groups = [addressBook getGroups];
        for(QABGroup *group in groups) {
            for(NSNumber *filterdGroupId in groupsIds) {
                if([group groupIdentifier] == [filterdGroupId intValue]) {
                    [mappedGroups addObject:[self mappingGroup:group]];
                }
            }
        }
        
        CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsArray:mappedGroups];
        
        [self.commandDelegate sendPluginResult:result callbackId:[command callbackId]];
    }];
}
    
- (void) forContacts:(CDVInvokedUrlCommand*)command {
    NSArray *contactIds = [[command arguments] objectAtIndex:0];
    BOOL doUnion = [[[command arguments] objectAtIndex:1] boolValue];
    [self resolvePermissionAccess:[command callbackId] andCallback:^(QABAdressBook *addressBook) {
        NSMutableArray<NSDictionary*> *mappedGroups = [NSMutableArray array];
        NSArray<QABGroup*> *groups = [addressBook getGroups];
        
        if(doUnion) {
            for(QABGroup *group in groups) {
                for(NSNumber *contactId in contactIds) {
                    if([[group getMembers] containsObject:[[QABContact alloc] initWithABRecordID:[contactId intValue]]]) {
                        [mappedGroups addObject:[self mappingGroup:group]];
                        break;
                    }
                }
            }
        } else {
             for(QABGroup *group in groups) {
                BOOL isMembers = NO;
                for(NSNumber *contactId in contactIds) {
                    if(![[group getMembers] containsObject:[[QABContact alloc] initWithABRecordID:[contactId intValue]]]) {
                        isMembers = NO;
                        break;
                    } else {
                        isMembers = YES;
                    }
                }
                 
                if(isMembers) {
                    [mappedGroups addObject:[self mappingGroup:group]];
                }
             }
        }
        
        CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsArray:mappedGroups];
        
        [self.commandDelegate sendPluginResult:result callbackId:[command callbackId]];
    }];
}
    
- (void) save:(CDVInvokedUrlCommand*)command {
    NSNumber *groupId = [[command arguments] objectAtIndex:0];
    NSString *groupName = [[command arguments] objectAtIndex:1];
    [self resolvePermissionAccess:[command callbackId] andCallback:^(QABAdressBook *addressBook) {
        
        QABGroup *group = nil;
        if(groupId == nil || [groupId intValue] == kABRecordInvalidID) {
            // add new group
            group = [addressBook addNewGroup:groupName];
        } else {
            // edit existed group
            group = [addressBook renameGroup:groupId to:groupName];
        }
        
        CDVPluginResult* result = nil;
        if(group != nil) {
            result = [CDVPluginResult
                                resultWithStatus:CDVCommandStatus_OK
                                messageAsDictionary:[self mappingGroup:group]];
        } else {
            result = [CDVPluginResult
                      resultWithStatus:CDVCommandStatus_ERROR
                      messageAsString:@"Error occurred"];
        }
        
        [self.commandDelegate sendPluginResult:result callbackId:[command callbackId]];
    }];
}

- (void) remove:(CDVInvokedUrlCommand*)command {
    NSNumber *groupId = [[command arguments] objectAtIndex:0];
    [self resolvePermissionAccess:[command callbackId] andCallback:^(QABAdressBook *addressBook) {
        
        BOOL removeResult  = [addressBook removeGroup:groupId];
        
        CDVPluginResult* result = nil;
        if(removeResult) {
            result = [CDVPluginResult
                      resultWithStatus:CDVCommandStatus_OK
                      messageAsBool:YES];
        } else {
            result = [CDVPluginResult
                      resultWithStatus:CDVCommandStatus_ERROR
                      messageAsString:@"Error occurred"];
        }
        
        [self.commandDelegate sendPluginResult:result callbackId:[command callbackId]];
    }];
}


- (void) addContact:(CDVInvokedUrlCommand*)command {
    NSNumber *labelId = [[command arguments] objectAtIndex:0];
    NSArray *contactIds = [[command arguments] objectAtIndex:1];
    [self resolvePermissionAccess:[command callbackId] andCallback:^(QABAdressBook *addressBook) {
        
        NSMutableArray *contacts = [NSMutableArray array];
        for(NSNumber *contactId in contactIds) {
            [contacts addObject:[[QABContact alloc] initWithABRecordID:[contactId intValue]]];
        }
        
        
        BOOL addResult = [addressBook addMembers:contacts toGroup:labelId];
        
        CDVPluginResult* result = nil;
        if(addResult) {
            result = [CDVPluginResult
                      resultWithStatus:CDVCommandStatus_OK
                      messageAsBool:YES];
        } else {
            result = [CDVPluginResult
                      resultWithStatus:CDVCommandStatus_ERROR
                      messageAsString:@"Error occurred"];
        }
        
        [self.commandDelegate sendPluginResult:result callbackId:[command callbackId]];
    }];
}

- (void) removeContact:(CDVInvokedUrlCommand*)command {
    NSNumber *labelId = [[command arguments] objectAtIndex:0];
    NSArray *contactIds = [[command arguments] objectAtIndex:1];
    [self resolvePermissionAccess:[command callbackId] andCallback:^(QABAdressBook *addressBook) {
        
        NSMutableArray *contacts = [NSMutableArray array];
        for(NSNumber *contactId in contactIds) {
            [contacts addObject:[[QABContact alloc] initWithABRecordID:[contactId intValue]]];
        }
        
        
        BOOL removeResult = [addressBook removeMembers:contacts fromGroup:labelId];
        
        CDVPluginResult* result = nil;
        if(removeResult) {
            result = [CDVPluginResult
                      resultWithStatus:CDVCommandStatus_OK
                      messageAsBool:YES];
        } else {
            result = [CDVPluginResult
                      resultWithStatus:CDVCommandStatus_ERROR
                      messageAsString:@"Error occurred"];
        }
        
        [self.commandDelegate sendPluginResult:result callbackId:[command callbackId]];
    }];
}

- (void) setForContact:(CDVInvokedUrlCommand*)command {
    NSNumber *contactId = [[command arguments] objectAtIndex:0];
    NSArray *labelIds = [[command arguments] objectAtIndex:1];
    [self resolvePermissionAccess:[command callbackId] andCallback:^(QABAdressBook *addressBook) {
        
        QABContact *member = [[QABContact alloc] initWithABRecordID:[contactId intValue]];
        
        NSArray<QABGroup*> *allGroups = [addressBook getGroups];
        BOOL operationResult = YES;
        for(QABGroup *group in allGroups) {
            NSNumber *groupIdentifier = [NSNumber numberWithInt:[group groupIdentifier]];
            
            BOOL isMember = [[group getMembers] containsObject:member];
            BOOL isGroupInFilteredList = [labelIds containsObject:groupIdentifier];
            
            if(isGroupInFilteredList && !isMember) {
                // need to add
                operationResult = [addressBook addMember:member toGroup:groupIdentifier];
            } else if (!isGroupInFilteredList && isMember) {
                // need to remove from group
                operationResult = [addressBook removeMember:member fromGroup:groupIdentifier];
            }
            
            if(!operationResult) {
                break;
            }
        }
        
        
        CDVPluginResult* result = nil;
        if(operationResult) {
            result = [CDVPluginResult
                      resultWithStatus:CDVCommandStatus_OK
                      messageAsBool:YES];
        } else {
            result = [CDVPluginResult
                      resultWithStatus:CDVCommandStatus_ERROR
                      messageAsString:@"Error occurred"];
        }
        
        [self.commandDelegate sendPluginResult:result callbackId:[command callbackId]];
    }];
}

//-(void) setAddressBookChangeListener:(CDVInvokedUrlCommand*)command
//{
//    [self setAddressBookChangeListener:[command callbackId]];
//}

//- (void) pluginInitialize {
//    [super pluginInitialize];
//    
//    // Initalize global listener to addressbook changed
////    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(addressBookChanged) name:ADDRESS_BOOK_CHANGED_NOTIFICATION_EVENT object:nil];
////    
////    // Initialize listener to groups change
////    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(groupsChanged:) name:GROUPS_CHANGED_NOTIFICATION_EVENT object:nil];
//}

//-(void) executeCallbackToJS:(NSDictionary*) changed {
//    if([self addressBookChangeListener] != nil) {
//        CDVPluginResult* result = [CDVPluginResult
//                                   resultWithStatus:CDVCommandStatus_OK
//                                   messageAsDictionary:changed];
//        [result setKeepCallbackAsBool:YES];
//        [self.commandDelegate sendPluginResult:result callbackId:[self addressBookChangeListener]];
//    }
//}
//
//-(void) addressBookChanged
//{    
//    NSDictionary *result = @{@"addressbook":[NSNumber numberWithInt:1]};
//    [self  executeCallbackToJS:result];
//}
//
//-(void) groupsChanged:(NSNotification *)notification
//{
//    [self executeCallbackToJS:notification.userInfo];
//}

@end
