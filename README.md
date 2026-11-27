# Java Link

> **初心者の今しか生まれない「わからない」を教材に。**
>
> **― 学びながら開発する、開発しながら学ぶ ―**

<p align="center">
  <img src="docs/images/README_header.png" alt="Java Link" width="900">
</p>


Java Linkは、Javaを学び始めて2週間目に開発を始めたWebアプリです。

自身が学習する中で感じた課題を解決するために、学習と並行して開発を始めました。

初心者の今だからこそ感じる「わからない」を大切な視点と捉え、その感覚が薄れてしまう前に教材設計に取り入れ、**教材を作る過程を通して自身の学習課題も解決していきます。**

Java Linkは、開発者自身が初心者である「今」だからこそ作れる教材を目指しています。

---

# 教材設計

## ① 説明は変わらない

Java Linkでは、「今はここだけ覚えれば大丈夫」といった学習段階に応じた説明ではなく、最初から最後まで同じ説明を使います。

**説明は変わりません。**

**変わるのは、学習者の理解です。**

学習を進めるほど、同じ説明から読み取れる内容が増え、見える景色が変わっていきます。

---

## ② 用語は何度でも確認できる

一度出てきた用語を覚えた前提で学習を進めることはしません。

思い出したいときに、いつでも、すぐに、何度でも確認できる教材を目指しています。

何度も忘れ、コードの中で出てくるたびに思い出すことを繰り返すことで、用語同士がつながり、点だった知識が線になっていくと考えています。

---

## ③ 用語の説明にはJavaの公式資料のみ参照する

初心者にわかりやすいことだけでなく、説明の正確性も大切にしています。

用語の説明では、Javaの公式ドキュメントに限定して参照しています。

NotebookLMのソースにはJava Language Specification（JLS）およびJava APIドキュメントのみを使用し、**公式資料で根拠を確認できた内容のみを記載しています。**

サイト上では、各用語に根拠となる公式ドキュメントへのリンクを記載しています。

### 公式資料との対応例

**Java Language Specification（JLS）**

<p align="center">
  <img src="docs/images/jls-reference.png" alt="Java Language Specificationを根拠として表示している画面" width="900">
</p>

**Java SE API**

<p align="center">
  <img src="docs/images/api-reference.png" alt="Java SE APIを根拠として表示している画面" width="900">
</p>
---

# Java Linkの画面

<p align="center">
  <img src="docs/images/java-link-home.png" alt="Java Link トップ画面" width="900">
</p>

---


# 学習モード

## コードを左から読む

Javaコードを左から順番に読み進めながら、それぞれの部分の意味や役割を理解し、そのつながりからJava全体の仕組みを学ぶ学習モードです。

### 学習の流れ

#### ① 最初にコード全体を見る

学習を始める前に、これから読むプログラム全体を確認します。

<p align="center">
  <img src="docs/images/code-reading-start.png" alt="学習開始時にプログラム全体を確認する画面" width="900">
</p>

**気になるコード行から学習を始めることもできます。**

- コード行をクリックすると、対応するPartへ直接移動
- 最初から順番に学ぶことも、確認したい部分から学ぶことも可能



<p align="center">
  <img src="docs/images/code-reading-part-jump.png" alt="Stage 1のコード行を選択して対応するPartへ移動できる画面" width="900">
</p>

#### ② コードを左からひとつずつ読み進める

問題に答える形式ではなく、コードの下に並ぶボタンを押しながら、public や class などの意味や役割を順番に確認していきます。

<p align="center">
  <img src="docs/images/code-reading-before.png" alt="コードを左から順番に読み始める画面" width="900">
</p>

ボタンを押すと、その部分の意味が表示されて💡が点灯し、用語についての説明も表示されます。

<p align="center">
  <img src="docs/images/code-reading-public.png" alt="publicを押して意味を確認する画面" width="900">
</p>

用語の説明では、関連する知識に加えて、説明の根拠となるJava公式ドキュメントまで確認できます。

<p align="center">
  <img src="docs/images/code-reading-reference.png" alt="publicの詳しい説明とJava公式ドキュメントの技術的根拠を確認する画面" width="900">
</p>

#### ③ 読み終えたコード全体を確認する

すべての部分を読み終えると、完成したプログラム全体と、それぞれの意味をまとめて確認できます。

<p align="center">
  <img src="docs/images/code-reading-before-run.png" alt="読み終えたプログラム全体と実行ボタンを確認する画面" width="900">
</p>

#### ④ 実行結果をシミュレーションで確認する

