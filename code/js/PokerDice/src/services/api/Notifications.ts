import { RequestUri } from "./RequestUri";
import { fetchWrapper, Result} from './utils';

export const notificationsService = {
    joinChannel: (channelId: number): Promise<Result<any>> => {
        return fetchWrapper(RequestUri.channels.join(channelId),{
            method: 'POST',
        })
    },


    declineChannel: (channelId: number): Promise<Result<any>> => {
        return fetchWrapper(RequestUri.channels.decline(channelId),
        {
            method: 'PATCH',
        })
    }
}