package org.abratuhi.kafka.connect.git;

import java.util.List;
import java.util.Map;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

public class GitSourceConnector extends SourceConnector {

  private String gitRepoDir;
  private String targetTopicName;

  @Override
  public void start(Map<String, String> props) {
    this.gitRepoDir = props.get(GitSourceConfig.SOURCE_GITREPO_DIR);
    this.targetTopicName = props.get(GitSourceConfig.TARGET_TOPIC_NAME);
  }

  @Override
  public Class<? extends Task> taskClass() {
    return GitSourceTask.class;
  }

  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks) {
    return List.of(
        Map.of(
            GitSourceConfig.SOURCE_GITREPO_DIR,
            this.gitRepoDir,
            GitSourceConfig.TARGET_TOPIC_NAME,
            this.targetTopicName));
  }

  @Override
  public void stop() {}

  @Override
  public ConfigDef config() {
    return GitSourceConfig.config();
  }

  @Override
  public String version() {
    return Version.get();
  }
}
