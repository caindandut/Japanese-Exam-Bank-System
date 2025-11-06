CREATE DATABASE IF NOT EXISTS nganhangdethi
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE nganhangdethi;

-- Create Levels table
CREATE TABLE Levels (
    LevelID BIGINT AUTO_INCREMENT PRIMARY KEY,
    LevelName VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create QuestionTypes table
CREATE TABLE QuestionTypes (
    QuestionTypeID BIGINT AUTO_INCREMENT PRIMARY KEY,
    TypeName VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Questions table
CREATE TABLE Questions (
    QuestionID BIGINT AUTO_INCREMENT PRIMARY KEY,
    LevelID BIGINT NOT NULL,
    QuestionTypeID BIGINT NOT NULL,
    QuestionText TEXT NOT NULL,
    AudioPath VARCHAR(2048) NULL,
    CreatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (LevelID) REFERENCES Levels(LevelID),
    FOREIGN KEY (QuestionTypeID) REFERENCES QuestionTypes(QuestionTypeID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Choices table
CREATE TABLE Choices (
    ChoiceID BIGINT AUTO_INCREMENT PRIMARY KEY,
    QuestionID BIGINT NOT NULL,
    ChoiceText TEXT NOT NULL,
    IsCorrect BOOLEAN NOT NULL DEFAULT 0,
    FOREIGN KEY (QuestionID) REFERENCES Questions(QuestionID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Exams table
CREATE TABLE Exams (
    ExamID BIGINT AUTO_INCREMENT PRIMARY KEY,
    ExamName VARCHAR(255) NULL,
    LevelID BIGINT NOT NULL,
    DurationMinutes INT NULL DEFAULT NULL, -- Thời gian làm bài (phút), cho phép NULL
    GeneratedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (LevelID) REFERENCES Levels(LevelID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create ExamQuestions table
CREATE TABLE ExamQuestions (
    ExamID BIGINT NOT NULL,
    QuestionID BIGINT NOT NULL,
    QuestionOrder INT NOT NULL,
    PRIMARY KEY (ExamID, QuestionID),
    FOREIGN KEY (ExamID) REFERENCES Exams(ExamID) ON DELETE CASCADE,
    FOREIGN KEY (QuestionID) REFERENCES Questions(QuestionID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Thêm dữ liệu mẫu các cấp độ
INSERT INTO Levels (LevelName) VALUES
('N5'),
('N4'),
('N3'),
('N2'),
('N1');

-- Thêm dữ liệu mẫu các loại câu hỏi
INSERT INTO QuestionTypes (TypeName) VALUES
(N'Ngữ pháp'),
(N'Kanji'),
(N'Từ vựng'),
(N'Đọc hiểu'),
(N'Nghe hiểu');

-- thêm các câu hỏi vào ngân hàng đề thi

-- N5 Ngữ pháp - Câu 1
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES
(1, 1, N'わたし＿がくせいです。適切な助詞を選びなさい。\nA. は\nB. が\nC. も\nD. を');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES
(@current_q_id, N'A. は', 1), 
(@current_q_id, N'B. が', 0), 
(@current_q_id, N'C. も', 0), 
(@current_q_id, N'D. を', 0);

-- N5 Ngữ pháp - Câu 2
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES
(1, 1, N'これは＿のつくえですか。適切な助詞を選びなさい。\nA. だれ\nB. どこ\nC. なん\nD. いつ');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES
(@current_q_id, N'A. だれ', 1), 
(@current_q_id, N'B. どこ', 0), 
(@current_q_id, N'C. なん', 0), 
(@current_q_id, N'D. いつ', 0);

-- N4 Ngữ pháp - Câu 3
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES
(2, 1, N'雨が＿＿＿、試合は中止になりました。\nA. 降ったので\nB. 降るなら\nC. 降っても\nD. 降るし');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES
(@current_q_id, N'A. 降ったので', 1), 
(@current_q_id, N'B. 降るなら', 0), 
(@current_q_id, N'C. 降っても', 0), 
(@current_q_id, N'D. 降るし', 0);

-- N4 Ngữ pháp - Câu 4
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES
(2, 1, N'この本を＿＿＿ください。\nA. 読んで\nB. 読みて\nC. 読んでも\nD. 読まないで');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES
(@current_q_id, N'A. 読んで', 1), 
(@current_q_id, N'B. 読みて', 0), 
(@current_q_id, N'C. 読んでも', 0), 
(@current_q_id, N'D. 読まないで', 0);

-- N3 Ngữ pháp - Câu 5
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES
(3, 1, N'彼は来月日本へ行く＿＿＿です。\nA. つもり\nB. はず\nC. よう\nD. そう');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES
(@current_q_id, N'A. つもり', 1), 
(@current_q_id, N'B. はず', 0), 
(@current_q_id, N'C. よう', 0), 
(@current_q_id, N'D. そう', 0);

-- N3 Ngữ pháp - Câu 6
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES
(3, 1, N'この問題は難しくて、＿＿＿分かりません。\nA. なかなか\nB. ぜんぜん\nC. あまり\nD. いつも');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES
(@current_q_id, N'A. なかなか', 1), 
(@current_q_id, N'B. ぜんぜん', 0), 
(@current_q_id, N'C. あまり', 0), 
(@current_q_id, N'D. いつも', 0);

-- N2 Ngữ pháp - Câu 7
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES
(4, 1, N'努力した＿＿＿、試験に合格した。\nA. かいがあって\nB. わりに\nC. くせに\nD. せいか');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES
(@current_q_id, N'A. かいがあって', 1),
(@current_q_id, N'B. わりに', 0), 
(@current_q_id, N'C. くせに', 0), 
(@current_q_id, N'D. せいか', 0);

-- N2 Ngữ pháp - Câu 8
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES
(4, 1, N'この仕事は経験がない＿＿＿、難しいでしょう。\nA. だけに\nB. ばかりに\nC. ものだから\nD. ことだから');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES
(@current_q_id, N'A. だけに', 1), 
(@current_q_id, N'B. ばかりに', 0), 
(@current_q_id, N'C. ものだから', 0), 
(@current_q_id, N'D. ことだから', 0);

-- N1 Ngữ pháp - Câu 9
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES
(5, 1, N'彼は大臣＿＿＿、その態度は許せない。\nA. にあるまじき\nB. たるもの\nC. の極み\nD. を禁じ得ない');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES
(@current_q_id, N'A. にあるまじき', 1), 
(@current_q_id, N'B. たるもの', 0), 
(@current_q_id, N'C. の極み', 0), 
(@current_q_id, N'D. を禁じ得ない', 0);

-- N1 Ngữ pháp - Câu 10
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES
(5, 1, N'いくら反対されよう＿＿＿、私は自分の道を貫く。\nA. とも\nB. か\nC. なら\nD. こそ');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES
(@current_q_id, N'A. とも', 1), 
(@current_q_id, N'B. か', 0), 
(@current_q_id, N'C. なら', 0), 
(@current_q_id, N'D. こそ', 0);


INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES 
(1, 2, N'「山」の読み方は何ですか。\nA. やま\nB. かわ\nC. そら\nD. うみ');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. やま', 1), 
(@current_q_id, N'B. かわ', 0), 
(@current_q_id, N'C. そら', 0), 
(@current_q_id, N'D. うみ', 0);
-- N5 Kanji - Câu 12
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES 
(1, 2, N'「川」と同じ意味の言葉はどれですか。\nA. 河\nB. 海\nC. 池\nD. 湖');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. 河', 1), 
(@current_q_id, N'B. 海', 0), 
(@current_q_id, N'C. 池', 0), 
(@current_q_id, N'D. 湖', 0);
-- N4 Kanji - Câu 13
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (2, 2, N'「新しい」の正しい漢字はどれですか。\nA. 新しい\nB. 新い\nC. 薪しい\nD. 親しい');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 新しい', 1), (@current_q_id, N'B. 新い', 0), (@current_q_id, N'C. 薪しい', 0), (@current_q_id, N'D. 親しい', 0);
-- N4 Kanji - Câu 14
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES 
(2, 2, N'「図書館」の「図」の意味は何ですか。\nA. 地図、計画 (ちず、けいかく)\nB. 本 (ほん)\nC. 建物 (たてもの)\nD. 学習 (がくしゅう)');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. 地図、計画 (ちず、けいかく)', 1), 
(@current_q_id, N'B. 本 (ほん)', 0), 
(@current_q_id, N'C. 建物 (たてもの)', 0), 
(@current_q_id, N'D. 学習 (がくしゅう)', 0);
-- N3 Kanji - Câu 15
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (3, 2, N'「複雑」の読み方は何ですか。\nA. ふくざつ\nB. ふうざつ\nC. ふくさつ\nD. ふうさつ');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. ふくざつ', 1), (@current_q_id, N'B. ふうざつ', 0), (@current_q_id, N'C. ふくさつ', 0), (@current_q_id, N'D. ふうさつ', 0);
-- N3 Kanji - Câu 16
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (3, 2, N'「解決」の「解」の部首は何ですか。\nA. 角 (つの)\nB. 刀 (かたな)\nC. 牛 (うし)\nD. 言 (げん)');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 角 (つの)', 1), (@current_q_id, N'B. 刀 (かたな)', 0), (@current_q_id, N'C. 牛 (うし)', 0), (@current_q_id, N'D. 言 (げん)', 0);
-- N2 Kanji - Câu 17
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (4, 2, N'「曖昧」の正しい意味はどれですか。\nA. はっきりしないこと\nB. 明確なこと\nC. 簡単なこと\nD. 重要なこと');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. はっきりしないこと', 1), (@current_q_id, N'B. 明確なこと', 0), (@current_q_id, N'C. 簡単なこと', 0), (@current_q_id, N'D. 重要なこと', 0);
-- N2 Kanji - Câu 18
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (4, 2, N'「頻繁」の対義語はどれですか。\nA. 稀 (まれ)\nB. 普通 (ふつう)\nC. 時々 (ときどき)\nD. 連続 (れんぞく)');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 稀 (まれ)', 1), (@current_q_id, N'B. 普通 (ふつう)', 0), (@current_q_id, N'C. 時々 (ときどき)', 0), (@current_q_id, N'D. 連続 (れんぞく)', 0);
-- N1 Kanji - Câu 19
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (5, 2, N'「脆弱」の読み方は何ですか。\nA. ぜいじゃく\nB. せいじゃく\nC. ぜいじゃい\nD. せいじゃい');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. ぜいじゃく', 1), (@current_q_id, N'B. せいじゃく', 0), (@current_q_id, N'C. ぜいじゃい', 0), (@current_q_id, N'D. せいじゃい', 0);
-- N1 Kanji - Câu 20
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (5, 2, N'「杞憂」の「杞」はどのような意味ですか。\nA. 古代中国の国名\nB. 心配すること\nC. 木の名前\nD. 昔の道具');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 古代中国の国名', 1), (@current_q_id, N'B. 心配すること', 0), (@current_q_id, N'C. 木の名前', 0), (@current_q_id, N'D. 昔の道具', 0);


-- N5 Từ vựng - Câu 21
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (1, 3, N'「おはようございます」はいつ使いますか。\nA. 朝 (あさ)\nB. 昼 (ひる)\nC. 夜 (よる)\nD. いつでも');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 朝 (あさ)', 1), (@current_q_id, N'B. 昼 (ひる)', 0), (@current_q_id, N'C. 夜 (よる)', 0), (@current_q_id, N'D. いつでも', 0);
-- N5 Từ vựng - Câu 22
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (1, 3, N'「ありがとう」と言われたら、何と答えますか。\nA. どういたしまして\nB. さようなら\nC. すみません\nD. おやすみなさい');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. どういたしまして', 1), (@current_q_id, N'B. さようなら', 0), (@current_q_id, N'C. すみません', 0), (@current_q_id, N'D. おやすみなさい', 0);
-- N4 Từ vựng - Câu 23
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (2, 3, N'「趣味」は何ですか。\nA. Sở thích\nB. Công việc\nC. Gia đình\nD. Bạn bè');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. Sở thích', 1), (@current_q_id, N'B. Công việc', 0), (@current_q_id, N'C. Gia đình', 0), (@current_q_id, N'D. Bạn bè', 0);
-- N4 Từ vựng - Câu 24
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (2, 3, N'風邪をひいた時、どこへ行きますか。\nA. 病院 (びょういん)\nB. 学校 (がっこう)\nC. 郵便局 (ゆうびんきょく)\nD. 銀行 (ぎんこう)');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 病院 (びょういん)', 1),(@current_q_id, N'B. 学校 (がっこう)', 0), (@current_q_id, N'C. 郵便局 (ゆうびんきょく)', 0), (@current_q_id, N'D. 銀行 (ぎんこう)', 0);
-- N3 Từ vựng - Câu 25
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (3, 3, N'「一生懸命」と同じような意味の言葉はどれですか。\nA. 熱心に (ねっしんに)\nB. 簡単に (かんたんに)\nC. 静かに (しずかに)\nD. 時々 (ときどき)');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 熱心に (ねっしんに)', 1), (@current_q_id, N'B. 簡単に (かんたんに)', 0), (@current_q_id, N'C. 静かに (しずかに)', 0), (@current_q_id, N'D. 時々 (ときどき)', 0);
-- N3 Từ vựng - Câu 26
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (3, 3, N'会議の資料を「配布」するとはどういう意味ですか。\nA. 配る (くばる)\nB. 集める (あつめる)\nC. 作る (つくる)\nD. 読む (よむ)');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 配る (くばる)', 1), (@current_q_id, N'B. 集める (あつめる)', 0), (@current_q_id, N'C. 作る (つくる)', 0), (@current_q_id, N'D. 読む (よむ)', 0);
-- N2 Từ vựng - Câu 27
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (4, 3, N'「抜本的」な改革とは、どのような改革ですか。\nA. 根本からの大きな改革\nB. 部分的な小さな改革\nC. 一時的な改革\nD. 表面的な改革');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 根本からの大きな改革', 1), (@current_q_id, N'B. 部分的な小さな改革', 0), (@current_q_id, N'C. 一時的な改革', 0), (@current_q_id, N'D. 表面的な改革', 0);
-- N2 Từ vựng - Câu 28
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (4, 3, N'相手の意見を「尊重」するとはどういうことですか。\nA. 大切に思うこと\nB. 反対すること\nC. 無視すること\nD. 批判すること');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 大切に思うこと', 1), (@current_q_id, N'B. 反対すること', 0), (@current_q_id, N'C. 無視すること', 0), (@current_q_id, N'D. 批判すること', 0);
-- N1 Từ vựng - Câu 29
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (5, 3, N'「陳腐」な表現とは、どのような表現ですか。\nA. ありふれていて新しさがない\nB. 斬新で独創的である\nC. 難解で理解しにくい\nD. 感情豊かである');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. ありふれていて新しさがない', 1), (@current_q_id, N'B. 斬新で独創的である', 0), (@current_q_id, N'C. 難解で理解しにくい', 0), (@current_q_id, N'D. 感情豊かである', 0);
-- N1 Từ vựng - Câu 30
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (5, 3, N'「齟齬」が生じるとは、どのような状況ですか。\nA. 食い違いが起こり、話がうまく進まない\nB. 意見が完全に一致する\nC. 問題が解決する\nD. 協力体制が整う');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 食い違いが起こり、話がうまく進まない', 1), (@current_q_id, N'B. 意見が完全に一致する', 0), (@current_q_id, N'C. 問題が解決する', 0), (@current_q_id, N'D. 協力体制が整う', 0);


-- N5 Đọc hiểu - Câu 31
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (1, 4, N'「わたしの なまえは マイクです。アメリカじんです。がくせいです。どうぞ よろしく。」マイクさんは どこから きましたか。\nA. アメリカ\nB. 日本\nC. 中国\nD. 学校');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. アメリカ', 1), (@current_q_id, N'B. 日本', 0), (@current_q_id, N'C. 中国', 0), (@current_q_id, N'D. 学校', 0);
-- N5 Đọc hiểu - Câu 32
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (1, 4, N'「きょうは いい てんきです。こうえんに いきましょう。さくらが きれいです。」どこへ 行きますか。\nA. こうえん\nB. がっこう\nC. うち\nD. びょういん');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. こうえん', 1), (@current_q_id, N'B. がっこう', 0), (@current_q_id, N'C. うち', 0), (@current_q_id, N'D. びょういん', 0);
-- N4 Đọc hiểu - Câu 33
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (2, 4, N'田中さんは毎朝早く起きて、公園を散歩します。それから、朝ごはんを食べて、会社へ行きます。田中さんは朝何をしませんか。\nA. 新聞を読む\nB. 早く起きる\nC. 公園を散歩する\nD. 朝ごはんを食べる');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 新聞を読む', 1), (@current_q_id, N'B. 早く起きる', 0), (@current_q_id, N'C. 公園を散歩する', 0), (@current_q_id, N'D. 朝ごはんを食べる', 0);
-- N4 Đọc hiểu - Câu 34
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (2, 4, N'「来週の日曜日、友達と映画を見に行く約束があります。とても楽しみにしています。」この人は来週の日曜日何をしますか。\nA. 友達と映画を見る\nB. 一人で映画を見る\nC. 仕事をする\nD. 勉強する');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 友達と映画を見る', 1),(@current_q_id, N'B. 一人で映画を見る', 0), (@current_q_id, N'C. 仕事をする', 0), (@current_q_id, N'D. 勉強する', 0);
-- N3 Đọc hiểu - Câu 35
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (3, 4, N'日本では、食事の前に「いただきます」と言い、食後には「ごちそうさまでした」と言う習慣があります。これは、食べ物や作ってくれた人への感謝の気持ちを表すためです。この習慣について、筆者はどう考えていますか。\nA. 感謝を表す良い習慣だ\nB. 時代遅れの習慣だ\nC. 面倒な習慣だ\nD. 特に何も考えていない');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 感謝を表す良い習慣だ', 1), (@current_q_id, N'B. 時代遅れの習慣だ', 0), (@current_q_id, N'C. 面倒な習慣だ', 0), (@current_q_id, N'D. 特に何も考えていない', 0);
-- N3 Đọc hiểu - Câu 36
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (3, 4, N'最近、スマートフォンの使いすぎによる健康問題が指摘されています。特に若い世代で、視力低下や睡眠不足が問題となっています。この文章で問題とされていることは何ですか。\nA. スマートフォンの使いすぎによる健康問題\nB. スマートフォンの料金が高いこと\nC. 若い世代の学力低下\nD. 睡眠時間の増加');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. スマートフォンの使いすぎによる健康問題', 1), (@current_q_id, N'B. スマートフォンの料金が高いこと', 0), (@current_q_id, N'C. 若い世代の学力低下', 0), (@current_q_id, N'D. 睡眠時間の増加', 0);
-- N2 Đọc hiểu - Câu 37
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (4, 4, N'地球温暖化は、私たちの生活に様々な影響を及ぼしています。海水温の上昇による生態系の変化、異常気象の頻発などがその例です。この問題に対し、国際的な協力が不可欠です。筆者が最も重要だと考えていることは何ですか。\nA. 国際的な協力で地球温暖化対策を進めること\nB. 個人の努力で節電を心がけること\nC. 新しい技術を開発すること\nD. 影響を無視すること');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 国際的な協力で地球温暖化対策を進めること', 1), (@current_q_id, N'B. 個人の努力で節電を心がけること', 0), (@current_q_id, N'C. 新しい技術を開発すること', 0), (@current_q_id, N'D. 影響を無視すること', 0);
-- N2 Đọc hiểu - Câu 38
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (4, 4, N'現代社会において、情報の真偽を見極める能力はますます重要になっています。インターネット上には誤った情報や意図的に操作された情報も少なくありません。私たちはどのように情報と向き合うべきですか。\nA. 情報の真偽を批判的に吟味する能力を養うこと\nB. インターネットの利用を避けること\nC. 全ての情報を鵜呑みにすること\nD. 専門家の意見だけを信じること');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 情報の真偽を批判的に吟味する能力を養うこと', 1),(@current_q_id, N'B. インターネットの利用を避けること', 0), (@current_q_id, N'C. 全ての情報を鵜呑みにすること', 0), (@current_q_id, N'D. 専門家の意見だけを信じること', 0);
-- N1 Đọc hiểu - Câu 39
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (5, 4, N'文化とは、ある社会の成員が共有する価値観、行動様式、信念の総体である。それは言語、芸術、宗教、習慣など多岐にわたる。文化の多様性を理解し尊重することは、グローバル化が進む現代において極めて重要である。この文章における「文化」の定義に最も近いものはどれか。\nA. 社会のメンバーが共有する価値観や行動様式の全体\nB. 特定の地域の芸術や音楽のみ\nC. 個人の趣味や嗜好\nD. 経済的な発展の度合い');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. 社会のメンバーが共有する価値観や行動様式の全体', 1), (@current_q_id, N'B. 特定の地域の芸術や音楽のみ', 0), (@current_q_id, N'C. 個人の趣味や嗜好', 0), (@current_q_id, N'D. 経済的な発展の度合い', 0);
-- N1 Đọc hiểu - Câu 40
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText) VALUES (5, 4, N'人工知能（AI）の急速な発展は、社会に大きな変革をもたらすと期待される一方、倫理的な課題も提起している。AIが人間の仕事を奪う可能性、プライバシー侵害、判断の偏りなどが懸念される。AI技術の発展と倫理的配慮のバランスをどのように取るべきか。この文章が示唆する主な課題は何か。\nA. AI技術の発展に伴う倫理的問題への対応\nB. AI開発のスピードが遅すぎること\nC. AIが人間より賢くなることへの恐怖\nD. AI技術のコストが高いこと');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES (@current_q_id, N'A. AI技術の発展に伴う倫理的問題への対応', 1), (@current_q_id, N'B. AI開発のスピードが遅すぎること', 0), (@current_q_id, N'C. AIが人間より賢くなることへの恐怖', 0), (@current_q_id, N'D. AI技術のコストが高いこと', 0);


INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText, AudioPath) VALUES 
(1, 5, N'（音声を聞いて）男の人はどこへ行きますか。\nA. 郵便局 (ゆうびんきょく)\nB. 銀行 (ぎんこう)\nC. 学校 (がっこう)\nD. 駅 (えき)', 'audio/n5_listening_sample1.mp3');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. 郵便局 (ゆうびんきょく)', 1), 
(@current_q_id, N'B. 銀行 (ぎんこう)', 0), 
(@current_q_id, N'C. 学校 (がっこう)', 0), 
(@current_q_id, N'D. 駅 (えき)', 0);
-- N5 Nghe hiểu - Câu 42
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText, AudioPath) VALUES 
(1, 5, N'（音声を聞いて）女の人は何を買いますか。\nA. りんご\nB. みかん\nC. バナナ\nD. パン', 'audio/n5_listening_sample2.mp3');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. りんご', 1), 
(@current_q_id, N'B. みかん', 0), 
(@current_q_id, N'C. バナナ', 0), 
(@current_q_id, N'D. パン', 0);
-- N4 Nghe hiểu - Câu 43
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText, AudioPath) VALUES 
(2, 5, N'（音声を聞いて）二人はこれから何をしますか。\nA. 映画を見に行く\nB. 食事をする\nC. 買い物に行く\nD. 家に帰る', 'audio/n4_listening_sample3.mp3');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. 映画を見に行く', 1), 
(@current_q_id, N'B. 食事をする', 0), 
(@current_q_id, N'C. 買い物に行く', 0), 
(@current_q_id, N'D. 家に帰る', 0);
-- N4 Nghe hiểu - Câu 44
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText, AudioPath) VALUES 
(2, 5, N'（音声を聞いて）男の子はどうして泣いていますか。\nA. おもちゃをなくしたから\nB. お母さんに叱られたから\nC. 転んだから\nD. お腹が空いたから', 'audio/n4_listening_sample4.mp3');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. おもちゃをなくしたから', 1), 
(@current_q_id, N'B. お母さんに叱られたから', 0), 
(@current_q_id, N'C. 転んだから', 0), 
(@current_q_id, N'D. お腹が空いたから', 0);
-- N3 Nghe hiểu - Câu 45
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText, AudioPath) VALUES 
(3, 5, N'（音声を聞いて）会議の主な議題は何ですか。\nA. 新製品の開発について\nB. 来月の予算について\nC. 社員旅行の計画について\nD. オフィスの移転について', 'audio/n3_listening_sample3.mp3');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. 新製品の開発について', 1), 
(@current_q_id, N'B. 来月の予算について', 0), 
(@current_q_id, N'C. 社員旅行の計画について', 0), 
(@current_q_id, N'D. オフィスの移転について', 0);
-- N3 Nghe hiểu - Câu 46
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText, AudioPath) VALUES 
(3, 5, N'（音声を聞いて）女の人は男の人に何を頼んでいますか。\nA. 資料のコピー\nB. 会議室の予約\nC. 駅までの送り迎え\nD. 昼食の準備', 'audio/n3_listening_sample4.mp3');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. 資料のコピー', 1), 
(@current_q_id, N'B. 会議室の予約', 0), 
(@current_q_id, N'C. 駅までの送り迎え', 0), 
(@current_q_id, N'D. 昼食の準備', 0);
-- N2 Nghe hiểu - Câu 47
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText, AudioPath) VALUES 
(4, 5, N'（音声を聞いて）男の人が探している本はどのような内容ですか。\nA. 日本の現代美術に関する研究書\nB. 江戸時代の歴史小説\nC. 初心者向けの料理本\nD. 最新の経済分析レポート', 'audio/n2_listening_sample3.mp3');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. 日本の現代美術に関する研究書', 1), 
(@current_q_id, N'B. 江戸時代の歴史小説', 0), 
(@current_q_id, N'C. 初心者向けの料理本', 0), 
(@current_q_id, N'D. 最新の経済分析レポート', 0);
-- N2 Nghe hiểu - Câu 48
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText, AudioPath) VALUES 
(4, 5, N'（音声を聞いて）女の人はなぜ今の仕事を選んだのですか。\nA. 自分の専門知識を活かせるから\nB. 給料が高いから\nC. 勤務時間が短いから\nD. 家から近いから', 'audio/n2_listening_sample4.mp3');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. 自分の専門知識を活かせるから', 1), 
(@current_q_id, N'B. 給料が高いから', 0), 
(@current_q_id, N'C. 勤務時間が短いから', 0), 
(@current_q_id, N'D. 家から近いから', 0);
-- N1 Nghe hiểu - Câu 49
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText, AudioPath) VALUES 
(5, 5, N'（音声を聞いて）講演者は、グローバル化が進む現代において最も重要な能力は何だと述べていますか。\nA. 異文化理解力とコミュニケーション能力\nB. 高度な専門技術\nC. 外国語の流暢さ\nD. リーダーシップ', 'audio/n1_listening_sample3.mp3');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. 異文化理解力とコミュニケーション能力', 1), 
(@current_q_id, N'B. 高度な専門技術', 0), 
(@current_q_id, N'C. 外国語の流暢さ', 0), 
(@current_q_id, N'D. リーダーシップ', 0);
-- N1 Nghe hiểu - Câu 50
INSERT INTO Questions (LevelID, QuestionTypeID, QuestionText, AudioPath) VALUES 
(5, 5, N'（音声を聞いて）この対談で、二人の専門家が一致して指摘している問題点は何ですか。\nA. 若者の読書離れによる思考力の低下\nB. AI技術の倫理的な利用に関する規制の遅れ\nC. 伝統文化の継承者不足\nD. 少子高齢化による労働力不足', 'audio/n1_listening_sample4.mp3');
SET @current_q_id = LAST_INSERT_ID();
INSERT INTO Choices (QuestionID, ChoiceText, IsCorrect) VALUES 
(@current_q_id, N'A. 若者の読書離れによる思考力の低下', 1), 
(@current_q_id, N'B. AI技術の倫理的な利用に関する規制の遅れ', 0), 
(@current_q_id, N'C. 伝統文化の継承者不足', 0), 
(@current_q_id, N'D. 少子高齢化による労働力不足', 0);

