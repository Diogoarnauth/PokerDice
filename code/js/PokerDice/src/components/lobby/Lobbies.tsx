import React, {useEffect, useState, useCallback} from "react";
import {useNavigate} from 'react-router-dom';
import './Lobbies.css';

import {lobbiesService} from "../../services/api/Lobbies";
import {playersService} from "../../services/api/Players";
import {useAuthentication} from "../../providers/Authentication";

import {useSSE} from "../../providers/SSEContext";
import {useAlert} from "../../providers/AlertContexts";
import {isOk} from "../../services/api/utils";

type LobbyWithPlayers = {
    id: number;
    name: string;
    description: string;
    hostId: number;
    minUsers: number;
    maxUsers: number;
    rounds: number;
    minCreditToParticipate: number;
    isRunning?: boolean;
    currentPlayers: number;
};

export default function LobbiesList() {
    const {addHandler, updateTopic, removeHandler} = useSSE();
    const {showAlert} = useAlert();
    const {username, isLoading: authLoading} = useAuthentication();
    const navigate = useNavigate();

    const [lobbies, setLobbies] = useState<LobbyWithPlayers[]>([]);
    const [loading, setLoading] = useState(true);

    // Função para carregar lobbies
    const loadLobbies = useCallback(async () => {
        // setLoading(true); //         setLoading(prev => prev && lobbies.length === 0);

        setLoading(prev => prev && lobbies.length === 0);

        try {
            const response = await lobbiesService.getLobbies();

            if (isOk(response)) {
                const baseLobbies = response.value;
                const enriched = await Promise.all(
                    baseLobbies.map(async (lobby) => {
                        try {
                            const playersResponse = await playersService.getPlayerCount(lobby.id);
                            return {
                                ...lobby,
                                currentPlayers: isOk(playersResponse) ? playersResponse.value.count : 0
                            } as LobbyWithPlayers;
                        } catch (e) {
                            return {...lobby, currentPlayers: 0} as LobbyWithPlayers;
                        }
                    })
                );
                setLobbies(enriched);
            } else {
                // Tratamento de erro da API
                const problem = response.error;

                const status = problem.status || 0;
                const isCritical = status >= 500 || status === 0;

                if (isCritical) {
                    navigate('/error', {
                        state: {
                            error: {
                                status: status || 503,
                                title: "Service Unavailable",
                                detail: problem.detail || problem.title || "Server connection error."
                            }
                        }
                    });
                } else {
                    // Erro ligeiro -> Alerta flutuante
                    // Garante que passas uma STRING para o showAlert
                    showAlert(`Error: ${problem.title || "Failed to load"}`, "error");
                }
            }
        } catch (error) {
            // Erro Fatal de JavaScript/Rede
            navigate('/error', {
                state: {
                    error: {
                        status: 503,
                        title: "No Connection",
                        detail: "Could not contact the server."
                    }
                }
            });
        } finally {
            setLoading(false);
        }
    }, [navigate, showAlert,]);

    useEffect(() => {

        if (!authLoading && !username) {
            navigate("/login");
        }
    }, [authLoading, username, navigate]);

    useEffect(() => {
        if (authLoading || !username) return;
        updateTopic("lobbies");
        loadLobbies();

        const handleChanges = (data: any) => {
            if (data.changeType === "created" || data.changeType === "deleted") {
                console.log("SSE Update:", data);
                loadLobbies();
            }
        };

        addHandler("lobbies_list_changes", handleChanges);

        return () => {
            removeHandler("lobbies_list_changes");
        };
    }, [username, navigate, authLoading, updateTopic, addHandler, removeHandler, loadLobbies]);

    async function handleEnterLobby(lobbyId: number) {
        navigate(`/lobbies/${lobbyId}/info`);
    }

    if (authLoading || loading && lobbies.length === 0) return <p className="p-8 text-center">Loading lobbies...</p>;

    return (
        <div className="lobbies-page">
            <div className="lobbies-header">
                <h1 className="lobbies-title">Available Lobbies</h1>
                <p className="lobbies-subtitle">
                    Choose your table and join the game.
                </p>
            </div>

            {lobbies.length === 0 && (
                <p className="lobbies-empty">There are no rooms available.</p>
            )}

            <div className="lobbies-grid">
                {lobbies.map((lobby) => (
                    <div key={lobby.id} className="lobby-card">
                        <h2 className="lobby-card-title">{lobby.name}</h2>
                        <p className="lobby-card-description">{lobby.description}</p>

                        <div className="lobby-card-meta">
                        <span className="lobby-meta-item">
                            👥 {lobby.currentPlayers} / {lobby.maxUsers}
                        </span>
                            <span className="lobby-meta-item lobby-meta-credit">
                            💰 {lobby.minCreditToParticipate}
                        </span>
                        </div>

                        <button
                            onClick={() => handleEnterLobby(lobby.id)}
                            className="lobby-card-button"
                        >
                            Enter Lobby
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}