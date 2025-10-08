-- Tabela player
CREATE TABLE Player (
                        id SERIAL PRIMARY KEY,
                        username VARCHAR(50) UNIQUE NOT NULL,
                        name VARCHAR(100) NOT NULL,

                        age INT CHECK (age >= 0),
                        credit DECIMAL(10,2) DEFAULT 0 CHECK (credit >= 0),
                        winCounter INT DEFAULT 0,
                        token VARCHAR(255)
);

-- Tabela Lobby
CREATE TABLE Lobby (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       description TEXT,
                       minPlayers INT CHECK (minPlayers >= 1),
                       maxPlayers INT CHECK (maxPlayers >= minPlayers),
                       nRounds INT CHECK (nRounds > 0),
                       owner INT NOT NULL REFERENCES Player(id) ON DELETE CASCADE
);

-- Tabela Relação Player-Lobby
CREATE TABLE Player_Lobby (
                              player_id INT NOT NULL REFERENCES Player(id) ON DELETE CASCADE,
                              lobby_id INT NOT NULL REFERENCES Lobby(id) ON DELETE CASCADE,
                              PRIMARY KEY (player_id, lobby_id)
);

-- Tabela Game
CREATE TABLE Game (
                      id SERIAL PRIMARY KEY,
                      lobby_id INT NOT NULL REFERENCES Lobby(id) ON DELETE CASCADE,
                      min_credit DECIMAL(10,2) DEFAULT 0 CHECK (min_credit >= 0),
                      n_players INT CHECK (n_players >= 0),
                      is_active BOOLEAN DEFAULT TRUE
);

-- Relação Player-Game (N:N)
CREATE TABLE Game_Player (
                             player_id INT NOT NULL REFERENCES Player(id) ON DELETE CASCADE,
                             game_id INT NOT NULL REFERENCES Game(id) ON DELETE CASCADE,
                             PRIMARY KEY (player_id, game_id)
);

-- Tabela Round
CREATE TABLE Round (
                       id SERIAL PRIMARY KEY,
                       game_id INT NOT NULL REFERENCES Game(id) ON DELETE CASCADE,
                       winner INT REFERENCES Player(id) ON DELETE SET NULL
);

-- Tabela Bet
CREATE TABLE Bet (
                     id SERIAL PRIMARY KEY,
                     round_id INT NOT NULL REFERENCES Round(id) ON DELETE CASCADE,
                     player_id INT NOT NULL REFERENCES Player(id) ON DELETE CASCADE,
                     amount DECIMAL(10,2) NOT NULL CHECK (amount > 0)
);