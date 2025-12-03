package org.abratuhi.kafka.connect.git;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class GitSourceConfig extends AbstractConfig {
    public static final String SOURCE_GITREPO_DIR = "gitrepo.dir";
    public static final String TARGET_TOPIC_NAME = "topic.name";

    public GitSourceConfig(ConfigDef definition, Map<?, ?> originals) {
        super(definition, originals);
    }

    public static ConfigDef config() {
        return new ConfigDef()
                .define(SOURCE_GITREPO_DIR, ConfigDef.Type.STRING, "/tmp", ConfigDef.Importance.HIGH, "Git repository directory")
                .define(TARGET_TOPIC_NAME, ConfigDef.Type.STRING, "commits", ConfigDef.Importance.HIGH, "Kafka topic name")
                ;
    }
}
