# Java Link DB導入・ログイン・学習進捗保存 設計

## 1. 目的

現在のJava Linkでは、学習中の進捗をHTTPセッションで管理している。

HTTPセッションはブラウザを閉じたりセッションが終了したりすると失われるため、次回利用時に前回の続きから学習することができない。

第一段階では、利用者を識別するログイン機能とデータベースを導入し、

**利用者ごとの学習進捗を保存し、次回ログイン後も前回学習していたStepから再開できる**

ことを目的とする。

---

## 2. 第一段階の実装範囲

第一段階では、次の機能を実装する。

| 機能 | 第一段階 |
| --- | --- |
| ユーザー登録 | ○ |
| メールアドレス＋パスワードによるログイン | ○ |
| ログアウト | ○ |
| パスワードのハッシュ化 | ○ |
| 利用者ごとの学習進捗保存 | ○ |
| 前回のStepからの再開 | ○ |
| Lesson完了状態の保存 | ○ |
| 「最初からやり直す」による進捗リセット | ○ |
| Googleなどの外部ログイン | × |
| MFA（多要素認証） | × |
| パスワード再設定 | × |
| アカウント編集 | × |
| 個々の💡状態の永続化 | × |
| 回答履歴の永続化 | × |
| 詳細な学習履歴 | ×（将来対応） |

第一段階では、必要以上に認証機能を広げず、

**ユーザー登録 → ログイン → 学習 → 保存 → 次回再開**

までを完成させる。

---

## 3. 使用技術

| 用途 | 技術 |
| --- | --- |
| アプリケーション | Spring Boot |
| 認証・認可 | Spring Security |
| DB | PostgreSQL |
| 永続化 | Spring Data JPA |
| ORM | Hibernate（Spring Data JPA経由） |
| パスワード保存 | Spring Security PasswordEncoder |
| 画面 | Thymeleaf |
| 学習中の一時状態 | HttpSession |
| テスト | JUnit / Spring Boot Test |
| テスト用DB | H2 |

PostgreSQLを実際の永続化DBとして使用する。

アプリケーションからSQLを直接大量に記述する方式ではなく、Spring Data JPAを利用する。

---

## 4. ログイン状態と学習進捗管理

Java Linkの学習自体は、ログインしていない利用者でも使用できるようにする。

ログイン状態によって、進捗管理方法を切り替える。

| 利用状態 | 進捗管理 |
| --- | --- |
| 未ログイン | HTTPセッションのみ |
| ログイン済み | HTTPセッション ＋ DB |

未ログインの場合は、現在と同じようにHTTPセッション内で学習を進める。

ログイン済みの場合は、学習中の細かな状態はHTTPセッションで管理し、次回利用時にも必要な進捗だけDBへ保存する。

これにより、

**Java Linkを試すだけならログイン不要**

としながら、

**継続して学習する利用者はログインすると進捗を保存できる**

構成とする。

---

## 5. ユーザー登録

### 5.1 登録項目

利用者が入力する項目は次の3つとする。

| 項目 | 内容 |
| --- | --- |
| 表示名 | Java Link上で使用する名前 |
| メールアドレス | ログインID |
| パスワード | ログイン用パスワード |

内部ユーザーIDは利用者には入力させない。

---

## 6. usersテーブル

### 6.1 テーブル構造

```text
users
------------------------------------------------
id
display_name
email
password_hash
created_at
```

具体的な設計は次のとおりとする。

| カラム | PostgreSQL | Java | 制約 |
| --- | --- | --- | --- |
| id | BIGINT | Long | PRIMARY KEY / 自動生成 |
| display_name | VARCHAR(100) | String | NOT NULL |
| email | VARCHAR(255) | String | NOT NULL / UNIQUE |
| password_hash | VARCHAR(255) | String | NOT NULL |
| created_at | TIMESTAMP | LocalDateTime | NOT NULL |

`updated_at` は、アカウント編集機能を実装するときに必要性を再検討する。

---

## 7. ユーザーID

`id` はJava Link内部で利用者を識別するためのIDとする。

