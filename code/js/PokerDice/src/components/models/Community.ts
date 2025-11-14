export class CommunityChannelsPayload {
    channels: Array<Channel>;

    constructor(data: any) {
        this.channels = data.channels.map((item: any) => new Channel(item));
    }
}

class Channel {
    adminId: number;
    id: number;
    isPublic: boolean;
    name: string;

    constructor(data: any) {
        this.adminId = data.adminId;
        this.id = data.id;
        this.isPublic = data.isPublic;
        this.name = data.name;
    }
}