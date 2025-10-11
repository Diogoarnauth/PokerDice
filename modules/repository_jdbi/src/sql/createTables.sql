-- Tabela Player
CREATE TABLE dbo.Player (
    id SERIAL PRIMARY KEY,
    token UUID NOT NULL
    username VARCHAR(50) UNIQUE NOT NULL,
    passwordValidation VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    age INT CHECK (age BETWEEN 18 AND 100), 
    credit INT DEFAULT 0 CHECK (credit >= 0),
    winCounter INT DEFAULT 0 CHECK (winCounter >= 0)
    lobby_id INT REFERENCES dbo.Lobby(id) ON DELETE SET NULL
);

-- Tabela Lobby
CREATE TABLE dbo.Lobby (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    host_id INT NOT NULL REFERENCES Player(id) ON DELETE CASCADE, -- faz sentido caso haja delete do player delete dos respetivos lobbys ?
    minPlayers INT NOT NULL,
    maxPlayers INT NOT NULL,
    rounds INT NOT NULL CHECK (rounds > 0)
);

-- Tabela Game
CREATE TABLE dbo.Game (
    id UUID PRIMARY KEY NOT NULL
    lobby_id INT REFERENCES Lobby(id) ON DELETE CASCADE, -- tambem faz sentido eliminar
    state VARCHAR(20) NOT NULL DEFAULT 'WAITING_FOR_PLAYERS', -- corresponde ao enum State
    nrPlayers INT NOT NULL,
    minCredits INT NOT NULL CHECK (minCredits >= 0)
);

-- Tabela Round
CREATE TABLE dbo.Round (
    id SERIAL PRIMARY KEY,
    game_id UUID REFERENCES Game(id) ON DELETE CASCADE,
    winner INT REFERENCES Player(id) ON DELETE SET NULL,
    bet INT NOT NULL CHECK (bet >= 10),
    roundOver BOOLEAN DEFAULT FALSE,
    timeToPlay INT NOT NULL CHECK (timeToPlay >= 1000) -- em ms
);

create table dbo.APP_INVITE(
    id serial primary key,
    inviterId integer references dbo.Player(id),
    inviteValidationInfo varchar(255) unique not null,
    state varchar(20) not null CHECK (state IN ('pending', 'used', 'expired')),
    createdAt bigint not null
);

create table dbo.TOKEN (
    tokenValidation varchar(255) primary key ,
    createdAt bigint not null,
    lastUsedAt bigint not null,
    playerId integer,
    foreign key (playerId) references dbo.Player(id) on delete cascade
);
