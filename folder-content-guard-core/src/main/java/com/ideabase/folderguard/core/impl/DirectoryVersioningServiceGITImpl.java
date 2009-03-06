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

import com.ideabase.folderguard.core.DirectoryVersioningService;
import com.ideabase.folderguard.core.VersionableDirectory;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

/**
 * Git based folder version service implementation
 * @author <a href="http://hasan.we4tech.com">nhm tanveer...(hasan)</a>
 */
public class DirectoryVersioningServiceGITImpl
    implements DirectoryVersioningService {

  private final Logger mLogger = LogManager.getLogger(getClass());
  private final Map<String, VersionableDirectory> mVersionableDirectories =
      new HashMap<String, VersionableDirectory>();

  public VersionableDirectory findVersionableDirectory(final File pDirectory) {
    // verify the directory if not exists throw an invalid argument error
    if (pDirectory == null || !pDirectory.exists()) {
      throw new IllegalArgumentException(
          "please specific a valid directory which is already exists.");
    }

    // lookup an existing instance of the given directory
    VersionableDirectory versionableDirectory =
        mVersionableDirectories.get(pDirectory.getAbsolutePath());

    // if existing instance not found
    if (versionableDirectory == null) {
      // create a new GIT implemented versioinable directory
      versionableDirectory = new VersionableDirectoryGITImpl(pDirectory);
    }

    return versionableDirectory;
  }
}
