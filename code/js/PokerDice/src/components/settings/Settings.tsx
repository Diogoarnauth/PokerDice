import React ,{useReducer} from 'react'
import { SplitColumnLayout } from '../layout/SplitColumnLayout'
import { Navigate, useLocation} from 'react-router-dom'
import { AppInviteCode } from '../models/AppInviteCode'
import { useSSEEmitter } from '../../providers/SSEContext'
import { settingsService } from '../../services/api/settings'
import { isOk } from '../../services/api/utils'
import { useAuthentication } from '../../providers/authentication'
import { messageCounters } from '../../services/storage/counterStorage'
import { useMessageNotifications } from '../../hooks/messageAlertDispatch'

type State =
| {
    type: 'idle';
    appInviteCode: AppInviteCode | null;
    showAppInviteModal: boolean;
    showCopiedMessage: boolean;
    error?: string;
    messageAlert?: { userName: string; channelName: string; channelId: number; content: string };
}
| {type: 'loading'}
| {type: 'redirect'};

type Action =
| {type: 'load'}
| {type: 'showAppInviteModal', appInviteCode: AppInviteCode}
| { type: 'closeModal' }
| { type: 'showCopiedMessage', showCopiedMessage: boolean }
| { type: 'setRedirect' }
| { type: 'setError'; error: string }
| { type: 'showMessageAlert', message: { userName: string; channelName: string; channelId: number; content: string } }
| { type: 'hideMessageAlert' };

function unexpectedAction(action: Action, state: State) {
    console.error('Unexpected action:', action, 'in state:', state)
}

function reduce(state: State, action: Action): State {
    switch (state.type) {
        case 'idle':
            switch (action.type) {
                case 'showAppInviteModal':
                    return { ...state, appInviteCode: action.appInviteCode, showAppInviteModal: true }
                case 'closeModal':
                    return { ...state, showAppInviteModal: false }
                case 'showCopiedMessage':
                    return { ...state, showCopiedMessage: action.showCopiedMessage }
                case 'setError':
                    return { ...state, error: action.error }
                case 'setRedirect':
                    return {type: 'redirect'}
                case 'load':
                    return {type: 'loading'}
                case 'hideMessageAlert':
                    return { ...state, messageAlert: undefined }
                case 'showMessageAlert':
                    return { ...state, messageAlert: action.message }
                default:
                    unexpectedAction(action, state)
                    return state
            }
        case 'loading':
            switch (action.type) {
                case 'showAppInviteModal':
                    return {
                        type: 'idle',
                        appInviteCode: action.appInviteCode,
                        showAppInviteModal: true,
                        showCopiedMessage: false,
                        error: null,
                        messageAlert: undefined
                    }
                case 'setError':
                    return {
                        type: 'idle',
                        appInviteCode: null,
                        showAppInviteModal: false,
                        showCopiedMessage: false,
                        error: action.error,
                        messageAlert: undefined
                    }
                default:
                    unexpectedAction(action, state)
                    return state
            }
        default:
            unexpectedAction(action, state)
            return state
    }
}

