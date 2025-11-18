export class LobbyCreationPayload {
    lobby: LobbyCreation;

    constructor(data: any) {
        this.lobby = new LobbyCreation(data);
    }
}

export class LobbyCreation {
    id: number;
    name: string;
    description: string;
    hostId: number;
    minUsers: number;
    maxUsers: number;
    rounds: number;
    minCreditToParticipate: number;
    turnTime: number;

    constructor(data: any) {
        this.id = data.id;
        this.name = data.name;
        this.description = data.description;
        this.hostId = data.hostId;
        this.minUsers = data.minUsers;
        this.maxUsers = data.maxUsers;
        this.rounds = data.rounds;
        this.minCreditToParticipate = data.minCreditToParticipate;
        this.turnTime = data.turnTime;
    }
}
