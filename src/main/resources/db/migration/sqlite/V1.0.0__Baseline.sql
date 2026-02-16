-- V1.0.0__Baseline.sql
-- Captures existing schema for User, Team, TeamMember, Ceremony, Questions, Responses, and Answers

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    role VARCHAR(255),
    jobTitle VARCHAR(255)
);

CREATE TABLE teams (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255),
    timezoneId VARCHAR(255)
);

CREATE TABLE team_members (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    team_id INTEGER,
    role VARCHAR(255),
    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(team_id) REFERENCES teams(id),
    UNIQUE(user_id, team_id)
);

CREATE TABLE ceremonies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(255),
    description VARCHAR(255),
    team_id INTEGER,
    scheduleType VARCHAR(255),
    type INTEGER, -- Enum ordinal
    isActive BOOLEAN,
    FOREIGN KEY(team_id) REFERENCES teams(id)
);

CREATE TABLE ceremony_questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ceremony_id INTEGER,
    text VARCHAR(255),
    type VARCHAR(255),
    sequence INTEGER,
    isRequired BOOLEAN,
    FOREIGN KEY(ceremony_id) REFERENCES ceremonies(id)
);

CREATE TABLE ceremony_responses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ceremony_id INTEGER,
    user_id INTEGER,
    date DATE,
    submittedAt TIMESTAMP,
    comments VARCHAR(255),
    FOREIGN KEY(ceremony_id) REFERENCES ceremonies(id),
    FOREIGN KEY(user_id) REFERENCES users(id)
);

CREATE TABLE ceremony_answers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    response_id INTEGER,
    question_id INTEGER,
    answerValue TEXT, -- @Lob
    FOREIGN KEY(response_id) REFERENCES ceremony_responses(id),
    FOREIGN KEY(question_id) REFERENCES ceremony_questions(id)
);
