package com.javalink.model;

import java.util.Objects;

/**
 * 左側の完成コードへ、現在・完了状態を渡す表示用データです。
 */
public record CodeReadingStageState(
        CodeReadingStage stage,
        boolean current,
        boolean completed
) {

    public CodeReadingStageState {
        Objects.requireNonNull(stage, "stage must not be null");
    }
}
