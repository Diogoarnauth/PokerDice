-- Player
INSERT INTO Player (token, username, passwordValidation, name, age, credit, winCounter)
VALUES
    ('a3e1f8b2-1c2d-4e5f-9a6b-7c8d9e0f1a2b', 'user1', 'pass1', 'Alice', 25, 100, 2),
    ('b4f2e9c3-2d3e-5f6a-8b7c-9d0e1f2a3b4c', 'user2', 'pass2', 'Bob', 30, 200, 5),
    ('c5g3h0d4-3e4f-6a7b-9c8d-0e1f2a3b4c5d', 'user3', 'pass3', 'Charlie', 40, 150, 3);

-- Lobby
INSERT INTO Lobby (name, description, host_id, minPlayers, maxPlayers, rounds)
VALUES
    ('Fun Lobby', 'Casual games', 1, 2, 6, 5),
    ('Pro Lobby', 'Competitive matches', 2, 4, 8, 10),
    ('Night Owls', 'Late night games', 3, 3, 5, 7);

-- Game
INSERT INTO Game (id, lobby_id, state, nrPlayers, minCredits)
VALUES
    ('11111111-1111-1111-1111-111111111111', 1, 'WAITING_FOR_PLAYERS', 2, 50),
    ('22222222-2222-2222-2222-222222222222', 2, 'IN_PROGRESS', 4, 100),
    ('33333333-3333-3333-3333-333333333333', 3, 'FINISHED', 3, 75);

-- Round
INSERT INTO Round (game_id, winner, bet, roundOver, timeToPlay)
VALUES
    ('11111111-1111-1111-1111-111111111111', 1, 20, FALSE, 2000),
    ('22222222-2222-2222-2222-222222222222', 2, 50, TRUE, 3000),
    ('33333333-3333-3333-3333-333333333333', NULL, 30, FALSE, 1500);

-- APP_INVITE
INSERT INTO APP_INVITE (inviterId, inviteValidationInfo, state, createdAt)
VALUES
    (1, 'invite1', 'pending', 1710000000),
    (2, 'invite2', 'used', 1710000100),
    (3, 'invite3', 'expired', 1710000200);

-- TOKEN
INSERT INTO TOKEN (tokenValidation, createdAt, lastUsedAt, playerId)
VALUES
    ('token1', 1710000000, 1710000500, 1),
    ('token2', 1710000100, 1710000600, 2),
    ('token3', 1710000200, 1710000700, 3);
