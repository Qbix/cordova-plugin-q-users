# Q Users Cordova Pluign

This plugin gives you an access to device's groups(labels) and lets you to get, add, update or delete them.
It also lets you to get contacts that are related to labels one or another way. 

__WARNING__:Collection and use of label and contact data raises
important privacy issues.  Your app's privacy policy should discuss
how the app uses contact data and whether it is shared with any other
parties.  Contact information is considered sensitive because it
reveals the people with whom a person communicates.  Therefore, in
addition to the app's privacy policy, you should strongly consider
providing a just-in-time notice before the app accesses or uses
contact data, if the device operating system doesn't do so
already. That notice should provide the same information noted above,
as well as obtaining the user's permission (e.g., by presenting
choices for __OK__ and __No Thanks__).  Note that some app
marketplaces may require the app to provide a just-in-time notice and
obtain the user's permission before accessing contact data.  A
clear and easy-to-understand user experience surrounding the use of
contact data helps avoid user confusion and perceived misuse of
contact data.

# Objects

* __`QbixGroup`__
* __`QbixContact`__
	- QbixName:
	- QbixOrganization:
	- QbixAddress:
	- QbixPhone:
	- QbixEmail:
	- QbixIm:
	- QbixWebsite:
	- QbixPhoto:

## QbixGroup

The `QbixGroup` object represents a users label.

### Properties

- __sourceId__: The unique(some system related and default labels may have same sourceId for different accounts) id of the label. [String]
- __title__: The title of the label. [String]
- __notes__: The notes about the label. [String]
- __summaryCount__: Count of all contacts related to the label.
  (If `sourceId` of the label is default or system related(repeatable),
  `summaryCount` can provide wrong count(only first accounts contacts' count)) [int]
- __isVisible__: Shows if this label is visible to any user interface. 
  (Some system related labels are not visible to users) [boolean]
- __isDeleted__: Shows if this label has been deleted. (Label can be deleted, but until syncronization it is stored in database) [boolean]
- __shouldSync__: Shows if this label should be syncronized with account or not. [boolean]
- __readOnly__: Shows if this label is for reading only and cant be modified/deleted. [boolean]
- __contactIds__: The list of contact ids, which related to the label. [Integer]

## QbixContact

The `QbixContact` object represents a users contact.

### Properties

- __displayName__: The display name of the contact. [String]
- __name__: Complex object for name related information. [`QbixName`]
	- displayName: [String]
	- givenName: [String]
	- familyName: [String]
	- prefix: Common prefixes in English names are "Mr", "Ms", "Dr" etc. [String]
	- middleName: [String]
	- suffix: Common suffixes in English names are "Sr", "Jr", "III" etc. [String]
	- phoneticGivenName: Used for phonetic spelling of the name, e.g. Pinyin, Katakana, Hiragana [String]
	- phoneticMiddleName: [String]
	- phoneticFamilyName: [String]
- __organizations__: List of complex objects for organization related information. [`QbixOrganization`]
 	- company: [String]
	- type: [int]
	- customType: [String]
	- title: [String]
	- department: [String]
	- jobDescription: [String]
	- symbol: [String]
	- phoneticName: [String]
	- officeLocation: [String]
	- phoneticNameStyle: [String]
- __addresses__: List of complex objects for address related information. [`QbixAddress`]
 	- formattedAddress: [String]
	- type: [int]
	- customType: [String]
	- street: [String]
	- pobox: Post Office Box number. [String]
	- neighborhood: [String]
	- city: [String]
	- region: [String]
	- postcode: [String]
	- country: [String]
- __phones__: List of complex objects for phone(number) related information. [`QbixPhone`]
 	- number: [String]
	- type: [int]
	- customType: [String]
- __emails__: List of complex objects for email related information. [`QbixEmail`]
 	- address: [String]
	- type: [int]
	- customType: [String]
- __ims__: List of complex objects for IM related information. [`QbixIm`]
 	- data: [String]
	- type: [int]
	- customType: [String]
	- protocol: [String]
	- customProtocol: Post Office Box number. [String]
- __websites__: List of complex objects for website related information. [`QbixWebsite`]
 	- url: [String]
	- type: [int]
	- customType: [String]       
- __photos__: List of complex objects for photo related information. [`QbixPhoto`]
 	- photoFileId: [Number]
	- photo: Encoded by base64. [String]
- __note__: Note of the contact. [String]
- __nickname__: Nicname of the contact. [String]
- __birthday__: birthday of the contact (timestamp). [String]

# Methods

* `getAll`
* `get`
* `forContacts`
* `save`
* `remove`
* `addContact`
* `removeContact`
* `setForContact`
* `smart`

## getAll
Gets all labels asynchronously. Returns JSON array of `QbixGroup` object. Property names are the keys.

## get
Gets labels by given `sourceId`'s. Returns JSON array of `QbixGroup` object. Property names are the keys.

### Parameters
- __sourceIds__: `sourceId`s of labels wanted to be returned. [JSON array of Strings]

## forContacts
Gets labels for specific contact(s). Returns JSON array of `QbixGroup` object. Property names are the keys.

### Parameters
- __contactIds__: `contactId`s of contacts which labels wanted to be returned. [JSON array of Strings]
- __doUnion__: If `true` gets only those labeles which are bound to every single given contact (System related ones are not count). If `false` gets all labels that are bound to at least one given contact. [boolean]

## save
Edits existing labels title or adds new one. If label is added (not edited), device will syncronize all accounts for generating some autogenerating information (`sourceId`, `syncAdapter`s e.t.c).

### Parameters
- __labelInfo__: JSON array which contains:
	- sourceId: first item. `sourceId` of the label wanted to be edited. If it equals "-1", label will be added instead of editing existing one. [String]
	- title: second item. New title of adding/editing label. [String]
  
## remove
Removes existing label. 

### Parameters
- __sourceId__: The `sourceId` of label wanted to be removed. If there is multiple labels with same `sourceId`, they all will be removed. [String] 

## addContact
Add existing label to given contact(s).

### Parameters
- __sourceId__: The `sourceId` of the label wanted to be added. [String]
- __contactIds__: List of `contactId`s to which label wants to be added. [JSON array of Strings]

## removeContact
Removes label from given contact(s).

### Parameters
- __sourceId__: The `sourceId` of the label wanted to be removed. [String]
- __contactIds__: List of `contactId`s from which label wants to be removed. [JSON array of Strings]

## setForContact
Sets all the labels for the contact. This can be used to add or remove labels since the list of `sourceId`s is supposed to be the total set of labels for the contact, eg [ ] removes all labels.

### Parameters
- __contactId__: The `contactId` of contact to which the label list wanted to be set. [String]
- __sourceIds__: The `sourceId`s wanted to be set to contact. (Can be empty which means to remove all non-system-related labels from contact)

## smart
Gets contacts list depending on given `smartName`. [`QbixContact`]

### Parameters
- __smartName__: The criteria of filtering/sorting contacts. Must be one of the following keywords. [String]
	- __uncategorized__: Get contacts which don't belong to any group.
	- __byTimeAdded__: Get contacts sorted by last time updated.
	- __byCompany__: Get contacts that have filled the "Company" or "Organization" field(s).
	- __hasEmail__: Get contacts that have "email" field(s).
	- __hasPhone__: Get contacts that have "phone" field(s).  
	- __hasPhoto__: Get contacts that have photo(s).   
