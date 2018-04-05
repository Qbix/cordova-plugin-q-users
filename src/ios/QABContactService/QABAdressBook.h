//
//  QABAdressBook.h
//  Groups
//
//  Created by Igor on 6/20/17.
//
//

#import <Foundation/Foundation.h>
#import "ABContactService.h"
#import "QABGroup.h"

typedef enum QABGroupMemberFilter:NSInteger {
    QABGroupMemberFilterNotInGroup,
    QABGroupMemberFilterHasCompany,
    QABGroupMemberFilterHasEmail,
    QABGroupMemberFilterHasPhone,
    QABGroupMemberFilterHasPhoto
} QABGroupMemberFilter;


@interface QABAdressBook : NSObject
+(QABAdressBook*) sharedInstance;
+(void) requestPermission:(void (^)(BOOL)) callback;
-(ABRecordRef) getRecordForId:(ABRecordID) recordId;
-(ABAddressBookRef) getAddressBookRef;
@end

@interface QABAdressBook(ABGroups)
-(NSArray<QABGroup*>*) getGroups;
-(NSArray<QABGroup*>*) getGroupsWith:(QABContact*) membeer;
-(QABGroup*) getGroupAllContacts;
-(QABGroup*) getGroupFiltered:(QABGroupMemberFilter) filter;
-(QABGroup*) getGroup:(NSNumber*) groupId;
-(QABGroup*) addNewGroup:(NSString*) name;
-(QABGroup*) renameGroup:(NSNumber*) groupId to:(NSString*) name;
-(BOOL) removeGroup:(NSNumber*) groupId;
-(BOOL) addMember:(QABContact*) member toGroup:(NSNumber*) groupId;
-(BOOL) addMembers:(NSArray<QABContact*>*) members toGroup:(NSNumber*) groupId;
-(BOOL) removeMember:(QABContact*) member fromGroup:(NSNumber*) groupId;
-(BOOL) removeMembers:(NSArray<QABContact*>*) members fromGroup:(NSNumber*) groupId;
-(BOOL) addMember:(ABRecordRef) member;
-(BOOL) removeMember:(ABRecordRef) member;
@end

