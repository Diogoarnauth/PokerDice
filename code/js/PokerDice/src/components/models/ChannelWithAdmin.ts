export class ChannelWithAdmin {
    id: number;
    name: string;
    isPublic: boolean;
    adminName: string;

    constructor(data: any) {
        this.id = data.id;
        this.name = data.name;
        this.isPublic = data.isPublic;
        this.adminName = data.adminName;
    }

}
