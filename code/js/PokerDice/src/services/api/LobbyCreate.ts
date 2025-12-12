import { fetchWrapper, Result } from "./utils";
import { RequestUri } from "./RequestUri";

export interface LobbyCreationInput {
    name: string;
    description: string;
    minUsers: number;
    maxUsers: number;
    rounds: number;
    minCreditToParticipate: number;
    turnTime: number;
}

export const lobbyCreationService = {
    createLobby(data: LobbyCreationInput): Promise<Result<any>> {
        return fetchWrapper(
            RequestUri.lobbies.create,
            {
                method: "POST",
                body: JSON.stringify(data),
            }
        );
    }
};