package com.gameofcoding.spy.server;

import com.gameofcoding.spy.utils.XLog;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * RepoManager provides high level git features.
 *
 * <b>NOTE: We are currently using github as server</b>
 *
 * As we know about the famous version control tool 'git', 'jgit' is alternative for that, it works
 * just like git but we can use it in java and fortunatly jgit is written in pure java.
 *
 * <b>NOTE: Latest version of Jgit is not supported by android, so we are using 3.7.xxxxx of jgit
 * which works fine for our work.</b>
 */
class RepoManager {
    private static final String TAG = "RepoManager";
    public static final String REMOTE_NAME = "origin";
    public static final String REF_BRANCH_SUFFIX = "refs/heads/";
    public static final String BRANCH_MASTER = "master";
    private Git mGit;

    /**
     * ProgressMonitor for logging all networking progress. This helps a lot during debugging.
     */
    private ProgressMonitor progressMonitor = new ProgressMonitor() {
	    int mTotalWork;
	    int mWorkDone;
	    int mLastProgress;
	    String mTitle;

	    private void logProgress() {
		if(mTotalWork == 0)
		    return;
		// Log percentage progress
		int prog = 100 * mWorkDone / mTotalWork;
		if(prog - mLastProgress < 1)
		    return;
		mLastProgress = prog;
		XLog.v(TAG, mTitle + ": " + prog);
	    }

	    @Override
	    public void start(int totalTasks) {
		logProgress();
	    }

	    @Override
	    public void beginTask(String title, int totalWork) {
		mTotalWork = totalWork;
		mWorkDone = 0;
		mLastProgress = 0;
		mTitle = title;
		logProgress();
	    }

	    @Override
	    public void update(int completed) {
		mWorkDone += completed;
		logProgress();
	    }

	    @Override
	    public void endTask() {}

	    @Override
	    public boolean isCancelled() {
		return false;
	    }
	};

    /* package */ RepoManager() {}

    /**
     * Clones git repo.
     *
     * @param dir, file where to store cloned repo.
     *             It must be empty.
     * @param branchesToClone, list of branches that you want to clone.
     *
     * @return true if repository was successfully cloned.
     */
    public boolean clone(File dir, List<String> branchesToClone)
	throws InvalidRemoteException, TransportException, GitAPIException, IOException {

	if(dir.exists() && dir.listFiles().length > 0) {
	    // Directory already exists and is not empty
	    XLog.e(TAG, "clone(File, List<String>[]): Repo. dir=["
		   + dir + "], already exists and is not empty");
	    return false;
	}

	// Format branch name
	for(int i = 0; i < branchesToClone.size(); i++) {
	    String branch = branchesToClone.get(i);
	    if(!branch.startsWith(REF_BRANCH_SUFFIX)) {
		String newBranchName = REF_BRANCH_SUFFIX + branch;
		branchesToClone.set(i, newBranchName);
		XLog.v(TAG, "Branch name formatted, '" + branch + "' -> '" + newBranchName + "'");
	    }
	}

	// Clone Repo
        XLog.v(TAG, "Cloning repository with specified branches...");
	Git.cloneRepository()
	    .setURI(getRepoURI())
	    .setCredentialsProvider(getCredentialsProvider())
	    .setBranchesToClone(branchesToClone)
	    .setDirectory(dir)
            .setProgressMonitor(progressMonitor)
	    .call();
        XLog.v(TAG, "Repository cloned.");
	loadRepo(dir);

	// Create remote tracking branches of cloned branches
	for(String branch : branchesToClone) {
	    branch = branch.replace(REF_BRANCH_SUFFIX, "");
	    if(branch.equals(BRANCH_MASTER))
		continue;
	    createBranch(branch, branch);
	}
	return true;
    }

    /**
     * Loads git repo. from the specified directory
     */
    public void loadRepo(File repoDir) throws IOException {
	mGit = Git.open(repoDir);
    }

    /**
     * Commits changes with the specified message.
     * <b>NOTE: This method not only commits changes but also stages them for convinence.</b>
     */
    public boolean commit(String commitMsg) throws GitAPIException {
	if(hasCleanStatus()) // Working tree is clean
	    return true;

	// git add .
	mGit.add()
	    .addFilepattern(".")
	    .call();
	XLog.v(TAG, "commit(String): Git staged changes");

	// git commit -m "$commitMsg"
	mGit.commit()
	    .setMessage(commitMsg)
	    .call();
	XLog.v(TAG, "commit(String): Git committed changes");
	return true;
    }

    /**
     * Checks whether the passed branch name exists or not (as local)
     */
    public boolean hasLocalBranch(String branch) throws GitAPIException {
	if(!branch.startsWith(REF_BRANCH_SUFFIX))
	    branch = REF_BRANCH_SUFFIX + branch;
	List<Ref> refs = mGit.branchList()
	    .call();
	for(Ref ref : refs) {
	    if(ref.getName().equals(branch))
		return true;
	}
	return false;
    }

