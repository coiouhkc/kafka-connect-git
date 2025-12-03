package org.abratuhi.kafka.connect.git;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
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
  private final Queue<Struct> changes = new ConcurrentLinkedQueue<>();

  private String gitRepoDir;
  private String targetTopicName;
  private Schema valueSchema;

  @Override
  public void start(Map<String, String> props) {
    this.gitRepoDir = props.get(GitSourceConfig.SOURCE_GITREPO_DIR);
    this.targetTopicName = props.get(GitSourceConfig.TARGET_TOPIC_NAME);

    this.valueSchema =
        SchemaBuilder.struct()
            .name("GitSourceCommitChange")
            .field("commitId", SchemaBuilder.STRING_SCHEMA)
            .field("shortMessage", SchemaBuilder.STRING_SCHEMA)
            .field("authorEmail", SchemaBuilder.STRING_SCHEMA)
            .field("committerEmail", SchemaBuilder.STRING_SCHEMA)
            .field("authoredAt", SchemaBuilder.INT32_SCHEMA)
            .field("committedAt", SchemaBuilder.INT32_SCHEMA)
            .field("oldPath", SchemaBuilder.STRING_SCHEMA)
            .field("newPath", SchemaBuilder.STRING_SCHEMA)
            .field("linesInserted", SchemaBuilder.INT32_SCHEMA)
            .field("linesDeleted", SchemaBuilder.INT32_SCHEMA)
            .field("changeType", SchemaBuilder.STRING_SCHEMA)
            .build();

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
              new Struct(valueSchema)
                  .put("commitId", rc.getId().getName())
                  .put("shortMessage", rc.getShortMessage())
                  .put("authorEmail", rc.getAuthorIdent().getEmailAddress())
                  .put("committerEmail", rc.getCommitterIdent().getEmailAddress())
                  .put("authoredAt", rc.getCommitTime())
                  .put("committedAt", rc.getCommitTime())
                  .put("oldPath", change.getOldPath())
                  .put("newPath", change.getNewPath())
                  .put("linesInserted", inserted)
                  .put("linesDeleted", deleted)
                  .put("changeType", change.getChangeType().name()));
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
      Struct change = changes.poll();
      result.add(new SourceRecord(null, null, targetTopicName, valueSchema, change));
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
