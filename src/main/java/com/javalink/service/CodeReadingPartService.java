package com.javalink.service;

import com.javalink.model.CodeReadingPart;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 「Hello」と表示するプログラムを、意味のまとまりごとに管理します。
 */
@Service
public class CodeReadingPartService {

    private static final List<CodeReadingPart> HELLO_PARTS = List.of(
            new CodeReadingPart(
                    "part-1",
                    1,
                    "クラスを作る",
                    "public class Main {",
                    List.of(
                            "class-public",
                            "class-keyword",
                            "class-name",
                            "class-open"
                    ),
                    List.of(
                            "{ は、Mainクラスの中身がここから始まることを表します。"
                    ),
                    "外から使えるMainクラスを作る"
            ),
            new CodeReadingPart(
                    "part-2",
                    2,
                    "mainメソッドを作る",
                    "public static void main",
                    List.of("main-public", "static", "void", "main"),
                    List.of(
                            "public static void mainを、Javaが最初に実行する入口として読みます。"
                    ),
                    "Javaが最初に実行するmainメソッド"
            ),
            new CodeReadingPart(
                    "part-3",
                    3,
                    "mainメソッドが受け取る情報",
                    "(String[] args) {",
                    List.of("string-array", "args", "main-open"),
                    List.of(
                            "( ) は、mainメソッドが受け取る情報を書く場所です。",
                            "String[] は文字列の配列です。",
                            "args は受け取った値につける名前です。",
                            "{ は、mainメソッドの中身がここから始まることを表します。"
                    ),
                    "文字列の配列をargsという名前で受け取る"
            ),
            new CodeReadingPart(
                    "part-4",
                    4,
                    "「Hello」を表示する",
                    "System.out.println(\"Hello\");",
                    List.of("print-command", "hello-string", "semicolon"),
                    List.of(
                            "System.out.println(...) は、かっこの中の内容を画面へ表示して改行します。",
                            ". は、左側のものが持つ機能へ順番につなぐ記号です。",
                            "( ) の中には、表示したい内容を書きます。"
                    ),
                    "Helloと表示して改行する"
            ),
            new CodeReadingPart(
                    "part-5",
                    5,
                    "コードのまとまりを閉じる",
                    "}\n}",
                    List.of("main-close", "class-close"),
                    List.of(
                            "最初の } でmainメソッドを閉じます。",
                            "最後の } でMainクラスを閉じます。"
                    ),
                    "mainメソッドとMainクラスを順番に終了する"
            )
    );

    public List<CodeReadingPart> getParts() {
        return HELLO_PARTS;
    }

    public CodeReadingPart getPart(String partId) {
        return HELLO_PARTS.stream()
                .filter(part -> part.id().equals(partId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Partが見つかりません。partId: " + partId
                ));
    }

    public CodeReadingPart getPartForStep(String stepId) {
        return HELLO_PARTS.stream()
                .filter(part -> part.stepIds().contains(stepId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "ステップに対応するPartが見つかりません。stepId: "
                                + stepId
                ));
    }

    public boolean isLastPart(CodeReadingPart part) {
        return part.order() == HELLO_PARTS.size();
    }
}
