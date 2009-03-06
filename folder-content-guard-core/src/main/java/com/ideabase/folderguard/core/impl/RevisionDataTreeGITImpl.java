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

import com.ideabase.folderguard.core.RevisionDataTree;
import org.spearce.jgit.revwalk.RevCommit;
import org.spearce.jgit.treewalk.TreeWalk;
import org.spearce.jgit.treewalk.filter.TreeFilter;
import org.spearce.jgit.lib.*;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * GiT based revision data tree implementation
 * @author <a href="http://hasan.we4tech.com">nhm tanveer...(hasan)</a>
 */
public class RevisionDataTreeGITImpl implements RevisionDataTree {

  private final RevCommit mRevisionCommit;
  private final Repository mRepository;
  private TreeWalk mTreeWalk;
  private int mOffset = 0;

  public RevisionDataTreeGITImpl(final Repository pRepository,
                                 final RevCommit pRevCommit) {
    mRevisionCommit = pRevCommit;
    mRepository = pRepository;

    init();
  }

  private void init() {
    mTreeWalk = new TreeWalk(mRepository);
    mTreeWalk.setFilter(TreeFilter.ANY_DIFF);
    try {
      final List<ObjectId> parentIds = new ArrayList<ObjectId>();
      for (final RevCommit revCommit : mRevisionCommit.getParents()) {
        parentIds.add(revCommit.getTree());
      }
      parentIds.add(mRevisionCommit.getTree());
      mTreeWalk.reset(parentIds.toArray(new ObjectId[] {}));
    } catch (IOException e) {
      throw new RuntimeException("failed to reset tree walker", e);
    }
  }

  public void reset() {
    mOffset = 0;
  }

  public boolean next() {
    try {
      boolean state = mTreeWalk.next();
      if (state) {
        if (mTreeWalk.getFileMode(mTreeWalk.getTreeCount() - 1).getObjectType() != Constants.OBJ_BLOB) {
          if (mTreeWalk.isSubtree()) {
            mTreeWalk.enterSubtree();
          }
          return next();
        }
      }
      return state;
    } catch (IOException e) {
      throw new RuntimeException("failed to move on next tree", e);
    }
  }

  public File getFile() {
    return new File(mRepository.getWorkDir(), mTreeWalk.getPathString());
  }
}
