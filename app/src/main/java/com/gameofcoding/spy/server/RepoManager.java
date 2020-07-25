package com.gameofcoding.spy.server;

import com.gameofcoding.spy.utils.XLog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

class RepoManager {
    private static final String TAG = "RepoManager";
    public static final String BRANCH_MASTER = "master";
    public static final String BRANCH_APP_DATA = "app_data";
    public static final String LOCAL_BRANCH_SUFFIX = "refs/heads/";
    public static final String REMOTE_BRANCH_SUFFIX = "refs/remotes/origin/";
    private Git mGit;
    
    RepoManager() {}

    public boolean clone(File dir)
	throws InvalidRemoteException, TransportException, GitAPIException, IOException {
	return clone(dir, null);
    }

    public boolean clone(File dir, String[] extraBranches)
	throws InvalidRemoteException, TransportException, GitAPIException, IOException {
	if(dir.exists()) {
	    // Log error
	    XLog.e(TAG, "clone(File, String[]): Repo. dir=[" + dir + "], already exists.");
	    return false;
	}

	// Load Remote Branches to clone
	List<String> branchesToClone = new ArrayList<String>(3);
	branchesToClone.add(LOCAL_BRANCH_SUFFIX + BRANCH_MASTER);
	branchesToClone.add(LOCAL_BRANCH_SUFFIX + BRANCH_APP_DATA);
	if(extraBranches != null) {
	    for(String extraBranch : extraBranches)
		branchesToClone.add(LOCAL_BRANCH_SUFFIX + extraBranch);
	}

	// Clone Repo
	Git.cloneRepository()
	    .setCredentialsProvider(getCredentialsProvider())
	    .setURI(getRepoURI())
	    .setBranchesToClone(branchesToClone)
	    .setDirectory(dir)
	    .call();
	loadRepo(dir);
	return true;
    }

    public void loadRepo(File repoDir) throws IOException {
	mGit = Git.open(new File(repoDir, ".git"));
    }
    
    public boolean commit(String commitMsg) throws GitAPIException {
	// git add -A
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

    public boolean isLocalBranch(String branchName) throws GitAPIException {
	List<Ref> branches = mGit.branchList()
	    .setListMode(ListBranchCommand.ListMode.ALL)
	    .call();
	for(Ref branch : branches) {
	    if((LOCAL_BRANCH_SUFFIX + branchName).equals(branch.getName()))
		// Exists as Remote branch
		return true;
	}
	return false;
    }

    public boolean isRemoteBranch(String branchName) throws GitAPIException {
	List<Ref> branches = mGit.branchList()
	    .setListMode(ListBranchCommand.ListMode.ALL)
	    .call();
	for(Ref branch : branches) {
	    if((REMOTE_BRANCH_SUFFIX + branchName).equals(branch.getName())) {
		// Exists as Local branch
		return true;
	    }
	}
	return false;
    }

    public boolean hasBranch(String branchName) throws GitAPIException {
	return (isLocalBranch(branchName) || isRemoteBranch(branchName));
    }
    
    public boolean createBranch(String branch) throws GitAPIException {
	if(hasBranch(branch))
	   return false;
	mGit.branchCreate()
	    .setName(branch)
	    .call();
	return true;
    }

    public boolean checkout(String branch) throws GitAPIException {
	mGit.checkout()
	    .setName(branch)
	    .call();
	return true;
    }
    
    public boolean pullRemoteBranch(String branchName) throws GitAPIException {
	checkout(REMOTE_BRANCH_SUFFIX + branchName);
	mGit.pull()
	    .setCredentialsProvider(getCredentialsProvider())
	    .setRemoteBranchName(LOCAL_BRANCH_SUFFIX + branchName)
	    .call();
	return true;
    }

    public boolean push() throws GitAPIException {
	mGit.push()
	    .setCredentialsProvider(getCredentialsProvider())
	    .setPushAll()
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
