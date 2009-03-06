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

import java.util.Date;

/**
 * Hold all revision related information
 * @author <a href="http://hasan.we4tech.com">nhm tanveer...(hasan)</a>
 */
public class Revision {

  private String mCommitId;
  private Date mDate;
  private String mMessage;
  private User mAuthor;
  private User mCommitter;
  private RevisionDataTree mRevisionDataTree;

  public String getCommitId() {
    return mCommitId;
  }

  public Revision setCommitId(final String pCommitId) {
    mCommitId = pCommitId;
    return this;
  }

  public Date getDate() {
    return mDate;
  }

  public Revision setDate(final Date pDate) {
    mDate = pDate;
    return this;
  }

  public String getMessage() {
    return mMessage;
  }

  public Revision setMessage(final String pMessage) {
    mMessage = pMessage;
    return this;
  }

  public User getAuthor() {
    return mAuthor;
  }

  public Revision setAuthor(final User pAuthor) {
    mAuthor = pAuthor;
    return this;
  }

  public User getCommitter() {
    return mCommitter;
  }

  public Revision setCommitter(final User pCommitter) {
    mCommitter = pCommitter;
    return this;
  }

  public RevisionDataTree getRevisionDataTree() {
    return mRevisionDataTree;
  }

  public Revision setRevisionDataTree(final RevisionDataTree pRevisionDataTree)
  {
    mRevisionDataTree = pRevisionDataTree;
    return this;
  }
}
