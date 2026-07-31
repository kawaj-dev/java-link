package com.javalink.service;

import com.javalink.model.CodeReadingFlowState;
import com.javalink.model.CodeReadingPhase;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 教材ごとの導入・学習・まとめ画面をHTTPセッションで管理します。
 */
@Service
public class CodeReadingFlowService {

    public static final String CODE_READING_FLOW_MAP =
            "CODE_READING_FLOW_MAP";

    private final LessonService lessonService;

    public CodeReadingFlowService(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    public CodeReadingFlowState getState(
            HttpSession session,
            String lessonId
    ) {
        validate(session, lessonId);
        return getStateMap(session).computeIfAbsent(
                lessonId,
                CodeReadingFlowState::initial
        );
    }

    public CodeReadingFlowState startLearning(
            HttpSession session,
            String lessonId
    ) {
        return updatePhase(session, lessonId, CodeReadingPhase.LEARNING);
    }

    public CodeReadingFlowState showSummary(
            HttpSession session,
            String lessonId
    ) {
        return updatePhase(session, lessonId, CodeReadingPhase.SUMMARY);
    }

    public CodeReadingFlowState reset(
            HttpSession session,
            String lessonId
    ) {
        validate(session, lessonId);
        CodeReadingFlowState initial = CodeReadingFlowState.initial(lessonId);
        getStateMap(session).put(lessonId, initial);
        return initial;
    }

    private CodeReadingFlowState updatePhase(
            HttpSession session,
            String lessonId,
            CodeReadingPhase phase
    ) {
        validate(session, lessonId);
        CodeReadingFlowState state = new CodeReadingFlowState(lessonId, phase);
        getStateMap(session).put(lessonId, state);
        return state;
    }

    private void validate(HttpSession session, String lessonId) {
        Objects.requireNonNull(session, "session must not be null");
        lessonService.getLesson(lessonId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, CodeReadingFlowState> getStateMap(
            HttpSession session
    ) {
        Object stored = session.getAttribute(CODE_READING_FLOW_MAP);
        if (stored == null) {
            Map<String, CodeReadingFlowState> states = new LinkedHashMap<>();
            session.setAttribute(CODE_READING_FLOW_MAP, states);
            return states;
        }
        if (!(stored instanceof Map<?, ?>)) {
            throw new IllegalStateException(
                    "セッションのコード読解画面状態が正しいMap形式ではありません。"
            );
        }
        return (Map<String, CodeReadingFlowState>) stored;
    }
}
