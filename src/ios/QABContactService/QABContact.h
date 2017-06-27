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

-(ABRecordID) identifier;
-(NSString*) fullName;
-(NSString*) phone;
-(NSDate*) timeAdded;

@end
