import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { lobbyDetailsService } from "../../services/api/LobbyDetails"; // Importa o serviço para a API
import {gameService} from "../../services/api/Games";
import {isOk} from "../../services/api/utils";

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
  const [owner, setOwner] = useState<User | null>(null);
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

          const ownerId = response.value.hostId;

            const ownerResponse = await lobbyDetailsService.getOwner(ownerId);
            if (ownerResponse.success) {
              setOwner(ownerResponse.value);
            } else {
              console.error("Failed to fetch owner", ownerResponse.error);
            }

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
      console.log("joinResponse", joinResponse)

      if (!joinResponse.success) {
        alert("Failed to join lobby: " + joinResponse.error);
        setJoinLoading(false);
        return;
      }

      // 👉 ATUALIZAR O USER NO ESTADO PARA REFLETIR QUE ENTROU NO LOBBY
      setUser(prev =>
        prev
          ? { ...prev, lobbyId: Number(id) }
          : prev
      );

      setJoinLoading(false);
    }

  // Função para quando o usuário clicar em "Leave Lobby"
   async function handleLeaveLobby() {
     console.log("Leaving lobby:", id);
     setLeaveLoading(true);

     const leaveResponse = await lobbyDetailsService.leaveLobby(Number(id));
     if (!leaveResponse.success) {
       console.log("leaveResponse")
       alert("Failed to leave lobby: " + leaveResponse.error);
       setLeaveLoading(false);
       return;
     }

     // 👉 ATUALIZAR O USER NO ESTADO PARA REFLETIR QUE SAIU DO LOBBY
     setUser(prev =>
       prev
         ? { ...prev, lobbyId: null }
         : prev
     );

     setLeaveLoading(false);
     navigate("/lobbies");
   }

  // Função para quando o host clicar em "Start Game"
  async function handleStartGame() {

    if (!id) return;
    setError(undefined);

    console.log("Starting game for lobby:", id);

      try {
        const startResponse = await gameService.startGame(Number(id));
        console.log("StartGame response:", startResponse);

      if (startResponse.success) {
        alert("Game started successfully!");
        // Você pode redirecionar para outra página ou fazer o que for necessário após iniciar o jogo
          setTimeout(() => {
              navigate(`/games/lobby/${id}`);
          }, 1000);
      } else {
          console.log("startResponse", startResponse.error)
        alert("Failed to start game: " + startResponse.error);
      }
    } catch (err) {
          alert("Error starting game: " + err);
      }
  }

  if (loading) return <p>Loading lobby details...</p>;
  if (error) return <p style={{ color: "red" }}>{error}</p>;
  if (!lobby) return <p>Lobby not found</p>;

  const isUserInLobby = user?.lobbyId === lobby.id;
  const isUserHost = user?.id === lobby.hostId;  // Verifica se o usuário é o host

  return (
    <div>
      <h1>{lobby.name}</h1>
      <p><strong>Description:</strong>{lobby.description}</p>
      <p><strong>Host:</strong> {owner?.username}</p>
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

      {/* Botão de Start Game só visível se o usuário for o host */}
      {isUserHost && (
        <button onClick={handleStartGame} disabled={loading}>
          {loading ? "Starting Game..." : "Start Game"}
        </button>
      )}
    </div>
  );
}
