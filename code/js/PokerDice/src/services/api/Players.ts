import { fetchWrapper } from "./utils";
import { RequestUri } from "./RequestUri";

export const playersService = {
  // GET /lobbies/{id}/players/count
  getPlayerCount(lobbyId: number) {
    return fetchWrapper<{ count: number }>(
      RequestUri.lobbies.playerCount(lobbyId),
      { method: "GET" }
    );
  }
};
