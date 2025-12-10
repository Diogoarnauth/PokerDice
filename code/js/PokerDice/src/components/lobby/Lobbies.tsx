import React, {useEffect, useState, useCallback} from "react";
import {useNavigate} from 'react-router-dom';

import {lobbiesService} from "../../services/api/Lobbies";
import {playersService} from "../../services/api/Players";
import {useAuthentication} from "../../providers/authentication";

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
                                title: "Serviço Indisponível",
                                detail: problem.detail || problem.title || "Erro de conexão com o servidor."
                            }
                        }
                    });
                } else {
                    // Erro ligeiro -> Alerta flutuante
                    // Garante que passas uma STRING para o showAlert
                    showAlert(`Erro: ${problem.title || "Falha ao carregar"}`, "error");
                }
            }
        } catch (error) {
            // Erro Fatal de JavaScript/Rede
            navigate('/error', {
                state: {
                    error: {
                        status: 503,
                        title: "Sem Conexão",
                        detail: "Não foi possível contactar o servidor."
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

    if (authLoading || loading && lobbies.length === 0) return <p className="p-8 text-center">A carregar lobbies...</p>;

    return (
        <div>
            <h1 className="text-2xl font-bold mb-4">Lobbies Disponíveis</h1>

            {lobbies.length === 0 && <p>Não há salas disponíveis.</p>}

            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {lobbies.map((lobby) => (
                    <div key={lobby.id} className="border p-4 rounded shadow bg-white hover:shadow-lg transition">
                        <h2 className="font-bold text-lg">{lobby.name}</h2>
                        <p className="text-gray-600 text-sm mb-2">{lobby.description}</p>
                        <div className="text-sm flex justify-between mt-4">
                            <span>👥 {lobby.currentPlayers} / {lobby.maxUsers}</span>
                            <span className="font-bold text-green-600">💰 {lobby.minCreditToParticipate}</span>
                        </div>
                        <button
                            onClick={() => handleEnterLobby(lobby.id)}
                            className="mt-4 w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
                        >
                            Entrar
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}