例：

```text
id = 15
```

利用者自身が入力する値ではなく、DB側で自動生成する。

学習進捗はメールアドレスではなく、この内部IDと関連付ける。

```text
users.id
    ↓
learning_progress.user_id
```

メールアドレスが将来変更可能になっても、内部ユーザーIDを基準にすることで学習進捗との関連を維持できる。

---

## 8. メールアドレス

メールアドレスをログインIDとして使用する。

同じメールアドレスで複数のアカウントは作成できない。

保存時には、メールアドレスの表記ゆれによって重複登録が起きないように扱う。

具体的な正規化方法については、実装時にSpring Securityおよびユーザー登録処理との整合性を確認して決定する。

---

## 9. パスワード

パスワードをそのままDBへ保存しない。

利用者が入力したパスワードをSpring Securityの `PasswordEncoder` で変換し、

```text
password_hash
```

へ保存する。

DBには平文パスワードを保持しない。

また、ログやエラーメッセージにもパスワードを出力しない。

---

## 10. ログイン処理

基本的な流れは次のとおりとする。

```text
ログイン画面
    ↓
メールアドレス・パスワード入力
    ↓
Spring Security
    ↓
メールアドレスからユーザーを検索
    ↓
PasswordEncoderでパスワード確認
    ↓
認証成功
    ↓
ログイン状態をHTTPセッションで管理
```

認証処理そのものを独自実装するのではなく、Spring Securityの仕組みを利用する。

---

## 11. ログアウト

ログアウト時にはSpring Securityのログアウト機能を使用する。

ログアウトすると認証状態とHTTPセッション内の一時的な学習状態は終了する。

ただし、DBへ保存済みの学習進捗は削除しない。

そのため、次回ログイン後も学習を再開できる。

---

## 12. 学習進捗の考え方

Java Linkでは、

**1ユーザー × 1Lesson = 1学習進捗レコード**

とする。

例：

```text
ユーザー15
 ├─ Stage1 → 1レコード
 ├─ Stage2 → 1レコード
 └─ Stage3 → 1レコード
```

同じユーザー・同じLessonに複数の現在進捗レコードを作らない。

---

## 13. learning_progressテーブル

```text
learning_progress
------------------------------------------------
id
user_id
lesson_id
current_step_id
completed
updated_at
```

設計は次のとおりとする。

| カラム | PostgreSQL | Java | 制約 |
| --- | --- | --- | --- |
| id | BIGINT | Long | PRIMARY KEY / 自動生成 |
| user_id | BIGINT | Long | NOT NULL / FK |
| lesson_id | VARCHAR(100) | String | NOT NULL |
| current_step_id | VARCHAR(100) | String | NULL可 |
| completed | BOOLEAN | boolean | NOT NULL |
| updated_at | TIMESTAMP | LocalDateTime | NOT NULL |

さらに、

```text
UNIQUE(user_id, lesson_id)
```

を設定する。

これにより、同一ユーザー・同一Lessonの学習進捗が複数作成されることを防ぐ。

---

## 14. user_id

`learning_progress.user_id` は、

```text
users.id
```

を参照する外部キーとする。

関係は次のようになる。

```text
users
  1
  │
  │
  N
learning_progress
```

1人の利用者は複数Lessonの学習進捗を持つことができる。

---

## 15. lesson_id

Lessonそのものの教材データは、第一段階ではDBへ移さない。

現在と同じようにJava側で管理する。

したがって、

```text
learning_progress.lesson_id
```

にはJava側のLessonが持っている安定した識別子を保存する。

例：

```text
stage1
stage2
stage3
```

`lesson_id` は第一段階ではDBの `lessons` テーブルへの外部キーにはしない。

将来、教材データそのものをDBで管理する場合に `lessons` テーブルの導入を再検討する。

---

## 16. current_step_id

`current_step_id` は、

**次回そのLessonを開いたときに再開するStep**

を表す。

例：

```text
Step3を完了
    ↓
次はStep4
    ↓
current_step_id = Step4
```

と保存する。

