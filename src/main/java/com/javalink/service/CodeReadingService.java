package com.javalink.service;

import com.javalink.model.CodeReadingCodeLine;
import com.javalink.model.CodeReadingCodeLineDefinition;
import com.javalink.model.CodeReadingCodeToken;
import com.javalink.model.CodeReadingItem;
import com.javalink.model.CodeReadingLessonDefinition;
import com.javalink.model.CodeReadingPart;
import com.javalink.model.CodeReadingStepDefinition;
import com.javalink.model.LessonProgress;
import com.javalink.model.QuizOption;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 教材定義と進捗から、コードリーディング画面の表示データを作ります。 */
@Service
public class CodeReadingService {

    private final CodeReadingLessonCatalog lessonCatalog;

    public CodeReadingService(CodeReadingLessonCatalog lessonCatalog) {
        this.lessonCatalog = lessonCatalog;
    }

    /** 教材の登録順に、正解カードと説明を返します。 */
    public List<CodeReadingItem> createItems(
            String lessonId,
            LessonProgress progress
    ) {
        return definition(lessonId).steps().stream()
                .filter(CodeReadingStepDefinition::required)
                .map(step -> createItem(step, progress))
                .toList();
    }

    /**
     * 現在の正解1枚と、意味が重複しない誤答3枚を返します。
     * 将来の1枚カード方式では、現在項目の正解カードをそのまま利用できます。
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

    /** 指定したPartに含まれる項目だけを教材の登録順で返します。 */
    public List<CodeReadingItem> createPartItems(
            String lessonId,
            LessonProgress progress,
            CodeReadingPart part
    ) {
        Set<String> stepIds = Set.copyOf(part.stepIds());
        return createItems(lessonId, progress).stream()
                .filter(item -> stepIds.contains(item.stepId()))
                .map(item -> new CodeReadingItem(
                        item.stepId(),
                        part.displayTokenFor(item.stepId()),
                        item.optionId(),
                        item.meaning(),
                        item.roleLabel(),
                        item.explanations(),
                        item.order(),
                        item.completed(),
                        item.current()
                ))
                .toList();
    }

    /** Part固有のHTML分岐を使わずに描画できるコード行を返します。 */
    public List<CodeReadingCodeLine> createCodeLines(
            String lessonId,
            LessonProgress progress,
            CodeReadingPart part
    ) {
        Map<String, CodeReadingItem> items = createPartItems(
                lessonId,
                progress,
                part
        ).stream().collect(Collectors.toMap(
                CodeReadingItem::stepId,
                Function.identity()
        ));
        List<CodeReadingCodeLineDefinition> definitions = definition(lessonId)
                .codeLinesByPart()
                .get(part.id());
        return definitions.stream()
                .map(line -> new CodeReadingCodeLine(
                        line.tokens().stream()
                                .map(token -> new CodeReadingCodeToken(
                                        items.get(token.stepId()),
                                        token.prefix(),
                                        token.suffix()
                                ))
                                .toList(),
                        line.trailingCode(),
                        line.cssClass()
                ))
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
            CodeReadingStepDefinition step,
            LessonProgress progress
    ) {
        return new CodeReadingItem(
                step.id(),
                step.displayLabel(),
                step.correctCard().id(),
                step.correctCard().text(),
                step.technicalTerm(),
                step.beginnerExplanations(),
                step.order(),
                progress.isStepCompleted(step.id()),
                progress.getCurrentStepId().equals(step.id())
        );
    }

    private CodeReadingLessonDefinition definition(String lessonId) {
        return lessonCatalog.getDefinition(lessonId);
    }
}
