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

/**
 * Traverse through the data tree.
 * @author <a href="http://hasan.we4tech.com">nhm tanveer...(hasan)</a>
 */
public interface RevisionDataTree {

  /**
   * Reset internal iterator state to the top.
   */
  void reset();

  /**
   * Return {@code true} if iterator has more data.
   * @return {@code true} if iterator has more data.
   */
  boolean next();

  /**
   * Return file properties
   * @return file properties
   */
  File getFile();
}