これにより、ブラウザを閉じた場合でも次回ログイン後にStep4から再開できる。

---

## 17. completed

Lesson全体を完了しているかを表す。

```text
false
```

なら学習途中、

```text
true
```

ならLesson完了とする。

Lesson完了時には、

```text
completed = true
```

へ更新する。

Lessonが完了した場合の `current_step_id` の具体的な値については、既存のLesson構造と完了画面の処理を確認したうえで実装時に最終決定する。

---

## 18. updated_at

学習進捗が最後に更新された日時を保存する。

Stepが進んだとき、Lessonが完了したとき、「最初からやり直す」を実行したときに更新する。

将来、

- 最終学習日時
- 最近学習したLesson
- 学習履歴

などを実装するときにも利用できる。

---

## 19. HTTPセッションとDBの役割

HTTPセッションとDBは、どちらか一方だけを使用するのではない。

それぞれ役割を分ける。

### 19.1 HTTPセッション

現在の利用中だけ必要な状態を管理する。

現在の `LessonProgress` が持つ主な状態は次のとおり。

```text
lessonId
currentStepId
completedStepIds
selectedOptionId
answered
correct
completed
programExecuted
```

このうち、学習中の画面表示や操作に必要な一時的な状態は、HTTPセッションで引き続き管理する。

### 19.2 DB

利用終了後も保持し、次回利用時に復元する必要がある状態を管理する。

主な保存対象は次のとおり。

```text
user_id
lesson_id
current_step_id
completed
updated_at
```

### 19.3 HTTPセッションとDBの関係

DBを導入しても、現在の `LessonProgress` やHTTPセッションを廃止するわけではない。

DBから取得した永続的な学習進捗をもとに、現在利用中の学習状態をHTTPセッションへ反映する。

概念的な流れは次のとおり。

```text
ログイン
    ↓
Lessonを開く
    ↓
DBからそのLessonの学習進捗を取得
    ↓
前回のStepから学習を再開
    ↓
学習中の一時的な状態はHTTPセッションで管理
    ↓
学習が進んだときに必要な進捗をDBへ保存
```

---

## 20. completedStepIds

現在の `LessonProgress` が持つ、

```java
completedStepIds
```

はHTTPセッション内では引き続き使用する。

💡の点灯状態など、現在の学習画面を制御するために必要である。

ただし、第一段階ではDBへ保存しない。

したがって、ブラウザを閉じて再ログインした場合、

**前回のStepからは再開できるが、以前点灯させた個々の💡状態までは復元しない。**

個々のコード要素まで永続化する設計は将来機能とする。

---

## 21. 第一段階でDBへ保存しない状態

現在の `LessonProgress` のうち、次の状態は第一段階ではDBへ保存しない。

```text
completedStepIds
selectedOptionId
answered
correct
programExecuted
```

これらは現在利用中の画面状態としてHTTPセッションで管理する。

---

## 22. DBへの保存タイミング

DBへの保存タイミングは、

**Stepが完了し、次に進むStepが確定した時点**

とする。

例：

```text
Step3を学習
    ↓
Step3完了
    ↓
次のStep = Step4
    ↓
DBへ保存
current_step_id = Step4
```

回答ボタンを押すたび、💡を点灯するたびなどの細かな操作ではDB保存を行わない。

Lesson全体が完了した場合は、完了状態もDBへ保存する。

---

## 23. Step完了時に保存する理由

ログアウト時だけ保存する方式にはしない。

利用者が、

- ブラウザを閉じる
- PCを終了する
- ネットワークが切れる
- セッションが終了する

などの場合、ログアウト処理が実行されない可能性があるためである。

Step完了時に保存しておくことで、直前までの学習位置をDBへ残すことができる。

---

## 24. DBからの復元タイミング

DBから学習進捗を読み込むタイミングは、

**ログイン済み利用者がLessonを開いたとき**

とする。

例：

```text
ログイン
    ↓
Stage2を選択
    ↓
ユーザーID + lesson_id でDB検索
    ↓
learning_progressを取得
```

