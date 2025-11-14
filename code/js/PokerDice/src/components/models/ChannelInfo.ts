export class ChannelInfoPayload {
    channels: Array<ChannelInfo>;

    constructor(data: any) {
        this.channels = data.channels.map((item: any) => new ChannelInfo(item));
    }
}

class ChannelInfo {
    id: number;
    name: string;
    isPublic: boolean;
    adminId: number;
    lastMessage: {
        id: number;
        content: string;
        username: string;
        channelId: number;
        createdAt: string;
    } | null;

    constructor(data: any) {
        this.id = data.id;
        this.name = data.name;
        this.isPublic = data.isPublic;
        this.adminId = data.adminId;
        this.lastMessage = data.lastMessage ? {
            id: data.lastMessage.id,
            content: data.lastMessage.content,
            username: data.lastMessage.userName,
            channelId: data.lastMessage.channelId,
            createdAt: data.lastMessage.createdAt
        } : null;
    }
}