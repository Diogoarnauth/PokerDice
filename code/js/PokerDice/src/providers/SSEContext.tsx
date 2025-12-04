import React, { createContext, useContext, useEffect, useRef, useState } from "react";
import { RequestUri } from "../services/api/RequestUri";

type SSEContextType = {
  isConnected: boolean;
  addHandler: (type: string, handler: (data: any) => void) => void;
  removeHandler: (type: string) => void;
  updateTopic: (newTopic: string) => void; // Função para atualizar o tópico
};

const SSEContext = createContext<SSEContextType | null>(null);

export function SSEProvider({ children }: { children: React.ReactNode }) {
  const eventSourceRef = useRef<EventSource | null>(null);
  const handlers = useRef<Record<string, (data: any) => void>>({});
  const [isConnected, setConnected] = useState(false);
  const [topic, setTopic] = useState<string>("lobbies"); // Definindo o tópico inicial

  // Função para atualizar o tópico
  const updateTopic = (newTopic: string) => {
    setTopic(newTopic);
  };

  useEffect(() => {
    console.log("Topic atualizado:", topic);

    const token = localStorage.getItem("token");
    if (!token) return;

    // Montar a URL do EventSource com o tópico dinâmico
    const url = `/api/users/listen?token=${encodeURIComponent(token)}&topic=${topic}`;

    console.log("Vou me conectar")

    if (eventSourceRef.current) {
      console.log("EventSource já existe, mantendo a conexão.");
      // Não fecha a conexão, mantém o que já existe
      return;
    }

    const es = new EventSource(url);
    eventSourceRef.current = es;

    es.onopen = () => {
      console.log("SSE connected");
      setConnected(true);
    };

    es.onerror = (err) => {
      console.error("SSE error", err);
      setConnected(false);
    };

    es.onmessage = (msg) => {
      console.warn("Received unnamed event:", msg.data);
    };

    // Eventos nomeados dinâmicos
    es.addEventListener("lobbies_list_changes", (e: any) => {
      console.log("addEventListener lobbies_list_changes");
      const h = handlers.current["lobbies_list_changes"];
      if (h) h(JSON.parse(e.data));
    });

    // Cleanup ao desmontar o componente
    return () => {
      console.log("SSE CLOSED");
      // Não feche o EventSource, apenas quando o app for desmontado
      // eventSourceRef.current?.close();
    };
  }, [topic]); // Use o tópico como dependência para refazer a conexão quando o tópico mudar

  // Função para adicionar handler
  function addHandler(type: string, handler: (data: any) => void) {
    handlers.current[type] = handler;
  }

  // Função para remover handler
  function removeHandler(type: string) {
    delete handlers.current[type];
  }

  return (
    <SSEContext.Provider value={{ isConnected, addHandler, removeHandler, updateTopic }}>
      {children}
    </SSEContext.Provider>
  );
}

// Hook para consumir o SSEContext
export function useSSE() {
  const ctx = useContext(SSEContext);
  if (!ctx) throw new Error("useSSE must be inside SSEProvider");
  return ctx;
}
