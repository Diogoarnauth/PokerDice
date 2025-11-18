export class AppInvitePayload {
    invite: AppInviteCode;

    constructor(data: any) {
        this.invite = new AppInviteCode(data);
    }
}

export class AppInviteCode {
    inviteCode: string;
    constructor(data: any) {
        this.inviteCode = data.inviteCode;
    }
}
