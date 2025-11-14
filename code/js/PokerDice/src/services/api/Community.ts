import {fetchWrapper, Result} from "./utils";
import {RequestUri} from "./RequestUri";
import {CommunityChannelsPayload} from "../../components/models/Community";



export const communityService = {
    join(channelId: number): Promise<Result<any>> {
        return fetchWrapper(RequestUri.channels.join(channelId), {
            method: 'POST',
        });
    },

    getMessages(channelId: number, offset:number=0): Promise<Result<any>> {
        return fetchWrapper(RequestUri.channels.getMessages(channelId, offset));
    },

    searchCommunityChannels(query: string, offset: number = 0): Promise<Result<CommunityChannelsPayload>> {
        return fetchWrapper(RequestUri.channels.searchPublic + query + '&offset=' + offset, { method: 'GET' });
    },
}