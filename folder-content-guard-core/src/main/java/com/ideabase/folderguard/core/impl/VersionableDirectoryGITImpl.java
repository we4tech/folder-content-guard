/**
 * $Id$
 * *****************************************************************************
 *    Copyright (C) 2005 - 2007 somewhere in .Net ltd.
 *    All Rights Reserved.  No use, copying or distribution of this
 *    work may be made except in accordance with a valid license
 *    agreement from somewhere in .Net LTD.  This notice must be included on
 *    all copies, modifications and derivatives of this work.
 * *****************************************************************************
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 * *****************************************************************************
 */

package com.ideabase.folderguard.core.impl;

import com.ideabase.folderguard.core.VersionableDirectory;
import com.ideabase.folderguard.core.RevisionList;
import com.ideabase.folderguard.core.Revision;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.spearce.jgit.lib.*;
import org.spearce.jgit.revwalk.RevWalk;
import org.spearce.jgit.revwalk.RevCommit;
import org.spearce.jgit.revwalk.RevTree;
import org.spearce.jgit.treewalk.filter.TreeFilter;
import org.spearce.jgit.treewalk.TreeWalk;

/**
 * Git based versionable directory implementation
 * @author <a href="http://hasan.we4tech.com">nhm tanveer...(hasan)</a>
 */
public class VersionableDirectoryGITImpl implements VersionableDirectory {

  private static final String VERSION = "GIT";
  private static final Logger LOG =
      LogManager.getLogger(VersionableDirectoryGITImpl.class);

  private final File mWorkingDirectory;
  private final File mGitDirectory;
  private Repository mRepository;
  private boolean mVersionable;
  private Thread mWorkerThread;
  private boolean mProcessStarted = false;
  private final List<String> mAllowedFormats = new ArrayList<String>();
  private List<File> mCommittableFiles;
  private boolean mTestMode = false;
  private List<List<File>> mLastCommits = new ArrayList<List<File>>();
  private ObjectWriter mObjectWriter;

  /**
   * Default constructor which accepts working directory as the default option.
   * @param pWorkingDirectory working directory
   */
  public VersionableDirectoryGITImpl(final File pWorkingDirectory) {
    mWorkingDirectory = pWorkingDirectory;
    mGitDirectory = new File(pWorkingDirectory, ".git");
    initGitSupportedDirectory();
    addDefaultAllowedTypes();
  }

  private void addDefaultAllowedTypes() {
    addAllowedFormat(".txt")
    .addAllowedFormat(".html")
    .addAllowedFormat(".doc")
    .addAllowedFormat(".xls")
    .addAllowedFormat(".htm");
  }

  /**
   * If .GIT file is not already exists setup a git project on the
   * specified directory.
   */
  private void initGitSupportedDirectory() {

    LOG.debug("Initiating GIT supported directory.");
    try {
      mRepository = new Repository(mGitDirectory);
      if (!mGitDirectory.exists()) {
        mRepository.create();
        LOG.info("Creating new repository on path - " +
                 mGitDirectory.getAbsolutePath());
      }
      mObjectWriter = new ObjectWriter(mRepository);
      mVersionable = true;
    } catch (IOException rootException) {
      throw new RuntimeException(
          "failed to create new repository on path - " +
              mGitDirectory.getAbsolutePath(), rootException);
    }
  }

  public VersionableDirectory addAllowedFormat(final String pFormat) {
    mAllowedFormats.add(pFormat);
    return this;
  }

  public List<String> getAllowedFormats() {
    return mAllowedFormats;
  }

  public VersionableDirectory removeAllowedFormat(final String pFormat) {
    mAllowedFormats.remove(pFormat);
    return this;
  }

  public File getWorkingDirectory() {
    return mWorkingDirectory;
  }

  public boolean isVersionable() {
    return mVersionable;
  }

  public String getVersionControlSystemName() {
    return VERSION;
  }

