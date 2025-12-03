package org.abratuhi.kafka.connect.git;

import java.time.Instant;

public class GitSourceCommitChange {
    private String commitId;
    private String shortMessage;
    private String authorEmail;
    private String committerEmail;
    private Instant authoredAt;
    private Instant committedAt;
    private String oldPath;
    private String newPath;
    private Integer linesInserted;
    private Integer linesDeleted;
    private String changeType;

    public GitSourceCommitChange() {
    }

    public GitSourceCommitChange(String commitId, String shortMessage, String authorEmail, String committerEmail, Instant authoredAt, Instant committedAt, String oldPath, String newPath, Integer linesInserted, Integer linesDeleted, String changeType) {
        this.commitId = commitId;
        this.shortMessage = shortMessage;
        this.authorEmail = authorEmail;
        this.committerEmail = committerEmail;
        this.authoredAt = authoredAt;
        this.committedAt = committedAt;
        this.oldPath = oldPath;
        this.newPath = newPath;
        this.linesInserted = linesInserted;
        this.linesDeleted = linesDeleted;
        this.changeType = changeType;
    }

    public Integer getLinesDeleted() {
        return linesDeleted;
    }

    public void setLinesDeleted(Integer linesDeleted) {
        this.linesDeleted = linesDeleted;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getCommitterEmail() {
        return committerEmail;
    }

    public void setCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail;
    }

    public Instant getAuthoredAt() {
        return authoredAt;
    }

    public void setAuthoredAt(Instant authoredAt) {
        this.authoredAt = authoredAt;
    }

    public Instant getCommittedAt() {
        return committedAt;
    }

    public void setCommittedAt(Instant committedAt) {
        this.committedAt = committedAt;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public String getNewPath() {
        return newPath;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public Integer getLinesInserted() {
        return linesInserted;
    }

    public void setLinesInserted(Integer linesInserted) {
        this.linesInserted = linesInserted;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }
}
