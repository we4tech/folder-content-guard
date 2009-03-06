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

/**
 * FolderVersioningService is used as the central API to access through
 * all possible functionalities.
 *
 * @author <a href="http://hasan.we4tech.com">nhm tanveer...(hasan)</a>
 */
public interface DirectoryVersioningService {

  /**
   * Create new instance of object which is used for managing revision
   * inside the specified directory.
   *
   * @param pDirectory specific the parent directory
   * @return {@code VersionableFolder} instance.
   */
  VersionableDirectory findVersionableDirectory(final File pDirectory);

}
