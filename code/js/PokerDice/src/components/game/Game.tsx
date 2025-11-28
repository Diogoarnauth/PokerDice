// src/components/game/Game.tsx
import React, { useEffect, useState } from "react";
import { gameService } from "../../services/api/Games";
import { isOk } from "../../services/api/utils";
import { GamePayload, Game } from "../models/Game";
import { useParams } from "react-router-dom";

export default function GamePage() {
    const { lobbyId } = useParams<{ lobbyId: string }>(); // supõe rota /games/lobby/:lobbyId
    const [game, setGame] = useState<Game | null>(null);
    const [gameId, setGameId] = useState<number | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [info, setInfo] = useState<string | null>(null);
    const [rerollInput, setRerollInput] = useState<string>("");
    const [currentPlayer, setCurrentPlayer] = useState<string | null>(null);


    const token = localStorage.getItem("token");
    const currentUsername = localStorage.getItem("username"); // ou de onde guardares

    useEffect(() => {
        console.log("Fetching lobby:", lobbyId);

        if (!lobbyId) {
            setError("Lobby id em falta.");
            setLoading(false);
            return;
        }

        async function fetchGame() {
            setLoading(true);
            setError(null);
            setInfo(null);

            const result = await gameService.getGameByLobbyId(Number(lobbyId));
            console.log("Fetching game:", result);

            if (isOk(result)) {
                const payload = new GamePayload(result.value);
                setGame(payload.game);
                setGameId(payload.game.id);

                // Buscar jogador atual via endpoint /games/{gameId}/player-turn
                try {
                    const gameIdFromPayload = payload.game.id;;
                    console.log("Fetching player turn do game:", gameIdFromPayload);

                    const turnRes = await gameService.getPlayerTurn(gameIdFromPayload);
                    console.log("Fetching player turn:", turnRes);

                    if (isOk(turnRes)) {
                        setCurrentPlayer(turnRes.value.username); // string com username
                    } else {
                        setInfo(turnRes.error ?? "Não foi possível obter o jogador atual.");
                    }
                } catch {
                    setInfo("Erro ao obter o jogador atual.");
                }
            } else {
                setError(result.error ?? "Erro a carregar o jogo.");
            }
            setLoading(false);
        }

        fetchGame();
    }, [lobbyId]);


    function ensureMyTurn(): boolean {
        if (!game || !currentUsername) return false;
        if (game.currentPlayerUsername !== currentUsername) {
            setInfo("Não é a tua vez de jogar.");
            return false;
        }
        setInfo(null);
        return true;
    }

    async function handleRoll() {
        if (!gameId || !ensureMyTurn()) return;

        if (!game?.isFirstRoll) {
            setInfo("Já fizeste o primeiro roll. Usa o reroll.");
            return;
        }

        const result = await gameService.roll(Number(gameId));
        if (isOk(result)) {
            const payload = new GamePayload(result.value);
            setGame(payload.game);
            setInfo("Dados rolados!");
        } else {
            setError(result.error ?? "Erro ao rolar dados.");
        }
    }

    async function handleReroll() {
        if (!gameId || !ensureMyTurn()) return;

        if (game?.isFirstRoll) {
            setInfo("Ainda não fizeste o primeiro roll.");
            return;
        }

        // transformar string "0,2,4" em [0,2,4]
        const diceMask = rerollInput
            .split(",")
            .map(s => s.trim())
            .filter(s => s !== "")
            .map(Number)
            .filter(n => !Number.isNaN(n));

        if (diceMask.length === 0) {
            setInfo("Indica pelo menos um índice de dado para reroll (ex: 0,2,4).");
            return;
        }

        const result = await gameService.reroll(Number(gameId), diceMask);
        if (isOk(result)) {
            const payload = new GamePayload(result.value);
            setGame(payload.game);
            setInfo("Reroll efetuado!");
        } else {
            setError(result.error ?? "Erro ao fazer reroll.");
        }
    }

    async function handleEndTurn() {
        if (!gameId || !ensureMyTurn()) return;

        const result = await gameService.endTurn(Number(gameId));
        if (isOk(result)) {
            const payload = new GamePayload(result.value);
            setGame(payload.game);
            setInfo("Jogada terminada, próximo jogador.");
        } else {
            setError(result.error ?? "Erro ao terminar jogada.");
        }
    }

    if (!token) {
        return <p>Não há utilizador autenticado.</p>;
    }

    if (loading) return <p>Loading game...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;
    if (!game) return <p>Jogo não encontrado.</p>;

    return (
        <div style={{ padding: 24 }}>
            <h1>Game #{game.id}</h1>

            <p>
                Jogador atual: <strong>{currentPlayer}</strong>
            </p>

            {info && <p style={{ color: "blue" }}>{info}</p>}

            <div style={{ margin: "16px 0" }}>
                <h3>Dados</h3>
                {game.dice.length === 0 ? (
                    <p>Ainda não há dados rolados.</p>
                ) : (
                    <p>{game.dice.join(" - ")}</p>
                )}
                {!game.isFirstRoll && (
                    <div style={{ marginTop: 8 }}>
                        <label>
                            Índices para reroll (ex: 0,2,4):
                            <input
                                type="text"
                                value={rerollInput}
                                onChange={e => setRerollInput(e.target.value)}
                                style={{ marginLeft: 8 }}
                            />
                        </label>
                    </div>
                )}
            </div>

            <div style={{ display: "flex", gap: 8 }}>
                <button onClick={handleRoll} disabled={loading}>
                    Roll (primeira jogada)
                </button>
                <button onClick={handleReroll} disabled={loading}>
                    Reroll
                </button>
                <button onClick={handleEndTurn} disabled={loading}>
                    Terminar jogada
                </button>
            </div>
        </div>
    );
}