最後に「Run」ボタンを押すと、教材に定義された実行結果がTERMINALに表示され、読んできたコードがどのような結果になるのかを確認できます。現段階ではJavaコードを実際にコンパイル・実行するのではなく、JavaScriptでコンパイルから結果表示までの流れを視覚的に表現する学習用シミュレーションです。

<p align="center">
  <img src="docs/images/code-reading-complete.png" alt="実行シミュレーションの結果をTERMINALで確認する画面" width="900">
</p>

### Java Linkでできるようになること

- 初めて見るコードでも、左からひとつずつ意味を追いながら読み進められる
- コードの一部分だけでなく、全体の流れを捉えられる
- 繰り返し登場するコードに触れることで、その意味や役割を自然に身につけられる
- すべての部分に説明があることで、「わからないところが出るたびに学習を中断して調べる」という負担を減らしながら学習を進められる


---

# 現在の実装状況

## コードを左から読む

- ✅ Stage 1「Hello」と表示するプログラムを読めるようになろう
- ✅ Stage 2 変数を使って年齢を表示しよう
- ✅ Stage 3 変数を使った計算を読もう
- ✅ Stage 4 信号を見て行動を決めよう
- ✅ Stage 5 繰り返し処理を使って数字を表示しよう
- ✅ Stage 6 配列を使って複数の数字を表示しよう
- ✅ Stage 7 メソッドを使って計算結果を表示しよう
- ✅ Stage 8 クラスから2台の車を作ってみよう
- ✅ Stage 9 コンストラクタを使って車の色を設定しよう
- ✅ Stage 10 メソッドを使って車の色を変更しよう

Stage 4では、`if`・`else`による条件分岐と、`String.equals()`による文字列の内容比較を学びます。9 Part・33 Stepで構成し、クラス宣言から最後の閉じ波括弧まで、完成コードを省略せず左から順番に確認できます。

Stage 8では、`Car`クラスから2つのインスタンスを生成し、それぞれが異なるフィールド値を持つことや、インスタンスメソッドを呼び出して値を表示する流れを学びます。

Stage 9では、コンストラクタを使ったインスタンスの初期化と、`this`によるフィールドと仮引数の区別を学びます。

Stage 10では、`private`によるフィールドへの直接アクセスの制限と、getter・setterを通じたフィールド値の取得・変更を学びます。1つの`Car`インスタンスの色をredからblueへ変更するプログラムを通して、カプセル化の考え方を学びます。

学習目次ではStageごとの学習状況と進捗率を確認できます。Stage 1〜10のINTRO画面では、完成コードの行をマウスまたはキーボードで選択し、対応するPartへ移動できます。移動しても学習済みStepと💡の点灯状態は保持されます。

学習画面の「Javaことば図鑑」では、`class`、フィールド、メソッド、`public`、`static`、`void`、`main`、`String[]`、`args`、`private`、getter、setterなどの用語を検索し、説明とJava公式資料へのリンクを確認できます。

## DB・ログイン・学習進捗の保存・復元

ユーザー登録からログイン、学習進捗のPostgreSQLへの保存・復元までを実装しています。

- ✅ PostgreSQL / Spring Data JPAによるユーザー情報・学習進捗の永続化

- ✅ メールアドレスとパスワードによるユーザー登録・ログイン

- ✅ BCryptによるパスワードのハッシュ化と、メールアドレスの重複防止

- ✅ ログイン成功時にユーザーIDだけをHttpSessionへ保存し、必要なときにユーザー情報を取得

- ✅ Lesson単位の現在位置・完了状態と、完了済みStepの保存

- ✅ 正解直後のStep詳細、Step移動後の現在位置、Lesson完了時の完了状態をDBへ同期

- ✅ 未ログインの場合は、従来どおりHttpSessionだけで学習可能

- ✅ DBに保存した現在Stepと完了済みStepを復元し、前回の位置と💡の点灯状態から学習を再開

- ✅ 対象Stageと全Stageの学習進捗リセット

### 学習進捗のDB構造

`learning_progress`は、ユーザーとLessonの組み合わせごとに現在位置とLessonの完了状態を管理する親進捗です。`learning_step_progress`は親進捗に紐づく完了済みStepを管理し、レコードが存在することをStep完了として扱います。

ユーザーとLesson、親進捗とStepの組み合わせにはそれぞれUNIQUE制約を設け、同じ進捗やStepが重複して登録されないようにしています。親進捗とStep詳細は同一トランザクションで保存します。

