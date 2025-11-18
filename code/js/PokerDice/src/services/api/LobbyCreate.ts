import { fetchWrapper, Result } from "./utils";
import { RequestUri } from "./RequestUri";

export const lobbyCreationService = {
    createLobby(data: {
        name: string;
        description: string;
        minUsers: number;
        maxUsers: number;
        rounds: number;
        minCreditToParticipate: number;
        turnTime: number;
    }): Promise<Result<any>> {
        return fetchWrapper(
            RequestUri.lobbies.create,
            {
                method: "POST",
                body: JSON.stringify(data),
            }
        );
    }
};