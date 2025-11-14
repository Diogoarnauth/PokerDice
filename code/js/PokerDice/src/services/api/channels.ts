import {fetchWrapper, Result} from "./utils";
import { ChannelInfoPayload} from "../../components/models/ChannelInfo";
import {RequestUri} from "./RequestUri";

interface CreateChannel {
    "name" : string,
    "isPublic": boolean;
}

export const channelsService = {
    searchChannels(query: string) {
        return fetchWrapper<ChannelInfoPayload>(RequestUri.channels.search + query, { method: 'GET' });
    },

    getChannels(offset: number = 0){
        return fetchWrapper<ChannelInfoPayload>(RequestUri.channels.getUserChannels + `?offset=${offset}`, { method: 'GET' });
    },

    createChannel(name: string, isPublic: boolean): Promise<Result<any>> {
        const content: CreateChannel = {
            "name" : name,
            "isPublic": isPublic
        }
        return fetchWrapper(RequestUri.channels.create, {
            method: 'POST',
            body: JSON.stringify(content)
        });

    }

}