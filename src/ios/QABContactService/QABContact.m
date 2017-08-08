//
//  QABContact.m
//  Groups
//
//  Created by Igor on 6/20/17.
//
//

#import "QABContact.h"
#import "QABAdressBook.h"

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

- (NSArray<NSString*>*) phones {
    ABRecordRef recordRef = [self getContact];
    
    NSMutableArray *phones = [NSMutableArray array];
    ABMultiValueRef phonesProperty = ABRecordCopyValue(recordRef, kABPersonPhoneProperty);
    if (phonesProperty != NULL) {
        NSArray *phonesArray = (__bridge NSArray*)ABMultiValueCopyArrayOfAllValues(phonesProperty);
        [phones addObjectsFromArray:phonesArray];
        CFRelease(phonesProperty);
    }
    
    return [phones copy];
}

- (NSString*) email {
    NSArray *emails = [self emails];
    
    if(emails == nil || [emails count] == 0) {
        return nil;
    }
    return [emails objectAtIndex:0];
}

- (NSArray<NSString*>*) emails {
    ABRecordRef recordRef = [self getContact];
    
    NSMutableArray *emails = [NSMutableArray array];
    ABMultiValueRef emailsProperty = ABRecordCopyValue(recordRef, kABPersonEmailProperty);
    if (emailsProperty != NULL) {
        NSArray *emailsArray = (__bridge NSArray*)ABMultiValueCopyArrayOfAllValues(emailsProperty);
        [emails addObjectsFromArray:emailsArray];
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
