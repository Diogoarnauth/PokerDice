import React, {createContext, useCallback, useContext, useRef} from 'react';
import {MessagesEvent} from '../components/models/MessagesEvent';
import {RequestUri} from "../services/api/RequestUri";

export type HandlerType = 'chat' | 'other';

interface MessageHandler {
  type: HandlerType;
  channelId: number | null;
  onMessage: (message: MessagesEvent) => void;
  onOtherChannelMessage: (channelId: number | MessagesEvent) => void;
}

interface SSEEmitterContextType {
  connectSSE: () => Promise<void>
  disconnectSSE: () => void
  isSSEConnected: boolean
  registerMessageHandler: (
    type: HandlerType,
    channelId: number | null,
    onMessage: (message: MessagesEvent) => void,
    onOtherChannelMessage: (channelId: number | MessagesEvent) => void) => void
  unregisterMessageHandler: () => void
}

const SSEEmitterContext = createContext<SSEEmitterContextType | undefined>(undefined);


export function SSEEmitterProvider({ children }: { children: React.ReactNode }) {
  const emitterRef = useRef<EventSource | null>(null);
  const [isSSEConnected, setIsConnected] = React.useState(false);
  // Para nao causar re-render
  const handlers = useRef<MessageHandler>();


  const handleMessage = (event: MessageEvent) => {
    try {
        const data = JSON.parse(event.data);
        const message = new MessagesEvent(data);
        const handler = handlers.current
          if (handler.type === 'chat') {
            if (handler.channelId === message.channelId) {
                handler.onMessage(message);
            } else {
                handler.onOtherChannelMessage(message.channelId);
            }
          } else {
            handler.onOtherChannelMessage(message);
        }
    } catch (e) {
        console.log('Raw SSE data:', event.data);
    }
  }
  const registerMessageHandler = useCallback((
    type: HandlerType,
    channelId: number | null,
    onMessage: (message: MessagesEvent) => void,
    onOtherChannelMessage: (channelId: number | MessagesEvent) => void
  ) => {
      handlers.current= {
        type,
        channelId,
        onMessage,
        onOtherChannelMessage
    };
      console.log('Message Handler Registered ', handlers.current)
  }, []);

  const unregisterMessageHandler = useCallback(() => {
    handlers.current = null
  }, []);

  const connectSSE = useCallback(() => {
    return new Promise<void>((resolve, reject) => {
        if (!emitterRef.current) {
            console.log('Connecting to SSE Emitter...')

            emitterRef.current = new EventSource(RequestUri.user.listen, {
                withCredentials: true
            });

            emitterRef.current.onopen = () => {
                console.log('SSE Emitter Connected')
                setIsConnected(true)
                resolve()
            };

            emitterRef.current.onerror = (error) => {
                console.error('SSE Emitter Error:', error)
                setIsConnected(false)
                reject(error)
            };
            emitterRef.current.addEventListener('message', handleMessage);
        } else {
            resolve()
        }
    });
}, []);

  const disconnectSSE = useCallback(() => {
    if (emitterRef.current) {

      emitterRef.current.removeEventListener('message', handleMessage);

      emitterRef.current.onopen = null;
      emitterRef.current.onerror = null;

      emitterRef.current.close();
      emitterRef.current = null;
      setIsConnected(false);
    }
  }, []);


  return (
    <SSEEmitterContext.Provider value={{
        connectSSE,
        disconnectSSE,
        isSSEConnected,
        registerMessageHandler,
        unregisterMessageHandler
    }}>
        {children}
    </SSEEmitterContext.Provider>
);
}

export function useSSEEmitter(): [
  () => Promise<void>,
  () => void,
  boolean,
  (type: HandlerType,
   channelId: number | null,
   onMessage: (message: MessagesEvent) => void,
   onOtherChannelMessage: (channelId: number | MessagesEvent) => void) => void,
  () => void
] {
  const context = useContext(SSEEmitterContext);
  if (context === undefined) {
      throw new Error('useSSEEmitter must be used within a SSEEmitterProvider');
  }
  return [
      context.connectSSE,
      context.disconnectSSE,
      context.isSSEConnected,
      context.registerMessageHandler,
      context.unregisterMessageHandler
  ];
}