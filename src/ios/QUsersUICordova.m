//
//  QUsersUICordova.m
//  BusinessCardsScan
//
//  Created by Igor on 1/16/19.
//

#import "QUsersUICordova.h"
#import "CDVContacts.h"

@implementation QUsersUICordova

- (void) show:(CDVInvokedUrlCommand*)command {
    QUsersCordova *qUsersCordova = [[QUsersCordova alloc] init];
    [qUsersCordova resolvePermissionAccess:[command callbackId] andCallback:^(QABAdressBook *addressBook) {
    
        ABRecordID recordID = [[command argumentAtIndex:0] intValue];
        bool bEdit = false;

        ABRecordRef rec = ABAddressBookGetPersonWithRecordID([addressBook getAddressBookRef], recordID);
        
        CDVPluginResult* result = nil;
        if (rec) {
            CDVDisplayContactViewController* personController = [[CDVDisplayContactViewController alloc] init];
            personController.displayedPerson = rec;
            personController.personViewDelegate = self;
            personController.allowsEditing = NO;
            
            // create this so DisplayContactViewController will have a "back" button.
            UIViewController* parentController = [[UIViewController alloc] init];
            UINavigationController* navController = [[UINavigationController alloc] initWithRootViewController:parentController];
            
            [navController pushViewController:personController animated:YES];
            
            [self.viewController presentViewController:navController animated:YES completion:nil];
            
            if (bEdit) {
                // create the editing controller and push it onto the stack
                ABPersonViewController* editPersonController = [[ABPersonViewController alloc] init];
                editPersonController.displayedPerson = rec;
                editPersonController.personViewDelegate = self;
                editPersonController.allowsEditing = YES;
                [navController pushViewController:editPersonController animated:YES];
            }
            
            result = [CDVPluginResult
                                       resultWithStatus:CDVCommandStatus_OK
                                       messageAsBool:YES];
        } else {
            // no record, return error
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsInt:UNKNOWN_ERROR];;
        }

        [self.commandDelegate sendPluginResult:result callbackId:[command callbackId]];
    }];
}

- (BOOL)personViewController:(ABPersonViewController *)personViewController shouldPerformDefaultActionForPerson:(ABRecordRef)person
                    property:(ABPropertyID)property identifier:(ABMultiValueIdentifier)identifierForValue {
    return YES;
}


@end
