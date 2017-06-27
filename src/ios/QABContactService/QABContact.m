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
    
    NSString* firstName = (__bridge NSString*)ABRecordCopyValue(recordRef, kABPersonFirstNameProperty);
    NSString* lastName = (__bridge NSString*)ABRecordCopyValue(recordRef, kABPersonLastNameProperty);
    NSString* middleName = (__bridge NSString*)ABRecordCopyValue(recordRef, kABPersonMiddleNameProperty);
    
    NSMutableString* fullName = [NSMutableString string];
    if(firstName) {
        [fullName appendString:[NSString stringWithFormat:@"%@ ", firstName]];
    }
    
    if(lastName) {
        [fullName appendString:[NSString stringWithFormat:@"%@ ", lastName]];
    }
    
    if( middleName) {
        [fullName appendString:middleName];
    }
    
    return fullName;
}

- (NSString*) phone {
    ABRecordRef recordRef = [self getContact];
    
    NSMutableArray *phones = [NSMutableArray array];
    ABMultiValueRef phonesProperty = ABRecordCopyValue(recordRef, kABPersonPhoneProperty);
    if (phonesProperty != NULL) {
        NSArray *phonesArray = (__bridge NSArray*)ABMultiValueCopyArrayOfAllValues(phonesProperty);
        [phones addObjectsFromArray:phonesArray];
        CFRelease(phonesProperty);
    }

    if(phones == nil || [phones count] == 0) {
        return nil;
    }
    return [phones objectAtIndex:0];
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
