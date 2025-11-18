// src/models/PlayerProfile.ts
export class PlayerProfilePayload {
    profile: PlayerProfile;

    constructor(data: any) {
        this.profile = new PlayerProfile(data);
    }
}

export class PlayerProfile {
    username: string;
    name: string;
    // Adiciona outros campos se o backend os fornecer

    constructor(data: any) {
        this.username = data.username;
        this.name = data.name;
    }
}