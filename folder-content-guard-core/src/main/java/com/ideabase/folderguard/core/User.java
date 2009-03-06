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
import java.util.TimeZone;

/**
 * User related information
 * @author <a href="http://hasan.we4tech.com">nhm tanveer...(hasan)</a>
 */
public class User {

  private String mName;
  private String mEmail;
  private Date mWhen;
  private TimeZone mTimeZone;

  public String getName() {
    return mName;
  }

  public User setName(final String pName) {
    mName = pName;
    return this;
  }

  public String getEmail() {
    return mEmail;
  }

  public User setEmail(final String pEmail) {
    mEmail = pEmail;
    return this;
  }

  public Date getWhen() {
    return mWhen;
  }

  public User setWhen(final Date pWhen) {
    mWhen = pWhen;
    return this;
  }

  public TimeZone getTimeZone() {
    return mTimeZone;
  }

  public User setTimeZone(final TimeZone pTimeZone) {
    mTimeZone = pTimeZone;
    return this;
  }
}
