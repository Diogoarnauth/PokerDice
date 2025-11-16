import {fetchWrapper, Result} from "./utils";
import {RequestUri} from "./RequestUri";
import {LobbiesInfoPayload, LobbyInfo} from "../../components/models/LobbyInfo";

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
        // Se esperas uma lista
        return fetchWrapper<LobbiesInfoPayload>(
            RequestUri.lobbies.list,
            {method: 'GET'}
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
        // Se esperas um lobby único
        return fetchWrapper<LobbyInfo>(
            RequestUri.lobbies.byId(id),
            {method: 'GET'}
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
