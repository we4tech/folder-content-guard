package com.ideabase.folderguard.core;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.spearce.jgit.lib.*;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * expertimenting GIT for the basic support
 */
public class GitTest extends TestCase {

  // files
  private final List<File> mFiles = Arrays.asList(
      new File("test-dir" + File.separator + "test.txt")
  );


  public void testShouldCreateAGitEnvironment() throws IOException {
    // repository path
    final File repositroyPath = new File("test-dir" + File.separator + ".git");
    System.out.println("Absolute repo path - " + repositroyPath.getAbsolutePath());

    // create new GIT repository
    final Repository repository = openRepository(repositroyPath);

    // get head branch
    final String head = repository.getFullBranch();
    System.out.println("Full branch - " + head);
    if (head.startsWith("refs/heads/")) {
      System.out.println("Current branch - " + repository.getBranch());
    }

    // git index
    final GitIndex index = repository.getIndex();

    // first tree
    final Tree headTree = openHeadTree(repository);

    // object writer
    final ObjectWriter objectWriter = new ObjectWriter(repository);

    // first commit
    commitContent(index, headTree, "first commit", objectWriter, repository);

    // traverse through all references
    System.out.println("Refs - " + repository.getAllRefs());

    // find git index
    if (index != null) {
      for (final GitIndex.Entry entry : index.getMembers()) {
        System.out.println("Entry - " + entry.getName() + ":" + entry.getObjectId().name());
      }
    } else {
      System.out.println("No git index found");
    }

    // close repository
    repository.close();
  }

  private void commitContent(final GitIndex pIndex,
                             final Tree pHeadTree,
                             final String pMessage,
                             final ObjectWriter pObjectWriter,
                             final Repository pRepository)
      throws IOException {
    final String fileName = "test.txt";
    final FileTreeEntry gitFile;
    boolean exists = false;
    if (!pHeadTree.existsBlob(fileName)) {
      gitFile = pHeadTree.addFile(fileName);
    } else {
      System.out.println("Already exists - " + pHeadTree.members());
      gitFile = (FileTreeEntry) pHeadTree.findBlobMember(fileName);
      System.out.println("Exists - " + gitFile);
      exists = true;
    }

    gitFile.setExecutable(false);
    final ObjectId objectId = pObjectWriter.writeBlob(readFile("test.txt"));
    gitFile.setId(objectId);
    pObjectWriter.writeTree(pHeadTree);
    pIndex.readTree(pHeadTree);
    pIndex.write();

    pHeadTree.setId(pIndex.writeTree());

    // previouse commit reference
    final List<ObjectId> parents = new ArrayList<ObjectId>();

    // retrieve existing tree
    final ObjectId existingTreeId =
        pRepository.resolve(org.spearce.jgit.lib.Constants.HEAD);
    if (existingTreeId != null) {
      parents.add(existingTreeId);
    }
    System.out.println("parents - " + parents);

    // commit the changes
    final Commit commit = new Commit(pRepository, parents.toArray(new ObjectId[] {}));
    commit.setTree(pHeadTree);
    System.out.println("ThreadId - " + pHeadTree.getId());
    commit.setAuthor(new PersonIdent("hasan", "hasan83bd@gmail.com"));
    commit.setCommitter(new PersonIdent("hasan", "hasan83bd@gmail.com"));
    commit.setMessage("second commit");
    final ObjectId commitId = pObjectWriter.writeCommit(commit);
    System.out.println("Commit id - " + commitId);

    // update reference
    final RefUpdate refUpdate =
        pRepository.updateRef(org.spearce.jgit.lib.Constants.HEAD);
    refUpdate.setNewObjectId(commitId);
    refUpdate.setRefLogMessage(commit.getMessage(), false);
    final RefUpdate.Result result = refUpdate.forceUpdate();
    System.out.println("Result - " + result);
  }

  private byte[] readFile(final String pFileName)
      throws IOException {
    final File file = mFiles.get(0);
    FileInputStream fileInputStream = new FileInputStream(file);
    byte[] data = new byte[fileInputStream.available()];
    fileInputStream.read(data);
    return data;
  }

  private Tree openHeadTree(final Repository pRepository) throws IOException {
    final Tree tree = pRepository.mapTree(
        org.spearce.jgit.lib.Constants.HEAD);
    if (tree == null) {
      return new Tree(pRepository);
    }
    return tree;
  }

  private Repository openRepository(final File pRepositroyPath)
      throws IOException {
    final Repository repository = new Repository(pRepositroyPath);
    if (!pRepositroyPath.exists()) {
      repository.create();
    }

    return repository;
  }
}
