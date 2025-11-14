import React, { useEffect, useReducer } from 'react';
import { SplitColumnLayout } from '../layout/SplitColumnLayout';
import { useFetch } from '../../hooks/useFetch';
import { isOk } from '../../services/api/utils';
import { NotificationPayload } from '../models/Notification';
import { notificationsService } from '../../services/api/Notifications';
import { useMessageNotifications } from '../../hooks/messageAlertDispatch';
import {RequestUri} from "../../services/api/RequestUri";

type State = {
    type: 'loading'
    payload?: NotificationPayload
} | {
    type: 'loaded',
    payload: NotificationPayload,
    error?: string
    messageAlert?: {
        userName: string;
        channelName: string;
        channelId: number;
        content: string;
    }
}

type Action =
| { type: 'load' }
| { type: 'setError', error: string}
| { type: 'setLoaded', payload: NotificationPayload }
| { type: 'start' }
| { type: 'showMessageAlert', message: { userName: string; channelName: string; channelId: number; content: string } }
| { type: 'hideMessageAlert' }

function reducer(state: State, action: Action): State {
    switch (state.type) {
        case 'loading':
            switch (action.type) {
                case 'setError':
                    return { type: 'loaded',payload: state.payload || {channels: []}, error: action.error };
                case 'setLoaded':
                    return { type: 'loaded', payload: action.payload};
                default:
                    unexpectedAction(action, state);
                    return state;
            }
        
        case 'loaded':
            switch (action.type) {
                case 'start':
                    return { type: 'loading' , payload: state.payload};
                case 'setError':
                    return { type: 'loaded', payload: state.payload, error: action.error };
                case 'showMessageAlert':
                    return { ...state, messageAlert: action.message };
                case 'hideMessageAlert':
                    return { type: 'loaded', payload: state.payload }
                default:
                    unexpectedAction(action, state);
                    return state;
            }
    }
}

function unexpectedAction(action: Action, state: State) {
    console.error('Unexpected action:', action, 'in state:', state)
}

export function Notifications() {
    const fetchState = useFetch(RequestUri.notifications.getIvites);
    const [state, dispatch] = useReducer(reducer, { type: 'loading', payload: {channels: []} });

    useMessageNotifications(dispatch)


    useEffect(() => {
        switch (fetchState.type) {
            case 'loaded':
                dispatch({
                    type: 'setLoaded',
                    payload:  fetchState.payload as unknown as NotificationPayload
                });
                break;
            case 'error':
                dispatch({
                    type: 'setError',
                    error: fetchState.error.message,
                });
                break;
        }
    }, [fetchState]);

    const handleJoin = async (notificationId: number) => {
        dispatch({ type: 'start' });
        const result = await notificationsService.joinChannel(notificationId);
        if (! isOk(result)) {
            dispatch({ type: 'setError', error: result.error });
        }else{
            const newPayload = state.payload.channels.filter(channel => channel.channelId !== notificationId);
            dispatch({ type: 'setLoaded', payload: {channels: newPayload} });
        }
    };

    const handleDecline = async (notificationId: number) => {
        dispatch({ type: 'start' });
        const result = await notificationsService.declineChannel(notificationId)
        if (!isOk(result)) {
            dispatch({ type: 'setError', error: result.error });
        }else{
            const newPayload = state.payload.channels.filter(channel => channel.channelId !== notificationId);
            dispatch({ type: 'setLoaded', payload: {channels: newPayload} });
        }
    };

    const leftContent = (
        <div className="p-6">
            <h1 className="text-2xl font-bold text-gray-800">Notifications</h1>
        </div>
    );
    
    const rightContent = (
        <div className="h-full bg-gray-50">
            <div className="max-w-3xl mx-auto p-6 space-y-4">
            {state.type === 'loaded' && state.messageAlert && (
                <div 
                    className="fixed top-4 left-4 z-50 animate-slide-in"
                    style={{
                        maxWidth: 'calc(100vw - 32px)',
                        width: '400px',
                    }}
                >
                    <div 
                        onClick={(e) => dispatch({type: 'hideMessageAlert'})}
                        className="bg-purple-50 border border-purple-100 rounded-xl p-4 cursor-pointer
                                hover:bg-purple-100 transition-colors shadow-lg backdrop-blur-sm
                                relative group"
                    >
                        <button 
                            className="absolute -top-2 -right-2 w-6 h-6 bg-white rounded-full shadow-sm
                                    flex items-center justify-center text-gray-400 hover:text-gray-600
                                    opacity-0 group-hover:opacity-100 transition-opacity"
                            onClick={(e) => {
                                e.stopPropagation();
                                dispatch({type: 'hideMessageAlert'});
                            }}
                        >
                        </button>
                        <div className="text-sm font-medium text-purple-800 mb-1">
                            New message on channel {state.messageAlert.channelName}
                        </div>
                        <div className="text-sm text-purple-600">
                            <span className="font-medium">{state.messageAlert.userName}</span>: {state.messageAlert.content}
                        </div>
                    </div>
                </div>
            )}
                {state.type === 'loading' && (
                    <div className="flex items-center justify-center py-12">
                        <div className="animate-pulse flex flex-col items-center">
                            <div className="w-12 h-12 rounded-full bg-purple-200 mb-4"></div>
                            <div className="text-gray-500">Loading notifications...</div>
                        </div>
                    </div>
                )}
    
                {state.type === 'loaded' && state.payload && (
                    state.payload.channels.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-12 text-gray-500">
                            <svg className="w-16 h-16 text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" 
                                      d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                            </svg>
                            <p className="text-lg font-medium">No invites yet</p>
                            <p className="text-sm">When someone invites you to a channel, you'll see it here</p>
                        </div>
                    ) : (
                        <ul className="space-y-4">
                            {state.type === 'loaded' && state.payload.channels.map((notification) => (
                                <li
                                    key={notification.channelId}
                                    className="bg-white rounded-xl border border-gray-200 shadow-sm 
                                             hover:border-purple-200 transition-colors"
                                >
                                    <div className="p-4">
                                        <div className="flex items-start gap-3">
                                            <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-purple-500 to-purple-600 
                                                          flex items-center justify-center text-white font-medium">
                                                {notification.channelName.charAt(0).toUpperCase()}
                                            </div>
                                            <div className="flex-1">
                                                <h3 className="text-base font-medium text-gray-900 mb-1">
                                                    {notification.channelName}
                                                </h3>
                                                <p className="text-sm text-gray-500">
                                                    Invited by <span className="font-medium">{notification.inviterName}</span>
                                                </p>
                                            </div>
                                        </div>
                                        
                                        <div className="flex items-center gap-3 mt-4 pt-4 border-t border-gray-100">
                                            <button
                                                onClick={() => handleJoin(notification.channelId)}
                                                className="flex-1 py-2 bg-purple-600 text-white text-sm font-medium rounded-lg
                                                         hover:bg-purple-700 transition-colors"
                                            >
                                                Accept Invite
                                            </button>
                                            <button
                                                onClick={() => handleDecline(notification.channelId)}
                                                className="flex-1 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg
                                                         hover:bg-gray-200 transition-colors"
                                            >
                                                Decline
                                            </button>
                                        </div>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )
                )}
    
                {state.type === 'loaded' && state.error && (
                    <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
                        <div className="flex items-center gap-2">
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" 
                                      d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                            </svg>
                            {state.error}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );

    return (<SplitColumnLayout left={leftContent} right={rightContent} />);
}