ログインした時点ですべてのLessonの詳細な進捗を一括で復元するのではなく、学習対象となるLessonを開いたときに、そのLessonの進捗を取得する。

---

## 25. 学習進捗が存在しない場合

DBを検索して、

```text
user_id + lesson_id
```

に対応するレコードが存在しなければ、そのLessonは未学習と判断する。

```text
進捗なし
    ↓
Lessonの最初のStepから開始
```

する。

---

## 26. 学習途中の場合

DBに、

```text
completed = false
current_step_id = Step4
```

が保存されている場合、

```text
Lessonを開く
    ↓
DBからStep4を取得
    ↓
HTTPセッション上の学習状態へ反映
    ↓
Step4から表示
```

とする。

---

## 27. Lesson完了済みの場合

```text
completed = true
```

の場合、そのLessonが完了済みであることを判断できるようにする。

完了済みLessonを開いた場合の具体的な画面遷移については、現在の完了画面の構造を確認して実装時に決定する。

第一段階では個々の💡の過去状態までは復元しない。

---

## 28. 「最初からやり直す」

利用者が「最初からやり直す」を選択した場合は、HTTPセッションとDBの両方の進捗を初期状態へ戻す。

DBレコード自体は削除せず、

```text
current_step_id = 最初のStep
completed = false
updated_at = 現在日時
```

となるように更新する。

HTTPセッション側では、

```text
completedStepIds
selectedOptionId
answered
correct
programExecuted
```

などの現在の学習状態を初期化する。

レコードを削除せず更新することで、

**そのユーザーとLessonの進捗レコードを1件に保つ**

設計とする。

---

## 29. 学習進捗保存処理

同一ユーザー・同一Lessonの進捗がすでに存在する場合は更新する。

存在しない場合は新規作成する。

概念的には次のようになる。

```text
user_id + lesson_id で検索
        ↓
      存在？
      ↙   ↘
    Yes    No
     ↓      ↓
   UPDATE  INSERT
```

実装ではSpring Data JPAを利用し、既存の進捗レコードを取得してEntityを保存する構成を基本とする。

---

## 30. Entity

第一段階では、少なくとも次のEntityが必要になる。

```text
UserAccount
LearningProgress
```

`UserAccount` という名称は、Spring Securityなどで使用される `User` との混同を避ける候補とする。

実際のクラス名は、既存ソースコードとの重複や命名規則を確認して実装時に最終決定する。

---

## 31. Repository

Spring Data JPAのRepositoryとして、概念上次の2つが必要になる。

```text
UserAccountRepository
LearningProgressRepository
```

必要となる主な検索は、

```text
メールアドレスからユーザーを検索

ユーザー + lesson_id から学習進捗を検索
```

である。

Repositoryの具体的なメソッド名は、Entity設計確定後に決定する。

---

## 32. Serviceの役割

既存のHTTPセッション用 `LessonProgressService` は残す。

DB導入によって置き換えない。

現在の `LessonProgressService` は、

```text
現在利用中の学習状態を管理する
```

役割を引き続き担当する。

DBへの保存・復元については、HTTPセッション管理とは別の責務として扱う。

概念上、

```text
学習進捗のDB保存
学習進捗のDB取得
学習進捗のリセット
```

を担当するServiceを設ける。

実際のクラス名や既存Serviceとの接続位置については、実装開始時に現在のソースコードを確認して最終決定する。

---

## 33. 全体の処理イメージ

```text
                    ┌───────────────┐
                    │   PostgreSQL  │
                    └───────▲───────┘
                            │
                   Spring Data JPA
                            │
                ┌───────────┴───────────┐
                │                       │
           UserAccount            LearningProgress
                │                       │
                └───────────┬───────────┘
                            │
                         Service
                            │
                       Controller
                            │
                       Thymeleaf
                            │
                         Browser
```

学習中の細かな状態については、別途、

```text
Browser
   ↓
Controller
   ↓
LessonProgressService
   ↓
HttpSession
```

で管理する。

---

## 34. ログインから学習再開まで

