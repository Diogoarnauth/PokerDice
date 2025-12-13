import React, {useEffect, useState} from "react";
import {gameService} from "../../services/api/Games";
import {playersService} from "../../services/api/Players";

import {isOk} from "../../services/api/utils";
import {GamePayload, Game} from "../models/Game";
import {lobbyDetailsService} from "../../services/api/LobbyDetails"; // Importa o serviço para a API
import {playerProfileService} from "../../services/api/PlayerProfile";
import {useSSE} from "../../providers/SSEContext";
import {useParams, useNavigate} from "react-router-dom";
import {useAlert} from "../../providers/AlertContexts";
import {User} from "../lobby/LobbyDetails";
// Função para obter o token do cookie
/*function getTokenFromCookies() {
  const token = document.cookie.split(';').find(cookie => cookie.trim().startsWith('token='));
  return token ? token.split('=')[1] : null;
}*/

export default function GamePage() {
    const {lobbyId} = useParams<{ lobbyId: string }>(); // supõe rota /games/lobby/:lobbyId
    const [game, setGame] = useState<Game | null>(null);
    const [gameId, setGameId] = useState<number | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [info, setInfo] = useState<string | null>(null);
    const [rerollInput, setRerollInput] = useState<string>("");
    const [currentUser, setCurrentUser] = useState<User | null>(null);
    const [currentPlayer, setCurrentPlayer] = useState<string | null>(null);
    const {addHandler, removeHandler, updateTopic} = useSSE();
    const [players, setPlayers] = useState<{ username: string; credit: number }[]>([]);
    const {showAlert} = useAlert();

    const navigate = useNavigate();

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


            //TODO aqui vai carregar todos os players em vez de carregar so um...
            const meRes = await playerProfileService.getProfile();
            console.log("getMe:", meRes);
            if (isOk(meRes)) {
                setCurrentUser(meRes.value);
            } else {
                setError("Não foi possível obter o utilizador autenticado.");
                setLoading(false);
                return;
            }


            const playerListOnLobby = await playersService.getObjPlayersOnLobby(Number(lobbyId));

            if (isOk(playerListOnLobby)) {
                const mappedPlayers = playerListOnLobby.value.map((u: any) => ({
                    username: u.username,
                    credit: u.credit,
                }));

                setPlayers(mappedPlayers);
            } else {
                console.log("Erro ao buscar jogadores:", playerListOnLobby.error);
            }

            console.log("playerListOnLobby.value", playerListOnLobby.value)


            console.log("lobbyId", lobbyId)
            const result = await gameService.getGameByLobbyId(Number(lobbyId));
            console.log("Fetching game:", result);

            if (isOk(result)) {

                const currentTurnResult = await gameService.getCompleteCurrentTurn(Number(result.value.id))
                const payload = new GamePayload(currentTurnResult.value);

                const initialDiceArray = currentTurnResult.value.diceFaces
                    ? currentTurnResult.value.diceFaces.split(",")
                    : [];

                let boolean
                if (currentTurnResult.value.rollCount == 0) {
                    boolean = true
                } else {
                    boolean = false
                }

                setGame(payload.game);
                setGame({
                    ...game,
                    dice: initialDiceArray,
                    id: result.value.id,
                    isFirstRoll: boolean,
                });

                setGameId(result.value.id);

                updateTopic("lobbies");

                const handleGameUpdated = (data: any) => {
                    if (data.changeType === "ended" && data.lobbyId == lobbyId) {
                        navigate(`/lobbies`); // Redireciona para a página do jogo
                    }
                };
                addHandler("gameUpdated", handleGameUpdated); // Certifique-se de que o handler é registrado


                // Buscar jogador atual via endpoint /games/{gameId}/player-turn
                try {
                    console.log("gameIdgameIdgameIdgameIdgameId", gameId)
                    const turnRes = await gameService.getPlayerTurn(result.value.id);
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
        if (!game || !currentUser || currentUser.username != currentPlayer) {
            alert("Não é a tua vez de jogar");
            return false;
        }
        setInfo(null);
        return true;
    }

    async function handleRoll() {
        if (!gameId || !ensureMyTurn()) return;

        const result = await gameService.roll(Number(lobbyId));
        console.log("Rolling game:", result);

        if (isOk(result)) {
            const diceArray = result.value.dice.split(",");

            if (!game) return;

            //TODO perceber se o useState game quando é atualizado se perde o valor ou não
            console.log("gameee", game)
            setGame({
                ...game,
                dice: diceArray,
                isFirstRoll: false,
            });
            console.log("game testezinho maroto", game)

            setInfo("Dados rolados!");
        } else {
            alert("Só podes dar roll uma vez, experimenta Reroll")
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

            setGame({
                ...game,
                dice: result.value.dice,
                isFirstRoll: false,
            });
            console.log("game no reroll", game)
            setInfo("Reroll efetuado!");
        } else {
            setError(result.error ?? "Erro ao fazer reroll.");
        }
    }

    async function handleEndTurn() {
        if (!gameId || !ensureMyTurn()) return;

        if (game && game.isFirstRoll) {
            showAlert("Tens de rodar os dados pelo menos uma vez antes de terminar!", "error");
            return;
        }
        const result = await gameService.endTurn(Number(gameId));
        console.log("result", result.value)

        if (isOk(result)) {
            if (result.value.winners) {
                const winnersString = result.value.winners.join(", ");
                alert(result.value.message + " winner: " + winnersString)
            }

            //TODO implementar isto de forma a que nao gere um novo objeto game...
            //console.log("<Estudo> result.value", result.value)
            //const payload = new GamePayload(result.value);
            //console.log("payloaddddd", payload)
            console.log("game.id", game.id)
            const turnRes = await gameService.getPlayerTurn(game.id);
            console.log("Fetching player turn minions:", turnRes);

            if (isOk(turnRes)) {
                setCurrentPlayer(turnRes.value.username); // string com username
            } else {
                setInfo(turnRes.error ?? "Não foi possível obter o jogador atual.");
            }
            setGame({
                ...game,
                dice: [],
                isFirstRoll: true,
            });
            setInfo("Jogada terminada, próximo jogador.");
        } else {
            const errorMsg = typeof result.error === 'object'
                ? (result.error.detail || result.error.title || "Erro ao terminar jogada.")
                : String(result.error);

            showAlert(errorMsg, "error");
            setError(errorMsg);
        }

    }

    if (loading) return <p>Loading game...</p>;
    if (error) return <p style={{color: "red"}}>{error}</p>;
    if (!game) return <p>Jogo não encontrado.</p>;

    const safeDice = Array.isArray(game.dice) ? game.dice : [game.dice || ""]; // Garante que seja sempre um array de strings
    console.log("info:", info);


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
                        {typeof info === "object" ? `${info.title}: ${info.detail}` : info}
                    </div>
                )}

                <div className="dice-section">
                    <h3>Dados</h3>

                    {safeDice && safeDice.length > 0 ? (
                        <div className="dice-display">
                            {safeDice.join(" - ")}
                        </div>
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
                                    onChange={e => setRerollInput(e.target.value)}
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
                        </div>
                    ))
                )}
            </div>

        </div>
    );
}