  /**
   * <h2>basic process flow</h2>
   * Start a thread with the given interval.
   * if an existing process is in progress new process would be posponded.
   * iterate through all files and directory and store in version control system
   * version only the given pattern or suffixed files
   * <br>
   * <h2>how to detect file changes</h2>
   * when watch dog will iterate through all files, it would verify the file
   * modify timestamp with the last timestamp. if no change found it won't
   * add in change list.
   * <br>
   * <h2>how commit will works</h2>
   * all changed files will be kept in a single set to commit together.
   * <br>
   * <h2>how commit message will be formatted</h2>
   * message which is required during new commit will be formatted with
   * timestamp and list of changes files.
   */
  public boolean startWatchDog(final long pInterval) {
    if (mProcessStarted) {
      throw new IllegalStateException("Already a watch dog process running.");
    }

    mProcessStarted = true;
    mWorkerThread = new Thread(new Runnable() {
      public void run() {
        while (mProcessStarted) {
          checkFileSystem();
          try {
            Thread.sleep(pInterval);
          } catch (InterruptedException e) {
            mProcessStarted = false;
          }
        }
      }
    });
    mWorkerThread.start();
    return true;
  }

  /*
   * Exposed through package private to invoke this method from test case.
   */
  List<File> checkFileSystem() {

    LOG.info("Checking filesystem for possible changes.");
    try {
      final GitIndex index = mRepository.getIndex();
      if (LOG.isDebugEnabled()) {
        LOG.debug("Git Index - " + index);
        for (final GitIndex.Entry entry : index.getMembers()) {
          LOG.debug(entry);
        }
      }
      if (index != null) {
        final Tree headTree = findHeadTree();
        mCommittableFiles = new ArrayList<File>();

        // check file changes
        checkDirectoryForChanges(mWorkingDirectory, headTree, mObjectWriter, index);

        // commit changes if committable files found
        LOG.debug("Committable files - ");
        LOG.debug(mCommittableFiles);
        if (!mCommittableFiles.isEmpty()) {
          commitChanges(headTree);
          return mCommittableFiles;
        }
      }
    } catch (IOException e) {
      LOG.warn("Failed to check filesystem", e);
    }
    return Collections.EMPTY_LIST;
  }

  /**
   * Create a new commit and update reference to all updated files which
   * was already added in the head tree
   */
  private void commitChanges(final Tree pHeadTree)
      throws IOException {

    LOG.info("Committing new changes on the repository.");
    final Commit commit;

    // retrieve previouse commit reference.
    final ObjectId existingCommitId = mRepository.resolve(Constants.HEAD);
    if (existingCommitId != null) {
      commit = new Commit(mRepository, new ObjectId[] {existingCommitId});
    } else {
      commit = new Commit(mRepository);
    }

    commit.setTree(pHeadTree);
    final PersonIdent person = new PersonIdent("folder guard", "hasan83bd@gmail.com");
    commit.setAuthor(person);
    commit.setCommitter(person);
    commit.setMessage(buildCommitLogMessage());

    // perform commit
    commit.setCommitId(mObjectWriter.writeCommit(commit));
    LOG.info("Performed commit id - " + commit.getCommitId());

    // create new update reference
    final RefUpdate updateReference = mRepository.updateRef(Constants.HEAD);
    updateReference.setNewObjectId(commit.getCommitId());
    updateReference.setRefLogMessage(commit.getMessage(), true);
    final RefUpdate.Result result = updateReference.forceUpdate();
    LOG.debug(result);

    if (mTestMode) {
      addLastCommitHistory(mCommittableFiles);
    }
  }

  /**
   * Build log message for the commit log.
   * usually "chage files -" and list of file names are added for the
   * commit log message
   */
  private String buildCommitLogMessage() {

    final StringBuilder builder = new StringBuilder();
    builder.append("changed files - \n");
    for (final File file : mCommittableFiles) {
      builder.append(file.getAbsolutePath()).append("\n");
    }
    return builder.toString();
  }

  /**
   * Iterate through directories and files of the specific directory to sense
   * the changes since the last update.
   *
   * this process is also dependent {@code isAllowedFileType) method to verify
   * whether the specific type of files are allowed to include in the
   * version control.
   *
   * by default: "." prefixed files are ignored.
   */
  private void checkDirectoryForChanges(
      final File pFile, final Tree pHeadTree,
      final ObjectWriter pObjectWriter, final GitIndex pIndex)
      throws IOException {

    if (isAllowedFileType(pFile)) {
      if (pFile.isDirectory()) {
        for (final File file : pFile.listFiles()) {
          checkDirectoryForChanges(file, pHeadTree, pObjectWriter, pIndex);
        }
      } else {
        checkFileForChanges(pFile, pHeadTree, pObjectWriter, pIndex);
      }
    }
  }

