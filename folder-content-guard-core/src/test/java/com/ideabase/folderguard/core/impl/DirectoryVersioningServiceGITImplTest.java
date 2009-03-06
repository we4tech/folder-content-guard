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

import org.jmock.MockObjectTestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.spearce.jgit.lib.ObjectId;
import com.ideabase.folderguard.core.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileFilter;
import java.util.*;

/**
 * Test Git based versioning service implementation
 * @author <a href="http://hasan.we4tech.com">nhm tanveer...(hasan)</a>
 */
public class DirectoryVersioningServiceGITImplTest extends MockObjectTestCase {

  private DirectoryVersioningService mService;
  private final Logger mLogger = LogManager.getLogger(getClass());

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // create new git implemented service instance
    mService = new DirectoryVersioningServiceGITImpl();

    // clean existing .git directory
    System.out.println("DELETE Status - " + deleteDirectory(new File("test-dir" + File.separator + ".git")));
  }

  private boolean deleteDirectory(final File pFile) {
    if (pFile.isDirectory()) {
      for (final File dirFile : pFile.listFiles()) {
        deleteDirectory(dirFile);
      }
      pFile.delete();
    } else {
      pFile.delete();
    }
    return true;
  }

  public void testShouldFindServiceInstance() {
    assertNotNull("service shouldn't be empty", mService);    
  }

  public VersionableDirectory testShouldFindVersionableDirectory() {
    final File parentDirectory = new File("test-dir");
    mLogger.debug(parentDirectory.getAbsolutePath());

    final VersionableDirectory versionableDirectory =
        mService.findVersionableDirectory(parentDirectory);
    
    assertNotNull(versionableDirectory);
    return versionableDirectory;
  }

  public void testShouldThrowIllegalArgumentExceptionWhenNullIsPassed() {
    RuntimeException exception = null;
    try {
      mService.findVersionableDirectory(null);
    } catch (RuntimeException e) {
      exception = e;
    }
    assertNotNull(exception);
    assertTrue(exception instanceof IllegalArgumentException);
  }

  public void testShouldReturnTrueForVersionableProperty() {
    assertTrue("Directory should be versionable",
               testShouldFindVersionableDirectory().isVersionable());
  }

  public void testShouldReturnTheWorkingDirectory() {
    assertNotNull(testShouldFindVersionableDirectory().getWorkingDirectory());
  }

  public void testShouldReturnVesionControlSystemName() {
    final String vcsName =
        testShouldFindVersionableDirectory().getVersionControlSystemName();
    mLogger.debug("VCS name - " + vcsName);
    assertNotNull(vcsName);
  }

  public List<Object> testShouldCommitNewFiles() {
    final VersionableDirectoryGITImpl directory =
        (VersionableDirectoryGITImpl) testShouldFindVersionableDirectory();
    final List<File> committedFiles = directory.checkFileSystem();
    assertNotNull(committedFiles);
    assertFalse(committedFiles.isEmpty());
    assertDirectoryFiles(committedFiles, directory);
    mLogger.debug(committedFiles);

    return Arrays.asList(committedFiles, directory);
  }

  public void testShouldCommitOnlyChangedFiles() throws IOException {
    final List<Object> returnedObjects = testShouldCommitNewFiles();
    final List<File> committedFiles = (List<File>) returnedObjects.get(0);
    final VersionableDirectoryGITImpl directory =
        (VersionableDirectoryGITImpl) returnedObjects.get(1);

    // change content for the first file
    changeFileContent(committedFiles.get(0), System.currentTimeMillis() + "\n");

    // commit changes
    List<File> changedFiles = directory.checkFileSystem();
    assertNotNull(changedFiles);
    assertFalse(changedFiles.isEmpty());
    // verify total committed files
    assertEquals(changedFiles.size(), 1);
    // verify whether same files were committed
    assertTrue(changedFiles.contains(committedFiles.get(0)));

    // change content for first 2 files
    changeFileContent(committedFiles.get(0), System.currentTimeMillis() + "\n");
    changeFileContent(committedFiles.get(1), System.currentTimeMillis() + "\n");

    // commit changes
    changedFiles = directory.checkFileSystem();
    assertNotNull(changedFiles);
    assertFalse(changedFiles.isEmpty());
    // verify total comitted files
    assertEquals(2, changedFiles.size());
    // verify whether same files were comitted
    assertTrue(changedFiles.contains(committedFiles.get(0)));
    assertTrue(changedFiles.contains(committedFiles.get(1)));

    // change content for all previously committed files
    for (final File file : committedFiles) {
      changeFileContent(file, System.currentTimeMillis() + "\n");
    }
    // commit changes
    changedFiles = directory.checkFileSystem();
    assertNotNull(changedFiles);
    assertFalse(changedFiles.isEmpty());

    // verify total comitted files
    assertEquals(committedFiles.size(), changedFiles.size());
    // verify whether same files were comitted
    for (final File file : committedFiles) {
      assertTrue(changedFiles.contains(file));
    }
  }

  private void changeFileContent(final File pFile, final String pAppendText)
      throws IOException {
    final FileOutputStream fout = new FileOutputStream(pFile, true);
    fout.write(pAppendText.getBytes());
    fout.close();
  }

  private void assertDirectoryFiles(
      final List<File> pCommittedFiles,
      final VersionableDirectoryGITImpl pDirectory) {

    final List<File> commitableFiles = new ArrayList<File>();
    findCommitableFiles(commitableFiles, pDirectory.getWorkingDirectory(), pDirectory);

    for (final File file : commitableFiles) {
      mLogger.debug("Comparingn file - " + file);
      assertTrue(commitableFiles.contains(file));
    }
  }

  private void findCommitableFiles(final List<File> pCommitableFiles,
                                   final File pFile,
                                   final VersionableDirectory pDirectory) {
    if (pFile.isDirectory()) {
      for (final File file : pFile.listFiles()) {
        findCommitableFiles(pCommitableFiles, file, pDirectory);
      }
    } else {
      if (((VersionableDirectoryGITImpl) pDirectory).isAllowedFileType(pFile)) {
        pCommitableFiles.add(pFile);
      }
    }
  }

  public VersionableDirectoryGITImpl testShouldReturnTheListOfAllowedFormats() {
    final VersionableDirectoryGITImpl directory =
        (VersionableDirectoryGITImpl) testShouldFindVersionableDirectory();
    // verify default allowed formats
    assertEquals(5, directory.getAllowedFormats().size());
    return directory;
  }

  public void testShouldAddAndDeleteFormats() {

    final VersionableDirectoryGITImpl directory =
        testShouldReturnTheListOfAllowedFormats();

    // add new type
    directory.addAllowedFormat(".xml");
    assertEquals(6, directory.getAllowedFormats().size());

    // delete type
    directory.removeAllowedFormat(".doc");
    assertEquals(5, directory.getAllowedFormats().size());
  }

  public VersionableDirectoryGITImpl testShouldStartWatchDog()
      throws IOException, InterruptedException {
    final List<Object> returnedObjects = testShouldCommitNewFiles();
    final List<File> committedFiles = (List<File>) returnedObjects.get(0);
    final VersionableDirectoryGITImpl directory =
        (VersionableDirectoryGITImpl) returnedObjects.get(1);

    // enable test mode
    directory.setTestMode(true);

    // start watch dog
    final long interval = 100;
    assertTrue("failed to start watch dog", directory.startWatchDog(interval));

    // change the first file and wait for the update
    final File file1 = committedFiles.get(0);
    changeFileContent(file1, System.currentTimeMillis() + " changed - ");
    verifyWhetherACommitWasPerformed(Arrays.asList(file1), directory, interval);

    // change 2 files together and wait for the update
    final File file2 = committedFiles.get(1);
    final File file3 = committedFiles.get(2);
    changeFileContent(file2, System.currentTimeMillis() + " changed - ");
    changeFileContent(file3, System.currentTimeMillis() + " changed - ");
    verifyWhetherACommitWasPerformed(
        Arrays.asList(file2, file3), directory, interval);

    return directory;
  }

  private void verifyWhetherACommitWasPerformed(
      final List<File> pFiles, final VersionableDirectoryGITImpl pDirectory,
      final long pInterval) throws InterruptedException {

    Thread.sleep(pInterval * 2);
    final List<List<File>> changes =
        pDirectory.getLastCommitHistories();

    mLogger.debug(changes);
    assertFalse(changes.isEmpty());

    // retrieve the last change
    final List<File> lastChanges = changes.get(changes.size() - 1);
    assertNotNull(lastChanges);
    mLogger.debug("Last changes - " + lastChanges);
    for (final File file : pFiles) {
      assertTrue(lastChanges.contains(file));
    }
  }

  public void testShouldStopWatchDog()
      throws IOException, InterruptedException {

    final VersionableDirectoryGITImpl directory = testShouldStartWatchDog();
    assertTrue(directory.isWatchDogRunning());
    assertTrue(directory.stopWatchDog());
    Thread.sleep(100);
    assertFalse(directory.isWatchDogRunning());
  }

  public List<Object> testShouldReturnAllAvailableRevisions()
      throws IOException, InterruptedException {

    final VersionableDirectoryGITImpl directory = testShouldStartWatchDog();
    final List<Revision> revisions = directory.findAllRevisions();
    mLogger.debug(revisions);

    assertNotNull(revisions);
    assertFalse(revisions.isEmpty());

    // iterate through each revision
    for (final Revision revision : revisions) {
      assertRevisionObject(revision);
    }

    return Arrays.asList(revisions, directory);
  }

  private void assertRevisionObject(final Revision pRevision) {
    System.out.println("");
    System.out.println("Commit " + pRevision.getCommitId());
    System.out.println("Author: " + buildUserName(pRevision.getAuthor()));
    System.out.println("Committer: " + buildUserName(pRevision.getCommitter()));
    System.out.println("");
    System.out.println(pRevision.getMessage());
    System.out.println("");
    assertNotNull(pRevision.getCommitId());
    assertNotNull(pRevision.getAuthor());
    assertNotNull(pRevision.getCommitter());
    assertNotNull(pRevision.getMessage());
    assertNotNull(pRevision.getDate());
    assertNotNull(pRevision.getRevisionDataTree());
    final RevisionDataTree dataTree = pRevision.getRevisionDataTree();
    while (dataTree.next()) {
      final File file = dataTree.getFile();
      mLogger.debug("file name - " + file);
      mLogger.debug("Exists - " + file.exists());
    }
  }

  private String buildUserName(final User pAuthor) {
    return pAuthor.getName() + "<" + pAuthor.getEmail() + ">";
  }

  public void testShouldRevertToASpecificCommit()
      throws IOException, InterruptedException {

    final List<Object> returnedObjects = testShouldReturnAllAvailableRevisions();
    List<Revision> revisions = (List<Revision>) returnedObjects.get(0);
    final VersionableDirectoryGITImpl directory =
        (VersionableDirectoryGITImpl) returnedObjects.get(1);

    // retrieve file from a specific revision
    final File firstFile = directory.getWorkingDirectory().listFiles(new FileFilter() {
      public boolean accept(final File pFile) {
        return !pFile.isDirectory() && directory.isAllowedFileType(pFile);
      }
    })[0];

    // change locally
    changeFileContent(firstFile, "\nagain\n");

    // commit changes
    final List<File> changedFiles = directory.checkFileSystem();
    mLogger.debug("Changed files - " + changedFiles);
    assertTrue(changedFiles.contains(firstFile));

    // checkout data from a specific commit
    final String commitId = revisions.get(0).getCommitId();
    byte[] data = directory.
        checkoutFile(commitId, firstFile.getName());
    mLogger.debug("Commit id - " + commitId);
    mLogger.debug(data);
    mLogger.debug(new String(data));

  }
}
