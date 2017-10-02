# Sign up Page

Adopted from course website lab rubric and description


## Assumptions

- Password Confirmation
	- Pops out when password field loses focus: i.e. when user clicks other fields or submit button
	- displays a "matches" indication when the two passwords entered match
	- negative button always clickable but postivie button clickable only when passwords match

	
- Profile picture is optional

- Upper button shows *Sign In* (equivalent to "I already have an account")
	- diabled for now
	- switch to clear button once user start entering data

-Submit button:
	- save data only if everything entered and validated(i.e. password matched).
	- otherwise make toast to inform user about what went wrong

## Storage

text: sharedPreference<br>
picture: internal
