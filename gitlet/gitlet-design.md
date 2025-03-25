# Gitlet Design Document

*Name*:

## Classes and Data Structures
####Main class
*does all text recognition and calls methods

####Commit Class
*blob hashmap- with shaid -- add will check if shaid is already in hashmap in which case nothing will happen

LINKED HASHMAP FOR COMMITS, SHAID TO COMMIT, TRANSIENT PARENT SO POINTER SO PARENT WILL BE CURR.LAST IN THE LIST

Content of file. new blob with each change of a file, file name will point to new blob.

So program checks if content of newly commit file w same name as another commit is the same as previous file, if not new pointer from file name to blob in the commit, pointer names of files are cloned


Each file is hashed with additional word-blob

add adds new blobs to linkedhashmap, commit has keys which will point to hashmap


*Commit* linked hashmap- commits w heads and pointers, hashed with "commmit"
clones las commit, then adds new changes if they are different, use shaid to check this. Points to all committed files
## Algorithms
###Commit


### Main
*add- adds serialized files to staging area--should probably be a file, could be a hashset though

## Persistence