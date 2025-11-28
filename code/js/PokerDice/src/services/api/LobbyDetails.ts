import { fetchWrapper, Result } from "./utils"
import { RequestUri } from "./RequestUri"
import { User } from "../../components/lobby/LobbyDetails";

// ----------------------------------------
// Lobby Details API
// Apenas operações *dentro* do lobby
// ----------------------------------------
export const lobbyDetailsService = {

  // GET /lobbies/{id} → obter detalhes completos do lobby
  getLobby(id: number) {
    return fetchWrapper(
      RequestUri.lobbies.byId(id),
      { method: "GET" }
    )
  },

    // Função para obter os dados do usuário
   getMe(token: string): Promise<Result<User>> {
      return fetchWrapper(
        RequestUri.user.getMe,
        {method: "GET",}
      );
    },

  // POST /lobbies/{id}/users → entrar no lobby
  joinLobby(id: number): Promise<Result<any>> {
    return fetchWrapper(
      RequestUri.lobbies.join(id),
      { method: "POST" }
    )
  },

  // DELETE /lobbies/{id}/leave → sair do lobby
  leaveLobby(id: number): Promise<Result<any>> {
    return fetchWrapper(
      RequestUri.lobbies.leave(id),
      { method: "DELETE" }
    )
  },

    /*
  // POST /games/{lobbyId}/start → host inicia o jogo
  startGame(lobbyId: number): Promise<Result<any>> {
    return fetchWrapper(
      RequestUri.games.start(lobbyId),
      { method: "POST" }
    )
  }
     */
}
