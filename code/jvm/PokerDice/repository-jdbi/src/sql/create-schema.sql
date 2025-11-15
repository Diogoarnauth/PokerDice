CREATE SCHEMA IF NOT EXISTS dbo;

-- Tabela Player
CREATE TABLE dbo.users (
                           id SERIAL PRIMARY KEY,
                           username VARCHAR(50) UNIQUE NOT NULL,
                           passwordValidation VARCHAR(255) NOT NULL,
                           name VARCHAR(100) NOT NULL,
                           age INT CHECK (age BETWEEN 18 AND 100),
                           credit INT DEFAULT 0 CHECK (credit >= 0),
                           winCounter INT DEFAULT 0 CHECK (winCounter >= 0)
);

-- Tabela Lobby
CREATE TABLE dbo.lobby (
                           id SERIAL PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           description TEXT,
                           isRunning BOOLEAN NOT NULL DEFAULT FALSE,
                           minPlayers INT NOT NULL CHECK (minPlayers >= 2),
                           maxPlayers INT NOT NULL CHECK (maxPlayers >= minPlayers),
                           rounds INT NOT NULL CHECK (rounds > 0),
                           min_credit_to_participate INT NOT NULL DEFAULT 10 CHECK (min_credit_to_participate >= 10),
                           turn_time INTERVAL NOT NULL CHECK (turn_time > INTERVAL '0 seconds')

);

ALTER TABLE dbo.lobby
    ADD COLUMN host_id INT,
    ADD CONSTRAINT fk_host FOREIGN KEY (host_id) REFERENCES dbo.users (id) ON DELETE CASCADE;

ALTER TABLE dbo.users
    ADD COLUMN lobby_id INT,
    ADD CONSTRAINT fk_lobby FOREIGN KEY (lobby_id) REFERENCES dbo.lobby(id) ON DELETE SET NULL;

-- Tabela Game
CREATE TABLE IF NOT EXISTS dbo.game (
                                        id SERIAL PRIMARY KEY NOT NULL,
                                        lobby_id INT REFERENCES dbo.Lobby(id) ON DELETE CASCADE, -- tambem faz sentido eliminar
                                        state VARCHAR(20) NOT NULL DEFAULT 'CLOSED', -- corresponde ao enum State
                                        rounds_counter INT DEFAULT 0 CHECK (rounds_counter >= 0),
                                        nrUsers INT NOT NULL
);

-- Tabela Round
CREATE TABLE IF NOT EXISTS dbo.round (
                                         id SERIAL PRIMARY KEY,
                                         game_id INT REFERENCES dbo.game(id) ON DELETE CASCADE,
                                         bet INT NOT NULL CHECK (bet >= 10),
                                         roundOver BOOLEAN DEFAULT FALSE,
                                         round_number INT NOT NULL
                                         --timeToPlay INT NOT NULL CHECK (timeToPlay >= 1000) -- em ms
);


CREATE TABLE IF NOT EXISTS dbo.app_invite(
                                             id SERIAL primary key,
                                             inviterId integer references dbo.users(id),
                                             inviteValidationInfo varchar(255) unique not null,
                                             state varchar(20) not null CHECK (state IN ('pending', 'used', 'expired')),
                                             createdAt bigint not null
);

CREATE TABLE IF NOT EXISTS dbo.token (
                                         tokenValidation varchar(255) primary key ,
                                         createdAt bigint not null,
                                         lastUsedAt bigint not null,
                                         userId integer,
                                         foreign key (userId) references dbo.users(id) on delete cascade
);

CREATE TABLE IF NOT EXISTS dbo.turn (
                          id SERIAL PRIMARY KEY,
                          round_id INTEGER NOT NULL REFERENCES dbo.round(id) ON DELETE CASCADE,
                          player_id INTEGER NOT NULL REFERENCES dbo.users(id) ON DELETE CASCADE,
                          roll_count INTEGER NOT NULL DEFAULT 0 CHECK (roll_count >= 0),
                          dice_faces TEXT,
                          value_of_combination INTEGER DEFAULT 0,
                          is_done BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS dbo.round_winner (
    round_id INT NOT NULL REFERENCES dbo.round(id) ON DELETE CASCADE,
    user_id  INT NOT NULL REFERENCES dbo.users(id) ON DELETE CASCADE,
    PRIMARY KEY (round_id, user_id)
);

CREATE TABLE IF NOT EXISTS dbo.game_winner (
    game_id INT NOT NULL REFERENCES dbo.game(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES dbo.users(id) ON DELETE CASCADE,
    PRIMARY KEY (game_id, user_id)
);


