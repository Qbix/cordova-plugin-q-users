//
//  QABGroup.h
//  Groups
//
//  Created by Igor on 6/20/17.
//
//

#import <Foundation/Foundation.h>
#import "ABContactService.h"
#import "QABContact.h"



typedef enum QABGroupType:NSInteger {
    QABGroupTypeAllContacts,
    QABGroupTypeNative,
    QABGroupTypeFiltered
}QABGroupType;

@interface QABGroup : NSObject

-(instancetype _Nonnull ) initWithType:(QABGroupType) qGroupType andName:(NSString* _Nonnull) name andMembers:( NSArray<QABContact*>* _Nonnull ) members;
-(instancetype _Nonnull ) initWithABGroupID:(ABRecordID) group andName:(NSString* _Nonnull) name andMembers:(NSArray<QABContact*>* _Nonnull) members;


@property(nonatomic, readonly, strong) NSString * _Nonnull name;
@property(nonatomic, readonly, assign) NSInteger count;
@property(nonatomic, assign, readonly) ABRecordID groupIdentifier;

-(NSArray<QABContact*>*_Nonnull) getMembers;

@end