  /**
   * Check specific file for changes, if any change is found since the
   * last commit, it would be added to the new change set.
   * later that change set will be committed.
   */
  private void checkFileForChanges(
      final File pFile, final Tree pHeadTree,
      final ObjectWriter pObjectWriter, final GitIndex pIndex)
      throws IOException {

    if (LOG.isDebugEnabled()) {
      LOG.debug("Checking file - " + pFile + " for update.");
    }

    // create or retrieving existing file reference from GIT
    final TreeEntry gitFile = findOrCreateFileInTree(pHeadTree, pFile, pObjectWriter, pIndex);

    if (gitFile != null) {
      addForCommit(pFile, pHeadTree, pObjectWriter, pIndex, gitFile);
    }
  }

  /**
   * Add file (which was changed recently) to the change set so later this
   * could be commited on the version controlling system.
   */
  private void addForCommit(final File pFile,
                            final Tree pHeadTree,
                            final ObjectWriter pObjectWriter,
                            final GitIndex pIndex,
                            final TreeEntry pGitFile) throws IOException {
    
    // store file content in GIT repository
    // store GIT file stored reference to the entry
    pGitFile.setId(pObjectWriter.writeBlob(pFile));

    // read the tree recursivly
    pIndex.readTree(pHeadTree);

    // store index to the file store.
    pIndex.write();

    // store tree from the index.
    pHeadTree.setId(pIndex.writeTree());

    // store newly changed file reference
    mCommittableFiles.add(pFile);
  }

  /**
   * Find a specific file from the specific tree, if that file is not exist
   * that file tree leaf would be created on the version control tree.
   */
  private TreeEntry findOrCreateFileInTree(
      final Tree pHeadTree, final File pFile,
      final ObjectWriter pObjectWriter, final GitIndex pIndex)
      throws IOException {

    final String filePath = buildFilePath(pFile);
    final Tree filePathTree = findOrCreateFilePathInTree(filePath, pHeadTree, pObjectWriter);

    // create file entry
    final String fileName = pFile.getName();
    final TreeEntry gitFile;
    if (filePathTree.existsBlob(fileName)) {
      gitFile = filePathTree.findBlobMember(fileName);
      if (!isFileModified(filePath, fileName, pIndex)) {
        return null;
      }
    } else {
      gitFile = filePathTree.addFile(fileName);
    }
    ((FileTreeEntry) gitFile).setExecutable(false);
    return gitFile;
  }

  /**
   * Find specific path from the head tree, if not found create a new tree.
   */
  private Tree findOrCreateFilePathInTree(
      final String pFilePath, final Tree pHeadTree,
      final ObjectWriter pObjectWriter) throws IOException {

    if (!pFilePath.equals("")) {
      if (!pHeadTree.existsTree(pFilePath)) {
        // create file path tree
        final Tree filePathTree = pHeadTree.addTree(pFilePath);
        filePathTree.setId(pObjectWriter.writeTree(filePathTree));
        return filePathTree;
      } else {
        return (Tree) pHeadTree.findTreeMember(pFilePath);
      }
    } else {
      return pHeadTree;
    }
  }

  /**
   * Verify whether the specific file is modified since the last commit.
   */
  private boolean isFileModified(
      final String pFilePath, final String pFileName, final GitIndex pIndex)
      throws UnsupportedEncodingException {

    String file = pFileName;
    if (pFilePath != null && !pFilePath.equals("")) {
      file = pFilePath + File.separator + file;
    }
    final GitIndex.Entry indexEntry = pIndex.getEntry(file);
    return indexEntry != null && indexEntry.isModified(mWorkingDirectory, true);
  }

  /**
   * Build file path from the physical file system structure. 
   */
  private String buildFilePath(final File pFile) {

    final StringBuilder pathBuilder = new StringBuilder();
    final String[] paths = pFile.getParent().split(File.separator);
    if (paths.length > 1) {
      for (int i = 1; i < paths.length; i++) {
        if (i > 1) {
          pathBuilder.append(File.separator);
        }
        pathBuilder.append(paths[i]);
      }
    }
    return pathBuilder.toString();
  }

