import { useEffect } from 'react';
import { useSSEEmitter } from '../providers/SSEContext';
import { MessagesEvent } from '../components/models/MessagesEvent';
import { messageCounters } from '../services/storage/counterStorage';


type MessageAlertAction = 
    | { 
        type: 'showMessageAlert';
        message: {
            userName: string;
            channelName: string;
            channelId: number;
            content: string;
        };
    }
    | { type: 'hideMessageAlert' };

export function useMessageNotifications(dispatch: (action: MessageAlertAction) => void) {
    const [,,isSSEConnected, registerMessageHandler, unregisterMessageHandler] = useSSEEmitter();

    useEffect(() => {
        registerMessageHandler(
            'other',
            null,
            () => {},
            (messageData: MessagesEvent) => {
                dispatch({
                    type: 'showMessageAlert',
                    message: {
                        userName: messageData.username,
                        channelName: messageData.channelName,
                        channelId: messageData.channelId,
                        content: messageData.content
                    }
                });
                messageCounters.increment(messageData.channelId);
                setTimeout(() => {
                    dispatch({ type: 'hideMessageAlert' });
                }, 5000);
            },
        );

        return () => {
            unregisterMessageHandler();
        };
    }, [isSSEConnected]);
}