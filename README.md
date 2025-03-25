# Git Clone
This is a git clone with simplified implementations of most Git functions, including add, commit, rm, log, global-log, find, status, checkout, branch, rm-branch, reset, and merge.

I have written all of the code regarding this functionality, and all files relevant to it are marked with the author tag: Aayush Patel. These are primarily the main.java and commit.java files.
This particular directory contains much of the infrastructure necessary to interface with the OS, which was provided to me. My role was implementing the logic for the above functions.

Further, this code has been specially formatted to serve as a code sample for Tubi's Builders Program. As such, some functionality may be limited somewhat, but personal integration testing has been successful. As such
you will notice that there is only the initial commit, as the relevant code has been copied over from a project repo.

## Dependencies
If you are aiming to run the inbuilt functionality for testing, please ensure that you have the utility "Make" installed in order to make use of the Makefiles for compilation and testing.
Further, please make sure you have Java11 and python3 installed.

## Usage
While I will provide a high-level explanation of the functionality implemented in the Main.java and Commit.java files, please refer to [this](https://sp21.datastructur.es/materials/proj/proj2/proj2) project spec, which is similar to the spec this project was based on. There you will find more detail about
the implemented functionality. More detail can also be found in each function's commented documentation.

### Commit.java
Here is where the commit structure used for the rest of the project is implemented. Each commit has a message, a timestamp, a HashMap of blobs (our version of text files)
and either one or two parents, depending on whether a particular commit is a merged one.

### Main.java
This is the primary driver class for our git-clone. I will provide a high level overview of the primary functions in this class below:

#### init()

Init initializes the directory we will be working in. There is an initial commit with the message "initial commit" and one master branch. 

#### add()

This function takes in a file name, which if in the current working directory, is added to the staging area to be committed.

#### rm()

This function takes in a file name, which if in the current staging area, is added to the removal staging area to be addressed at the time of next commit

#### commit()
This function takes in a commit message, and saves a snapshot of the tracked files, i.e. those in the add staging areas. It will update the contents of the files tracked by its parent
if the file is not in the removal staging area. If it is in the removal staging area, the file from the commit's parent will not be tracked. It is then added as a new node to the commit tree.

#### branch()
This function takes in a new branch name. It creates a new branch with the gien name and points it at the curent head node. It is merely a file which points to a commit identifier.

#### rmBranch()
This funciton takes in a branch name. This function deletes the branch with the given name, i.e. delete the file holding the pointer to a commit.

#### checkout3()
This funcion takes in a file name. This is the simplest checkout case where the version of the file at the head commit is put in or overwrites the version in the current working directory.

#### checkout4()
This function takes in a commit id and a file name. It takes the file from the given commit and is put in or overwrites the version in the current working directory.

#### checkout3()
This function takes in a branch's name. It takes all the files in the commit at the head of the branch and either puts them in or rewrites the versions in the curent working directory.

#### reset()
This function takes in a commit id. It checks out all the files in the commit, and removes all files not in that commit from the cwd.

#### merge()
This funcition is rather complicated so please refer to the linked spec and the code for more detailed documentation. It takes in a branch name, and merges all files from the given branch into the current one.

### Testing
Tests are given in the "testing" file in the repo. If you would like to run all tests, run "make test." If you would like to run a specific test compile with make, then run: python3 tester.py --verbose --keep FILE.in, Replaceing FILE with the desired .in file name. 
You may also write your own tests by creating .in files in the testing directory with similar syntax to other files