    /**
     * Deletes remote branch (which may or may not be cloned)
     */
    public boolean deleteRemoteBranch(String branch) throws GitAPIException {
	// Create branch ref spec.
	if(!branch.startsWith(REF_BRANCH_SUFFIX))
	    branch = REF_BRANCH_SUFFIX + branch;
	RefSpec branchRefSpec = new RefSpec()
	    .setSource(null)
	    .setDestination(branch);
	
	// Delete remote branch
	mGit.push()
	    .setCredentialsProvider(getCredentialsProvider())
	    .setRemote(REMOTE_NAME)
	    .setRefSpecs(branchRefSpec)
	    .call();
	return true;
    }
    
    /**
     * Creates a new local branch.
     *
     * @param branch, name of the branch to be created
     *
     * @return true
     */
    public boolean createBranch(String branch) throws GitAPIException {
	// Create branch
	mGit.branchCreate()
	    .setName(branch)
	    .call();
	return true;
    }

    /**
     * Create a local branch that tracks the specified remote branch.
     *
     * @param branch, name of the branch to be created
     * @param remoteBranch, name of the remote branch that needs to be tracked
     *
     * @return true
     */
    public boolean createBranch(String branch, String remoteBranch) throws GitAPIException {
	// Create remote tracking branch
	mGit.branchCreate()
	    .setName(branch)
	    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
	    .setStartPoint("remotes/origin/" + remoteBranch)
	    .call();
	return true;
    }

    /**
     * Creates a new branch with zero commits plus it is not linked with other branches.
     * In native git its command is:<i>git checkout --orphan $branch_name</i>
     *
     * @param branch, name of the branch to be created as orphan
     *
     * @return true
     */
    public boolean checkoutOrphanBranch(String branch) throws GitAPIException {
	// Create branch which is independent of other branchs
	mGit.checkout()
	    .setOrphan(true)
	    .setName(branch)
	    .call();

	// Reset index (git reset --hard)
	mGit.reset()
	    .setMode(ResetCommand.ResetType.HARD)
	    .call();

	// Create empty commit (git commit -m "initial commit" --allow-empty)
	mGit.commit()
	    //.setAllowEmpty(true) // It is default before 4.2v of Jgit
	    .setMessage("initial commit")
	    .call();
	return true;
    }

    /**
     * Converts already created branch as remote tracking branch.
     * <b>NOTE: This method forecefully sets a branch to remote tracking branch</b>
     */
    public boolean setRemoteTrackingBranch(String branch, String remoteBranch) throws GitAPIException {
	mGit.branchCreate()
	    .setName(branch)
	    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
	    .setStartPoint("remotes/origin/" + remoteBranch)
	    .setForce(true)
	    .call();
	return true;
    }

    /**
     * Returns true if working tree is clean
     */
    public boolean hasCleanStatus() throws GitAPIException {
	return mGit.status().call()
	    .isClean();
    }

    /**
     * Checkouts the specified branch.
     */
    public boolean checkout(String branch) throws GitAPIException {
	// Create branch
	mGit.checkout()
	    .setName(branch)
	    .call();
	return true;
    }

    /**
     * Pulls the current branch.
     */
    public boolean pull() throws GitAPIException {
	mGit.pull()
	    .setCredentialsProvider(getCredentialsProvider())
	    .setRemote(REMOTE_NAME)
	    .call();
	return true;
    }

    /**
     * Pulls the specified branch.
     */
    public boolean pull(String branch) throws GitAPIException {
	checkout(branch);
	pull();
	return true;
    }

    /**
     * Fetches remote branch
     */
    public boolean fetchBranch(String remoteBranch) throws GitAPIException {
	String refSpec = REF_BRANCH_SUFFIX + remoteBranch + ":refs/remotes/origin/" + remoteBranch;
	mGit.fetch()
	    .setCredentialsProvider(getCredentialsProvider())
	    .setRemote(REMOTE_NAME)
	    .setProgressMonitor(progressMonitor)
	    .setRefSpecs(new RefSpec(refSpec))
	    .call();
	return true;
    }

    /**
     * Pushes changes to remote.
     */
    public boolean push() throws GitAPIException {
	mGit.push()
	    .setCredentialsProvider(getCredentialsProvider())
	    .setRemote(REMOTE_NAME)
	    .setProgressMonitor(progressMonitor)
	    .call();
	return true;
    }

    /**
     * Pushs a ref spec to remote.
     * This method is usually used to create a remote branch.
     */
    public boolean push(String fromRefSpec, String toRefSpec) throws GitAPIException {
	mGit.push()
	    .setCredentialsProvider(getCredentialsProvider())
	    .setRemote(REMOTE_NAME)
	    .setProgressMonitor(progressMonitor)
	    .setRefSpecs(new RefSpec(fromRefSpec + ":" + toRefSpec))
	    .call();
	return true;
    }

    private UsernamePasswordCredentialsProvider getCredentialsProvider() {
	return new UsernamePasswordCredentialsProvider(getUserName(), getPassword());
    }

    private String getUserName() {
	return "game-of-coding";
    }

    private String getPassword() {
	return "1021n194";
    }

    private String getRepoURI() {
	return "https://github.com/Game-Of-Coding/Spy-Server.git";
    }
}
