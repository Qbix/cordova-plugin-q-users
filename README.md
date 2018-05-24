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
* __`AccountContactIds`__
* __`AccountLabelIds`__

## QbixGroup

The `QbixGroup` object represents a users label.

### Properties

- __id__: The source_id of the label.Some system related and default labels may have same sourceId for different accounts. [String]
- __title__: The title of the label. [String]
- __notes__: The notes about the label. [String]
- __summaryCount__: Count of all contacts related to the label.
  (If `id` (source_id) of the label is default or system related(repeatable),
  `summaryCount` can provide wrong count(only first accounts contacts' count)) [int]
- __isVisible__: Shows if this label is visible to any user interface. 
  (Some system related labels are not visible to users) [boolean]
- __isDeleted__: Shows if this label has been deleted. (Label can be deleted, but until syncronization it is stored in database) [boolean]
- __shouldSync__: Shows if this label should be syncronized with account or not. [boolean]
- __readOnly__: Shows if this label is for reading only and cant be modified/deleted. [boolean]
- __contactIds__: The list of contact ids, which related to the label. [Integer]

```js
{
  "id": "4ea633c10987aff9",
  "title": "Colleagues",
  "notes": "Some notes about \"Colleagues\" group.",
  "summaryCount": 7,
  "isVisible": true,
  "isDeleted": false,
  "shouldSync": true,
  "readOnly": false,
  "contactIds": [
    2,
    5,
    9,
    10,
    17
  ]
}
```

## AccountContactIds

The `AccountContactIds` object represents an account information (name, type) and list of `contactId`s from givel list (See in __checkContactsAccount__ method)
that belongs to that account.

### Properties

- __accountName__: The name of account. [String]
- __accountType__: The type of account. [String]
- __contactIds__: The list of contact ids, which belongs to the account (not all, only from given list). [Integer]

```js
{
  "accountName": "account.name@gmail.com",
  "accountType": "com.google",
  "contactIds": [
    15,
    55,
    96
  ]
}
```

## AccountLabelIds

The `AccountLabelIds` object represents an account information (name, type) and list of label `id`s (source_id) from givel list (See in __checkLabelsAccount__ method)
that belongs to that account. If multiple accounts have the same `id`, then it will be assigned to the first account in device's databese order.

### Properties

- __accountName__: The name of account. [String]
- __accountType__: The type of account. [String]
- __labelIds__: The list of label ids, which belongs to the account (not all, only from given list). [String]

```js
{
  "accountName": "account.name@gmail.com",
  "accountType": "com.google",
  "labelIds": [
    "e",
    "797fe7510dd13f31",
    "ad615688bac8449"
  ]
}
```


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
* `checkContactsAccount`
* `checkLabelsAccount`

## getAll
Gets all labels asynchronously. Returns JSON array of `QbixGroup` object. Property names are the keys.

### Supporded platforms
- __Android__

__Example__:
```js
Q.Users.Cordova.Labels.getAll(function(data){console.log(data);}, function(err){console.log(err)})
```

## get
Gets labels by given `id`'s. Returns JSON array of `QbixGroup` object. Property names are the keys.

### Parameters
- __ids__: `id`s of labels wanted to be returned. [JSON array of Strings]

### Supporded platforms
- __Android__

__Example__:
```js
var getLabels = Q.Users.Cordova.Labels.get(["74d148cb8cbc7d41","6982bdf4897c3c6f","e"],function(data){console.log(data);}, function(err){console.log(err)})
```

## forContacts
Gets labels for specific contact(s). Returns JSON array of `QbixGroup` object. Property names are the keys.

### Parameters
- __contactIds__: `contactId`s of contacts which labels wanted to be returned. [JSON array of ints]
- __doUnion__: If `true` gets only those labeles which are bound to every single given contact (System related ones are not count). If `false` gets all labels that are bound to at least one given contact. [boolean]

### Supporded platforms
- __Android__

__Example__:
```js
var unionLabels = Q.Users.Cordova.Labels.forContacts([5,12,56],true,function(data){console.log(data);}, function(err){console.log(err)})
```

## save
Edits existing labels title or adds new one. If label is added (not edited), device will syncronize all accounts for generating some autogenerating information (`id`, `syncAdapter`s e.t.c).

### Parameters
- __labelInfo__: JSON array which contains:
	- id: first item. `id` of the label wanted to be edited. If it equals "-1", label will be added instead of editing existing one. [String]
	- title: second item. New title of adding/editing label. [String]

### Supporded platforms
- __Android__

__Example__:
```js
Q.Users.Cordova.Labels.save({
  "labelId": "54ed2c5e8de2b47c",
  "title": "New Title"
},function(data){console.log(data);}, function(err){console.log(err)})
```

## remove
Removes existing label. 

### Parameters
- __id__: The `id` of label wanted to be removed. If there is multiple labels with same `id`, they all will be removed. [String] 

### Supporded platforms
- __Android__

__Example__:
```js
Q.Users.Cordova.Labels.remove("54ed2c5e8de2b47c",function(data){console.log(data);}, function(err){console.log(err)})
```

## addContact
Add existing label to given contact(s).

### Parameters
- __id__: The `id` of the label wanted to be added. [String]
- __contactIds__: List of `contactId`s to which label wants to be added. [JSON array of ints]

### Supporded platforms
- __Android__

__Example__:
```js
Q.Users.Cordova.Labels.addContact("54ed2c5e8de2b47c",[5,12,25],function(data){console.log(data);}, function(err){console.log(err)})
```

## removeContact
Removes label from given contact(s).

### Parameters
- __id__: The `id` of the label wanted to be removed. [String]
- __contactIds__: List of `contactId`s from which label wants to be removed. [JSON array of ints]

### Supporded platforms
- __Android__

__Example__:
```js
Q.Users.Cordova.Labels.removeContact("54ed2c5e8de2b47c",[5,12,25],function(data){console.log(data);}, function(err){console.log(err)})
```

## setForContact
Sets all the labels for the contact. This can be used to add or remove labels since the list of `id`s is supposed to be the total set of labels for the contact, eg [ ] removes all labels.

### Parameters
- __contactId__: The `contactId` of contact to which the label list wanted to be set. [int]
- __ids__: The `id`s wanted to be set to contact. (Can be empty which means to remove all non-system-related labels from contact)

### Supporded platforms
- __Android__

__Example__:
```js
Q.Users.Cordova.Labels.setForContact(5,["54ed2c5e8de2b47c", "130e58e00af1beec"],function(data){console.log(data);}, function(err){console.log(err)})
```

## smart
Gets contacts' id list depending on given `smartName`. [String]

### Parameters
- __smartName__: The criteria of filtering/sorting contacts. Must be one of the following keywords. [String]
	- __uncategorized__: Get contacts which don't belong to any group.
	- __byTimeAdded__: Get contacts sorted by last time updated.
	- __byCompany__: Get contacts that have filled the "Company" or "Organization" field(s).
	- __hasEmail__: Get contacts that have "email" field(s).
	- __hasPhone__: Get contacts that have "phone" field(s).  
	- __hasPhoto__: Get contacts that have photo(s).   


### Supporded platforms
- __Android__

__Example__:
```js
var photoContacts = Q.Users.Cordova.Labels.smart("hasPhoto",function(data){console.log(data);}, function(err){console.log(err)})
```

## checkContactsAccount
Binds given `contactId`s to their account's name and type. Returns JSON array of `AccountContactIds` object. Property names are the keys.

### Parameters
- __contactIds__: List of `contactId`s which accounts' information wanted to be returned. [JSON array of ints] 

### Supporded platforms
- __Android__

__Example__:
```js
var accountContactIdsList = Q.Users.Cordova.Labels.checkContactsAccount([11,25,47],function(data){console.log(data);}, function(err){console.log(err)})
```

## checkContactsAccount
Binds given label `id`s to their account's name and type. Returns JSON array of `AccountLabelIds` object. Property names are the keys.

### Parameters
- __labelIds__: List of label `id`s which accounts' information wanted to be returned. [JSON array of Strings] 

### Supporded platforms
- __Android__

__Example__:
```js
var accountLabelIdsList = Q.Users.Cordova.Labels.checkLabelsAccount(["d","65935bb78e39336d","3f336c768918b9dd"],function(data){console.log(data);}, function(err){console.log(err)})
```