```text
Java Link
   ↓
ログイン
   ↓
Spring Securityで認証
   ↓
トップ画面
   ↓
Lessonを選択
   ↓
ログイン済みか確認
   ↓
user_id + lesson_id でDB検索
   ↓
      ┌─────────────────┐
      │                 │
進捗あり              進捗なし
   ↓                    ↓
current_step_id      最初のStep
   ↓                    ↓
   └─────────┬──────────┘
             ↓
   HTTPセッションへ反映
             ↓
          学習開始
```

---

## 35. 学習中の保存

```text
学習
 ↓
Step完了
 ↓
次のStep確定
 ↓
ログイン済み？
 ↓
 ┌───────────────┐
 Yes             No
 ↓                ↓
DBへ保存      HTTPセッションのみ
 ↓
次のStepを表示
```

未ログイン利用者についてはDB保存を行わず、現在どおりHTTPセッションのみで進捗を管理する。

---

## 36. セキュリティ

第一段階では、最低限次の方針を守る。

```text
パスワードを平文保存しない
パスワードをログへ出さない
PasswordEncoderを利用する
メールアドレスを一意にする
他ユーザーのuser_idをリクエスト値から信用しない
認証中ユーザーからuser_idを取得する
DBパスワードをGitHubへコミットしない
```

特に学習進捗保存では、ブラウザから送られてきた `user_id` をそのまま使用せず、

**Spring Securityで現在ログインしているユーザーからユーザーIDを特定する。**

これにより、他ユーザーの学習進捗を誤って操作することを防ぐ。

---

## 37. DB接続情報

PostgreSQLの、

```text
URL
ユーザー名
パスワード
```

などの認証情報をGitHubへ直接コミットしない。

環境変数など、ソースコードへ秘密情報を直接記載しない方法を使用する。

GitHub上のソースコードに実際のDBパスワードを残さない。

---

## 38. 入力チェック

ユーザー登録時には最低限、次の入力チェックを行う。

| 項目 | チェック |
| --- | --- |
| 表示名 | 空欄不可 |
| メールアドレス | 空欄不可・メール形式 |
| パスワード | 空欄不可 |
| メールアドレス | DB上で重複不可 |

パスワードの長さなど、具体的な入力条件については、実装時にSpring Securityとフォーム設計を確認したうえで決定する。

---

## 39. エラー処理

第一段階では、最低限次の場合を想定する。

### 39.1 メールアドレス重複

登録済みのメールアドレスが入力された場合は、ユーザー登録を行わず、登録画面にエラーメッセージを表示する。

### 39.2 ログイン失敗

メールアドレスまたはパスワードが一致しない場合は、ログイン失敗として扱う。

どちらが間違っているかを詳細には表示しない。

### 39.3 DBに保存されたStepが現在の教材に存在しない場合

教材変更などによって、DBに保存されている `current_step_id` が現在のLessonに存在しなくなる可能性がある。

この場合の具体的な復旧方法は、既存のLesson構造を確認して実装時に決定する。

アプリケーションがそのままエラー終了するのではなく、安全に学習を開始できる状態へ戻すことを基本方針とする。

---

## 40. テスト方針

実装時には少なくとも次の内容を確認する。

### 40.1 ユーザー登録

```text
正常に登録できる
メールアドレスの重複を防げる
パスワードが平文で保存されない
```

### 40.2 ログイン

```text
正しい認証情報でログインできる
誤った認証情報ではログインできない
ログアウトできる
```

### 40.3 学習進捗

```text
初回学習で進捗を保存できる
Step完了時にcurrent_step_idが更新される
同じユーザー・同じLessonの進捗レコードが複数作成されない
別ユーザーの進捗が独立して管理される
Lesson完了状態を保存できる
再度Lessonを開いたときに前回のStepから再開できる
「最初からやり直す」で進捗を初期状態へ戻せる
```

既存テストへの影響を確認しながら、DBを利用する処理に必要なテストを追加する。

---

## 41. 学習履歴

第一段階の、

```text
learning_progress
```

は「現在どこまで進んでいるか」を管理するテーブルとする。

