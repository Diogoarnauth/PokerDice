export class MessagesPayload {
    messages: Message[];
}

export class Message{
    id: number;
    content: string;
    username: string;
    channelId: number;
    createdAt: string;

    constructor(data: any) {
        this.id = data.id;
        this.content = data.content;
        this.username = data.username;
        this.channelId = data.channelId;
        this.createdAt = data.createdAt;
    }
}