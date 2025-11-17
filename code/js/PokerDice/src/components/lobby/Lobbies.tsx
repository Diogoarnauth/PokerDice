// src/components/lobby/Lobbies.tsx
import React, { useEffect, useState } from "react";
import { lobbiesService } from "../../services/api/Lobbies";
import { playersService } from "../../services/api/Players";
import { isOk } from "../../services/api/utils";
import { useNavigate } from "react-router-dom";

// Tipo auxiliar
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
  const [lobbies, setLobbies] = useState<LobbyWithPlayers[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

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
    loadLobbies();
  }, []);

  async function handleEnterLobby(lobbyId: number) {
    console.log("Entering lobby:", lobbyId);

    // ---- API call (comentado para já) ----
    /*
    const joinResponse = await playersService.joinLobby(lobbyId);

    if (!isOk(joinResponse)) {
      alert("Failed to join lobby: " + joinResponse.error);
      return;
    }
    */

    // Simulação de delay & redirect
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
