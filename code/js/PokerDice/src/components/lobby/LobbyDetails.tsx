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

// Se o teu User vem de outro lado, podes importar, mas mantive aqui para facilitar
export type User = {
    id: number;
    username: string;
    name: string;
    age: number;
    credit: number;
    winCounter: number;
    lobbyId: number | null;
};

// Helper de formatação
function formatTurnTime(turnTime: string): string {
    const regex = /^PT(\d+)M$/;
    const match = regex.exec(turnTime);
    if (match) {
        return `${match[1]} min`;
    }
    // Fallback para segundos se necessário ou formato raw
    return turnTime;
}

export default function LobbyDetails() {
    // --- HOOKS ---
    const {id} = useParams<{ id: string }>();
    const lobbyId = Number(id);
    const navigate = useNavigate();

    const {username: authUsername} = useAuthentication(); // Só para saber se estamos logados
    const {addHandler, removeHandler, updateTopic} = useSSE();
    const {showAlert} = useAlert();

    // --- ESTADO ---
    const [lobby, setLobby] = useState<Lobby | null>(null);
    const [user, setUser] = useState<User | null>(null); // O utilizador atual
    const [owner, setOwner] = useState<User | null>(null); // O dono do lobby

    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(false); // Para botões (Join/Leave/Start)
    const [error, setError] = useState<string | null>(null);

    const hasLoadedSuccessfully = useRef(false);

    // --- CARREGAR DADOS ---
    const loadData = useCallback(async () => {
        try {
            const userResponse = await lobbyDetailsService.getMe();
            if (isOk(userResponse)) {
                setUser(userResponse.value);
            } else {
                return;
            }

            const lobbyResponse = await lobbyDetailsService.getLobby(lobbyId);

            if (isOk(lobbyResponse)) {
                const lobbyData = lobbyResponse.value;
                setLobby(lobbyData);
/*
                const gameCheck = await gameService.getGameByLobbyId(lobbyId);

                // TODO("AVALIAR SE PERDEU SSE AQUI??")

                if (isOk(gameCheck)) {
                    navigate(`/games/lobby/${lobbyId}`);
                    return;
                }
*/
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
        if (!authUsername) return; // Espera ter autenticação

        loadData();
        updateTopic("lobbies");

        const handleUpdates = (data: any) => {
            console.log("SSE Update no LobbyDetails:", data);

            if (data.changeType === 'deleted' && data.lobbyId === lobbyId) {
                setLobby(null);
                setError("The host has closed this lobby.");
                console.log("SSE: Lobby was deleted, the page reflects this.");
                return;
            }

            if (data.lobbyId === lobbyId || data.changeType === "updated") {
                loadData();
            }

            if (data.changeType === "game_started" && data.lobbyId === lobbyId) {
                navigate(`/games/lobby/${lobbyId}`);
            }
        };

        addHandler("lobbies_list_changes", handleUpdates);

        return () => {
            removeHandler("lobbies_list_changes");
        };
    }, [authUsername, lobbyId, loadData, updateTopic, addHandler, removeHandler, navigate]);

    // --- AÇÕES ---

    async function handleJoinLobby() {
        setActionLoading(true);


        const res = await lobbyDetailsService.joinLobby(lobbyId);

        if (isOk(res)) {
            showAlert("You have joined the lobby successfully!", "success");
            loadData(); // Recarrega para atualizar estado do user e contadores

            updateTopic("lobbies");

            const handleGameStarted = (data: any) => {
                console.log("epa entrei finalmente");

                console.log("SSE Update (gameStarted):", data);
                console.log("data.gameId", data.gameId);

                if (data.changeType === "started" && data.lobbyId == lobbyId) {
                    navigate(`/games/lobby/${lobbyId}`); // Redireciona para a página do jogo
                }
            };
            console.log("antes de criar o addHandler")
            addHandler("gameStarted", handleGameStarted); // Certifique-se de que o handler é registrado

        } else {
            // Tratamento de Erro Robusto
            const p = res.error;
            showAlert(p.detail || p.title || "Failed to join the lobby.", "error");
        }
        setActionLoading(false);
    }

    async function handleLeaveLobby() {
        setActionLoading(true);
        const res = await lobbyDetailsService.leaveLobby(lobbyId);
        console.log("res", res)
        if (isOk(res)) {
            showAlert("You have left the lobby.", "info");
            // Se saiu, se calhar quer ir para a lista de lobbies
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
            // O redirecionamento pode ser feito aqui ou via SSE (game_started)
            // Por segurança fazemos aqui também:
            setTimeout(() => {
                navigate(`/games/lobby/${lobbyId}`);
            }, 500);
        } else {
            const p = res.error;
            // Exemplo: "Not enough players"
            showAlert(p.detail || p.title || "Could not start the game.", "error");
        }
        setActionLoading(false);
    }

    // --- RENDER ---

    if (loading) {
        return (
            <div className="lobbyDetails-loading">
                <div className="lobbyDetails-spinner" />
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

    // Lógica de Estado do Utilizador
    const isUserInLobby = user?.lobbyId === lobby.id;
    const isUserHost = user?.id === lobby.hostId;
    // Se o user já estiver noutro lobby, não pode entrar neste
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