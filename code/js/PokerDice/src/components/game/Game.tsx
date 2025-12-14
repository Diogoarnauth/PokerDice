import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

import { gameService } from "../../services/api/Games";
import { playersService } from "../../services/api/Players";
import { lobbyDetailsService } from "../../services/api/LobbyDetails";
import { playerProfileService } from "../../services/api/PlayerProfile";
import { useSSE } from "../../providers/SSEContext";

import { isOk } from "../../services/api/utils";
import { GamePayload, Game } from "../models/Game";
import { User } from "../lobby/LobbyDetails";

type PlayerInGame = {
    playerId: number;
    username: string;
    credit: number;
    dices: string[];
    valueOfCombination: number;
};

export default function GamePage() {
    const { lobbyId } = useParams<{ lobbyId: string }>();
    const navigate = useNavigate();
    const { addHandler, removeHandler, updateTopic } = useSSE();

    const [game, setGame] = useState<Game | null>(null);
    const [gameId, setGameId] = useState<number | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [info, setInfo] = useState<string | null>(null);
    const [rerollInput, setRerollInput] = useState<string>("");

    const [currentUser, setCurrentUser] = useState<User | null>(null);
    const [currentPlayer, setCurrentPlayer] = useState<string | null>(null);
    const [players, setPlayers] = useState<PlayerInGame[]>([]);

    useEffect(() => {
        if (!lobbyId) {
            setError("Lobby id em falta.");
            setLoading(false);
            return;
        }

        let mounted = true;

        async function fetchGame() {
            setLoading(true);
            setError(null);
            setInfo(null);

            // utilizador autenticado
            const meRes = await playerProfileService.getProfile();
            if (!mounted) return;

            if (isOk(meRes)) {
                setCurrentUser(meRes.value);
            } else {
                setError("Não foi possível obter o utilizador autenticado.");
                setLoading(false);
                return;
            }

            // jogadores no lobby (sem passwordValidation)
            const playerListOnLobby = await playersService.getObjPlayersOnLobby(
                Number(lobbyId)
            );
            if (!mounted) return;

            if (isOk(playerListOnLobby)) {
                const mappedPlayers: PlayerInGame[] =
                    playerListOnLobby.value.players.map((u: any) => ({
                        playerId: u.id,
                        username: u.username,
                        credit: u.credit,
                        dices: [],
                        valueOfCombination: 0,
                    }));

                setPlayers(mappedPlayers);

                // jogo pelo lobby
                const result = await gameService.getGameByLobbyId(Number(lobbyId));
                if (!mounted) return;

                if (isOk(result)) {
                    const currentTurnResult = await gameService.getCompleteCurrentTurn(
                        Number(result.value.id)
                    );
                    if (!mounted) return;

                    if (!isOk(currentTurnResult)) {
                        setError(currentTurnResult.error ?? "Erro ao obter o turno atual.");
                        setLoading(false);
                        return;
                    }

                    const payload = new GamePayload(currentTurnResult.value);

                    const initialDiceArray = currentTurnResult.value.diceFaces
                        ? currentTurnResult.value.diceFaces.split(",")
                        : [];

                    const isFirst =
                        currentTurnResult.value.rollCount === 0 ? true : false;

                    setGame({
                        ...payload.game,
                        dice: initialDiceArray,
                        id: result.value.id,
                        isFirstRoll: isFirst,
                    });

                    setGameId(result.value.id);

                    updateTopic("lobbies");

                    const handleGameUpdated = (data: any) => {
                        if (data.changeType === "ended" && data.lobbyId == lobbyId) {
                            navigate("/lobbies");
                        }
                    };

                    addHandler("gameUpdated", handleGameUpdated);

                    // ronda atual
                    const currentRoundRes = await gameService.getCurrentRound(
                        result.value.id
                    );
                    if (!mounted) return;

                    if (isOk(currentRoundRes)) {
                        const roundId =
                            currentRoundRes.value.roundId ?? currentRoundRes.value.id;

                        const turnsRes = await gameService.getAllTurnsByRound(roundId);
                        if (!mounted) return;

                        if (isOk(turnsRes)) {
                            // exemplo 1: resposta é { value: [...] }
                            const turns = Array.isArray(turnsRes.value.value)
                                ? turnsRes.value.value
                                : Array.isArray(turnsRes.value.turns)
                                    ? turnsRes.value.turns
                                    : [];

                            const updatedPlayers = mappedPlayers.map((player) => {
                                const turn = turns.find(
                                    (t: any) => t.playerId === player.playerId
                                );
                                if (!turn) return player;

                                return {
                                    ...player,
                                    dices: turn.diceFaces ? String(turn.diceFaces).split(",") : [],
                                    valueOfCombination: turn.value_of_combination ?? 0,
                                };
                            });

                            setPlayers(updatedPlayers);
                        } else {
                            console.log("Erro ao buscar turns:", turnsRes.error);
                        }
                    } else {
                        console.log(
                            "Erro ao buscar currentRound:",
                            currentRoundRes.error
                        );
                    }

                    // jogador do turno atual
                    try {
                        const turnRes = await gameService.getPlayerTurn(result.value.id);
                        if (!mounted) return;

                        if (isOk(turnRes)) {
                            setCurrentPlayer(turnRes.value.username);
                        } else {
                            setInfo(
                                turnRes.error ?? "Não foi possível obter o jogador atual."
                            );
                        }
                    } catch {
                        if (!mounted) return;
                        setInfo("Erro ao obter o jogador atual.");
                    }
                } else {
                    setError(result.error ?? "Erro a carregar o jogo.");
                }
            } else {
                setError(playerListOnLobby.error ?? "Erro ao buscar jogadores.");
            }

            if (mounted) setLoading(false);
        }

        fetchGame();

        return () => {
            mounted = false;
            // removeHandler("gameUpdated", handleGameUpdated); // se tiveres referência guardada
        };
    }, [lobbyId, addHandler, navigate, updateTopic]);

    function ensureMyTurn(): boolean {
        if (!game || !currentUser || currentUser.username !== currentPlayer) {
            alert("Não é a tua vez de jogar");
            return false;
        }
        setInfo(null);
        return true;
    }

    async function handleRoll() {
        if (!gameId || !ensureMyTurn()) return;

        const result = await gameService.roll(Number(lobbyId));
        if (!isOk(result)) {
            alert("Só podes dar roll uma vez, experimenta Reroll");
            return;
        }

        if (!game) return;

        const diceArray = result.value.dice.split(",");

        setGame({
            ...game,
            dice: diceArray,
            isFirstRoll: false,
        });

        setInfo("Dados rolados!");
    }

    async function handleReroll() {
        if (!gameId || !ensureMyTurn()) return;

        if (game?.isFirstRoll) {
            setInfo("Ainda não fizeste o primeiro roll.");
            return;
        }

        const diceMask = rerollInput
            .split(",")
            .map((s) => s.trim())
            .filter((s) => s !== "")
            .map(Number)
            .filter((n) => !Number.isNaN(n));

        if (diceMask.length === 0) {
            setInfo("Indica pelo menos um índice de dado para reroll (ex: 0,2,4).");
            return;
        }

        const result = await gameService.reroll(Number(lobbyId), diceMask);
        if (!isOk(result) || !game) {
            setError(result.error ?? "Erro ao fazer reroll.");
            return;
        }

        setGame({
            ...game,
            dice: result.value.dice,
            isFirstRoll: false,
        });

        setInfo("Reroll efetuado!");
    }

    async function handleEndTurn() {
        if (!gameId || !ensureMyTurn()) return;

        const result = await gameService.endTurn(Number(gameId));
        if (!isOk(result) || !game) {
            setError(result.error ?? "Erro ao terminar jogada.");
            return;
        }

        if (result.value.winners) {
            const winnersString = result.value.winners.join(", ");
            alert(result.value.message + " winner: " + winnersString);
        }

        const turnRes = await gameService.getPlayerTurn(game.id);
        if (isOk(turnRes)) {
            setCurrentPlayer(turnRes.value.username);
        } else {
            setInfo(turnRes.error ?? "Não foi possível obter o jogador atual.");
        }

        setGame({
            ...game,
            dice: [],
            isFirstRoll: true,
        });

        setInfo("Jogada terminada, próximo jogador.");
    }

    if (loading) return <p>Loading game...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;
    if (!game) return <p>Jogo não encontrado.</p>;

    const safeDice = Array.isArray(game.dice) ? game.dice : [game.dice || ""];

    return (
        <div className="game-container">
            {/* COLUNA ESQUERDA */}
            <div className="game-left">
                <h1 className="game-title">Game #{game.id}</h1>

                <p className="current-player">
                    Jogador atual: <strong>{currentPlayer}</strong>
                </p>

                {info && (
                    <div className="info-box">
                        {typeof info === "object"
                            ? `${(info as any).title}: ${(info as any).detail}`
                            : info}
                    </div>
                )}

                <div className="dice-section">
                    <h3>Dados</h3>

                    {safeDice && safeDice.length > 0 ? (
                        <div className="dice-display">{safeDice.join(" - ")}</div>
                    ) : (
                        <p className="no-dice">Ainda não há dados rolados.</p>
                    )}

                    {!game.isFirstRoll && (
                        <div className="reroll-input">
                            <label>
                                Índices para reroll:
                                <input
                                    type="text"
                                    value={rerollInput}
                                    onChange={(e) => setRerollInput(e.target.value)}
                                />
                            </label>
                        </div>
                    )}
                </div>

                <div className="actions">
                    <button onClick={handleRoll}>🎲 Roll</button>
                    <button onClick={handleReroll}>🔁 Reroll</button>
                    <button onClick={handleEndTurn}>⏭️ Terminar</button>
                </div>
            </div>

            {/* SIDEBAR DIREITA */}
            <div className="players-sidebar">
                <h3 className="players-title">Jogadores no Lobby</h3>

                {players.length === 0 ? (
                    <p className="no-players">Nenhum jogador encontrado.</p>
                ) : (
                    players.map((p, idx) => (
                        <div key={idx} className="player-card">
                            <span className="player-name">{p.username}</span>
                            <span className="player-credit">💰 {p.credit}</span>

                            <div className="player-dice">
                                <span>🎲 Dados: </span>
                                {p.dices.length > 0 ? p.dices.join(" - ") : "—"}
                            </div>

                            <div className="player-combination">
                                <span>✨ Combinação: </span>
                                <strong>{p.valueOfCombination}</strong>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}
