// src/components/lobby/LobbyDetails.tsx
import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { lobbyDetailsService } from "../../services/api/LobbyDetails"; // Importa o serviço para a API

// Tipo para armazenar as informações do Lobby
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
  turnTime: string; // Formato de string para representar Duration
};

// Tipo para a resposta da API
type LobbyApiResponse = {
  success: boolean;
  value: Lobby; // Aqui definimos que a resposta terá um campo 'value' que será do tipo 'Lobby'
  error?: string;
};

// Função para formatar a duração para um formato legível (minutos)
function formatTurnTime(turnTime: string): string {
  // Regex para capturar o número de minutos no formato PTxM
  const regex = /^PT(\d+)M$/;

  // Tenta encontrar os minutos na string
  const match = regex.exec(turnTime);

  if (match) {
    const minutes = match[1]; // Extrai o número de minutos como string
    return `${minutes} min`; // Retorna o valor com "min"
  }

  return "Desconhecido"; // Caso não encontre o formato correto
}


export default function LobbyDetails() {
  const [lobby, setLobby] = useState<Lobby | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  useEffect(() => {
    async function loadLobby() {
      setLoading(true);
      setError(null);

      try {
        // Aqui dizemos ao TypeScript que a resposta da API é do tipo 'LobbyApiResponse'
        const response = await lobbyDetailsService.getLobby(Number(id)) as LobbyApiResponse;

        if (response.success) {
          setLobby(response.value); // Agora TypeScript sabe que 'response.value' é um Lobby
        } else {
          setError("Failed to load lobby details");
        }
      } catch (err) {
        setError("Error fetching lobby details");
      }

      setLoading(false);
    }

    loadLobby();
  }, [id]);

  // Função para quando o usuário clicar em "Leave Lobby"
  async function handleLeaveLobby() {
    console.log("Leaving lobby:", id);

    // ---- API call (comentado para já) ----
    /*
    const leaveResponse = await lobbiesService.leaveLobby(Number(id));

    if (!leaveResponse.success) {
      alert("Failed to leave the lobby: " + leaveResponse.error);
      return;
    }
    */

    // Após deixar o lobby, redireciona para a lista de lobbies
    navigate("/lobbies");
  }

  if (loading) return <p>Loading lobby details...</p>;
  if (error) return <p style={{ color: "red" }}>{error}</p>;

  if (!lobby) return <p>Lobby not found</p>;

  return (
    <div>
      <h1>{lobby.name}</h1>
      <p>{lobby.description}</p>
      <p><strong>Host ID:</strong> {lobby.hostId}</p>
      <p><strong>Min Users:</strong> {lobby.minUsers}</p>
      <p><strong>Max Users:</strong> {lobby.maxUsers}</p>
      <p><strong>Rounds:</strong> {lobby.rounds}</p>
      <p><strong>Min Credit to Participate:</strong> {lobby.minCreditToParticipate}</p>

      {/* Formatação do turnTime */}
      <p><strong>Turn Time:</strong> {formatTurnTime(lobby.turnTime)}</p>

      <button onClick={handleLeaveLobby}>Leave Lobby</button>
    </div>
  );
}
