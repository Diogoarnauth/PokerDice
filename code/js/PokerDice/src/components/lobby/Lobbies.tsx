import React, { useEffect, useState } from "react";
import { lobbiesService } from "../../services/api/Lobbies";
import { playersService } from "../../services/api/Players";
import { isOk } from "../../services/api/utils";
import { useNavigate } from 'react-router-dom';
import { useSSE } from "../../providers/SSEContext"; // Importando o contexto SSE

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
  const { addHandler, updateTopic, removeHandler } = useSSE(); // Usando o contexto para lidar com eventos SSE
  const [lobbies, setLobbies] = useState<LobbyWithPlayers[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  // Função para carregar lobbies
  async function loadLobbies() {
    setLoading(true);
    setError(null);

    const response = await lobbiesService.getLobbies();

    if (isOk(response)) {
      const baseLobbies = response.value;

      const enriched = await Promise.all(
        baseLobbies.map(async (lobby) => {
          const playersResponse = await playersService.getPlayerCount(lobby.id);

          let currentPlayers = 0;
          if (isOk(playersResponse)) {
            currentPlayers = playersResponse.value.count;
          }

          return {
            ...lobby,
            currentPlayers,
          } as LobbyWithPlayers;
        })
      );

      setLobbies(enriched);
    } else {
      setError(response.error || "Failed to load lobbies");
    }

    setLoading(false);
  }

  useEffect(() => {
    // 1) Verificar auth
    const token = localStorage.getItem("token");
    if (!token) {
      alert("You are not authenticated");
      navigate("/login"); // ou outra rota
      return;
    }

    loadLobbies();

    updateTopic("lobbies");

    addHandler("lobbies_list_changes", (data) => {
        console.log("entrei no handler data:", data)
       if (data.changeType === "created") {
           console.log("Novo lobby criado via SSE:", data);
           loadLobbies(); // Recarregar a lista de lobbies após a criação de um novo lobby
       } else if (data.changeType === "deleted") {
           console.log("Lobby fechado via SSE:", data);
           loadLobbies(); // Recarregar a lista de lobbies após a exclusão de um lobby
       }
    });

    // Cleanup ao desmontar o componente
    return () => {
      removeHandler("lobbies_list_changes"); // Remover o handler quando o componente for desmontado
    };
  }, [navigate, updateTopic, addHandler, removeHandler]); // Dependências ajustadas para garantir que o `useEffect` seja re-executado corretamente

  async function handleEnterLobby(lobbyId: number) {
    console.log("Entering lobby:", lobbyId);
    setTimeout(() => {
      navigate(`/lobbies/${lobbyId}/info`);
    }, 1000);
  }

  if (loading) return <p>Loading lobbies...</p>;
  if (error) return <p style={{ color: "red" }}>{error}</p>;

  return (
    <div>
      <h1>Available Lobbies</h1>

      {lobbies.length === 0 && <p>No lobbies available</p>}

      <div>
        {lobbies.map((lobby) => (
          <div
            key={lobby.id}
            style={{
              border: "1px solid #ccc",
              padding: "10px",
              marginBottom: "10px",
            }}
          >
            <h2>{lobby.name}</h2>
            <p>{lobby.description}</p>

            <p>
              {lobby.currentPlayers} / {lobby.maxUsers} players
            </p>

            <p>Min Credit: {lobby.minCreditToParticipate}</p>

            <button onClick={() => handleEnterLobby(lobby.id)}>
              Enter Lobby
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
