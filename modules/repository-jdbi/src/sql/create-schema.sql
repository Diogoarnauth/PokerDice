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
    -- lobby_id INT REFERENCES Lobby(id) ON DELETE SET NULL
);

-- Tabela Lobby
CREATE TABLE dbo.lobby (
                           id SERIAL PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           description TEXT,
    --host_id INT NOT NULL REFERENCES Player(id) ON DELETE CASCADE,
                           minPlayers INT NOT NULL CHECK (minPlayers >= 2),
                           maxPlayers INT NOT NULL CHECK (maxPlayers >= minPlayers),
                           rounds INT NOT NULL CHECK (rounds > 0),
                           min_credit_to_participate INT NOT NULL DEFAULT 10 CHECK (min_credit_to_participate >= 10)
);

ALTER TABLE dbo.lobby
    ADD COLUMN host_id INT,
    ADD CONSTRAINT fk_host FOREIGN KEY (host_id) REFERENCES dbo.users (id) ON DELETE CASCADE;

ALTER TABLE dbo.users
    ADD COLUMN lobby_id INT,
    ADD CONSTRAINT fk_lobby FOREIGN KEY (lobby_id) REFERENCES dbo.lobby(id) ON DELETE SET NULL;

-- Tabela Game
CREATE TABLE IF NOT EXISTS dbo.game (
                                        id UUID PRIMARY KEY NOT NULL,
                                        lobby_id INT REFERENCES dbo.Lobby(id) ON DELETE CASCADE, -- tambem faz sentido eliminar
                                        state VARCHAR(20) NOT NULL DEFAULT 'WAITING_FOR_PLAYERS', -- corresponde ao enum State
                                        rounds_counter INT DEFAULT 0 CHECK (rounds_counter >= 0),
                                        winner INT REFERENCES dbo.users(id) ON DELETE SET NULL,
                                        round_results JSONB,
                                        nrUsers INT NOT NULL,
                                        minCredits INT NOT NULL CHECK (minCredits >= 0)
);
-- Tabela Round
CREATE TABLE IF NOT EXISTS dbo.round (
                                         id SERIAL PRIMARY KEY,
                                         game_id UUID REFERENCES dbo.game(id) ON DELETE CASCADE,
                                         winner INT REFERENCES dbo.users(id) ON DELETE SET NULL,
                                         bet INT NOT NULL CHECK (bet >= 10),
                                         roundOver BOOLEAN DEFAULT FALSE,
                                         timeToPlay INT NOT NULL CHECK (timeToPlay >= 1000) -- em ms
);

CREATE TABLE IF NOT EXISTS dbo.app_invite(
                                             id serial primary key,
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

    /*
-- Criação do schema
CREATE SCHEMA IF NOT EXISTS dbo;

-- ========================================
-- Tabela users
-- ========================================
CREATE TABLE IF NOT EXISTS dbo.users (
                                         id SERIAL PRIMARY KEY,
                                         username VARCHAR(50) UNIQUE NOT NULL,
                                         passwordValidation VARCHAR(255) NOT NULL,
                                         name VARCHAR(100) NOT NULL,
                                         age INT CHECK (age BETWEEN 18 AND 100),
                                         credit INT DEFAULT 0 CHECK (credit >= 0),
                                         winCounter INT DEFAULT 0 CHECK (winCounter >= 0),
                                         lobby_id INT -- será adicionada a FK abaixo
);

-- ========================================
-- Tabela lobby
-- ========================================
CREATE TABLE IF NOT EXISTS dbo.lobby (
                                         id SERIAL PRIMARY KEY,
                                         name VARCHAR(100) NOT NULL,
                                         description TEXT,
                                         host_id INT NOT NULL, -- FK adicionada depois
                                         minPlayers INT NOT NULL CHECK (minPlayers >= 2),
                                         maxPlayers INT NOT NULL CHECK (maxPlayers >= minPlayers),
                                         rounds INT NOT NULL CHECK (rounds > 0),
                                         min_credit_to_participate INT NOT NULL DEFAULT 10 CHECK (min_credit_to_participate >= 10)
);

-- ========================================
-- Foreign Keys
-- ========================================
-- FK host_id em lobby → users(id)
ALTER TABLE dbo.lobby
    ADD CONSTRAINT fk_host FOREIGN KEY (host_id) REFERENCES dbo.users(id) ON DELETE CASCADE;

-- FK lobby_id em users → lobby(id)
ALTER TABLE dbo.users
    ADD CONSTRAINT fk_lobby FOREIGN KEY (lobby_id) REFERENCES dbo.lobby(id) ON DELETE SET NULL;

-- ========================================
-- Tabela game
-- ========================================
CREATE TABLE IF NOT EXISTS dbo.game (
                                        id UUID PRIMARY KEY NOT NULL,
                                        lobby_id INT REFERENCES dbo.lobby(id) ON DELETE CASCADE,
                                        state VARCHAR(20) NOT NULL DEFAULT 'WAITING_FOR_PLAYERS',
                                        nrUsers INT NOT NULL,
                                        minCredits INT NOT NULL CHECK (minCredits >= 0)
);

-- ========================================
-- Tabela round
-- ========================================
CREATE TABLE IF NOT EXISTS dbo.round (
                                         id SERIAL PRIMARY KEY,
                                         game_id UUID REFERENCES dbo.game(id) ON DELETE CASCADE,
                                         winner INT REFERENCES dbo.users(id) ON DELETE SET NULL,
                                         bet INT NOT NULL CHECK (bet >= 10),
                                         roundOver BOOLEAN DEFAULT FALSE,
                                         timeToPlay INT NOT NULL CHECK (timeToPlay >= 1000)
);

-- ========================================
-- Tabela app_invite
-- ========================================
CREATE TABLE IF NOT EXISTS dbo.app_invite (
                                              id SERIAL PRIMARY KEY,
                                              inviterId INT REFERENCES dbo.users(id),
                                              inviteValidationInfo VARCHAR(255) UNIQUE NOT NULL,
                                              state VARCHAR(20) NOT NULL CHECK (state IN ('pending', 'used', 'expired')),
                                              createdAt BIGINT NOT NULL
);

-- ========================================
-- Tabela token
-- ========================================
CREATE TABLE IF NOT EXISTS dbo.token (
                                         tokenValidation VARCHAR(255) PRIMARY KEY,
                                         createdAt BIGINT NOT NULL,
                                         lastUsedAt BIGINT NOT NULL,
                                         userId INT REFERENCES dbo.users(id) ON DELETE CASCADE
);

     */
