import { fetchWrapper, Result } from "./utils";
import { RequestUri } from "./RequestUri";

export interface PlayerProfileData {
    id: number;
    username: string;
    name: string;
    age: number;
    lobbyId: number | null;
    credit: number;
    winCounter: number;
}

export interface DepositResponse {
    newBalance: number;
    message?: string;
}

export const playerProfileService = {
    // GET /me
    getProfile(): Promise<Result<PlayerProfileData>> {
        return fetchWrapper<PlayerProfileData>(
            RequestUri.user.getMe,
            { method: "GET" }
        );
    },

    // POST /deposit
    deposit(payload: { value: number }): Promise<Result<DepositResponse>> {
        return fetchWrapper<DepositResponse>(
            RequestUri.user.deposit,
            {
                method: "POST",
                body: JSON.stringify(payload),
            }
        );
    }
};