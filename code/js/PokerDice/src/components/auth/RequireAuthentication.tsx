import React, {JSX, useEffect} from 'react';
import {useAuthentication} from '../../providers/Authentication';
import {Navigate, useLocation} from 'react-router-dom';
import {useSSEEmitter} from '../../providers/SSEContext';
import {messageCounters} from '../../services/storage/counterStorage';

export function RequireAuthentication({children}: { children: JSX.Element }) {
    const {username, setUsername, isLoading} = useAuthentication();
    const location = useLocation();

    if (isLoading) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-gray-50">
                <div className="flex flex-col items-center gap-4">
                    {/* Podes usar um spinner SVG aqui se quiseres */}
                    <div
                        className="w-10 h-10 border-4 border-purple-200 border-t-purple-600 rounded-full animate-spin"></div>
                    <p className="text-gray-500 font-medium animate-pulse">A verificar sessão...</p>
                </div>
            </div>
        );
    }

    if (!username) {
        // Limpeza de contadores locais (mantendo a lógica do teu projeto)
        messageCounters.delete();

        // Redireciona para o Login, guardando a página onde ele tentou ir (source)
        return <Navigate to="/login" state={{source: location.pathname}} replace={true}/>;
    }

    // 3. ESTADO AUTENTICADO
    // Temos username e não estamos a carregar. Mostra a página protegida.
    // (O SSEProvider vai ligar-se automaticamente porque o username existe)
    return <>{children}</>;
}