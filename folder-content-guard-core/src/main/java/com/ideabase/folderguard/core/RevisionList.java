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
 * Hold the list of revisions of the specified file
 * @author <a href="http://hasan.we4tech.com">nhm tanveer...(hasan)</a>
 */
public class RevisionList {

  private File mFile;
  private List<Revision> mRevisions;

  public File getFile() {
    return mFile;
  }

  public void setFile(final File pFile) {
    mFile = pFile;
  }

  public List<Revision> getRevisions() {
    return mRevisions;
  }

  public void setRevisions(final List<Revision> pRevisions) {
    mRevisions = pRevisions;
  }
}
