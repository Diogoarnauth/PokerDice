import React, { useEffect, useState } from "react";
import { gameService } from "../../services/api/Games";
import { isOk } from "../../services/api/utils";
import { GamePayload, Game } from "../models/Game";
import { lobbyDetailsService } from "../../services/api/LobbyDetails"; // Importa o serviço para a API
import {playerProfileService} from "../../services/api/PlayerProfile";


import { useParams } from "react-router-dom";

// Função para obter o token do cookie
/*function getTokenFromCookies() {
  const token = document.cookie.split(';').find(cookie => cookie.trim().startsWith('token='));
  return token ? token.split('=')[1] : null;
}*/

export default function GamePage() {
    const { lobbyId } = useParams<{ lobbyId: string }>(); // supõe rota /games/lobby/:lobbyId
    const [game, setGame] = useState<Game | null>(null);
    const [gameId, setGameId] = useState<number | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [info, setInfo] = useState<string | null>(null);
    const [rerollInput, setRerollInput] = useState<string>("");
    const [currentUser, setCurrentUser] = useState<User | null>(null);
    const [currentPlayer, setCurrentPlayer] = useState<string | null>(null);

    // Obter o token diretamente dos cookies

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


            const meRes = await playerProfileService.getProfile();
            console.log("getMe:", meRes);
            if (isOk(meRes)) {
                setCurrentUser(meRes.value);
            } else {
                setError("Não foi possível obter o utilizador autenticado.");
                setLoading(false);
                return;
            }

            const result = await gameService.getGameByLobbyId(Number(lobbyId));
            console.log("Fetching game:", result);

            if (isOk(result)) {
                const payload = new GamePayload(result.value);
                console.log("payload", payload)
                setGame(payload.game);
                setGameId(payload.game.id);

                // Buscar jogador atual via endpoint /games/{gameId}/player-turn
                try {
                    const gameIdFromPayload = payload.game.id;
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
       console.log("game", game)
       console.log("currentUser", currentUser)
       console.log("currentPlayer", currentPlayer)

       if (!game || !currentUser || currentUser.username != currentPlayer) {
           alert("Não é a tua vez de jogar");
           return false;
       }
       console.log("game", game)

       setInfo(null);
       return true;
   }

    async function handleRoll() {
        if (!gameId || !ensureMyTurn()) return;

        console.log("lobbyId", lobbyId)
        const result = await gameService.roll(Number(lobbyId));
        console.log("Rolling game:", result);

        if (isOk(result)) {
            const diceArray = result.value.dice.split(",");

            if (!game) return;

            console.log("gameee", game)
            setGame({
                ...game,
                dice: diceArray,
                isFirstRoll: false,
            });
            console.log("game testezinho maroto", game)

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

        const diceMask = rerollInput
            .split(",")
            .map(s => s.trim())
            .filter(s => s !== "")
            .map(Number)
            .filter(n => !Number.isNaN(n));

        console.log("diceMask", diceMask)

        if (diceMask.length === 0) {
            setInfo("Indica pelo menos um índice de dado para reroll (ex: 0,2,4).");
            return;
        }

        const result = await gameService.reroll(Number(lobbyId), diceMask);
        console.log("resultado", result)

        if (isOk(result)) {
            const payload = new GamePayload(result.value); //aqui substituir apenas o campo dices e nao tudo.
            console.log("payload", payload)
            setGame(payload.game);
            setInfo("Reroll efetuado!");
        } else {
            setError(result.error ?? "Erro ao fazer reroll.");
        }
    }

    async function handleEndTurn() {
        if (!gameId || !ensureMyTurn()) return;

        const result = await gameService.endTurn(Number(gameId));
        console.log("result", result.value.winners)

        if (isOk(result)) {
            if(result.value.winners){
                console.log("ahhhhhhhhhhhhhhhhhhhhhhhhhhhh")
                const winnersString = result.value.winners.join(", ");
                alert(result.value.message + " winner: " + winnersString)
            }

            const payload = new GamePayload(result.value);
            setGame(payload.game);
            setInfo("Jogada terminada, próximo jogador.");
        } else {
            setError(result.error ?? "Erro ao terminar jogada.");
        }

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