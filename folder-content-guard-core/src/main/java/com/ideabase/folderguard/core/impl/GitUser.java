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

import com.ideabase.folderguard.core.User;
import org.spearce.jgit.lib.PersonIdent;

/**
 * GIT User representation
 * @author <a href="http://hasan.we4tech.com">nhm tanveer...(hasan)</a>
 */
public class GitUser extends User {

  private final PersonIdent mPersonIdent;

  /**
   * Default constructor
   * @param pPersonIdent git user
   */
  public GitUser(final PersonIdent pPersonIdent) {
    mPersonIdent = pPersonIdent;
    transformProperties();
  }

  private void transformProperties() {
    if (mPersonIdent != null) {
      setName(mPersonIdent.getName())
      .setEmail(mPersonIdent.getEmailAddress())
      .setWhen(mPersonIdent.getWhen())
      .setTimeZone(mPersonIdent.getTimeZone());
    }
  }
}