```mermaid
erDiagram
    user_account ||--o{ learning_progress : "学習する"
    learning_progress ||--o{ learning_step_progress : "完了Stepを持つ"

    user_account {
        bigint id PK
        varchar display_name
        varchar email UK
        varchar password_hash
        timestamp created_at
    }

    learning_progress {
        bigint id PK
        bigint user_id FK
        varchar lesson_id
        varchar current_step_id
        boolean completed
        integer step_progress_version
        timestamp updated_at
    }

    learning_step_progress {
        bigint id PK
        bigint learning_progress_id FK
        varchar step_id
        timestamp completed_at
    }
```

### ログインから学習進捗の保存・復元まで

```mermaid
flowchart LR
    Login[ログイン]
    Session[ユーザーIDを<br/>HttpSessionへ保存]
    Learning[学習]
    Correct[正解したStepを完了]
    Progress[Step移動後の現在位置<br/>またはLesson完了]
    DB[(PostgreSQL)]
    SessionProgress{HttpSessionに<br/>進捗があるか}
    Restore[現在Stepと完了済みStepを復元]
    Resume[前回の位置と<br/>💡の点灯状態から再開]

    Login --> Session
    Session --> Learning
    Learning --> Correct
    Correct -->|Step詳細を同期| DB
    Correct --> Progress
    Progress --> DB
    Session --> SessionProgress
    SessionProgress -->|あり・セッション優先| Resume
    SessionProgress -->|なし| DB
    DB --> Restore
    Restore --> Resume
```

ログイン済みユーザーについては、HttpSessionにある学習状態を優先し、セッションに進捗が残っていない場合にDBから現在Stepと完了済みStepを復元します。未ログインユーザーはDBを使用せず、HttpSessionだけで学習できます。

対象Stageのリセットでは、ログイン済みユーザーのセッション進捗と対象LessonのDB進捗をリセットします。全Stageのリセットにも対応し、他ユーザーや対象外Lessonの進捗には影響を与えません。未ログインユーザーはセッション進捗だけをリセットします。

一時的な回答内容、選択肢、不正解履歴、Run済み状態はDBへ保存していません。また、「続きから／最初から」を選択するUI、ログアウト、アクセス制御、Spring SecurityのSecurityContextやSecurityFilterChainによる正式な認証・認可は未実装です。現在使用しているSpring Securityの機能は、パスワードのハッシュ化に利用する`spring-security-crypto`です。

---

# アプリケーション構成

Java Linkは、Spring Bootを使用したWebアプリケーションです。

Javaコードは役割ごとに `controller`、`service`、`model`、`entity`、`repository`、`config` に分けています。

- **Controller**：ブラウザからのリクエストを受け取り、Serviceの処理や画面表示につなぐ
- **Service**：学習進行、回答処理、進捗管理など、役割ごとに処理を分担する
- **Model**：教材、学習ステップ、進捗、画面表示などで使用するデータを表現する
- **Entity**：ユーザー情報と永続化する学習進捗を表現する
- **Repository**：Spring Data JPAを利用してPostgreSQLへの保存・取得を行う
- **Config**：パスワードのハッシュ化など、アプリケーションで共通して使用する設定を管理する
- **Templates**：Thymeleafを使用して画面を表示する
- **Static**：CSS、JavaScript、画像などの静的ファイルを配置する

「コードを左から読む」では、Controllerが受け取った操作をServiceへ渡し、
学習進行を管理するServiceから、進捗管理・回答処理・画面状態などを担当するクラスへ処理を分担する構成としています。

---

# 開発で実装した内容

Java Linkでは、学習の進行や画面表示を実現するために、次の機能や仕組みを実装しています。

* **Spring BootによるWebアプリケーション構築**
  ブラウザからアクセスして学習を進められるWebアプリとして構築しています。

* **Controller / Service / Modelによる処理の分担**
  ブラウザからの入力、学習処理、進捗管理、教材データなどを役割ごとに分けて実装しています。

* **Thymeleafによる画面表示**
  Java側で管理している教材や学習状態をHTMLへ渡し、学習画面に反映しています。

* **HTTPセッションを利用した学習進捗管理**
  現在のStep、完了したStep、回答状態などをセッションに保存し、学習途中の状態を管理しています。

* **Spring Data JPAによるデータ永続化**
  PostgreSQLへユーザー情報、Lesson単位の現在位置・完了状態、完了済みStepを保存しています。メールアドレス、ユーザーとLesson、親進捗とStepの組み合わせはDBのUNIQUE制約でも重複を防いでいます。

