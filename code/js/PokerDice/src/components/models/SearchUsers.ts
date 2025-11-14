export class SearchUsers {
    users: Array<UserName>;
    constructor(data: any) {
        this.users = data.users.map((item: any) => new UserName(item));
    }
}

class UserName {
    username: string;
    constructor(data: any) {
        this.username = data.username;
    }
}