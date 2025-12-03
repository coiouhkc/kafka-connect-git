package org.abratuhi.kafka.connect.git;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

public class GitSourceTask extends SourceTask {
  private final Queue<GitSourceCommitChange> changes = new ConcurrentLinkedQueue<>();

  private String gitRepoDir;
  private String targetTopicName;

  @Override
  public void start(Map<String, String> props) {
    this.gitRepoDir = props.get(GitSourceConfig.SOURCE_GITREPO_DIR);
    this.targetTopicName = props.get(GitSourceConfig.TARGET_TOPIC_NAME);

    try (Git git = Git.open(new File(gitRepoDir));
        ObjectReader reader = git.getRepository().newObjectReader();
        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE); ) {
      diffFormatter.setRepository(git.getRepository());
      Iterator<RevCommit> it = git.log().call().iterator();
      while (it.hasNext()) {
        RevCommit rc = it.next();

        List<DiffEntry> changes =
            git.diff()
                .setOldTree(
                    rc.getParentCount() > 0
                        ? new CanonicalTreeParser(null, reader, rc.getParent(0).getTree().getId())
                        : null)
                .setNewTree(new CanonicalTreeParser(null, reader, rc.getTree().getId()))
                .call();

        for (DiffEntry change : changes) {
          EditList el = diffFormatter.toFileHeader(change).toEditList();
          List<Integer> deletedAndInserted =
              el.stream()
                  .map(edit -> List.of(edit.getLengthA(), edit.getLengthB()))
                  .reduce(
                      List.of(0, 0),
                      (l1, l2) -> List.of(l1.get(0) + l2.get(0), l1.get(1) + l2.get(1)));
          int deleted = deletedAndInserted.get(0);
          int inserted = deletedAndInserted.get(1);

          this.changes.add(
              new GitSourceCommitChange(
                  rc.getId().getName(),
                  rc.getShortMessage(),
                  rc.getAuthorIdent().getEmailAddress(),
                  rc.getCommitterIdent().getEmailAddress(),
                  Instant.ofEpochSecond(rc.getCommitTime()),
                  Instant.ofEpochSecond(rc.getCommitTime()),
                  change.getOldPath(),
                  change.getNewPath(),
                  inserted,
                  deleted,
                  change.getChangeType().name()));
        }
      }
    } catch (IOException | GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
    List<SourceRecord> result = new ArrayList<>();
    while (!changes.isEmpty()) {
      GitSourceCommitChange change = changes.poll();
      result.add(new SourceRecord(null, null, targetTopicName, null, change));
    }
    return result;
  }

  @Override
  public void stop() {
      changes.clear();
  }

  @Override
  public String version() {
    return Version.get();
  }
}
