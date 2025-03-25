package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.ArrayList;


import static gitlet.Main.*;

/** @author Aayush Patel. */
@SuppressWarnings("unchecked")

public class Commit implements Serializable {
    /** Message of commit. */
    private String _message;

    /** Time of commit. */
    private Date _time;

    /** Blobs hashmap. */
    private LinkedHashMap _blobs = new LinkedHashMap();


    /** First parent String. */
    private String _parent;

    /** Second parent string, empty
     * unless merge commit. */
    private String _parent2 = "";

    /** Transient commit object so can
     *  be used for copying commit contents. */
    private transient Commit parent1;

    /** Transient commit object for
     *  other parent for copying commit contents. */
    private transient Commit parent2;

    /** HashID of commit. */
    private String _ID;

    /** Regular commit constructor.
     *
     * @param message Message of commit.
     * @param parent Parent of commit.
     */
    public Commit(String message, String parent) {

        if (parent == null) {
            this._parent = parent;
            this._message = message;
        } else {
            this._message = message;
            parent1 = Utils.readObject(Utils
                    .join(COMMIT_DIR, parent), Commit.class);
            this._blobs.putAll(parent1._blobs);
            this._parent = parent;
        }

        if (this._parent == null) {

            this._time = new Date(0);

        } else {
            this._time = new Date();
        }
    }

    /** Merge commit constructor.
     *
     * @param message Standard message of commit.
     * @param par First parent of commit.
     * @param par2 Second parent of commit.
     */
    public Commit(String message, String par, String par2) {
        this._message = message;
        parent1 = Utils.readObject(Utils.join(COMMIT_DIR, par), Commit.class);
        parent2 = Utils.readObject(Utils.join(COMMIT_DIR, par2), Commit.class);
        this._parent = par;
        this._parent2 = par2;
        this._time = new Date();
    }

    /** Get commit message.
     * @return Returns string. */
    public String getMessage() {
        return this._message;
    }

    /** Get time of commit.
     * @return Time */
    public Date getTime() {
        return this._time;
    }


    /** Get first parent of commit.
     * @return Returns String.*/
    public String getParent() {
        return this._parent;
    }

    /** Get second parent of merge commit.
     * @return Returns String. */
    public String getParent2() {
        return this._parent2;
    }

    /** Get ID of commit.
     * @return Returns String. */

    public String getID() {
        return this._ID;
    }

    /** Adds and serializes the commit (add name bc of capers
     * similar function).
     */
    public void addCommit() {

        File curr = Utils.join(COMMIT_DIR, this._ID);
        try {
            curr.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(curr, this);

    }



    /** Makes the shaID of the commit, accounting for whether or not
     * it is an
     * initial commit.
     */
    public void makeshaID() {

        String info = "commit" + _parent + _time + _message;
        this._ID = Utils.sha1(info);
    }

    /** Adds blobs to blob hashmap. */
    public void addBlobs() {
        LinkedHashMap inter =
                Utils.readObject(ADD_STAGING, LinkedHashMap.class);
        this._blobs.putAll(inter);
    }

    /** Removes blobs from commits in process of constructing them;
     * for merge Manipulations.
     */
    public void rmBlobs() {
        LinkedHashMap inter = Utils.readObject(RM_STAGING, LinkedHashMap.class);
        for (Object key : inter.keySet()) {
            this._blobs.remove(key);
        }
    }

    /** Giga important.
     *
     * @return Returns hashmap of Commits blobs.
     */
    public LinkedHashMap getBlobs() {
        return this._blobs;
    }

    /** Gets all parents.
     *
     * @return Returns ArrayList of commit's parents.
     */
    public ArrayList getParents() {
        ArrayList parents = new ArrayList();
        if (!_parent2.isEmpty()) {
            parents.add(_parent);
            parents.add(_parent2);
            return parents;
        }
        parents.add(_parent);
        if (_parent == null) {
            return new ArrayList();
        }
        return parents;
    }

    /** Overridden toString to format commit info accordingly
     * 
     * @return returns string with commit info
     */
    @Override
    public String toString() {
        StringBuffer builder = new StringBuffer();
        builder.append("===\n");
        builder.append("commit " + this._ID + "\n");
        if (!_parent2.isEmpty()) {
            builder.append("Merge: " + _parent.substring(0, 7) + " "
                    +  _parent2.substring(0, 7) + "\n");
        }
        SimpleDateFormat formatter =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        String formattedDate = formatter.format(this._time);
        builder.append("Date: " + formattedDate + "\n");

        builder.append(this._message + "\n\n");
        return builder.toString();
    }
}