export function Settings() {
    const [state, dispatch] = useReducer(reduce, {
        type: 'idle',
        appInviteCode: null,
        showAppInviteModal: false,
        showCopiedMessage: false,
        error: undefined,
        messageAlert: undefined
    })
    const location = useLocation()
    const [,disconnectSSE] = useSSEEmitter()
    const [, , clearUsername] = useAuthentication()

    useMessageNotifications(dispatch)

    const handleCreateAppInvite = async () => {
        dispatch({type: 'load'})
        const result = await settingsService.createInvite();

        if (isOk(result) && result.value && typeof result.value.inviteCode === "string") {
            dispatch({ type: "showAppInviteModal", appInviteCode: result.value });
        } else if (!isOk(result) && 'error' in result) {
            dispatch({ type: "setError", error: result.error });
        } else {
            dispatch({ type: "setError", error: "Código de convite não válido"});
        }

        }

    if (state.type === 'redirect') {
        return <Navigate to={location.state?.source || '/'} replace={true} />
    }

    const handleCopyCode = async () => {
        if (state.type === 'idle' && state.appInviteCode) {
            try {
                await navigator.clipboard.writeText(state.appInviteCode.inviteCode)
                dispatch({type: 'showCopiedMessage', showCopiedMessage: true})
                setTimeout(() => dispatch({type: 'showCopiedMessage', showCopiedMessage: false}), 1000)
            } catch (error) {
                dispatch({type: 'setError', error: 'Failed to copy code to clipboard'})
            }
        }
    }

    const handleLogout = async () => {
        const result = await settingsService.logout()
        if (isOk(result)) {
            disconnectSSE();
            clearUsername();
            messageCounters.delete()
            dispatch({type: 'setRedirect'})
        } else {
            dispatch({type: 'setError', error: result.error})
        }
    }

    const rightContent = (
        <div className="h-full bg-gray-50 p-6">
            {state.type === 'idle' && state.messageAlert && (
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
                    {/* Close button */}
                    <button 
                        className="absolute -top-2 -right-2 w-6 h-6 bg-white rounded-full shadow-sm
                                 flex items-center justify-center text-gray-400 hover:text-gray-600
                                 opacity-0 group-hover:opacity-100 transition-opacity"
                        onClick={(e) => {
                            e.stopPropagation();
                            dispatch({type: 'hideMessageAlert'});
                        }}
                    >
                        ×
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
            <div className="max-w-2xl mx-auto space-y-6">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <button 
                        onClick={handleCreateAppInvite}
                        className="flex items-center justify-center gap-2 px-6 py-3 bg-purple-600 text-white 
                                 rounded-xl font-medium hover:bg-purple-700 transition-colors
                                 shadow-sm hover:shadow-md"
                    >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" 
                                  d="M12 4v16m8-8H4"/>
                        </svg>
                        Create App Invite
                    </button>
                    
                    <button 
                        onClick={handleLogout}
                        className="flex items-center justify-center gap-2 px-6 py-3 bg-gray-100 text-gray-700
                                 rounded-xl font-medium hover:bg-gray-200 transition-colors
                                 shadow-sm hover:shadow-md"
                    >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" 
                                  d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
                        </svg>
                        Logout
                    </button>
                </div>
    
                {state.type === 'idle' && state.showAppInviteModal && (
                    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
                        <div className="bg-white rounded-2xl shadow-lg max-w-md w-full p-6 animate-fade-in">
                            <h3 className="text-xl font-bold text-gray-800 mb-4">App Invite Code</h3>
                            
                            <div className="bg-gray-50 rounded-lg p-4 mb-6">
                                <p className="font-mono text-lg text-center text-gray-700 select-all">
                                    {state.appInviteCode?.inviteCode}
                                </p>
                            </div>
    
                            <div className="flex flex-col gap-3">
                                <div className="relative">
                                    <button
                                        onClick={handleCopyCode}
                                        className="w-full py-2.5 bg-purple-600 text-white rounded-lg font-medium
                                                 hover:bg-purple-700 transition-colors flex items-center justify-center gap-2"
                                    >
                                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" 
                                                  d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3"/>
                                        </svg>
                                        Copy Code
                                    </button>
                                    {state.showCopiedMessage && (
                                        <div className="absolute -top-8 left-1/2 -translate-x-1/2 
                                                      bg-gray-800 text-white text-sm px-3 py-1 rounded-md">
                                            Copied!
                                        </div>
                                    )}
                                </div>
                                
                                <button 
                                    onClick={() => dispatch({type: 'closeModal'})}
                                    className="w-full py-2.5 bg-gray-100 text-gray-700 rounded-lg font-medium
                                             hover:bg-gray-200 transition-colors"
                                >
                                    Close
                                </button>
                            </div>
                        </div>
                    </div>
                )}
    
                {state.type === 'idle' && state.error && (
                    <div className="p-4 bg-red-50 border border-red-200 rounded-xl text-sm text-red-600">
                        <div className="flex items-center gap-2">
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" 
                                      d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                            </svg>
                            Error: {state.error}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
    
    return (
        <SplitColumnLayout 
            left={
                <div className="p-6">
                    <h1 className="text-2xl font-bold text-gray-800">Settings</h1>
                </div>
            }
            right={rightContent}
        />
    );
}
