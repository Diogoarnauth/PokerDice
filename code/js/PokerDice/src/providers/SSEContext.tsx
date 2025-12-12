import React, {createContext, useContext, useEffect, useRef, useState} from "react";
import {RequestUri} from "../services/api/RequestUri";
import {useAuthentication} from "./Authentication";

type SSEContextType = {
    isConnected: boolean;
    addHandler: (type: string, handler: (data: any) => void) => void;
    removeHandler: (type: string) => void;
    updateTopic: (newTopic: string) => void;
    connect: () => Promise<void>;
    disconnect: () => void;
};

const SSEContext = createContext<SSEContextType | null>(null);

export function SSEProvider({children}: { children: React.ReactNode }) {
    const {username, isLoading} = useAuthentication();

    const eventSourceRef = useRef<EventSource | null>(null);
    const handlers = useRef<Record<string, (data: any) => void>>({});
    const [isConnected, setConnected] = useState(false);
    const [topic, setTopic] = useState<string>("home"); // Começa em home


    const updateTopic = (newTopic: string) => {
        setTopic(newTopic);
    };

    // Função de desconexão manual
    const disconnect = () => {
        if (eventSourceRef.current) {
            eventSourceRef.current.close();
            eventSourceRef.current = null;
            setConnected(false);
        }
    };

    // Função de conexão (agora pode ser chamada manualmente)
    const connect = async () => {

        if (isLoading || !username) {
            console.log("SSE: Utilizador não autenticado. Conexão abortada.");
            return;
        }

        // Evitar duplicados
        if (eventSourceRef.current) {
            if (eventSourceRef.current.readyState !== EventSource.CLOSED) return;
            eventSourceRef.current.close();
        }

        const url = `${RequestUri.user.listen}?topic=${topic}`;
        console.log(`SSE: A conectar a ${url} como ${username}`);

        const es = new EventSource(url, {withCredentials: true});
        eventSourceRef.current = es;

        es.onopen = () => {
            console.log("SSE: Conectado!");
            setConnected(true);
        };

        es.onerror = (err) => {
            console.error("SSE: Erro", err);
            setConnected(false);
            es.close();
            eventSourceRef.current = null;
        };


        // Ouvir eventos genéricos (message)
        es.onmessage = (msg) => {
            console.log("Evento genérico recebido:", msg.data);
        };

        es.addEventListener("lobbies_list_changes", (e: any) => {
            console.log("Evento lobbies_list_changes recebido");
            const h = handlers.current["lobbies_list_changes"];
            if (h) h(JSON.parse(e.data));
        });

        es.addEventListener("game_update", (e: any) => {
            const h = handlers.current["game_update"];
            if (h) h(JSON.parse(e.data));
        });

    };

    useEffect(() => {
        if (!isLoading && username) {
            connect();
        } else {
            disconnect();
        }

        return () => disconnect();
    }, [username, isLoading, topic]);

    // Gestão de handlers
    function addHandler(type: string, handler: (data: any) => void) {
        handlers.current[type] = handler;
    }

    function removeHandler(type: string) {
        delete handlers.current[type];
    }

    return (
        <SSEContext.Provider value={{isConnected, addHandler, removeHandler, updateTopic, connect, disconnect}}>
            {children}
        </SSEContext.Provider>
    );
}

// HOOKS

export function useSSE() {
    const ctx = useContext(SSEContext);
    if (!ctx) throw new Error("useSSE must be inside SSEProvider");
    return ctx;
}


export function useSSEEmitter() {
    const ctx = useContext(SSEContext);
    if (!ctx) throw new Error("useSSEEmitter must be inside SSEProvider");

    // O RequireAuthentication espera um array [connect, disconnect, status]
    return [ctx.connect, ctx.disconnect, ctx.isConnected] as const;
}