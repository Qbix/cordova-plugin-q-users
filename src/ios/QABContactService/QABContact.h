//
//  QABContact.h
//  Groups
//
//  Created by Igor on 6/20/17.
//
//

#import <Foundation/Foundation.h>
#import "ABContactService.h"


@interface QABContact : NSObject

-(instancetype) initWithABRecordID:(ABRecordID) contact;
-(ABRecordRef) getContact;

-(ABRecordID) identifier;
-(NSString*) firstName;
-(NSString*) lastName;
-(NSString*) middleName;
-(NSString*) fullName;
-(NSString*) companyName;
-(NSString*) phone;
-(NSArray<NSString*>*) phones;
-(NSString*) email;
-(NSArray<NSString*>*) emails;
-(NSDate*) timeAdded;

@end
