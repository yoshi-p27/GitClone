package gitlet;


import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;



/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Aayush Patel. */

@SuppressWarnings("unchecked")
public class Main {


    /**Max hash length. */
    public static final int MAX_SIZE = 40;

    /** String of the _heads. */
    private static String _headString = null;

    /** Whether merge conflict was encountered. */
    private static boolean _encounteredMerge = false;

    /** Current working directory.*/
    static final File CWD = new File(".");

    /**Metadata folder. */
    static final File GITLET_FOLDER = Utils
            .join(CWD, ".gitlet");

    /** Commit directory. */
    static final File COMMIT_DIR = Utils
            .join(GITLET_FOLDER, "commits");

    /** Head pointer. */
    static final File HEAD = Utils
            .join(GITLET_FOLDER, ".head");

    /** Blobs directory. */
    static final File BLOBS = Utils
            .join(GITLET_FOLDER, ".blobs");

    /** Staging area. */
    static final File ADD_STAGING = Utils
            .join(GITLET_FOLDER, ".addStaging");

    /** Rm staging area. */
    static final File RM_STAGING = Utils
            .join(GITLET_FOLDER, ".rmStaging");

    /** Branches pointers directory. */
    static final File BRANCHES = Utils
            .join(GITLET_FOLDER, ".branches");

    /**Current branch pointer. */
    static final File CURR_BRANCH = Utils
            .join(GITLET_FOLDER, ".curr_branch");

