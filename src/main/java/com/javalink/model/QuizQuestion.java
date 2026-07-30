package com.javalink.model;

import java.util.List;

/**
 * Java Linkの問題1問分を表します。
 *
 * 問題を増やすときは、この形のデータをQuizServiceへ追加します。
 *
 * @param id              問題を区別するための値
 * @param codeBefore      学習部分より前のコード
 * @param targetCode      今回強調するコード
 * @param codeAfter       学習部分より後のコード
 * @param prompt          質問文
 * @param options         回答の選択肢
 * @param correctOptionId 正解となる選択肢のID
 */
public record QuizQuestion(
        String id,
        String codeBefore,
        String targetCode,
        String codeAfter,
        String prompt,
        List<QuizOption> options,
        String correctOptionId
) {
}
