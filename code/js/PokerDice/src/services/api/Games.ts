import { fetchWrapper, Result } from "./utils"
import { RequestUri } from "./RequestUri"

export interface RollResult {
    dice: number[] // backend devolve "A", "K", "10", etc
}

export interface RerollResult {
    dice: number[]
}

export const gameService = {

    // POST /games/{lobbyId}/start
    startGame(lobbyId: number): Promise<Result<any>> {
        return fetchWrapper(RequestUri.games.start(lobbyId), {
            method: "POST",
        })
    },

    // POST /games/{gameId}/roll
    roll(lobbyId: number): Promise<Result<RollResult>> {
        return fetchWrapper(RequestUri.games.roll(lobbyId), {
            method: "POST",
        });
    },

    // POST /games/{gameId}/reroll
    reroll(gameId: number, diceMask: number[]): Promise<Result<RerollResult>> {
        return fetchWrapper(RequestUri.games.reroll(gameId), {
            method: "POST",
            body: JSON.stringify(diceMask),
        })
    },

    // POST /games/{gameId}/end
    endTurn(gameId: number): Promise<Result<any>> {
        return fetchWrapper(RequestUri.games.endTurn(gameId), {
            method: "POST",
        })
    },

    // GET /games/{gameId}
    getById(gameId: number): Promise<Result<any>> {
        return fetchWrapper(RequestUri.games.byId(gameId), {
            method: "GET",
        })
    },

    //GET
    getGameByLobbyId(lobbyId: number): Promise<Result<any>> {
        return fetchWrapper(RequestUri.games.getGameByLobbyId(lobbyId), {
            method: "GET",
        })
    },

    // POST /games/{gameId}/end-game
    endGame(gameId: number): Promise<Result<any>> {
        return fetchWrapper(RequestUri.games.endGame(gameId), {
            method: "POST",
        })
    },

    // GET /games/{gameId}/player-turn
    getPlayerTurn(gameId: number): Promise<Result<{ username: string }>> {
        return fetchWrapper(RequestUri.games.playerTurn(gameId), {
            method: "GET",
        });
    },

    // POST /games/{gameId}/leave
    leave(gameId: number): Promise<Result<any>> {
        return fetchWrapper(RequestUri.games.leave(gameId), {
            method: "POST",
        })
    },
}
