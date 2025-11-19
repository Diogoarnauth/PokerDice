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

type LobbyApiResponse = {
  success: boolean;
  value: Lobby; // Aqui definimos que a resposta terá um campo 'value' que será do tipo 'Lobby'
  error?: string;
};

export type User = {
  id: number;
  username: string;
  name: string;
  age: number;
  credit: number;
  winCounter: number;
  lobbyId: number | null; // Pode ser null ou um número de lobby
};

function formatTurnTime(turnTime: string): string {
  const regex = /^PT(\d+)M$/;
  const match = regex.exec(turnTime);
  if (match) {
    const minutes = match[1];
    return `${minutes} min`;
  }
  return "Desconhecido";
}

export default function LobbyDetails() {
  const [lobby, setLobby] = useState<Lobby | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [joinLoading, setJoinLoading] = useState(false);
  const [leaveLoading, setLeaveLoading] = useState(false);
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  useEffect(() => {
    async function loadLobby() {
      setLoading(true);
      setError(null);

      try {
        // Pega o token do localStorage
        const token = localStorage.getItem("token");

        if (token) {
          // Chama a API para obter os dados do usuário
          const userResponse = await lobbyDetailsService.getMe(token);
          console.log("userResponse.value", userResponse)

          if (userResponse.success) {
            setUser(userResponse.value);

          } else {
            setError("Failed to fetch user data");
          }
        } else {
          setError("No token found");
        }

        // Requisição para obter detalhes do lobby
        const response = await lobbyDetailsService.getLobby(Number(id)) as LobbyApiResponse;
        if (response.success) {
          setLobby(response.value);
        } else {
          setError("Failed to load lobby details");
        }
      } catch (err) {
        setError("Error fetching data");
      }

      setLoading(false);
    }

    loadLobby();
  }, [id]);

  // Função para quando o usuário clicar em "Join Lobby"
  async function handleJoinLobby() {
    console.log("Joining lobby:", id);
    setJoinLoading(true);

    const joinResponse = await lobbyDetailsService.joinLobby(Number(id));
    if (!joinResponse.success) {
      alert("Failed to join lobby: " + joinResponse);
      return;
    }

    setJoinLoading(false);
    // Após juntar ao lobby, redireciona para os detalhes do lobby
    navigate(`/lobbies/${id}/info`);
  }

  // Função para quando o usuário clicar em "Leave Lobby"
  async function handleLeaveLobby() {
    console.log("Leaving lobby:", id);
    setLeaveLoading(true);

    const leaveResponse = await lobbyDetailsService.leaveLobby(Number(id));
    if (!leaveResponse.success) {
        console.log("leaveResponse")
      alert("Failed to leave lobby: " + leaveResponse);
      return;
    }

    setLeaveLoading(false);
    navigate("/lobbies");
  }

  if (loading) return <p>Loading lobby details...</p>;
  if (error) return <p style={{ color: "red" }}>{error}</p>;
  if (!lobby) return <p>Lobby not found</p>;

  const isUserInLobby = user?.lobbyId === lobby.id;

  return (
    <div>
      <h1>{lobby.name}</h1>
      <p>{lobby.description}</p>
      <p><strong>Host ID:</strong> {user?.username}</p>
      <p><strong>Min Users:</strong> {lobby.minUsers}</p>
      <p><strong>Max Users:</strong> {lobby.maxUsers}</p>
      <p><strong>Rounds:</strong> {lobby.rounds}</p>
      <p><strong>Min Credit to Participate:</strong> {lobby.minCreditToParticipate}</p>
      <p><strong>Turn Time:</strong> {formatTurnTime(lobby.turnTime)}</p>

      {/* Condicional para mostrar o botão Join ou Leave */}
      {user && user.lobbyId === null && (
        <button onClick={handleJoinLobby} disabled={joinLoading}>
          {joinLoading ? "Joining..." : "Join Lobby"}
        </button>
      )}

      {isUserInLobby && (
        <button onClick={handleLeaveLobby} disabled={leaveLoading}>
          {leaveLoading ? "Leaving..." : "Leave Lobby"}
        </button>
      )}
    </div>
  );
}