* **ユーザー登録・ログイン処理**
  パスワードは `spring-security-crypto` のBCryptでハッシュ化して保存し、ログイン時はメールアドレスとパスワードを照合します。ログイン成功後はUserAccountのIDだけをHttpSessionに保持します。

* **学習フローとDB進捗の保存・復元の連携**
  ログイン済みユーザーが正解した直後に完了済みStepを同期し、Step移動後には更新後のStep IDを、Lesson完了時には最終Step IDと完了状態をDBへ保存します。HttpSessionに進捗がない場合は、現在Stepと完了済みStepをDBから復元し、💡の点灯状態を再現します。未ログインユーザーはHttpSessionだけで学習を続けられます。

* **学習進捗のリセット**
  Stage単独と全Stageのリセットに対応しています。ログイン済みユーザーはセッションとDBの進捗を、未ログインユーザーはセッション進捗だけを初期化します。

* **JavaScriptによる学習画面の動的な更新**
  回答結果に応じた電球の点灯、説明・進捗表示の更新、次のStepの有効化に加え、SUMMARY画面ではコンパイルから固定の実行結果表示までの流れを学習用シミュレーションとして視覚的に表現しています。教材コード自体のコンパイル・実行は行いません。

* **コード行からのPartジャンプ機能**  
  Stage 1〜10のINTRO画面でコード行と学習Partを対応付け、選択した行から該当Partの先頭Stepへ移動できます。マウス・Tab・Enter・Spaceによる操作に対応し、学習済みStepや💡の点灯状態を保持したまま移動できます。

* **Stage別の学習状況と進捗率の表示**
  学習目次で各Stageの未学習・学習中・学習完了の状態と、完了済みStepに基づく進捗率を確認できます。

* **Javaことば図鑑**
  学習画面からJava用語を検索し、用語の説明とJava公式資料へのリンクを確認できます。

* **SUMMARY画面の完成コード表示**
  完成コードと各部分の意味をStage共通の構造で表示しています。行番号とコード本文をCSS Gridの同じ行に配置し、完成コードから行番号を自動生成することで、空白行やインデントを保ちながら構造的に位置をそろえています。

* **Mavenによるビルド・テスト**
  Mavenを利用してアプリケーションの実行やテストを行っています。

* **Git / GitHubによるバージョン管理**
  変更内容をGitで記録し、GitHubでソースコードを管理しています。

* **ブランチとPull Requestを利用した開発**
  mainブランチを直接編集せず、作業ごとにブランチを作成し、Pull Requestで差分を確認してからmainへ反映しています。

---

# テスト

教材定義、学習フロー、画面遷移、INTRO画面のPartジャンプ、学習進捗の保存・復元・リセット、ユーザー登録・ログイン、SUMMARY画面などを対象に、Controller・Service・Repositoryのテストを実装しています。Repositoryと永続化のテストにはH2を使用し、PostgreSQLの実データへ影響を与えずに検証できる構成としています。

PR #83時点で確認済みのテスト実績は次のとおりです。今回のREADME更新に伴って再実行した結果ではありません。

- Tests: 301
- Failures: 0
- Errors: 0
- Skipped: 0

---

# 使用技術

- Java 17
- Spring Boot 3.5.4
- Thymeleaf
- HTML
- CSS
- JavaScript
- PostgreSQL
- Spring Data JPA
- Hibernate
- H2（テスト）
- BCrypt（spring-security-crypto）
- Maven
- Git
- GitHub

---

# AIツールの活用

- **ChatGPT**：設計の検討、コード理解、教材内容の整理に活用
- **Codex**：実装・コードレビュー・テストなどの開発支援に活用
- **NotebookLM**：Java Language Specification（JLS）およびJava APIドキュメントをソースとして、教材説明の根拠確認に活用

Codexで作成・変更したファイルは、差分とテスト結果を確認したうえで反映しています。

実装されたコードは、意味のまとまりごとに区切り、Java文法、処理の内容、アプリ内での役割を確認しながら学習記録を残しています。

教材内容については、AIの回答そのものを根拠にはせず、NotebookLMのソースをJava Language Specification（JLS）とJava APIドキュメントに限定し、公式資料で根拠を確認できた内容を教材に採用しています。

---

# 開発環境

- VS Code
- JDK 17

---

# 今後の展開

- 「最初から／続きから」を選択するUI
- ログアウト・アクセス制御の追加
- Spring Securityによる正式な認証状態管理への移行
- コードを組み立てながら理解する学習機能への発展
- 学習Stage・教材内容の充実
- UI / UX の改善
- テストの充実
- README・設計資料の整備

---

