export class ChannelMembers {
    usernames: Array<Members>
    constructor(data: any) {
        this.usernames = data.usernames.map((item: any) => new Members(item).username);
    }
}

class Members{
    username: string;
    constructor(data: any) {
        this.username = data.username;
    }
}