//
//  QABAdressBook.m
//  Groups
//
//  Created by Igor on 6/20/17.
//
//

#import "QABAdressBook.h"

@interface QABAdressBook()
@property(nonatomic, assign) ABAddressBookRef addressBook;
@property(nonatomic, strong) NSArray<QABGroup*> *cachedGroups;
@end

@implementation QABAdressBook

+(instancetype) sharedInstance {
    static QABAdressBook *qAdressBook = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        @try {
            qAdressBook = [[QABAdressBook alloc] init];
        } @catch(NSException *e) {
            qAdressBook = nil;
        }
    });
    if(qAdressBook == nil) {
        NSException *e = [NSException
                          exceptionWithName:@"QABAccessDeniedException"
                          reason:@"Please allow permission in settings"
                          userInfo:nil];
        @throw e;
    }
    return qAdressBook;
}
    
+(void) requestPermission:(void (^)(BOOL)) callback {
    [self resolvePermissionAccess:ABAddressBookCreate() withCallback:callback];
}

-(ABAddressBookRef) getAddressBookRef {
    return _addressBook;
}
    
- (instancetype)init {
    self = [super init];
    if(self) {
        _addressBook = ABAddressBookCreate();
        dispatch_async(dispatch_get_main_queue(), ^ {
            ABAddressBookRegisterExternalChangeCallback(_addressBook, addressBookChanged, (__bridge void *)self);
        });
        
        [QABAdressBook resolvePermissionAccess:_addressBook withCallback:nil];
        
    }
    return self;
}

void addressBookChanged(ABAddressBookRef addressBook, CFDictionaryRef info, void *context)
{
    NSLog(@"AddressBook Changed");
    if(context != nil) {
        QABAdressBook *qAbAdressBook = (__bridge QABAdressBook*)context;
        [qAbAdressBook cleanCache];
    }
}

+(void) resolvePermissionAccess:(ABAddressBookRef) addressBook withCallback:(void (^)(BOOL)) callback {
    ABAuthorizationStatus status = ABAddressBookGetAuthorizationStatus();
    switch (status) {
        case kABAuthorizationStatusDenied:
        case kABAuthorizationStatusRestricted: {
            if(callback != nil) {
                callback(NO);
            } else {
                // show alert that need enable
                NSException *e = [NSException
                                  exceptionWithName:@"QABAccessDeniedException"
                                  reason:@"Access Denied"
                                  userInfo:nil];
                @throw e;
            }
        }
            break;
        case kABAuthorizationStatusNotDetermined: {
            if(callback!= nil) {
                ABAddressBookRequestAccessWithCompletion(addressBook, ^(bool granted, CFErrorRef error) {
                    callback(granted);
                });
            } else {
                ABAddressBookRequestAccessWithCompletion(addressBook, nil);
            }
            break;
        }
        default:
        if(callback != nil) {
            callback(YES);
        } else {
            return;
        }
    }
}

-(void)dealloc {
    CFRelease(_addressBook);
     [[NSNotificationCenter defaultCenter] removeObserver:self];
    #if !__has_feature(objc_arc)
        [super dealloc];
    #endif
}

-(void) cleanCache {
    self.cachedGroups = nil;
}

-(ABRecordRef) getGroupRecordForId:(ABRecordID) recordId {
    if(recordId == kABRecordInvalidID) {
        return NULL;
    }
    
    return ABAddressBookGetGroupWithRecordID(_addressBook, recordId);
}

-(ABRecordRef) getRecordForId:(ABRecordID) recordId {
    if(recordId == kABRecordInvalidID) {
        return NULL;
    }
    return ABAddressBookGetPersonWithRecordID(_addressBook, recordId);
}

