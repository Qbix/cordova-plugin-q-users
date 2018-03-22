//
//  QABGroup.m
//  Groups
//
//  Created by Igor on 6/20/17.
//
//

#import "QABGroup.h"

@interface QABGroup()
@property(nonatomic, assign) QABGroupType groupType;
@property(nonatomic, assign) ABRecordRef cachedGroup;
@property(nonatomic, strong) NSMutableArray<QABContact*> *members;
@end

@implementation QABGroup

- (instancetype)init {
    self = [super init];
    if(self) {
        _groupIdentifier = -1;
        _groupType = QABGroupTypeAllContacts;
        _membersSorting = QABGroupMemberSortingDefault;
    }
    return self;
}

-(instancetype) initWithType:(QABGroupType) qGroupType andName:(nonnull NSString*) name andMembers:(nonnull NSArray<QABContact*>*) members {
    self = [self init];
    if(self) {
        _groupType = qGroupType;
        _name = name;
        _members = [NSMutableArray arrayWithArray:members];
    }
    return self;
}

-(instancetype) initWithABGroupID:(ABRecordID) groupId andName:(NSString* _Nonnull) name  andMembers:(nonnull NSArray<QABContact*>*) members {
    self = [self init];
    if(self) {
        _groupIdentifier = groupId;
        _groupType = QABGroupTypeNative;
        _name = name;
        _members = [NSMutableArray arrayWithArray:members];
    }
    return self;
}

#pragma Public methods

- (NSInteger)count {
    return [self.members count];
}

-(NSArray<QABContact*>*) getMembers {
    return self.members;
}

//-(NSArray<QABContact*>*) getSortedMembers {
//    NSSortDescriptor* sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"lastName" ascending:YES selector:@selector(localizedCompare:)];;
//    
//    switch (_membersSorting) {
//        case QABGroupMemberSortingDefault:
//        case QABGroupMemberSortingByLast:
//            sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"lastName" ascending:YES selector:@selector(localizedCompare:)];
//            break;
//        case QABGroupMemberSortingByFirst:
//            sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"firstName" ascending:YES selector:@selector(localizedCompare:)];
//            break;
//        case QABGroupMemberSortingByCompany:
//            sortDescriptor = [NSSortDescriptor sortDescriptorWithKey:@"companyName" ascending:YES selector:@selector(localizedCompare:)];
//            break;
//        default:
//            break;
//    }
//    
//    
//    return [self.members sortedArrayUsingDescriptors:[NSArray arrayWithObject:sortDescriptor]];
//}

- (BOOL)isEqual:(id)object {
    if([object class] != [QABGroup class]) {
        return NO;
    }
    
    //check if group identifier is not empty
    if([(QABGroup*)object groupIdentifier] != kABRecordInvalidID && [self groupIdentifier] != kABRecordInvalidID) {
        return [(QABGroup*)object groupIdentifier] == [self groupIdentifier];
    }
    
    return [[(QABGroup*)object name] isEqualToString:[self name]];
}

@end