    /**Current branch name. */
    static final File CURR_BRANCH_NAME = Utils
            .join(GITLET_FOLDER, ".curr_branch_name");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        switch (args[0]) {
        case "init":

            init(); break;
        case "add":
            add(args); break;
        case "rm":
            rm(args); break;
        case "branch":
            branch(args); break;
        case "commit":
            commit(args); break;
        case "checkout":
            if (args.length == 4) {
                checkout4(args);
            }
            if (args.length == 3) {
                checkout3(args);
            }
            if (args.length == 2) {
                checkout2(args);
            }
            break;
        case "log":
            log(); break;
        case "status":
            status(); break;
        case "rm-branch":
            rmBranch(args); break;
        case "find":
            find(args); break;
        case "global-log":
            globalLog(); break;
        case "reset":
            reset(args); break;
        case "merge":
            merge(args); break;
        default:
            System.out.println("No command with that name exists.");
        }
        return;
    }





    /** Initializes .gitlet directory. 
     * The system startes with one commit with no files, 
     * the message "initial commit", and one master branch. 
     * Various error checks for invalid inputs
     */
    public static void init() {
        if (GITLET_FOLDER.exists()) {
            System.out.println("A Gitlet version-control system"
                    + " already exists in the current directory.");
            return;

        }
        GITLET_FOLDER.mkdir();
        COMMIT_DIR.mkdirs();
        BLOBS.mkdirs();
        BRANCHES.mkdirs();
        try {
            ADD_STAGING.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LinkedHashMap addStaging = new LinkedHashMap();
        Utils.writeObject(ADD_STAGING, addStaging);
        try {
            RM_STAGING.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LinkedHashMap rmStaging = new LinkedHashMap();
        Utils.writeObject(RM_STAGING, rmStaging);
        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            CURR_BRANCH.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            CURR_BRANCH_NAME.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Commit first = new Commit("initial commit",
                null);
        first.makeshaID();
        assignHeadString(first.getID());
        String[] arrForBranch = new String[2];
        arrForBranch[1] = "master";
        branch(arrForBranch);
        assignCurrBranchName(arrForBranch[1]);
        first.addCommit();
        assignHeadString(first.getID());
    }


    /** Adds blobs (files) to staging area for commits to use.
     *
     * @param args Array of arguments, array[0] will be "add" array[1] will be the file's name.
     */
    public static void add(String[] args) {
        LinkedHashMap addStaging = Utils
                .readObject(ADD_STAGING, LinkedHashMap.class);
        LinkedHashMap rmStaging = Utils
                .readObject(RM_STAGING, LinkedHashMap.class);
        File newFile = Utils.join(CWD, args[1]);
        if (newFile.exists()) {
            String headContents = Utils.readContentsAsString(HEAD);
            Commit currentComm = Utils.readObject(Utils
                            .join(COMMIT_DIR, Utils.readContentsAsString(HEAD)),
                    Commit.class);
            String newID = makeshaID(Utils
                    .readContentsAsString(newFile));
            if (currentComm.getBlobs().get(newFile.getName()) != null
                    && currentComm.getBlobs().get(args[1]).equals(newID)) {
                addStaging.remove(newFile.getName());
                rmStaging.remove(newFile.getName());
            } else if (addStaging.containsKey(newFile.getName())) {
                File blobbedFile = Utils.join(BLOBS, newID);
                try {
                    blobbedFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String inter = Utils.readContentsAsString(newFile);
                Utils.writeContents(blobbedFile, inter);
                addStaging.replace(newFile.getName(), newID);
            } else {
                File blobbedFile2 = Utils.join(BLOBS, newID);
                try {
                    blobbedFile2.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String inter = Utils.readContentsAsString(newFile);
                Utils.writeContents(blobbedFile2, inter);
                addStaging.put(newFile.getName(), newID);
            }
            Utils.writeObject(ADD_STAGING, addStaging);
            Utils.writeObject(RM_STAGING, rmStaging);
        } else {
            System.out.println("File does not exist");
            return;
        }
    }

    /** Removes file from current working directory and adds to rm staging area.
     *
     * @param args array[0] will be "add" array[1] will be the files name.
     */
    public static void rm(String[] args) {
        File cwdFile = Utils.join(CWD, args[1]);
        Commit headCommit = Utils.readObject(Utils.join(COMMIT_DIR,
                Utils.readContentsAsString(HEAD)), Commit.class);
        LinkedHashMap addStaging = Utils
                .readObject(ADD_STAGING, LinkedHashMap.class);
        LinkedHashMap rmStaging = Utils
                .readObject(RM_STAGING, LinkedHashMap.class);
        if (!headCommit.getBlobs().containsKey(args[1])
                && !addStaging.containsKey(args[1])) {
            System.out.println("No reason to remove the file.");
            return;
        }
        addStaging.remove(args[1]);
        Utils.writeObject(ADD_STAGING, addStaging);
        if (headCommit.getBlobs().containsKey(args[1])) {
            if (cwdFile.exists()) {
                String cwdID = makeshaID(Utils.readContentsAsString(cwdFile));
                Utils.restrictedDelete(cwdFile);
                rmStaging.put(args[1], cwdID);
            } else {
                rmStaging.put(args[1], headCommit.getBlobs().get(args[1]));
            }
        }
        Utils.writeObject(RM_STAGING, rmStaging);
    }

    /** Creates and saves a snapshot of the tracked files in the current commit and staging areas
     * and adds and removes files accordingly. Commit is added to commit tree and is identified by SHA-1 hash. 
     * Head is moved to new commit
     *
     * @param args Array of arguments, array[0] will be "commit" array[1] will be the commit message
     */
    public static void commit(String[] args) {
        LinkedHashMap inter = Utils
                .readObject(ADD_STAGING, LinkedHashMap.class);
        LinkedHashMap inter2 = Utils
                .readObject(RM_STAGING, LinkedHashMap.class);
        if (inter.isEmpty() && inter2.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }

        Commit newCommit = new Commit(args[1],
                Utils.readContentsAsString(HEAD));
        newCommit.addBlobs();
        newCommit.rmBlobs();
        newCommit.makeshaID();
        newCommit.addCommit();
        assignHeadString(newCommit.getID());
        File currBranch = Utils.join(BRANCHES,
                Utils.readContentsAsString(CURR_BRANCH_NAME));
        Utils.writeContents(currBranch, newCommit.getID());
        LinkedHashMap addStaging = Utils
                .readObject(ADD_STAGING, LinkedHashMap.class);
        addStaging.clear();
        Utils.writeObject(ADD_STAGING, addStaging);
        LinkedHashMap rmStaging = Utils
                .readObject(RM_STAGING, LinkedHashMap.class);
        rmStaging.clear();
        Utils.writeObject(RM_STAGING, rmStaging);
    }

    /** Makes new branch pointers at current Head.
     *
     * @param args Array of arguments. array[0] will be "branch" array[1] will be the branch's name
     */
    public static void branch(String[] args) {
        File newBranch = Utils.join(BRANCHES, args[1]);
        if (newBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        try {
            newBranch.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String headNode = Utils.readContentsAsString(HEAD);
        Utils.writeContents(newBranch, headNode);
    }

    /**Removes the branch pointer.
     *
     * @param args Array of arguments. array[0] will be "rm-branch" array[1] will be the branch's name
     */
    public static void rmBranch(String[] args) {
        if (args[1].equals(Utils.readContentsAsString(CURR_BRANCH_NAME))) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        File branch = Utils.join(BRANCHES, args[1]);
        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        branch.delete();
    }

    /** The version of checkout which was easy. Takes the version of the file at the head commit and puts it in the working directory, 
     * overwriting current version of file.
     * 
     * @param args Array of arguments. array[0] will be "checkout" array[1] will be "--" array[2] will be the files name
     */
    public static void checkout3(String[] args) {
        if (args.length == 3) {
            String fileName = args[2];
            String headCommName = Utils.readContentsAsString(HEAD);
            Commit headComm = Utils.readObject(Utils
                    .join(COMMIT_DIR, headCommName), Commit.class);
            if (!headComm.getBlobs().containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            String checkedFileID = (String)
                    headComm.getBlobs().get(fileName);
            File currBlob = Utils.join(BLOBS, checkedFileID);
            String blobContents = Utils.readContentsAsString(currBlob);
            File workingDirVersion = Utils.join(CWD, fileName);
            Utils.writeContents(workingDirVersion, blobContents);
        }
    }

    /** The version of checkout with the 4 arguments and shortened hashIDs.
     * Takes the version of the file at the commit with the given id and puts it in the working
     * directory, overwriting current version of the file.
     *
     * @param args Array of arguments again. array[0] will be "checkout" 
     * array[1] will be the commit's id array[2] will be"--" array[3] will be 
     * the files name
     */
    public static void checkout4(String[] args) {
        if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            String givenID = args[1];
            Commit checkedComm = null;
            if (givenID.length() < MAX_SIZE) {
                File[] fileList = COMMIT_DIR.listFiles();
                for (File i : fileList) {
                    if (i.getName().startsWith(givenID)) {
                        checkedComm = Utils.readObject(Utils
                                .join(COMMIT_DIR, i.getName()), Commit.class);
                        break;
                    }
                }
                if (checkedComm == null) {
                    System.out.println("No commit with that id exists.");
                    return;
                }
            } else {
                if (!Utils.join(COMMIT_DIR, givenID).exists()) {
                    System.out.println("No commit with that id exists.");
                    return;
                }
                checkedComm = Utils.readObject(Utils
                        .join(COMMIT_DIR, givenID), Commit.class);
            }

            String fileName = args[3];
            if (!checkedComm.getBlobs().containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            String checkedFileID =
                    (String) checkedComm.getBlobs().get(fileName);
            File currBlob = Utils.join(BLOBS, checkedFileID);
            String blobContents = Utils.readContentsAsString(currBlob);
            File workingDirVersion = Utils.join(CWD, fileName);
            Utils.writeContents(workingDirVersion, blobContents);
        }
    }

    /** The really scary checkout one with the branches. Takes all files in the commit at the head of the branch
     * and puts them in the working directory, overwriting files in the current directory. Head is reassigned.
     *
     * @param args Array of arguments. array[0] will be "checkout" array[1] will be the branch's name
     */
    public static void checkout2(String[] args) {
        if (args.length == 2) {
            File branch = Utils.join(BRANCHES, args[1]);
            if (!branch.exists()) {
                System.out.println("No such branch exists.");
                return;
            }
            if (branch.getName().equals(Utils
                    .readContentsAsString(CURR_BRANCH_NAME))) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            List<String> cwdFiles = Utils.plainFilenamesIn(CWD);
            Commit currCommit = Utils.readObject(Utils
                            .join(COMMIT_DIR, Utils.readContentsAsString(HEAD)),
                    Commit.class);
            File branchCommitName = Utils.join(COMMIT_DIR,
                    Utils.readContentsAsString(Utils.join(BRANCHES, args[1])));
            Commit branchCommit = Utils
                    .readObject(branchCommitName, Commit.class);
            ArrayList<String> markedForDeletion = new ArrayList<String>();
            for (String i : cwdFiles) {
                if (!currCommit.getBlobs().containsKey(i)) {
                    if (branchCommit.getBlobs().containsKey(i)) {
                        System.out.println("There is an untracked "
                                + "file in the way;"
                                + " delete it, "
                                + "or add and commit it first.");
                        return;
                    }
                } else if (!branchCommit.getBlobs().containsKey(i)) {
                    markedForDeletion.add(i);
                }
            }
            for (String i : markedForDeletion) {
                Utils.restrictedDelete(Utils.join(CWD, i));
            }
            for (Object i : branchCommit.getBlobs().keySet()) {
                File blobID = Utils.join(BLOBS,
                        (String) branchCommit.getBlobs().get(i));
                String blobContents = Utils.readContentsAsString(blobID);
                File workingDirVersion = Utils.join(CWD, (String) i);
                Utils.writeContents(workingDirVersion, blobContents);
            }
            assignHeadBranch(args[1]);
            assignCurrBranchName(args[1]);
        }
    }

    /** Checks out all files at the commit and removes all tracked files not present in the commit.
     * Current branch's head is moved ot that commit node. Staging areas are cleared
     * @param commID ID of commit to be reset to.
     */
    public static void reset(String[] commID) {
        String givenID = commID[1];
        boolean exists = false;
        if (givenID.length() < MAX_SIZE) {
            File[] fileList = COMMIT_DIR.listFiles();
            for (File i : fileList) {
                if (i.getName().startsWith(givenID)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                System.out.println("No commit with that id exists.");
                return;
            }
        } else {
            if (!Utils.join(COMMIT_DIR, givenID).exists()) {
                System.out.println("No commit with that id exists."); return;
            }
            List<String> cwdFiles = Utils.plainFilenamesIn(CWD);
            Commit currCommit = Utils
                    .readObject(Utils.join(COMMIT_DIR,
                                    Utils.readContentsAsString(HEAD)),
                    Commit.class);
            Commit checked = Utils.readObject(Utils.join(COMMIT_DIR,
                    commID[1]), Commit.class);
            ArrayList<String> markedForDeletion = new ArrayList<String>();
            for (String i : cwdFiles) {
                if (!currCommit.getBlobs().containsKey(i)) {
                    if (checked.getBlobs().containsKey(i)) {
                        System.out.println("There is an untracked file in "
                            + "the way; delete it,"
                            + " or add and commit it first.");
                        return;
                    }
                } else if (!checked.getBlobs().containsKey(i)) {
                    markedForDeletion.add(i);
                }
            }
            for (String i : markedForDeletion) {
                Utils.restrictedDelete(Utils.join(CWD, i));
            }
            for (Object i : checked.getBlobs().keySet()) {
                File blobID = Utils
                        .join(BLOBS, (String) checked.getBlobs().get(i));
                String blobContents = Utils.readContentsAsString(blobID);
                File workingDirVersion = Utils.join(CWD, (String) i);
                Utils.writeContents(workingDirVersion, blobContents);
            }
            LinkedHashMap addStaging = Utils
                    .readObject(ADD_STAGING, LinkedHashMap.class);
            addStaging.clear();
            Utils.writeObject(ADD_STAGING, addStaging);
            File currBranchFile = Utils
                    .join(BRANCHES, Utils
                            .readContentsAsString(CURR_BRANCH_NAME));
            Utils.writeContents(currBranchFile, commID[1]);
            assignHeadBranch(currBranchFile.getName());
        }
    }

    /**Finds the commit via commit message string. Prints out all commits that have the given message.
     * @param commMessage Commit message string.
     */
    public static void find(String[] commMessage) {
        Commit checkedComm = null;
        List<String> fileList = Utils.plainFilenamesIn(COMMIT_DIR);
        for (String i : fileList) {
            Commit currComm = Utils
                    .readObject(Utils.join(COMMIT_DIR, i), Commit.class);
            if (currComm.getMessage().equals(commMessage[1])) {
                System.out.println(currComm.getID());
                checkedComm = currComm;
            }
        }
        if (checkedComm == null) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**Gives the log. Starting at the current head commit, prints commit ID, Date, and commit message for 
     * each commit backwards along the tree until the initial commit.
     */
    public static void log() {
        StringBuilder fin = new StringBuilder();
        String headCommName = Utils.readContentsAsString(HEAD);
        Commit headComm = Utils
                .readObject(Utils
                        .join(COMMIT_DIR, headCommName), Commit.class);
        while (headComm.getParent() != null) {
            fin.append(headComm);
            String prevCommName = headComm.getParent();
            headComm = Utils.readObject(Utils
                    .join(COMMIT_DIR, prevCommName), Commit.class);
        }
        fin.append(headComm);
        String s = fin.toString();
        fin.deleteCharAt(fin.length() - 1);
        System.out.println(fin);
    }

    /**Gives the globalLog.
     * Displays unordered invormation about all commits ever made.
     */
    public static void globalLog() {
        StringBuilder fin = new StringBuilder();
        List<String> commits = Utils.plainFilenamesIn(COMMIT_DIR);
        for (String i : commits) {
            fin.append(Utils
                    .readObject(Utils
                            .join(COMMIT_DIR, i), Commit.class));
        }
        fin.toString();
        System.out.println(fin);
    }

    /** Gives the status.
     * Displays existing branches. Current branch is barked with *
     * Displays files in staging areas
     */
    public static void status() {
        if (!GITLET_FOLDER.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        StringBuilder fin = new StringBuilder();
        StringBuilder branches = new StringBuilder();
        StringBuilder stagedFiles = new StringBuilder();
        StringBuilder removedFiles = new StringBuilder();
        StringBuilder mods = new StringBuilder();
        StringBuilder untracked = new StringBuilder();
        List branchNamesList = Utils.plainFilenamesIn(BRANCHES);
        ArrayList<String> branchNames = new ArrayList<String>();
        branchNames.addAll(branchNamesList);
        String currBranchName = Utils.readContentsAsString(CURR_BRANCH_NAME);
        int currBranchIndex = branchNames.indexOf(currBranchName);
        branchNames.set(currBranchIndex, "*" + currBranchName);
        branches.append("=== Branches ===\n");
        for (String i : branchNames) {
            branches.append(i + "\n");
        }
        branches.append("\n");
        fin.append(branches);

        stagedFiles.append("=== Staged Files ===\n");
        LinkedHashMap addStaging = Utils
                .readObject(ADD_STAGING, LinkedHashMap.class);
        Set addKeySet = addStaging.keySet();

        for (Object i : addKeySet) {
            stagedFiles.append(i + "\n");
        }

        stagedFiles.append("\n");

        removedFiles.append("=== Removed Files ===\n");

        LinkedHashMap rmStaging = Utils
                .readObject(RM_STAGING, LinkedHashMap.class);
        Set rmKeySet = rmStaging.keySet();

        for (Object i : rmKeySet) {
            removedFiles.append(i + "\n");
        }

        removedFiles.append("\n");

        mods.append("=== Modifications Not Staged For Commit ===\n");
        mods.append("\n");
        untracked.append("=== Untracked Files ===\n");
        untracked.append("\n");

        fin.append(stagedFiles);
        fin.append(removedFiles);
        fin.append(mods);
        fin.append(untracked);
        System.out.println(fin);
    }

    /** Merges files from the given branch into the current branch.
     *
     * @param args Array of args. array[0] will be "merge" array[1] will be branch's name.
     */
    public static void merge(String[] args) {
        File givenBranch = Utils.join(BRANCHES, args[1]);
        if (!givenBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        LinkedHashMap addStaging = Utils
                .readObject(ADD_STAGING, LinkedHashMap.class);
        LinkedHashMap rmStaging = Utils
                .readObject(RM_STAGING, LinkedHashMap.class);
        if (!rmStaging.isEmpty() || !addStaging.isEmpty()) {
            System.out.println("You have uncommitted changes."); return;
        }
        String currBranchName = Utils.readContentsAsString(CURR_BRANCH_NAME);
        String currBranchHead;
        if (!currBranchName.equals(args[1])) {
            currBranchHead = Utils
                    .readContentsAsString(Utils.join(BRANCHES, currBranchName));
        } else {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Commit givenCommit = Utils.readObject(Utils
                        .join(COMMIT_DIR,
                                Utils.readContentsAsString(givenBranch)),
                Commit.class);
        Commit currCommit = Utils.readObject(Utils
                .join(COMMIT_DIR, currBranchHead), Commit.class);
        List<String> cwdFiles = Utils.plainFilenamesIn(CWD);
        for (String i : cwdFiles) {
            if (!currCommit.getBlobs().containsKey(i)) {
                if (givenCommit.getBlobs().containsKey(i)) {
                    System.out.println("There is an untracked"
                            + " file in the way; delete it, "
                           + "or add and commit it first.");
                    return;
                }
            }
        }
        String lcaName = lcaFinder(currBranchHead, givenCommit.getID());
        if (lcaName.equals(givenCommit.getID())) {
            System.out.println("Given branch is "
                    + "an ancestor of the current branch.");
            return;
        }
        if (lcaName.equals(currCommit.getID())) {
            System.out.println("Current branch fast-forwarded.");
            String[] thing = new String[2];
            thing[1] = args[1];
            checkout2(thing);
            return;
        }
        Commit lca = Utils.readObject(Utils
                .join(COMMIT_DIR, lcaName), Commit.class);
        mergecondition1(currCommit, givenCommit, lca);
        String commMessage = "Merged "
                + givenBranch.getName()
                + " into " + currBranchName + ".";
        commitForMerge(currBranchHead, givenCommit.getID(), commMessage);
    }

    /**Does the merge specific commit work. Helper function for merge which formally creates
     * and commits merged files onto the commit tree.
     *
     * @param current Current commit string.
     * @param given Given commit string.
     * @param commMessage Commit message for commit.
     */
    public static void commitForMerge(String current,
                                       String given, String commMessage) {
        LinkedHashMap inter = Utils.readObject(ADD_STAGING,
                LinkedHashMap.class);
        LinkedHashMap inter2 = Utils.readObject(RM_STAGING,
                LinkedHashMap.class);
        if (inter.isEmpty() && inter2.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit newCommit = new Commit(commMessage, current, given);

        if (returnEncounter()) {
            System.out.println("Encountered a merge conflict.");
        }
        newCommit.addBlobs();
        newCommit.rmBlobs();
        newCommit.makeshaID();
        newCommit.addCommit();
        assignHeadString(newCommit.getID());
        File currBranch = Utils.join(BRANCHES,
                Utils.readContentsAsString(CURR_BRANCH_NAME));
        Utils.writeContents(currBranch, newCommit.getID());
        LinkedHashMap addStaging = Utils.readObject(ADD_STAGING,
                LinkedHashMap.class);
        addStaging.clear();
        Utils.writeObject(ADD_STAGING, addStaging);

        LinkedHashMap rmStaging = Utils
                .readObject(RM_STAGING, LinkedHashMap.class);
        rmStaging.clear();
        Utils.writeObject(RM_STAGING, rmStaging);
    }


    /** Does heavy lifitng for merge. Here the various conditions for merging
     * blobs(files) occurs, such as checking for conflicts, rewriting or deleting blobs if necessary.
     *
     * @param currCommit Current commit.
     * @param givenCommit Given commit.
     * @param lca The found LCA.
     */
    public static void mergecondition1(Commit currCommit, Commit
            givenCommit, Commit lca) {
        LinkedHashMap addStaging = Utils.readObject(ADD_STAGING,
                LinkedHashMap.class);
        LinkedHashMap lcaBlobs = lca.getBlobs();
        LinkedHashMap gCBlobs = givenCommit.getBlobs();
        LinkedHashMap cCBlobs = currCommit.getBlobs();
        for (Object key : lca.getBlobs().keySet()) {
            File cwdVersion = (Utils.join(CWD, (String) key));
            if (!lcaBlobs.get(key).equals(gCBlobs
                    .get(key)) && gCBlobs.get(key) != null) {
                if (cCBlobs.get(key).equals(lcaBlobs.get(key))) {
                    String updatedContents = Utils.readContentsAsString(Utils
                            .join(BLOBS, (String) gCBlobs.get(key)));
                    String updatedID = makeshaID(updatedContents);
                    Utils.writeContents(cwdVersion, updatedContents);
                    addStaging.put(key, updatedID);
                }
            }
            if (lcaBlobs.get(key).equals(cCBlobs.get(key))) {
                if (!givenCommit.getBlobs().containsKey(key)) {
                    Utils.restrictedDelete(cwdVersion); addStaging.remove(key);
                }
            }
            if (!lca.getBlobs().get(key).equals(givenCommit
                    .getBlobs().get(key)) && !lca.getBlobs().get(key)
                    .equals(currCommit.getBlobs().get(key))
                    && currCommit.getBlobs().containsKey(key)
                    && !currCommit.getBlobs().get(key)
                    .equals(givenCommit.getBlobs().get(key))) {
                addStaging.putAll(conflict(currCommit,
                        givenCommit, lca, key, cwdVersion));
            }
        }
        for (Object key : givenCommit.getBlobs().keySet()) {
            if (!lcaBlobs.containsKey(key) && !cCBlobs.containsKey(key)) {
                File cwdVersion = (Utils.join(CWD, (String) key));
                String updatedContents = Utils
                        .readContentsAsString(Utils.join(BLOBS,
                        (String) givenCommit.getBlobs().get(key)));
                String updatedID = makeshaID(updatedContents);
                Utils.writeContents(cwdVersion, updatedContents);
                addStaging.put(key, updatedID);
            } else if (!givenCommit.getBlobs().get(key)
                    .equals(lca.getBlobs().get(key))
                    && !currCommit.getBlobs().get(key)
                            .equals(lca.getBlobs().get(key))
                    && !currCommit.getBlobs().get(key)
                            .equals(givenCommit.getBlobs().get(key))) {
                File cwdVersion = (Utils.join(CWD, (String) key));
                addStaging.putAll(conflict(currCommit,
                        givenCommit, lca, key, cwdVersion));
            }
        }
        if (addStaging.isEmpty()) {
            for (Object key : currCommit.getBlobs().keySet()) {
                File cwdVersion = (Utils.join(CWD, (String) key));
                addStaging.putAll(conflict(currCommit,
                        givenCommit, lca, key, cwdVersion)); }
        }
        Utils.writeObject(ADD_STAGING, addStaging); }

    /**Removes some of the heavy lifting from mergecondition1 for finding conflicts. In particular, conflicted files are rewritten
     * in accordance with the standard conflict format here.
     *
     * @param currCommit Current commit.
     * @param givenCommit Given commit.
     * @param lca LCA which is just so I can keep track.
     * @param key Key which is given by the loop
     *            this is called in; for blobbed hashmaps.
     * @param cwdVersion The files in the CWD passed in from mergecondition1.
     * @return
     */
    public static LinkedHashMap conflict(Commit currCommit,
                                         Commit givenCommit, Commit lca,
                                         Object key, File cwdVersion) {
        LinkedHashMap addStaging = Utils.readObject(ADD_STAGING,
                LinkedHashMap.class);
        mergeEncounterTrue();
        String contentsHead = Utils.readContentsAsString(Utils.join(BLOBS,
                (String) currCommit.getBlobs().get(key)));
        String branchContents = "";
        String debug2 = (String) givenCommit.getBlobs().get(key);
        if (debug2 != null) {
            branchContents = Utils.readContentsAsString(Utils.join(BLOBS,
                    (String) givenCommit.getBlobs().get(key)));
        }
        String newString = "<<<<<<< HEAD\n"  + contentsHead
                + "=======\n" + branchContents + ">>>>>>>\n";
        Utils.writeContents(cwdVersion, newString);
        String updatedID = makeshaID(newString);
        addStaging.put(key, updatedID);
        return addStaging;
    }



    /**BFS which gives list of ancestors.
     *
     * @param comm provided headString.
     * @return Returns ArrayList of commIDs for BFS2 to compare to.
     */
    public static ArrayList<String> bfsFinder1(String comm) {
        ArrayList commIDs = new ArrayList();
        ArrayDeque work = new ArrayDeque();
        work.push(comm);
        while (!work.isEmpty()) {
            Object node = work.remove();
            if (node != null || node.equals("")) {
                commIDs.add(node);
                ArrayList parents = Utils.readObject(Utils.join(COMMIT_DIR,
                        (String) node), Commit.class).getParents();
                for (int i = 0; i < parents.size(); i += 1) {
                    if (!parents.get(i).equals(null)) {
                        work.push(parents.get(i));
                    }
                }
            }
        }
        return commIDs;
    }

    /** BFS which stops at the first instance
     * where provided comm is in compareTo.
     *
     * @param comm Commit string to be checked.
     * @param compareTo ArrayList of comparisons from other BFS.
     * @return Returns string of the LCA.
     */
    public static String bfsFinder2(String comm, ArrayList compareTo) {
        ArrayDeque work = new ArrayDeque();
        work.push(comm);
        while (!work.isEmpty()) {
            Object node = work.remove();
            if (node != null) {
                if (compareTo.contains(node)) {
                    return (String) node;
                }
                ArrayList parents = Utils.readObject(Utils.join(COMMIT_DIR,
                        (String) node), Commit.class).getParents();
                for (int i = 0; i < parents.size(); i += 1) {
                    work.push(parents.get(i));
                }
            }
        }
        return null;
    }

    /** Finds the LCA of the given branch and the other branch.
     *
     * @param given It is usually actually the currCommit.
     * @param otherBranchHead It is the other branches Head.
     * @return returns the string of the LCA.
     */
    public static String lcaFinder(String given, String otherBranchHead) {
        ArrayList givenBranch = bfsFinder1(given);
        return bfsFinder2(otherBranchHead, givenBranch);
    }

    /** String makes a shaID out of a given string.
     *
     * @param descriptor given string out of which shaID is made,
     *                  usually contents of file or particular string
     *                   for Commits.
     * @return returns string which is new shaID.
     */
    public static String makeshaID(String descriptor) {
        return Utils.sha1(descriptor);
    }


    /**Assigns Head String to current commit.
     *
     * @param currCommit the new head commit.
     */
    public static void assignHeadString(String currCommit) {
        _headString = currCommit;
        Utils.writeContents(HEAD, currCommit);

    }

    /** Assigns HeadBranch to the given branch.
     * @param branch is the branch to be reassigned to. */
    public static void assignHeadBranch(String branch) {
        File branchFile = Utils.join(BRANCHES, branch);
        String branchCommit = Utils.readContentsAsString(branchFile);
        Utils.writeContents(HEAD, branchCommit);
    }

    /** Assigns currentbranchname to given string.
     *
     * @param branchName is a string which is the new headBranchName.
     */
    public static void assignCurrBranchName(String branchName) {
        Utils.writeContents(CURR_BRANCH_NAME, branchName);
    }

    /** Reassigns _encounteredMerge to true. */
    public static void mergeEncounterTrue() {
        _encounteredMerge = true;
    }

    /** Returns whether conflict was encountered. */
    public static boolean returnEncounter() {
        return _encounteredMerge;
    }

}