将来的な、

```text
いつ学習したか
何回学習したか
いつLessonを完了したか
どのStageを何回やり直したか
```

といった履歴は、現在進捗とは別の仕組みとして設計する。

第一段階では実装しない。

---

## 42. 第一段階の完成条件

次の流れが成立した時点で、DB・ログイン機能の第一段階を完成とする。

```text
ユーザー登録
    ↓
ログイン
    ↓
Lessonを学習
    ↓
Stepを進める
    ↓
進捗がPostgreSQLへ保存される
    ↓
ログアウト / ブラウザ終了
    ↓
再度ログイン
    ↓
同じLessonを開く
    ↓
前回のStepから再開できる
```

---

## 43. 実装順序

設計完了後は、次の順番で小さく実装する。

```text
① PostgreSQL / Spring Data JPA導入
        ↓
② UserAccount Entity / Repository
        ↓
③ ユーザー登録
        ↓
④ Spring Security導入・ログイン
        ↓
⑤ ログアウト
        ↓
⑥ LearningProgress Entity / Repository
        ↓
⑦ Step完了時のDB保存
        ↓
⑧ Lesson開始時のDB復元
        ↓
⑨ 「最初からやり直す」のDB連携
        ↓
⑩ テスト・README更新
```

変更範囲が大きくなりすぎないよう、可能な限り作業単位ごとにブランチとPull Requestを分ける。

---

## 44. 第一段階で決定したこと

### DB・永続化技術

- DBはPostgreSQLを使用する
- 永続化にはSpring Data JPAを使用する
- 認証にはSpring Securityを使用する

### ユーザー

- メールアドレス＋パスワードでログインする
- 表示名を登録する
- メールアドレスは一意とする
- パスワードはPasswordEncoderで変換して保存する
- 内部ユーザーIDで学習進捗と関連付ける

### 学習進捗

- 1ユーザー × 1Lesson = 1進捗レコードとする
- Lessonの教材データ自体は第一段階ではJava側で管理する
- `lesson_id` はJava側のLesson識別子を保存する
- 個々の💡状態は第一段階ではDBへ保存しない

### HTTPセッションとDB

- HTTPセッションは現在利用中だけ必要な一時的な状態を管理する
- DBは利用終了後も保持し、次回利用時に復元する状態を管理する
- DB導入後もHTTPセッションは引き続き使用する

### DBへの保存タイミング

- Stepが完了し、次に進むStepが確定した時点で保存する
- 細かな画面操作のたびには保存しない
- Lesson完了時には完了状態を保存する

### DBからの復元タイミング

- ログイン済み利用者がLessonを開いた時点で、そのLessonの進捗を取得する
- 前回保存された `current_step_id` をもとに学習を再開する

### 「最初からやり直す」

- DBレコードそのものは削除しない
- 最初のStep・未完了状態へ更新する
- HTTPセッション上の学習状態も初期化する

### 将来対応

第一段階では次の機能は実装しない。

- Googleなどの外部ログイン
- MFA
- パスワード再設定
- アカウント編集
- 個々の💡状態の永続化
- 回答履歴
- 詳細な学習履歴
- Lesson教材データそのもののDB管理

---

## 45. 実装時に最終確認する事項

設計の基本方針は以上で確定する。

ただし、次の項目については既存ソースコードやSpring Boot / Spring Security / JPAの実際の構成を確認しながら、実装時に最終決定する。

- Entity・Service・Repositoryの具体的なクラス名
- JPAアノテーションの具体的な指定
- 主キーの具体的な自動生成方式
- 日時カラムの具体的なJava・PostgreSQL型
- Lesson完了時の `current_step_id` の扱い
- 保存されたStepが教材変更によって存在しなくなった場合の復旧方法
- メールアドレスの正規化方法
- パスワードの具体的な入力条件
- Spring Securityの具体的な設定方法
- 既存Controller / Serviceとの接続位置
- DBを利用するテストの具体的な構成

これらは実装詳細として、既存コードを確認したうえで決定し、必要に応じて本設計書も更新する。