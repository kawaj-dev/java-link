package com.javalink.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 1つの学習進捗に対する、完了済みStepを表します。
 *
 * レコードが存在すること自体をStep完了として扱います。
 */
@Entity
@Table(
        name = "learning_step_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_learning_step_progress_progress_step",
                columnNames = {"learning_progress_id", "step_id"}
        )
)
public class LearningStepProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "learning_progress_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_learning_step_progress_learning_progress"
            )
    )
    private LearningProgress learningProgress;

    @Column(name = "step_id", nullable = false, length = 100)
    private String stepId;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    public Long getId() {
        return id;
    }

    public LearningProgress getLearningProgress() {
        return learningProgress;
    }

    public void setLearningProgress(LearningProgress learningProgress) {
        this.learningProgress = learningProgress;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    @PrePersist
    public void setCompletionTimestamp() {
        completedAt = LocalDateTime.now();
    }
}
