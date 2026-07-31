package com.javalink.service;

import com.javalink.model.CodeReadingItem;
import com.javalink.model.CodeReadingPart;
import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import com.javalink.model.QuizOption;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 「コードを左から読む」画面と復習画面の表示データを作ります。
 */
@Service
public class CodeReadingService {

    private static final Map<String, ReadingNote> READING_NOTES =
            Map.ofEntries(
            Map.entry("public", new ReadingNote(
                    "アクセス修飾子",
                    List.of(
                            "外から使えます。",
                            "他のクラスから呼び出せます。"
                    )
            )),
            Map.entry("class-public", new ReadingNote(
                    "アクセス修飾子",
                    List.of("ほかのクラスから使えるようにします。")
            )),
            Map.entry("main-public", new ReadingNote(
                    "アクセス修飾子",
                    List.of("Javaから呼び出せる開始地点にします。")
            )),
            Map.entry("class-keyword", new ReadingNote(
                    "クラス宣言のキーワード",
                    List.of("プログラムの設計図を作るときに使います。")
            )),
            Map.entry("class-name", new ReadingNote(
                    "クラス名",
                    List.of("このクラスにつけた名前です。")
            )),
            Map.entry("class-open", new ReadingNote(
                    "開始波かっこ",
                    List.of("クラスの中身がここから始まります。")
            )),
            Map.entry("static", new ReadingNote(
                    "static修飾子",
                    List.of(
                            "インスタンスを作らなくても使えます。",
                            "newしなくても呼び出せます。"
                    )
            )),
            Map.entry("void", new ReadingNote(
                    "戻り値",
                    List.of("戻り値を返しません。")
            )),
            Map.entry("main", new ReadingNote(
                    "メソッド名",
                    List.of("Javaはここから実行を始めます。")
            )),
            Map.entry("string-array", new ReadingNote(
                    "引数の型",
                    List.of("文字列の配列です。")
            )),
            Map.entry("args", new ReadingNote(
                    "引数名",
                    List.of("受け取った値につける名前です。")
            )),
            Map.entry("main-open", new ReadingNote(
                    "開始波かっこ",
                    List.of("mainメソッドの中身がここから始まります。")
            )),
            Map.entry("print-command", new ReadingNote(
                    "画面へ表示する命令",
                    List.of(
                            "かっこの中の内容を画面へ表示して改行します。",
                            "ドットは左側のものが持つ機能へ順番につなぎます。"
                    )
            )),
            Map.entry("system", new ReadingNote(
                    "クラス名",
                    List.of("Javaが最初から用意している機能をまとめたクラスです。")
            )),
            Map.entry("system-dot", new ReadingNote(
                    "メンバーアクセス演算子",
                    List.of("Systemの中からoutを使います。")
            )),
            Map.entry("out", new ReadingNote(
                    "staticフィールド",
                    List.of("画面へ文字を出すために使います。")
            )),
            Map.entry("out-dot", new ReadingNote(
                    "メンバーアクセス演算子",
                    List.of("outが持っている機能を使います。")
            )),
            Map.entry("println", new ReadingNote(
                    "インスタンスメソッド",
                    List.of("受け取った内容を表示し、最後に改行します。")
            )),
            Map.entry("hello-string", new ReadingNote(
                    "引数",
                    List.of("printlnに渡す文字列です。")
            )),
            Map.entry("semicolon", new ReadingNote(
                    "セミコロン",
                    List.of("この命令がここで終わることを表します。")
            )),
            Map.entry("main-close", new ReadingNote(
                    "終了波かっこ",
                    List.of("mainメソッドの中身がここで終わります。")
            )),
            Map.entry("class-close", new ReadingNote(
                    "終了波かっこ",
                    List.of("Mainクラスの中身がここで終わります。")
            ))
    );

    private final LessonService lessonService;
    private final QuizService quizService;

    public CodeReadingService(
            LessonService lessonService,
            QuizService quizService
    ) {
        this.lessonService = lessonService;
        this.quizService = quizService;
    }

    /**
     * 教材の登録順に、正解カードと復習説明を返します。
     */
    public List<CodeReadingItem> createItems(
            String lessonId,
            LessonProgress progress
    ) {
        Lesson lesson = lessonService.getLesson(lessonId);

        return lesson.steps().stream()
                .filter(LessonStep::required)
                .map(step -> createItem(step, progress))
                .toList();
    }

    /**
     * 現在の正解1枚と、意味が重複しない誤答3枚を返します。
     * 不正解直後は選んだ誤答を残して同じ問題を考え直せます。
     */
    public List<QuizOption> createSelectionOptions(
            String lessonId,
            LessonProgress progress
    ) {
        List<CodeReadingItem> readingItems = createItems(lessonId, progress);
        CodeReadingItem currentItem = readingItems.stream()
                .filter(CodeReadingItem::current)
                .findFirst()
                .orElse(null);

        if (currentItem == null) {
            return List.of();
        }

        List<CodeReadingItem> otherItems = readingItems.stream()
                .filter(item -> !item.stepId().equals(currentItem.stepId()))
                .filter(item -> !item.meaning().equals(currentItem.meaning()))
                .toList();
        CodeReadingItem previousIncorrectItem = findPreviousIncorrectItem(
                otherItems,
                progress
        );
        List<QuizOption> options = new ArrayList<>();
        options.add(toOption(currentItem));
        if (previousIncorrectItem != null) {
            options.add(toOption(previousIncorrectItem));
        }
        List<CodeReadingItem> candidates = new ArrayList<>(otherItems);
        candidates.remove(previousIncorrectItem);
        Collections.shuffle(candidates);
        candidates.stream()
                .limit(4 - options.size())
                .map(this::toOption)
                .forEach(options::add);
        Collections.shuffle(options);
        return List.copyOf(options);
    }

    /**
     * 指定したPartに含まれる項目だけを教材の登録順で返します。
     */
    public List<CodeReadingItem> createPartItems(
            String lessonId,
            LessonProgress progress,
            CodeReadingPart part
    ) {
        Set<String> stepIds = Set.copyOf(part.stepIds());
        return createItems(lessonId, progress).stream()
                .filter(item -> stepIds.contains(item.stepId()))
                .toList();
    }

    private CodeReadingItem findPreviousIncorrectItem(
            List<CodeReadingItem> otherItems,
            LessonProgress progress
    ) {
        if (!progress.isAnswered() || progress.isCorrect()) {
            return null;
        }
        return otherItems.stream()
                .filter(item -> item.optionId().equals(
                        progress.getSelectedOptionId()
                ))
                .findFirst()
                .orElse(null);
    }

    private QuizOption toOption(CodeReadingItem item) {
        return new QuizOption(item.optionId(), item.meaning());
    }

    private CodeReadingItem createItem(
            LessonStep step,
            LessonProgress progress
    ) {
        QuizOption correctOption =
                quizService.getCorrectOption(step.question());
        ReadingNote note = READING_NOTES.get(step.id());
        if (note == null) {
            throw new IllegalStateException(
                    "読み方の説明が見つかりません。stepId: " + step.id()
            );
        }

        return new CodeReadingItem(
                step.id(),
                step.displayLabel(),
                correctOption.id(),
                correctOption.text(),
                note.roleLabel(),
                note.explanations(),
                step.order(),
                progress.isStepCompleted(step.id()),
                progress.getCurrentStepId().equals(step.id())
        );
    }

    private record ReadingNote(
            String roleLabel,
            List<String> explanations
    ) {
    }
}
