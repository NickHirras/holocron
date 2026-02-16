-- V1.0.0__Baseline.sql
-- Captures existing schema for User, Team, TeamMember, Ceremony, Questions, Responses, and Answers

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    role VARCHAR(255),
    jobTitle VARCHAR(255)
);

CREATE TABLE teams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    timezoneId VARCHAR(255)
);

CREATE TABLE team_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    team_id BIGINT,
    role VARCHAR(255),
    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(team_id) REFERENCES teams(id),
    UNIQUE(user_id, team_id)
);

CREATE TABLE ceremonies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255),
    description VARCHAR(255),
    team_id BIGINT,
    scheduleType VARCHAR(255),
    type INTEGER, -- Enum ordinal
    isActive BOOLEAN,
    FOREIGN KEY(team_id) REFERENCES teams(id)
);

CREATE TABLE ceremony_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ceremony_id BIGINT,
    text VARCHAR(255),
    type VARCHAR(255),
    sequence INTEGER,
    isRequired BOOLEAN,
    FOREIGN KEY(ceremony_id) REFERENCES ceremonies(id)
);

CREATE TABLE ceremony_responses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ceremony_id BIGINT,
    user_id BIGINT,
    date DATE,
    submittedAt TIMESTAMP,
    comments VARCHAR(255),
    FOREIGN KEY(ceremony_id) REFERENCES ceremonies(id),
    FOREIGN KEY(user_id) REFERENCES users(id)
);

CREATE TABLE ceremony_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    response_id BIGINT,
    question_id BIGINT,
    answerValue CLOB, -- @Lob
    FOREIGN KEY(response_id) REFERENCES ceremony_responses(id),
    FOREIGN KEY(question_id) REFERENCES ceremony_questions(id)
);