  /**
   * Verify whether specfic file format is allowed to for keeping multi version.
   * ie. .doc, .xls can be specified to keep them in version control.
   */
  boolean isAllowedFileType(final File pFile) {

    if (pFile.isDirectory()) {
      if (!pFile.getName().startsWith(".")) {
        return true;
      }
    } else {
      for (final String format : mAllowedFormats) {
        if (pFile.getName().endsWith(format)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Find the head tree if first time and no head tree reference is resolved
   * create an empty head tree.
   */
  private Tree findHeadTree()
      throws IOException {

    Tree tree = mRepository.mapTree(Constants.HEAD);
    if (tree == null) {
      tree = new Tree(mRepository);
      // write tree to the object database
      mObjectWriter.writeTree(tree);
    }
    return tree;
  }

  /**
   * Change internal watch dog process state and stop currently active thread.
   */
  public boolean stopWatchDog() {
    mProcessStarted = false;
    mWorkerThread.interrupt();
    return true;
  }

  /**
   * Return the internal watch dog state
   */
  public boolean isWatchDogRunning() {
    return mProcessStarted;
  }

  /**
   * Retrieve revision history from the head tree
   */
  public List<Revision> findAllRevisions() {

    final List<Revision> revisions = new ArrayList<Revision>();
    try {
      final RevWalk revWalk = new RevWalk(mRepository);
      revWalk.markStart(revWalk.parseCommit(mRepository.resolve(Constants.HEAD)));

      for (final RevCommit revCommit : revWalk) {
        revisions.add(buildRevision(revCommit));
      }
      
    } catch (IOException e) {
      LOG.warn("failed to retrieve head tree", e);
    }
    return revisions;
  }

  /**
   * Build {@code Revision} from {@code RevCommit}
   * @param pRevCommit revision commit object
   * @return {@code Revision} built revision
   */
  private Revision buildRevision(final RevCommit pRevCommit) {
    final Revision revision = new Revision()
    .setCommitId(pRevCommit.getId().name())
    .setAuthor(new GitUser(pRevCommit.getAuthorIdent()))
    .setCommitter(new GitUser(pRevCommit.getCommitterIdent()))
    .setMessage(pRevCommit.getFullMessage())
    .setDate(new Date(pRevCommit.getCommitTime()))
    .setRevisionDataTree(new RevisionDataTreeGITImpl(mRepository, pRevCommit));
    return revision;
  }

  public List<RevisionList> findFileRevisions(final File pFile) {
    throw new NoSuchMethodError("this method is not yet implemented");
  }

  /*
   * Retrieve specific commit and associated tree
   * find blob data from specific commit revision
   * return in byte array
   */
  public byte[] checkoutFile(final String pCommitId, final String pFileName) {
    final ObjectId commitId = ObjectId.fromString(pCommitId);
    LOG.info("Reverting code from commit id - " + commitId);

    try {
      final Commit commit = mRepository.mapCommit(commitId);
      final FileTreeEntry treeEntry = (FileTreeEntry) findFileTree(pFileName);
      return treeEntry.openReader().getBytes();
    } catch (Exception e) {
      throw new RuntimeException(
          "failed to retrieve file - " + pFileName + " of commit id - " + pCommitId, e);
    }
  }

  private TreeEntry findFileTree(final String pFileName)
      throws IOException {

    final Tree headTree = findHeadTree();

    // split file by slash and keep remove the last part
    final String[] fileParts = pFileName.split(File.separator);
    if (fileParts.length > 1) {
      final StringBuilder filePathBuilder = new StringBuilder();
      for (int i = 0; i < fileParts.length - 1; i++) {
        filePathBuilder.append(fileParts[i]);
        if (i > 0) {
          filePathBuilder.append(File.separator);
        }
      }
      final TreeEntry treeEntry =
          headTree.findTreeMember(filePathBuilder.toString());
      return headTree.getRepository().mapTree(treeEntry.getId()).findBlobMember(fileParts[fileParts.length - 1]);
    } else {
      return headTree.findBlobMember(pFileName);
    }
  }

  /**
   * Exposed test mode property to enable or disable test mode. this is
   * used for exposing more internal behavior for more assertion purpose
   */
  void setTestMode(final boolean pTestMode) {
    mTestMode = pTestMode;
  }

  private void addLastCommitHistory(final List<File> mCommitedFiles) {
    mLastCommits.add(mCommitedFiles);
  }

  /**
   * Expose all last commits histories.
   */
  List<List<File>> getLastCommitHistories() {
    return mLastCommits;
  }
}
