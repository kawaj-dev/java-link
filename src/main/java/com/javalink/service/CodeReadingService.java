package com.javalink.service;

import com.javalink.model.CodeReadingItem;
import com.javalink.model.CodeReadingStage;
import com.javalink.model.CodeReadingStageState;
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

    private static final List<CodeReadingStage> HELLO_STAGES = List.of(
            new CodeReadingStage(
                    "stage-1",
                    1,
                    "Mainクラスを作る",
                    "public class Main {",
                    List.of(
                            "class-public",
                            "class-keyword",
                            "class-name",
                            "class-open"
                    ),
                    "外から使えるMainというクラスを作り、ここからクラスの中身を始めます。"
            ),
            new CodeReadingStage(
                    "stage-2",
                    2,
                    "プログラムの開始地点を作る",
                    "public static void main(String[] args) {",
                    List.of(
                            "main-public",
                            "static",
                            "void",
                            "main",
                            "string-array",
                            "args",
                            "main-open"
                    ),
                    "Javaが最初に実行するmainメソッドを作り、文字列の配列をargsという名前で受け取ります。"
            ),
            new CodeReadingStage(
                    "stage-3",
                    3,
                    "「Hello」を表示する",
                    "System.out.println(\"Hello\");",
                    List.of(
                            "system",
                            "system-dot",
                            "out",
                            "out-dot",
                            "println",
                            "hello-string",
                            "semicolon"
                    ),
                    "Javaの出力機能を使って、「Hello」と表示して改行します。"
            )
    );

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
     * 現在の正解カードと、未完了カードから選んだ1枚を返します。
     * 不正解直後は、選ばれた誤答カードを残して同じ2択を作ります。
     */
    public List<QuizOption> createSelectionOptions(
            String lessonId,
            LessonProgress progress
    ) {
        List<CodeReadingItem> incompleteItems =
                itemsForSelection(lessonId, progress).stream()
                        .filter(item -> !item.completed())
                        .toList();
        CodeReadingItem currentItem = incompleteItems.stream()
                .filter(CodeReadingItem::current)
                .findFirst()
                .orElse(null);

        if (currentItem == null) {
            return List.of();
        }

        List<CodeReadingItem> otherItems = incompleteItems.stream()
                .filter(item -> !item.stepId().equals(currentItem.stepId()))
                .filter(item -> !item.meaning().equals(currentItem.meaning()))
                .toList();
        CodeReadingItem otherItem = findPreviousIncorrectItem(
                otherItems,
                progress
        );
        if (otherItem == null && !otherItems.isEmpty()) {
            List<CodeReadingItem> candidates =
                    new ArrayList<>(otherItems);
            Collections.shuffle(candidates);
            otherItem = candidates.get(0);
        }

        List<QuizOption> options = new ArrayList<>();
        options.add(toOption(currentItem));
        if (otherItem != null) {
            options.add(toOption(otherItem));
        }
        Collections.shuffle(options);
        return List.copyOf(options);
    }

    public List<CodeReadingStage> getStages() {
        return HELLO_STAGES;
    }

    public CodeReadingStage getStageForStep(String stepId) {
        return HELLO_STAGES.stream()
                .filter(stage -> stage.stepIds().contains(stepId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "ステージが見つかりません。stepId: " + stepId
                ));
    }

    public List<CodeReadingItem> createCurrentStageItems(
            String lessonId,
            LessonProgress progress
    ) {
        CodeReadingStage stage =
                getStageForStep(progress.getCurrentStepId());
        Set<String> stepIds = Set.copyOf(stage.stepIds());
        return createItems(lessonId, progress).stream()
                .filter(item -> stepIds.contains(item.stepId()))
                .toList();
    }

    public boolean isStageCompleted(
            CodeReadingStage stage,
            LessonProgress progress
    ) {
        return stage.stepIds().stream()
                .allMatch(progress.getCompletedStepIds()::contains);
    }

    public boolean isLastStage(CodeReadingStage stage) {
        return stage.order() == HELLO_STAGES.size();
    }

    public List<CodeReadingStageState> createStageStates(
            LessonProgress progress
    ) {
        CodeReadingStage current =
                getStageForStep(progress.getCurrentStepId());
        return HELLO_STAGES.stream()
                .map(stage -> new CodeReadingStageState(
                        stage,
                        stage.id().equals(current.id()),
                        isStageCompleted(stage, progress)
                ))
                .toList();
    }

    private List<CodeReadingItem> itemsForSelection(
            String lessonId,
            LessonProgress progress
    ) {
        if (LessonService.HELLO_PROGRAM_LESSON_ID.equals(lessonId)) {
            return createCurrentStageItems(lessonId, progress);
        }
        return createItems(lessonId, progress);
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