-(QABGroup*) getGroupForRef:(ABRecordRef) groupRef {
    ABRecordID groupId = ABRecordGetRecordID(groupRef);
    if (groupId == kABRecordInvalidID) {
        return nil;
    }
    
    NSString *name = (__bridge NSString*)ABRecordCopyValue(groupRef, kABGroupNameProperty);
    
    NSArray *persons = (__bridge NSArray*)ABGroupCopyArrayOfAllMembers(groupRef);
    
    QABGroup *qABGroup = [[QABGroup alloc] initWithABGroupID:groupId andName:name andMembers:[self mapContacts:persons]];
    
    return qABGroup;
}

-(NSArray*) getContactsWithMatchBlock:(BOOL (^)(ABRecordRef *contact)) matchBlock {
   NSMutableArray *matchedContacts = [NSMutableArray array];
    
    NSArray *contacts = (__bridge NSArray *)(ABAddressBookCopyArrayOfAllPeople(_addressBook));
    [contacts enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if(matchBlock(((__bridge ABRecordRef)obj))) {
            [matchedContacts addObject:obj];
        }
    }];
    
    return [matchedContacts copy];
}

-(NSArray<QABContact*> *) mapContacts:(NSArray*) contacts {
    NSMutableArray<QABContact*> *qContacts = [NSMutableArray array];
    for (id contact in contacts) {
        ABRecordRef personRef = (__bridge ABRecordRef)contact;
        ABRecordID personId = ABRecordGetRecordID(personRef);
        
        QABContact *member = [[QABContact alloc] initWithABRecordID:personId];
        [qContacts addObject:member];
    }
    return [qContacts copy];
}

-(NSString*) getFilteredGroupName:(QABGroupMemberFilter) filter {
    switch (filter) {
        case QABGroupMemberFilterNotInGroup:
            return @"Uncategorized";
            break;
        case QABGroupMemberFilterHasCompany:
            return @"Has Company";
            break;
        case QABGroupMemberFilterHasPhone:
            return @"Uncategorized";
            break;
        case QABGroupMemberFilterHasPhoto:
            return @"Has Company";
            break;
        case QABGroupMemberFilterHasEmail:
            return @"Has Company";
            break;
            
        default:
            return @"Undefined";
            break;
    }
}

-(NSArray*) getFilteredContacts:(QABGroupMemberFilter) filter {
    switch (filter) {
        case QABGroupMemberFilterNotInGroup:
            //return @"Uncategorized";
            return [self getContactsWithMatchBlock:^BOOL(ABRecordRef *contact) {
                NSArray *groups = [self getGroups];
                for(QABGroup* group in groups) {
                    BOOL result = [[group getMembers] containsObject:[[QABContact alloc] initWithABRecordID:ABRecordGetRecordID(contact)]];
                    if(result) {
                        return NO;
                    }
                }
                
                return YES;
            }];

            break;
        case QABGroupMemberFilterHasCompany: {
            return [self getContactsWithMatchBlock:^BOOL(ABRecordRef *contact) {
                NSString *company = (__bridge NSString*)ABRecordCopyValue(contact, kABPersonOrganizationProperty);
                
                return (company != nil) && (![company isEqualToString:@""]);
            }];
        }
            break;
        case QABGroupMemberFilterHasPhone: {
            return [self getContactsWithMatchBlock:^BOOL(ABRecordRef *contact) {
                ABMultiValueRef phonesProp = ABRecordCopyValue(contact, kABPersonPhoneProperty);
                if (phonesProp != NULL) {
                    NSArray *phones = (__bridge NSArray*)ABMultiValueCopyArrayOfAllValues(phonesProp);
                    CFRelease(phonesProp);
                    return [phones count] > 0;
                }

                return NO;
            }];
        }
            break;
        case QABGroupMemberFilterHasPhoto: {
            return [self getContactsWithMatchBlock:^BOOL(ABRecordRef *contact) {
                @try {
                    return (ABPersonHasImageData(contact));
                } @catch (NSException *exception) {
                    return NO;
                }
                
                return NO;
            }];
        }
            break;
        case QABGroupMemberFilterHasEmail: {
            return [self getContactsWithMatchBlock:^BOOL(ABRecordRef *contact) {
                ABMultiValueRef emailProp = ABRecordCopyValue(contact, kABPersonEmailProperty);
                if (emailProp != NULL) {
                    NSArray *emails = (__bridge NSArray*)ABMultiValueCopyArrayOfAllValues(emailProp);
                    CFRelease(emailProp);
                    return [emails count] > 0;
                }
                
                return NO;
            }];
        }
            break;
            
        default:
            return @[];
            break;
    }
}

