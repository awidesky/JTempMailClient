# JTempMailClient
A simple CLI client uses [JMailTM](https://github.com/shivam1608/JMailTM)  
  
  
# Commands  
```
help [command]
	Get usage of certain command.
	If no argument is given, print help of all command.

login [<email> <password>]
	Login to existing account
	If no option given, find "autologin.txt" in working directory.
	You can write your email & pasword there to login.

gen [id] <password>
	Generate new email combined with given id(optional) and random domain.
	If id is not given, random id will be used.

delaccount
	Delete self account.

fetch [--overwrite|-o] [limit]
	Fetch messages from server. To show messages, use 'listmsg' command.
	If --overwrite or -o option is given, fetched message before
	will be overwrited to fresh fetched message
	If limit is given, up to <limit> messages will be fetched

listmsg
	List all fetched message.

showmsg [id]
	Show message that have given id.
	If id is not given, show ALL message.

getattat <id> [--path path]
	Download attatchment of mail that have given id.
	If path is given, the file(s) are stored in there.
	Else, files will be downloaded at working directory (C:\Users\CKIRUser\Downloads\gitRepos\JTempMailClient\.)

exit
	Kills the application.
```