import React, {useEffect, useState, useCallback, useRef} from "react";
import {useParams, useNavigate} from "react-router-dom";
import {lobbyDetailsService} from "../../services/api/LobbyDetails";
import {gameService} from "../../services/api/Games";
import {isOk} from "../../services/api/utils";
import {useAuthentication} from "../../providers/Authentication";
import {useSSE} from "../../providers/SSEContext";
import {useAlert} from "../../providers/AlertContexts";
import "../../styles/LobbyDetails.css";

// --- TIPOS ---
type Lobby = {
    id: number;
    name: string;
    description: string;
    hostId: number;
    minUsers: number;
    maxUsers: number;
    rounds: number;
    minCreditToParticipate: number;
    isRunning: boolean;
    turnTime: string;
};

export type User = {
    id: number;
    username: string;
    name: string;
    age: number;
    credit: number;
    winCounter: number;
    lobbyId: number | null;
};

function formatTurnTime(turnTime: string): string {
    const regex = /^PT(\d+)M$/;
    const match = regex.exec(turnTime);
    if (match) {
        return `${match[1]} min`;
    }
    return turnTime;
}

export default function LobbyDetails() {
    const {id} = useParams<{ id: string }>();
    const lobbyId = Number(id);
    const navigate = useNavigate();

    const {username: authUsername} = useAuthentication();
    const {addHandler, removeHandler, updateTopic} = useSSE();
    const {showAlert} = useAlert();

    const [lobby, setLobby] = useState<Lobby | null>(null);
    const [user, setUser] = useState<User | null>(null);
    const [owner, setOwner] = useState<User | null>(null);

    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const hasLoadedSuccessfully = useRef(false);

    // --- CARREGAR DADOS ---
    const loadData = useCallback(async () => {
        try {
            const userResponse = await lobbyDetailsService.getMe();
            let currentUser: User | null = null;

            if (isOk(userResponse)) {
                currentUser = userResponse.value;
                setUser(currentUser);
            } else {
                return;
            }

            const lobbyResponse = await lobbyDetailsService.getLobby(lobbyId);

            if (isOk(lobbyResponse)) {
                const lobbyData = lobbyResponse.value;
                setLobby(lobbyData);

                const amInLobby = currentUser && currentUser.lobbyId === lobbyId;

                if (amInLobby) {
                    const gameCheck = await gameService.getGameByLobbyId(lobbyId);

                    if (isOk(gameCheck)) {
                        console.log("Sou jogador e o jogo existe. A redirecionar...");
                        navigate(`/games/lobby/${lobbyId}`);
                        return;
                    }
                }

                hasLoadedSuccessfully.current = true;
                setError(null);

                const ownerResponse = await lobbyDetailsService.getOwner(lobbyData.hostId);
                if (isOk(ownerResponse)) {
                    setOwner(ownerResponse.value);
                }
            } else {
                const problem = lobbyResponse.error;
                if (problem.status === 404) {
                    // TODO("404 é not found será que faz sentido generalizar todos aqui ?")
                    if (hasLoadedSuccessfully.current) {
                        setError("The host has closed this lobby.");
                        setLobby(null);
                    } else {
                        setError("Lobby not found.");
                    }
                } else {
                    setError("Error loading lobby details.");
                }
            }
        } catch (err) {
            setError("Connection error.");
        } finally {
            setLoading(false);
        }
    }, [lobbyId, navigate]);

    useEffect(() => {
        if (!authUsername) return;

        loadData();
        updateTopic("lobbies");

        const handleUpdates = (data: any) => {
            console.log("SSE Lobby Update:", data);

            if (data.changeType === 'deleted' && data.lobbyId === lobbyId) {
                setLobby(null);
                setError("The host has closed this lobby.");
                return;
            }

            if (data.lobbyId === lobbyId || data.changeType === "updated") {
                loadData();
            }
        };

        const handleGameStart = (data: any) => {
            console.log("SSE Game Start:", data);
            if (data.lobbyId === lobbyId) {
                navigate(`/games/lobby/${lobbyId}`);
            }
        };

        addHandler("lobbies_list_changes", handleUpdates);
        addHandler("gameStarted", handleGameStart);

        return () => {
            removeHandler("lobbies_list_changes");
            removeHandler("gameStarted");
        };
    }, [authUsername, lobbyId, loadData, updateTopic, addHandler, removeHandler, navigate]);


    async function handleJoinLobby() {
        setActionLoading(true);

        const res = await lobbyDetailsService.joinLobby(lobbyId);

        if (isOk(res)) {
            showAlert("You have joined the lobby successfully!", "success");
            loadData();
            updateTopic("lobbies");

        } else {
            const p = res.error;
            showAlert(p.detail || p.title || "Failed to join the lobby.", "error");
        }
        setActionLoading(false);
    }

    async function handleLeaveLobby() {
        setActionLoading(true);
        const res = await lobbyDetailsService.leaveLobby(lobbyId);

        if (isOk(res)) {
            showAlert("You have left the lobby.", "info");
            navigate("/lobbies");
        } else {
            const p = res.error;
            showAlert(p.detail || p.title || "Failed to leave the lobby.", "error");
            setActionLoading(false);
        }
    }

    async function handleStartGame() {
        setActionLoading(true);
        const res = await gameService.startGame(lobbyId);

        if (isOk(res)) {
            showAlert("The game is about to start!", "success");
            setTimeout(() => {
                navigate(`/games/lobby/${lobbyId}`);
            }, 500);
        } else {
            const p = res.error;
            showAlert(p.detail || p.title || "Could not start the game.", "error");
        }
        setActionLoading(false);
    }


    if (loading) {
        return (
            <div className="lobbyDetails-loading">
                <div className="lobbyDetails-spinner"/>
            </div>
        );
    }

    if (error || !lobby) {
        return (
            <div className="lobbyDetails-error-card">
                <h2>Erro</h2>
                <p>{error || "Lobby not Found."}</p>
                <button
                    onClick={() => navigate("/lobbies")}
                    className="lobbyDetails-back-button"
                >
                    Voltar aos Lobbies
                </button>
            </div>
        );
    }

    const isUserInLobby = user?.lobbyId === lobby.id;
    const isUserHost = user?.id === lobby.hostId;
    const canJoin = user && user.lobbyId === null;

    return (
        <div className="lobbyDetails-page">
            <div className="lobbyDetails-card">
                <h1 className="lobbyDetails-title">{lobby.name}</h1>
                <p className="lobbyDetails-description">{lobby.description}</p>

                <div className="lobbyDetails-info">
                    <p><span>Host</span><strong>{owner?.username || "Unknown"}</strong></p>
                    <p><span>Players</span><strong>{lobby.minUsers} - {lobby.maxUsers}</strong></p>
                    <p><span>Rounds</span><strong>{lobby.rounds}</strong></p>
                    <p><span>Entry Cost</span><strong>{lobby.minCreditToParticipate}</strong></p>
                    <p><span>Turn Time</span><strong>{formatTurnTime(lobby.turnTime)}</strong></p>
                </div>

                <div className="lobbyDetails-actions">
                    {canJoin && (
                        <button
                            onClick={handleJoinLobby}
                            disabled={actionLoading}
                            className="lobbyDetails-button primary"
                        >
                            {actionLoading ? "Joining..." : "Join Lobby"}
                        </button>
                    )}

                    {isUserInLobby && (
                        <button
                            onClick={handleLeaveLobby}
                            disabled={actionLoading}
                            className="lobbyDetails-button secondary"
                        >
                            {actionLoading ? "Leaving..." : "Leave Lobby"}
                        </button>
                    )}

                    {isUserHost && (
                        <button
                            onClick={handleStartGame}
                            disabled={actionLoading}
                            className="lobbyDetails-button accent"
                        >
                            {actionLoading ? "Starting..." : "Start Game"}
                        </button>
                    )}
                </div>

                {!canJoin && !isUserInLobby && (
                    <p className="lobbyDetails-warning">
                        You are already in another lobby.
                    </p>
                )}
            </div>
        </div>
    );
}