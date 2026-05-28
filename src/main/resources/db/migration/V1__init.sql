CREATE TABLE users
(
    id             SERIAL PRIMARY KEY,
    uuid           UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    email          TEXT,
    role           TEXT        NOT NULL,
    minecraft_uuid UUID UNIQUE NOT NULL,
    password_hash  TEXT,
    totp_secret    TEXT,
    deactivated    BOOLEAN     NOT NULL
);

CREATE TABLE invites
(
    id              SERIAL PRIMARY KEY,
    token           TEXT UNIQUE                          NOT NULL,
    invited_user_id INTEGER REFERENCES users (id) UNIQUE NOT NULL
);