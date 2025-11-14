export class NotificationPayload {
    channels: Array<Notification_Info>;

    constructor(data: any) {
        this.channels = data.channels.map((item: any) => new Notification_Info(item));
    }
}

class Notification_Info {
    channelId: number;
    channelName: string;
    inviterName: string;

    constructor(data: any) {
        this.channelId = data.channelId;
        this.channelName = data.channelName;
        this.inviterName = data.inviterName;
    }
}

