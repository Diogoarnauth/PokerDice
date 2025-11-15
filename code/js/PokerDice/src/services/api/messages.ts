import {RequestUri} from "./RequestUri";
import {fetchWrapper} from "./utils";
import {Message, MessagesPayload} from "../../components/models/Message";
/*
export const messagesService = {
    getMessages(channelId: number, offset: number = 0) {
        return fetchWrapper<MessagesPayload>(RequestUri.channels.getMessages(channelId, offset), { method: 'GET' });
    },

    sendMessage(channelId: number, content: string) {
        return fetchWrapper<Message>(RequestUri.channels.sendMessages(channelId), { method: 'POST', body: JSON.stringify({content}) });
    }
}

 */