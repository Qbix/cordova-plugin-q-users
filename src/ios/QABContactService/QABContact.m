//
//  QABContact.m
//  Groups
//
//  Created by Igor on 6/20/17.
//
//

#import "QABContact.h"
#import "QABAdressBook.h"


@interface QABLabelValue()
@property(nonatomic, strong) NSString* innerValue;
@property(nonatomic, assign) NSInteger innerLabel;
@end

@implementation QABLabelValue
-(instancetype) init:(NSString*) value andLabel:(NSInteger)label {
    self = [super init];
    if(self) {
        [self setInnerValue:value];
        [self setInnerLabel:label];
    }
    return self;
}

-(NSString*) value {
    return [self innerValue];
}

-(NSInteger) label {
    return [self innerLabel];
}
@end


@interface QABContact()
@property(nonatomic, assign) ABRecordID contactIdentifier;
@property(nonatomic, assign) ABRecordRef cachedContact;
@end

@implementation QABContact

-(instancetype) initWithIdentifier:(ABRecordID) contactIdentifier {
    self = [super init];
    if(self) {
        [self setContactIdentifier:contactIdentifier];
    }
    return self;
}

-(instancetype) initWithABRecordRef:(ABRecordRef) recordRef {
    return [[super init] initWithIdentifier:ABRecordGetRecordID(recordRef)];
}

-(instancetype) initWithABRecordID:(ABRecordID) contact {
    return [[super init] initWithIdentifier:contact];
}

-(ABRecordRef) getContact {
    if(_cachedContact == NULL) {
        // TODO: need remove from this part dependency on QABAdressBook
        _cachedContact = [[QABAdressBook sharedInstance] getRecordForId:self.contactIdentifier];
    }
    
    return _cachedContact;
}

#pragma mark Contact Info method

-(ABRecordID) identifier {
    return _contactIdentifier;
}

-(NSString*) fullName {
    ABRecordRef recordRef = [self getContact];
    
    NSString* firstName = [self firstName];
    NSString* lastName = [self lastName];
    NSString* middleName = (__bridge NSString*)ABRecordCopyValue(recordRef, kABPersonMiddleNameProperty);
    
    NSString* fullName = @"";
    if(firstName) {
        fullName = [NSString stringWithFormat:@"%@%@ ", fullName, firstName];
    }
    
    if(lastName) {
        fullName = [NSString stringWithFormat:@"%@%@ ", fullName, lastName];
    }
    
    if( middleName) {
        fullName = [NSString stringWithFormat:@"%@%@", fullName, middleName];
    }
    
    return fullName;
}

-(NSString*) firstName {
    ABRecordRef recordRef = [self getContact];
    
    NSString* firstName = (__bridge NSString*)ABRecordCopyValue(recordRef, kABPersonFirstNameProperty);
    
    return firstName;
}

-(NSString*) lastName {
    ABRecordRef recordRef = [self getContact];
    
    NSString* lastName = (__bridge NSString*)ABRecordCopyValue(recordRef, kABPersonLastNameProperty);
    return lastName;
}

-(NSString*) middleName {
    ABRecordRef recordRef = [self getContact];
    
    NSString* middleName = (__bridge NSString*)ABRecordCopyValue(recordRef, kABPersonMiddleNameProperty);
    return middleName;
}

-(NSString*) companyName {
    ABRecordRef recordRef = [self getContact];
    
    NSString* companyName = (__bridge NSString*)ABRecordCopyValue(recordRef, kABPersonOrganizationProperty);
    return companyName;
}

- (NSString*) phone {
    NSArray *phones = [self phones];
    
    if(phones == nil || [phones count] == 0) {
        return nil;
    }
    return [phones objectAtIndex:0];
}

