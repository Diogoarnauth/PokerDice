import { RequestUri } from "./RequestUri";
import { fetchWrapper, Result } from './utils';

export const notificationsService = {
    joinLobby: (lobbyId: number): Promise<Result<any>> => {
        return fetchWrapper(RequestUri.lobbies.join(lobbyId), {
            method: 'POST',
        });
    },

    declineLobby: (lobbyId: number): Promise<Result<any>> => {
        return fetchWrapper(RequestUri.lobbies.leave(lobbyId), {
            method: 'PATCH', // ou DELETE, dependendo do backend
        });
    },
}