#pragma mark - Groups methods

-(NSArray<QABGroup*>*) getGroups {
    //if(self.cachedGroups == nil) {
        NSMutableArray<QABGroup*> *groups = [NSMutableArray array];
        
        NSArray *nativeGroups = (__bridge NSArray*)ABAddressBookCopyArrayOfAllGroups(_addressBook);
        for (id group in nativeGroups) {
            ABRecordRef groupRef = (__bridge ABRecordRef)group;
            QABGroup *qABGroup =  [self getGroupForRef:groupRef];
            if(qABGroup != nil) {
                [groups addObject:qABGroup];
            }
        }
        
        [self setCachedGroups:[groups copy]];
    //}
    
    return self.cachedGroups;
}

-(NSArray<QABGroup*>*) getGroupsWith:(QABContact*) member {
    NSMutableArray<QABGroup*> *groupsWithMember = [NSMutableArray array];
    for (QABGroup* group in [self getGroups]) {
        if([[group getMembers] containsObject:member]) {
            [groupsWithMember addObject:group];
            continue;
        }
    }
    
    return [NSArray arrayWithArray:groupsWithMember];
}

-(QABGroup*) getGroupAllContacts {
    NSArray*contacts = [self getContactsWithMatchBlock:^BOOL(ABRecordRef *contact) {
        return YES;
    }];
    
    QABGroup *group = [[QABGroup alloc] initWithType:QABGroupTypeAllContacts andName:@"All Contacts" andMembers:[self mapContacts:contacts]];
    [group setMembersSorting:QABGroupMemberSortingDefault];
    return group;
}

-(QABGroup*) getGroupFiltered:(QABGroupMemberFilter) filter {
    NSArray *contacts = [self getFilteredContacts:filter];
    
    return [[QABGroup alloc] initWithType:QABGroupTypeFiltered andName:[self getFilteredGroupName:filter] andMembers:[self mapContacts:contacts]];
}

-(QABGroup*) getGroup:(NSNumber*) groupId {
    return [self getGroupForRef:[self getRecordForId:[groupId intValue]]];
}

-(QABGroup*) addNewGroup:(NSString*) name {
    CFErrorRef error = NULL;
    ABRecordRef newGroup = ABGroupCreate();
    if(!ABRecordSetValue(newGroup, kABGroupNameProperty,(__bridge CFTypeRef)(name), &error)) {
        return nil;
    }
    if(!ABAddressBookAddRecord(_addressBook, newGroup, &error)) {
        ABAddressBookRevert(_addressBook);
        return nil;
    }
    if(!ABAddressBookSave(_addressBook, &error)) {
        return nil;
    }
    
    return [self getGroupForRef:newGroup];
}

-(QABGroup*) renameGroup:(NSNumber*) groupId to:(NSString*) name {
    CFErrorRef error = NULL;
    ABRecordRef groupRef = [self getGroupRecordForId:[groupId intValue]];
    if (groupRef == kABInvalidPropertyType) {
        return nil;
    }
    if(!ABRecordSetValue(groupRef, kABGroupNameProperty,(__bridge CFTypeRef)(name), &error)) {
        ABAddressBookRevert(_addressBook);
        return nil;
    }
    if(!ABAddressBookSave(_addressBook, &error)) {
        return nil;
    }
    
    return [self getGroupForRef:groupRef];
}