-(NSArray<QABLabelValue*>*) phonesWithLabel {
    ABRecordRef recordRef = [self getContact];
    
    NSMutableArray<QABLabelValue*> *phones = [NSMutableArray array];
    ABMultiValueRef phonesProperty =(__bridge ABMultiValueRef)((__bridge NSString*)ABRecordCopyValue(recordRef, kABPersonPhoneProperty));
    if (phonesProperty != NULL) {
        for(CFIndex i = 0; i < ABMultiValueGetCount(phonesProperty); i++) {
            NSString *mobileLabel = (__bridge NSString*)ABMultiValueCopyLabelAtIndex(phonesProperty, i);
            NSInteger label = OTHER_LABEL;
            if([mobileLabel isEqualToString:(NSString *)kABPersonPhoneMobileLabel]) {
                label = MOBILE_LABEL;
            } else if([mobileLabel isEqualToString:(NSString *)kABPersonPhoneIPhoneLabel])  {
                label = IPHONE_LABEL;
            } else if([mobileLabel isEqualToString:(NSString *)kABPersonPhoneMainLabel])  {
                label = MAIN_LABEL;
            } else if([mobileLabel isEqualToString:(NSString *)kABPersonPhoneHomeFAXLabel])  {
                label = HOME_FAX_LABEL;
            } else if([mobileLabel isEqualToString:(NSString *)kABPersonPhoneWorkFAXLabel])  {
                label = WORK_FAX_LABEL;
            } else if([mobileLabel isEqualToString:(NSString *)kABPersonPhoneOtherFAXLabel])  {
                label = OTHER_LABEL;
            } else if([mobileLabel isEqualToString:(NSString *)kABPersonPhonePagerLabel])  {
                label = PAGER_LABEL;
            } else if([mobileLabel containsString:@"Home"]) {
                label = HOME_LABEL;
            } else if([mobileLabel containsString:@"Work"]) {
                label = WORK_LABEL;
            }
            NSString *phone = (__bridge NSString*)ABMultiValueCopyValueAtIndex(phonesProperty, i);
            [phones addObject:[[QABLabelValue alloc] init:phone andLabel:label]];
        }
    }
    
    return phones;
}

- (NSArray<NSString*>*) phones {
    ABRecordRef recordRef = [self getContact];
    
    NSMutableArray *phones = [NSMutableArray array];
    ABMultiValueRef phonesProperty = ABRecordCopyValue(recordRef, kABPersonPhoneProperty);
    if (phonesProperty != NULL) {
        NSArray *phonesArray = (__bridge NSArray*)ABMultiValueCopyArrayOfAllValues(phonesProperty);
        if(phonesArray != nil) {
            [phones addObjectsFromArray:phonesArray];
        }
        CFRelease(phonesProperty);
    }
    
    return [phones copy];
}

- (UIImage *)photo {
    @try {
        if (ABPersonHasImageData([self getContact])) {
            NSData *data = (__bridge NSData*) ABPersonCopyImageDataWithFormat([self getContact], kABPersonImageFormatThumbnail);;
            return [UIImage imageWithData:data];
        }
    }
    @catch (NSException *exception) {
        return nil;
    }
    return nil;
}

- (NSString*) email {
    NSArray *emails = [self emails];
    
    if(emails == nil || [emails count] == 0) {
        return nil;
    }
    return [emails objectAtIndex:0];
}

-(NSArray<QABLabelValue*>*) emailsWithLabel {
    ABRecordRef recordRef = [self getContact];
    
    NSMutableArray<QABLabelValue*> *emails = [NSMutableArray array];
    ABMultiValueRef emailsProperty =(__bridge ABMultiValueRef)((__bridge NSString*)ABRecordCopyValue(recordRef, kABPersonEmailProperty));
    if (emailsProperty != NULL) {
        for(CFIndex i = 0; i < ABMultiValueGetCount(emailsProperty); i++) {
            NSString *mobileLabel = (__bridge NSString*)ABMultiValueCopyLabelAtIndex(emailsProperty, i);
            NSInteger label = EMAIL_OTHER_LABEL;
            if([mobileLabel containsString:@"Home"]) {
                label = EMAIL_HOME_LABEL;
            } else if([mobileLabel containsString:@"Work"]) {
                label = EMAIL_WORK_LABEL;
            } else if([mobileLabel containsString:@"iCloud"]) {
                label = EMAIL_ICLOUD_LABEL;
            }
            NSString *email = (__bridge NSString*)ABMultiValueCopyValueAtIndex(emailsProperty, i);
            [emails addObject:[[QABLabelValue alloc] init:email andLabel:label]];
        }
    }
    
    return emails;
}

- (NSArray<NSString*>*) emails {
    ABRecordRef recordRef = [self getContact];
    
    NSMutableArray *emails = [NSMutableArray array];
    ABMultiValueRef emailsProperty = ABRecordCopyValue(recordRef, kABPersonEmailProperty);
    if (emailsProperty != NULL) {
        NSArray *emailsArray = (__bridge NSArray*)ABMultiValueCopyArrayOfAllValues(emailsProperty);
        if(emailsArray != nil) {
            [emails addObjectsFromArray:emailsArray];
        }
        CFRelease(emailsProperty);
    }
    
    return [emails copy];
}

-(NSDate*) timeAdded {
    ABRecordRef recordRef = [self getContact];
    
    return (__bridge NSDate*)ABRecordCopyValue(recordRef, kABPersonCreationDateProperty);
}

- (BOOL)isEqual:(id)object {
    if([object class] != [QABContact class]) {
        return NO;
    }
    
    //check if group identifier is not empty
    if([(QABContact*)object identifier] != kABRecordInvalidID && [self identifier] != kABRecordInvalidID) {
        return [(QABContact*)object identifier] == [self identifier];
    }
    
    return NO;
}

@end
