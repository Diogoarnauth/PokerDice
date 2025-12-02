// src/models/GameModels.ts
export class GamePayload {
    game: Game;

    constructor(data: any) {
        this.game = new Game(data);
    }
}

export class Game {
    id: number;
    currentPlayerUsername: string;
    dice: string[];               // ex: [1, 3, 5, 2, 6]
    isFirstRoll: boolean;
    // adiciona outros campos conforme o backend devolver (scores, state, etc.)

    constructor(data: any) {
        this.id = data.id;
        this.currentPlayerUsername = data.currentPlayerUsername;
        this.dice = data.dice ?? [];
        this.isFirstRoll = data.isFirstRoll ?? false;
    }
}
