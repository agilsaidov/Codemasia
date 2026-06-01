-- Judge0 1.13.1 language catalog (ids match Judge0 /submissions API language_id)
CREATE TABLE programming_languages
(
    judge0_language_id INTEGER      PRIMARY KEY,
    name               VARCHAR(100) NOT NULL,
    enabled            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX programming_languages_name_idx ON programming_languages (name);

INSERT INTO programming_languages (judge0_language_id, name)
VALUES (43, 'Plain Text'),
       (44, 'Executable'),
       (45, 'Assembly (NASM 2.14.02)'),
       (46, 'Bash (5.0.0)'),
       (47, 'Basic (FBC 1.07.1)'),
       (48, 'C (GCC 7.4.0)'),
       (49, 'C (GCC 8.3.0)'),
       (50, 'C (GCC 9.2.0)'),
       (51, 'C# (Mono 6.6.0.161)'),
       (52, 'C++ (GCC 7.4.0)'),
       (53, 'C++ (GCC 8.3.0)'),
       (54, 'C++ (GCC 9.2.0)'),
       (55, 'Common Lisp (SBCL 2.0.0)'),
       (56, 'D (DMD 2.089.1)'),
       (57, 'Elixir (1.9.4)'),
       (58, 'Erlang (OTP 22.2)'),
       (59, 'Fortran (GFortran 9.2.0)'),
       (60, 'Go (1.13.5)'),
       (61, 'Haskell (GHC 8.8.1)'),
       (62, 'Java (OpenJDK 13.0.1)'),
       (63, 'JavaScript (Node.js 12.14.0)'),
       (64, 'Lua (5.3.5)'),
       (65, 'OCaml (4.09.0)'),
       (66, 'Octave (5.1.0)'),
       (67, 'Pascal (FPC 3.0.4)'),
       (68, 'PHP (7.4.1)'),
       (69, 'Prolog (GNU Prolog 1.4.5)'),
       (70, 'Python (2.7.17)'),
       (71, 'Python (3.8.1)'),
       (72, 'Ruby (2.7.0)'),
       (73, 'Rust (1.40.0)'),
       (74, 'TypeScript (3.7.4)'),
       (75, 'C (Clang 7.0.1)'),
       (76, 'C++ (Clang 7.0.1)'),
       (77, 'COBOL (GnuCOBOL 2.2)'),
       (78, 'Kotlin (1.3.70)'),
       (79, 'Objective-C (Clang 7.0.1)'),
       (80, 'R (4.0.0)'),
       (81, 'Scala (2.13.2)'),
       (82, 'SQL (SQLite 3.27.2)'),
       (83, 'Swift (5.2.3)'),
       (84, 'Visual Basic.Net (vbnc 0.0.0.5943)'),
       (85, 'Perl (5.28.1)'),
       (86, 'Clojure (1.10.1)'),
       (87, 'F# (.NET Core SDK 3.1.202)'),
       (88, 'Groovy (3.0.3)'),
       (89, 'Multi-file program');

DROP TABLE exam_session_languages;

CREATE TABLE exam_session_languages
(
    session_id         BIGINT  NOT NULL REFERENCES exam_sessions (id) ON DELETE CASCADE,
    judge0_language_id INTEGER NOT NULL REFERENCES programming_languages (judge0_language_id) ON DELETE RESTRICT,
    PRIMARY KEY (session_id, judge0_language_id)
);

CREATE INDEX exam_session_languages_session_id_idx ON exam_session_languages (session_id);
