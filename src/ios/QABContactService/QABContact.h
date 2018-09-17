//
//  QABContact.h
//  Groups
//
//  Created by Igor on 6/20/17.
//
//

#import <Foundation/Foundation.h>
#import "ABContactService.h"

typedef NS_ENUM(NSUInteger, PHONE_LABELS) {
    HOME_LABEL,
    WORK_LABEL,
    IPHONE_LABEL,
    MOBILE_LABEL,
    MAIN_LABEL,
    HOME_FAX_LABEL,
    WORK_FAX_LABEL,
    PAGER_LABEL,
    OTHER_LABEL
};
typedef NS_ENUM(NSUInteger, EMAIL_LABELS) {
    EMAIL_HOME_LABEL,
    EMAIL_WORK_LABEL,
    EMAIL_ICLOUD_LABEL,
    EMAIL_OTHER_LABEL
};

@interface QABLabelValue: NSObject
-(instancetype) init:(NSString*) value andLabel:(NSInteger)label;
@property(nonatomic, strong, readonly) NSString* value;
@property(nonatomic, assign, readonly) NSInteger label;
@end

@interface QABContact : NSObject

-(instancetype) initWithABRecordRef:(ABRecordRef) recordRef;
-(instancetype) initWithABRecordID:(ABRecordID) contact;
-(ABRecordRef) getContact;

-(ABRecordID) identifier;
-(NSString*) firstName;
-(NSString*) lastName;
-(NSString*) middleName;
-(NSString*) fullName;
-(NSString*) companyName;
-(NSString*) phone;
-(UIImage*) photo;
-(NSArray<NSString*>*) phones;
-(NSArray<QABLabelValue*>*) phonesWithLabel;
-(NSString*) email;
-(NSArray<QABLabelValue*>*) emailsWithLabel;
-(NSArray<NSString*>*) emails;
-(NSDate*) timeAdded;

@end
