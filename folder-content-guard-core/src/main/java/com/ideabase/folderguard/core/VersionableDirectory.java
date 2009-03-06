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

package com.ideabase.folderguard.core;

import java.io.File;
import java.util.Map;
import java.util.List;

/**
 * Keep and manage version for each files (of the specified pattern)
 * of the specified directory.<br>
 * this object is used for each separate parent directory.
 *
 * @author <a href="http://hasan.we4tech.com">nhm tanveer...(hasan)</a>
 */
public interface VersionableDirectory {

  /**
   * Return the working directory which is now under versioning support
   * @return working direcotry which is now under versioning support
   */
  File getWorkingDirectory();

  /**
   * {@code true} if already versioning support is enabled.
   * @return {@code true} if already versioning support is enabled.
   */
  boolean isVersionable();

  /**
   * Add allowed file format to keep under version control.
   * @param pFormat file format ie, .doc, .txt etc.
   * @return instance of the original object so we can use method chaining
   *         to make it for friendly code.
   */
  VersionableDirectory addAllowedFormat(final String pFormat);

  /**
   * List of all allowed file formats (by default, .doc, and .txt is used)
   * @return list of allowed file formats
   */
  List<String> getAllowedFormats();

  /**
   * Remove Allowed file format. this method chaining can be performed.
   * @param pFormat file format. ie. .doc, .txt etc.
   * @return self instance is returning to help method chaning.
   */
  VersionableDirectory removeAllowedFormat(final String pFormat);

  /**
   * Return the name of the backend version control system.
   * @return then name of the backend version control system.
   */
  String getVersionControlSystemName();

  /**
   * Start content change watching service, which will preiodically watch
   * the content and their respective timestamp, if any change occured it
   * would store a new version.
   *
   * @param pInterval interval between each watch.
   * @return {@code true} if successfully started
   */
  boolean startWatchDog(final long pInterval);

  /**
   * To stop watch dog which was watching file system for any changes.
   * @return {@code true} if successfully stopped.
   */
  boolean stopWatchDog();

  /**
   * Verify whether an existing watch dog is running.
   * @return {@code true} if already a watch dog is running.
   */
  boolean isWatchDogRunning();

  /**
   * Return the list of files which their list of revisions.
   * @return {@code List} of file and revision list of the respective files,
   *         empty map will be returned if nothing found.
   */
  List<Revision> findAllRevisions();

  /**
   * Return the list of revisions of the specified file.
   * @param pFile physical file
   * @return {@code RevisionList} of the specified file. empty array will be
   *         returned if nothing found.
   */
  List<RevisionList> findFileRevisions(final File pFile);

  /**
   * check out the specific file from the specific commit id
   * @param pCommitId commit id, if {@code null} is specified file will
   *        checked out from the HEAD
   * @param pFileName name of the file which has to be checked out
   * @return file data in {@code byte[]} array
   */
  byte[] checkoutFile(final String pCommitId, final String pFileName);
}
