import { fetchWrapper, Result } from "./utils"
import { RequestUri } from "./RequestUri"

export interface RollResult {
    dice: string[] // backend devolve "A", "K", "10", etc
}

export interface RerollResult {
    dice: string[]
}

export const gameService = {

    // POST /games/{lobbyId}/start
    startGame(lobbyId: number): Promise<Result<any>> {
        return fetchWrapper(RequestUri.games.start(lobbyId), {
            method: "POST",
        })
    },

    // POST /games/{gameId}/roll
    roll(gameId: string): Promise<Result<RollResult>> {
        return fetchWrapper(RequestUri.games.roll(gameId), {
            method: "POST",
        })
    },

    // POST /games/{gameId}/reroll
    reroll(gameId: string, diceMask: number[]): Promise<Result<RerollResult>> {
        return fetchWrapper(RequestUri.games.reroll(gameId), {
            method: "POST",
            body: JSON.stringify(diceMask),
        })
    },

    // POST /games/{gameId}/end
    endTurn(gameId: string): Promise<Result<any>> {
        return fetchWrapper(RequestUri.games.endTurn(gameId), {
            method: "POST",
        })
    },

    // GET /games/{gameId}
    getById(gameId: string): Promise<Result<any>> {
        return fetchWrapper(RequestUri.games.byId(gameId), {
            method: "GET",
        })
    },

    // POST /games/{gameId}/end-game
    endGame(gameId: string): Promise<Result<any>> {
        return fetchWrapper(RequestUri.games.endGame(gameId), {
            method: "POST",
        })
    },

    // GET /games/{gameId}/player-turn
    getPlayerTurn(gameId: string): Promise<Result<string>> {
        return fetchWrapper(RequestUri.games.playerTurn(gameId), {
            method: "GET",
        })
    },

    // POST /games/{gameId}/leave
    leave(gameId: string): Promise<Result<any>> {
        return fetchWrapper(RequestUri.games.leave(gameId), {
            method: "POST",
        })
    },
}
