package org.abratuhi.kafka.connect.git;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.kafka.connect.source.SourceRecord;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

class GitSourceTaskTest {

  private GitSourceTask gitSourceTask = new GitSourceTask();

  @Test
  void testGitSourceTask() throws IOException, GitAPIException, InterruptedException {
    Path testdir = Files.createTempDirectory("testdir");
    try (Git git = Git.init().setDirectory(testdir.toFile()).call()) {
      git.add().setAll(true).call();
      git.commit().setMessage("Initial commit").call();
      Path testfile1 = Files.createTempFile(testdir, "testfile1-", ".txt");
      git.add().setAll(true).call();
      git.commit().setMessage("Add " + testfile1.getFileName().toString()).call();
    }

    gitSourceTask.start(
        Map.of(
            GitSourceConfig.SOURCE_GITREPO_DIR,
            testdir.toAbsolutePath().toString(),
            GitSourceConfig.TARGET_TOPIC_NAME,
            "commits"));

    List<SourceRecord> poll = gitSourceTask.poll();

    assertThat(poll).isNotNull();
    assertThat(poll.size()).isEqualTo(2);

    assertThat(poll.get(0).topic()).isEqualTo("commits");
    assertThat(poll.get(1).topic()).isEqualTo("commits");

    gitSourceTask.stop();
  }
}
