-- Tabela Player
/*CREATE TABLE Player (
                        id SERIAL PRIMARY KEY,
                        token UUID NOT NULL,
                        username VARCHAR(50) UNIQUE NOT NULL,
                        passwordValidation VARCHAR(255) NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        age INT CHECK (age BETWEEN 18 AND 100),
                        credit INT DEFAULT 0 CHECK (credit >= 0),
                        winCounter INT DEFAULT 0 CHECK (winCounter >= 0)
    -- lobby_id INT REFERENCES Lobby(id) ON DELETE SET NULL
);*/

-- Tabela Player
CREATE TABLE "user" (
                        id SERIAL PRIMARY KEY,
                        token UUID NOT NULL,
                        username VARCHAR(50) UNIQUE NOT NULL,
                        passwordValidation VARCHAR(255) NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        age INT CHECK (age BETWEEN 18 AND 100),
                        credit INT DEFAULT 0 CHECK (credit >= 0),
                        winCounter INT DEFAULT 0 CHECK (winCounter >= 0)
    -- lobby_id INT REFERENCES Lobby(id) ON DELETE SET NULL
);

-- Tabela Lobby
CREATE TABLE Lobby (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    --host_id INT NOT NULL REFERENCES Player(id) ON DELETE CASCADE,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    password_validation VARCHAR(255),
    minPlayers INT NOT NULL CHECK (minPlayers >= 2),
    maxPlayers INT NOT NULL CHECK (maxPlayers >= minPlayers),
    rounds INT NOT NULL CHECK (rounds > 0),
    min_credit_to_participate INT NOT NULL DEFAULT 10 CHECK (min_credit_to_participate >= 10)
);

ALTER TABLE Lobby
    ADD COLUMN host_id INT,
    ADD CONSTRAINT fk_host FOREIGN KEY (host_id) REFERENCES "user" (id) ON DELETE CASCADE;

ALTER TABLE "user"
    ADD COLUMN lobby_id INT,
    ADD CONSTRAINT fk_lobby FOREIGN KEY (lobby_id) REFERENCES Lobby(id) ON DELETE SET NULL;

-- Tabela Game
CREATE TABLE IF NOT EXISTS Game (
    id UUID PRIMARY KEY NOT NULL,
    lobby_id INT REFERENCES Lobby(id) ON DELETE CASCADE, -- tambem faz sentido eliminar
    state VARCHAR(20) NOT NULL DEFAULT 'WAITING_FOR_PLAYERS', -- corresponde ao enum State
    nrUsers INT NOT NULL,
    minCredits INT NOT NULL CHECK (minCredits >= 0)
);
-- Tabela Round
CREATE TABLE IF NOT EXISTS Round (
    id SERIAL PRIMARY KEY,
    game_id UUID REFERENCES Game(id) ON DELETE CASCADE,
    winner INT REFERENCES "user"(id) ON DELETE SET NULL,
    bet INT NOT NULL CHECK (bet >= 10),
    roundOver BOOLEAN DEFAULT FALSE,
    timeToPlay INT NOT NULL CHECK (timeToPlay >= 1000) -- em ms
);

create table IF NOT EXISTS APP_INVITE(
    id serial primary key,
    inviterId integer references "user"(id),
    inviteValidationInfo varchar(255) unique not null,
    state varchar(20) not null CHECK (state IN ('pending', 'used', 'expired')),
    createdAt bigint not null
);

create table IF NOT EXISTS TOKEN (
    tokenValidation varchar(255) primary key ,
    createdAt bigint not null,
    lastUsedAt bigint not null,
    userId integer,
    foreign key (userId) references "user"(id) on delete cascade
);
