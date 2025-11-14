export class MessagesEvent{
    id: number;
    content: string;
    username: string;
    channelName: string;
    channelId: number;
    createdAt: string;

    constructor(data: any) {
        this.id = data.id;
        this.content = data.content;
        this.username = data.username;
        this.channelName = data.channelName;
        this.channelId = data.channelId;
        this.createdAt = data.createdAt;
    }
}