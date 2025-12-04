const BASE_API_URL = "http://localhost:8088/api"

export const RequestUri = {

    // -----------------------
    // USERS
    // -----------------------
    user: {
        register: `${BASE_API_URL}/users`,        // POST
        login: `${BASE_API_URL}/users/token`,     // POST (recebe token)
        logout: `${BASE_API_URL}/logout`,         // POST
        invite: `${BASE_API_URL}/invite`,         // POST invite
        deposit: `${BASE_API_URL}/deposit`,       // POST deposit money
        bootstrap: `${BASE_API_URL}/bootstrap`,   // GET bootstrap info
        getById: (id: number) => `${BASE_API_URL}/users/${id}`,
        listen: `${BASE_API_URL}/sse`,
        checkAdmin: `${BASE_API_URL}/checkAdmin`,
        getMe:  `${BASE_API_URL}/users/getMe`,
        getUser: (id: number) => `${BASE_API_URL}/users/${id}`,
    },

    // -----------------------
    // LOBBIES
    // -----------------------
    lobbies: {
        list: `${BASE_API_URL}/lobbies`,             // GET
        create: `${BASE_API_URL}/lobbies`,           // POST
        byId: (id: number) => `${BASE_API_URL}/lobbies/${id}`, // GET
        join: (id: number) => `${BASE_API_URL}/lobbies/${id}/users`, // POST
        leave: (id: number) => `${BASE_API_URL}/lobbies/${id}/leave`, // DELETE
        playerCount: (id: number) => `${BASE_API_URL}/users/lobby/${id}`

    },

    // -----------------------
    // GAMES
    // -----------------------
    games: {
        root: `${BASE_API_URL}/games`,

        byId: (gameId: number) => `${BASE_API_URL}/games/${gameId}`,
        start: (lobbyId: number) => `${BASE_API_URL}/games/${lobbyId}/start`,
        roll: (lobbyId: number) => `${BASE_API_URL}/games/${lobbyId}/roll`,
        reroll: (gameId: number) => `${BASE_API_URL}/games/${gameId}/reroll`,
        leave: (gameId: number) => `${BASE_API_URL}/games/${gameId}/leave`,
        endTurn: (gameId: number) => `${BASE_API_URL}/games/${gameId}/end`,
        endGame: (gameId: number) => `${BASE_API_URL}/games/${gameId}/end-game`,
        status: (gameId: number) => `${BASE_API_URL}/games/${gameId}/status`,
        finalResult: (gameId: number) => `${BASE_API_URL}/games/${gameId}/final-result`,
        playerTurn: (gameId: number) => `${BASE_API_URL}/games/${gameId}/player-turn`,
        getGame: (gameId: number) => `${BASE_API_URL}/games/${gameId}`,
        getGameByLobbyId :(lobbyId: number) => `${BASE_API_URL}/games/lobby/${lobbyId}`,
    },

    // -----------------------
    // STATUS
    // -----------------------
    status: {
        hostname: `${BASE_API_URL}/status/hostname`,
        ip: `${BASE_API_URL}/status/ip`
    }

}
