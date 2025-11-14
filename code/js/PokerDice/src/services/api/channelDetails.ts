import {fetchWrapper} from "./utils";
import {RequestUri} from "./RequestUri";
import {SearchUsers} from "../../components/models/SearchUsers";
import {ChannelMembers} from "../../components/models/ChannelMembers";

interface NewChannelDetails {
    newName: string;
    isPublic: boolean;
}

interface InviteUser {
    "inviteeUsername" : string,
    "isReadWrite": boolean;
}

export const channelDetailsService = {
    updateChannelDetails(channelId: number, newDetails: NewChannelDetails) {
        return fetchWrapper(RequestUri.channels.update(channelId), { method: 'PATCH', body: JSON.stringify(newDetails) });
    },
    leaveChannel(channelId: number) {
        return fetchWrapper(RequestUri.channels.leave(channelId), { method: 'POST' });
    },
    inviteUser(channelId: number, invite: InviteUser) {
        return fetchWrapper(RequestUri.channels.invite(channelId), {method: 'POST', body: JSON.stringify(invite)});
    },
    searchSuggestions(query: string) {
        return fetchWrapper<SearchUsers>(RequestUri.user.searchSuggestions + query, { method: 'GET' });
    },
    getMembers(channelId: number, offset: number) {
        return fetchWrapper<ChannelMembers>(RequestUri.channels.members(channelId) + `?offset=${offset}`, { method: 'GET' });
    }
}