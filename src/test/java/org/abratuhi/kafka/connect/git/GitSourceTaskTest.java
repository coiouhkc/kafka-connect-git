package org.abratuhi.kafka.connect.git;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.connect.source.SourceRecord;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GitSourceTaskTest {

  private GitSourceTask gitSourceTask;

  private Path repodir;
  private Git git;

  @BeforeEach
  void setUp() throws IOException, GitAPIException {
    gitSourceTask = new GitSourceTask();

    repodir = Files.createTempDirectory("testdir");
    git = Git.init().setDirectory(repodir.toFile()).call();
    git.add().addFilepattern(".").call();
    git.commit().setMessage("Initial commit").call();
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(git.getRepository().getDirectory().toPath().toFile());
  }

  @Test
  void testGitSourceTask() throws IOException, GitAPIException, InterruptedException {

    Path testfile1 = Files.createTempFile(repodir, "testfile1-", ".txt");
    git.add().addFilepattern(".").call();
    git.commit().setMessage("Add " + testfile1.getFileName().toString()).call();

    gitSourceTask.start(
        Map.of(
            GitSourceConfig.SOURCE_GITREPO_DIR,
            git.getRepository().getDirectory().toPath().toAbsolutePath().toString(),
            GitSourceConfig.TARGET_TOPIC_NAME,
            "commits"));

    List<SourceRecord> poll = gitSourceTask.poll();

    assertThat(poll).isNotNull();
    assertThat(poll).hasSize(2);

    assertThat(poll.get(0).topic()).isEqualTo("commits");
    assertThat(poll.get(1).topic()).isEqualTo("commits");

    poll = gitSourceTask.poll();
    assertThat(poll).isNotNull();
    assertThat(poll).isEmpty();

    Path testfile2 = Files.createTempFile(repodir, "testfile2-", ".txt");
    git.add().addFilepattern(".").call();
    git.commit().setMessage("Add " + testfile2.getFileName().toString()).call();

    poll = gitSourceTask.poll();
    assertThat(poll).isNotNull();
    assertThat(poll).hasSize(1);
    assertThat(poll.get(0).topic()).isEqualTo("commits");

    gitSourceTask.stop();
  }
}
