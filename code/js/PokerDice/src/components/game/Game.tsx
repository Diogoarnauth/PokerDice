// src/components/game/Game.tsx
import React, { useEffect, useState } from "react";
import { gameService } from "../../services/api/Games";
import { isOk } from "../../services/api/utils";
import { GamePayload, Game } from "../models/Game";
import { useParams } from "react-router-dom";

export default function GamePage() {
    const { gameId } = useParams<{ gameId: string }>(); // supõe rota /games/:gameId
    const [game, setGame] = useState<Game | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [info, setInfo] = useState<string | null>(null);
    const [rerollInput, setRerollInput] = useState<string>("");

    const token = localStorage.getItem("token");
    const currentUsername = localStorage.getItem("username"); // ou de onde guardares

    useEffect(() => {
        if (!gameId) {
            setError("Game id em falta.");
            setLoading(false);
            return;
        }

        async function fetchGame() {
            setLoading(true);
            setError(null);
            setInfo(null);

            const result = await gameService.getById(Number(gameId));
            if (isOk(result)) {
                const payload = new GamePayload(result.value);
                setGame(payload.game);
            } else {
                setError(result.error ?? "Erro a carregar o jogo.");
            }
            setLoading(false);
        }

        fetchGame();
    }, [gameId]);

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
                Jogador atual: <strong>{game.currentPlayerUsername}</strong>
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
