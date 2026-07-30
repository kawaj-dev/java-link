package com.javalink.model;

/**
 * クイズの選択肢を表します。
 *
 * @param id   回答を区別するための値
 * @param text 画面に表示する選択肢
 */
public record QuizOption(String id, String text) {
}
