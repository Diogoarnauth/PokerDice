import {fetchWrapper, Result} from "./utils";
import { LobbiesInfoPayload} from "../../components/models/LobbiesInfo";
import {RequestUri} from "./RequestUri";

export interface CreateLobbyInput {
  name: string
  description: string
  minUsers: number
  maxUsers: number
  rounds: number
  minCreditToParticipate: number
}
export const lobbiesService = {

  // GET /lobbies?offset=X
  getLobbies(offset: number = 0) {
    return fetchWrapper<LobbyPayload>(
      RequestUri.lobbies.getAll + `?offset=${offset}`,
      { method: 'GET' }
    )
  },

  // POST /lobbies
  createLobby(input: CreateLobbyInput): Promise<Result<any>> {
    return fetchWrapper(RequestUri.lobbies.create, {
      method: 'POST',
      body: JSON.stringify(input)
    })
  },

  // GET /lobbies/{id}
  getLobbyById(id: number) {
    return fetchWrapper<LobbyPayload>(
      RequestUri.lobbies.byId(id),
      { method: 'GET' }
    )
  },

  // POST /lobbies/{id}/join
  joinLobby(lobbyId: number) {
    return fetchWrapper(
      RequestUri.lobbies.join(lobbyId),
      { method: 'POST' }
    )
  }


}