-(BOOL) removeGroup:(NSNumber*) groupId {
    ABRecordRef groupRef = [self getGroupRecordForId:[groupId intValue]];
    if (groupRef == kABInvalidPropertyType) {
        return NO;
    }
    
    CFErrorRef error = NULL;
    if(!ABAddressBookRemoveRecord(_addressBook, groupRef, &error)) {
        ABAddressBookRevert(_addressBook);
        return NO;
    }
    
    
    CFRelease(groupRef);
    if(!ABAddressBookSave(_addressBook, &error)) {
        return NO;
    }
    
    return YES;
}

-(BOOL) addMembers:(NSArray<QABContact*>*) members toGroup:(NSNumber*) groupId {
    ABRecordRef groupRef = [self getGroupRecordForId:[groupId intValue]];
    if(groupRef == kABInvalidPropertyType) {
        return NO;
    }
    
    BOOL statusSuccess = YES;
    for(QABContact* member in members) {
        if(member == nil && [member identifier] == kABInvalidPropertyType) {
            statusSuccess = NO;
            break;
        }
        
        ABRecordRef memberRef = [self getRecordForId:[member identifier]];
        CFErrorRef error = NULL;
        if(!ABGroupAddMember(groupRef, memberRef, &error)) {
            ABAddressBookRevert(_addressBook);
            statusSuccess = NO;
            break;
        }
//        CFRelease(memberRef);
    }
    if(!statusSuccess) {
        ABAddressBookRevert(_addressBook);
        return NO;
    }
    
    CFErrorRef error = NULL;
    if(!ABAddressBookSave(_addressBook, &error)) {
        return NO;
    }
    
    return YES;
}

-(BOOL) addMember:(QABContact*) member toGroup:(NSNumber*) groupId {
    return [self addMembers:@[member] toGroup:groupId];
}

-(BOOL) removeMembers:(NSArray<QABContact*>*) members fromGroup:(NSNumber*) groupId {
    ABRecordRef groupRef = [self getGroupRecordForId:[groupId intValue]];
    if(groupRef == kABInvalidPropertyType) {
        return NO;
    }
    
    BOOL statusSuccess = YES;
    for(QABContact* member in members) {
        if(member == nil && [member identifier] == kABInvalidPropertyType) {
            statusSuccess = NO;
            break;
        }
        
        ABRecordRef memberRef = [self getRecordForId:[member identifier]];
        CFErrorRef error = NULL;
        if(!ABGroupRemoveMember(groupRef, memberRef, &error)) {
            ABAddressBookRevert(_addressBook);
            statusSuccess = NO;
            break;
        }
//        CFRelease(memberRef);
    }
    if(!statusSuccess) {
        ABAddressBookRevert(_addressBook);
        return NO;
    }
    
    CFErrorRef error = NULL;
    if(!ABAddressBookSave(_addressBook, &error)) {
        return NO;
    }
    
    return YES;
}

-(BOOL) removeMember:(QABContact*) member fromGroup:(NSNumber*) groupId {
    return [self removeMembers:@[member] fromGroup:groupId];
}

-(BOOL) removeMember:(ABRecordRef) member {
    if(member == nil && [member identifier] == kABInvalidPropertyType) {
        return false;
    }
    CFErrorRef error = NULL;
    if(!ABAddressBookRemoveRecord(_addressBook, member, &error)) {
        return false;
    }
    error = NULL;
    if(!ABAddressBookSave(_addressBook, &error)) {
        return false;
    }
    
    return true;
}

-(BOOL) addMember:(ABRecordRef) member {
    if(member == nil && [member identifier] == kABInvalidPropertyType) {
        return false;
    }
    CFErrorRef error = NULL;
    if(!ABAddressBookAddRecord(_addressBook, member, &error)) {
        return false;
    }
    error = NULL;
    if(!ABAddressBookSave(_addressBook, &error)) {
        return false;
    }
    
    return true;
}

@end
