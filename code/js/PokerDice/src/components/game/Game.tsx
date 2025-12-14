import React, {useEffect, useState, useCallback} from "react";
import {gameService} from "../../services/api/Games";
import {playersService} from "../../services/api/Players";

import {isOk} from "../../services/api/utils";
import {GamePayload, Game} from "../models/Game";
import {playerProfileService} from "../../services/api/PlayerProfile";
import {useSSE} from "../../providers/SSEContext";
import {useParams, useNavigate} from "react-router-dom";
import "../../styles/Game.css";

export default function GamePage() {
    const {lobbyId} = useParams<{ lobbyId: string }>();
    const [game, setGame] = useState<Game | null>(null);
    const [gameId, setGameId] = useState<number | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [info, setInfo] = useState<string | null>(null);
    const [rerollInput, setRerollInput] = useState<string>("");
    const [currentUser, setCurrentUser] = useState<User | null>(null);
    const [currentPlayer, setCurrentPlayer] = useState<string | null>(null);
    const {addHandler, removeHandler, updateTopic} = useSSE();
    const [roundNumber, setRoundNumber] = useState<number>(1);

    type PlayerInGame = {
        playerId: number;
        username: string;
        credit: number;
        dices: string[];
        valueOfCombination: number;
    };

    const [players, setPlayers] = useState<PlayerInGame[]>([]);
    const navigate = useNavigate();

    const fetchGame = useCallback(async (showLoading = true) => {
                if (showLoading) {
                    setLoading(true);
                    setError(null);
                    setInfo(null);
                }
                if (!lobbyId) return;

                const meRes = await playerProfileService.getProfile();
                if (isOk(meRes)) setCurrentUser(meRes.value);
                else {
                    setError("Não foi possível obter o utilizador autenticado.");
                    setLoading(false);
                    return;
                }

                if (meRes.value.lobbyId != lobbyId) {
                    alert("You are not part of this game")
                    navigate(`/lobbies`);
                    return;
                }

                const playerListOnLobby = await playersService.getObjPlayersOnLobby(Number(lobbyId));

                console.log("playerListOnLobby", playerListOnLobby)

                let mappedPlayers: PlayerInGame[] = [];

                if (isOk(playerListOnLobby)) {
                    mappedPlayers = playerListOnLobby.value.players.map((u: any) => ({
                        playerId: u.id,
                        username: u.username,
                        credit: u.credit,
                        dices: [],
                        valueOfCombination: 0,
                    }));

                    setPlayers(mappedPlayers);
                }

                const result = await gameService.getGameByLobbyId(Number(lobbyId));
                console.log("result", result)
                if (!isOk(result)) {
                    setError(result.error ?? "Erro ao carregar o jogo.");
                    setLoading(false);
                    return;
                }

                const currentTurnResult = await gameService.getCompleteCurrentTurn(result.value.id);
                const payload = new GamePayload(currentTurnResult.value);

                const initialDiceArray = currentTurnResult.value.diceFaces
                    ? currentTurnResult.value.diceFaces.split(",")
                    : [];

                setGame(prev => ({
                    ...payload.game,
                    dice: initialDiceArray,
                    id: result.value.id,
                    isFirstRoll: currentTurnResult.value.rollCount === 0,
                }));

                setGameId(result.value.id);
                updateTopic("lobbies");


                const currentRoundRes = await gameService.getCurrentRound(result.value.id);

                if (isOk(currentRoundRes)) {
                    const roundId = currentRoundRes.value.roundId ?? currentRoundRes.value.id;
                    setRoundNumber(currentRoundRes.value.roundNumber);

                    const turnsRes = await gameService.getAllTurnsByRound(roundId);
                    if (isOk(turnsRes)) {
                        const turns = turnsRes.value;

                        const updatedPlayers = mappedPlayers.map(player => {
                            const turn = turns.value.find((t: any) => t.playerId === player.playerId);

                            if (!turn) return player;

                            return {
                                ...player,
                                dices: turn.diceFaces ? turn.diceFaces.split(",") : [],
                                valueOfCombination: turn.value_of_combination ?? 0,
                            };
                        });

                        setPlayers(updatedPlayers);
                    }
                }

                const turnRes = await gameService.getPlayerTurn(result.value.id);
                if (isOk(turnRes)) setCurrentPlayer(turnRes.value.username);

                setLoading(false);
            }
            ,
            [lobbyId, navigate, updateTopic]
        )
    ;

    useEffect(() => {
        fetchGame(true);

        const handleGameUpdated = (data: any) => {
            if (String(data.lobbyId) !== String(lobbyId)) return;

            if (data.changeType === "ended") {
                navigate(`/lobbies`);
                return;
            }

            if ((data.changeType === "turn_ended" || data.changeType === "reroll_dice" || data.changeType === "roll_dice")) {
                fetchGame(false);
            }

            if (data.changeType === "host_runned_out_of_credits") {
                alert("Host ran out of credits, the lobby will be closed...");
                navigate(`/lobbies`);
            }
        };

        const handleRoundWinners = (data: any) => {
            if (String(data.lobbyId) !== String(lobbyId)) return;

            let winnersText = "";
            try {
                const winnersArray = JSON.parse(data.winners) as string[];
                if (Array.isArray(winnersArray) && winnersArray.length > 0) {
                    winnersText = winnersArray.join(", ");
                } else {
                    winnersText = "No winners";
                }
            } catch (e) {
                winnersText = String(data.winners);
            }

            if (data.changeType === "roundWinners") {
                fetchGame(false);
                alert(`Winner(s) of this round: ${winnersText}`);
            } else if (data.changeType === "gameWinners") {
                alert(`Winner(s) of the game: ${winnersText}`);
            }
        };

        addHandler("gameUpdated", handleGameUpdated);
        addHandler("winnerAlert", handleRoundWinners);

        return () => {
            removeHandler("gameUpdated");
            removeHandler("winnerAlert");
        };
    }, [fetchGame, addHandler, removeHandler, lobbyId, navigate]);

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

        if (isOk(result)) {
            const diceArray = result.value.dice.split(",");
            setGame(prev => ({
                ...prev!,
                dice: diceArray,
                isFirstRoll: false,
            }));
            setInfo("Dados rolados!");
        } else {
            alert("Só podes dar roll uma vez!");
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
            .map(s => Number(s.trim()))
            .filter(n => !Number.isNaN(n));

        console.log("diceMask", diceMask)
        if (diceMask.length === 0) {
            setInfo("Indica pelo menos um índice para reroll (ex: 0,2,4).");
            return;
        }

        const result = await gameService.reroll(Number(lobbyId), diceMask);

        if (isOk(result)) {
            setGame(prev => ({
                ...prev!,
                dice: result.value.dice,
                isFirstRoll: false,
            }));

            setInfo("Reroll efetuado!");
        } else {
            alert("Não podes fazer (mais) rerolls.");
            //setError(result.error ?? "Erro ao fazer reroll.");
        }
    }

    async function handleEndTurn() {
        if (!gameId || !ensureMyTurn()) return;

        const result = await gameService.endTurn(Number(gameId));

        if (isOk(result)) {

            const turnRes = await gameService.getPlayerTurn(game.id);
            if (isOk(turnRes)) {
                setCurrentPlayer(turnRes.value.username);
            }

            setGame(prev => ({
                ...prev!,
                dice: [],
                isFirstRoll: true,
            }));

            setInfo("Jogada terminada.");
        } else {
            alert("Não podes terminar sem jogar ao menos 1 vez.");
        }
    }

    if (loading) return <p>Loading game...</p>;
    if (error) return <p style={{color: "red"}}>{error}</p>;
    if (!game) return <p>Jogo não encontrado.</p>;

    const safeDice = Array.isArray(game.dice) ? game.dice : [];

    return (
        <div className="game-container">
            <div className="game-left">
                <h1 className="game-title">Game #{game.id}</h1>

                <div className="round-info">
                    <span>{roundNumber}ª Ronda</span>
                </div>

                <p className="current-player">
                    Jogador atual: <strong>{currentPlayer}</strong>
                </p>

                <div className="dice-section">
                    <h3>Dados</h3>
                    {safeDice.length > 0 ? (
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
                                    onChange={e => setRerollInput(e.target.value)}
                                />
                            </label>
                        </div>
                    )}
                </div>

                <div className="actions">
                    <button onClick={handleRoll}>🎲 Roll</button>
                    <button
                        onClick={handleReroll}
                        disabled={rerollInput.trim() === ""}   // ⬅️ aqui está a magia
                    >
                        🔁 Reroll
                    </button>
                    <button onClick={handleEndTurn}>⏭️ Terminar</button>
                </div>

            </div>

            <div className="players-sidebar">
                <h3 className="players-title">Jogadores no Lobby</h3>

                {players.map((p, idx) => (
                    <div key={idx} className="player-card">
                        <span className="player-name">{p.username}</span>
                        <span className="player-credit">💰 {p.credit}</span>

                        <div className="player-dice">
                            🎲 Dados: {p.dices.length ? p.dices.join(" - ") : "—"}
                        </div>

                        <div className="player-combination">
                            ✨ Combinação: <strong>{p.valueOfCombination}</strong>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
