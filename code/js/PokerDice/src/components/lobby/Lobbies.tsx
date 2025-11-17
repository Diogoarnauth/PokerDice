import React, { useEffect, useState } from "react";
import { lobbiesService } from "../../services/api/Lobbies";
import { isOk } from "../../services/api/utils";
import { useNavigate } from "react-router-dom";

export default function LobbiesList() {
  const [lobbies, setLobbies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  async function loadLobbies() {
    const response = await lobbiesService.getLobbies();

    if (isOk(response)) {
      // ✔ Aqui está a correção
      console.log("responde.value", response.value)
      setLobbies(response.value);
    } else {
      setError(response.error);
    }
    setLoading(false);
  }

  useEffect(() => {
    loadLobbies();
  }, []);

  if (loading) return <p>Loading lobbies...</p>;
  if (error) return <p style={{ color: "red" }}>{error}</p>;

  return (
    <div>
      <h1>Available Lobbies</h1>

      {lobbies.length === 0 && <p>No lobbies available</p>}

      <div>
        {lobbies.map((lobby: any) => (
          <div key={lobby.id} style={{border:"1px solid #ccc", padding:"10px", marginBottom:"10px"}}>
            <h2>{lobby.name}</h2>
            <p>{lobby.description}</p>
            <p>
              {lobby.currentUsers} / {lobby.maxUsers} players
            </p>
            <p>Min Credit: {lobby.minCreditToParticipate}</p>

            <button onClick={() => navigate(`/lobbies/${lobby.id}`)}>
              Enter Lobby
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
