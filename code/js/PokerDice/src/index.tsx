import * as React from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import { SSEProvider } from './providers/SSEContext';
import { RequestUri } from './services/api/RequestUri'
import { fetchWrapper } from './services/api/utils'

// Função para buscar o token nos cookies
function getCookie(name: string): string | null {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop()?.split(';').shift() ?? null;
    return null;
}

// Verificar o token ao iniciar a app
async function validateTokenOnStart() {
    const token = getCookie("token");  // Alteração: buscando o token nos cookies
    if (!token) return;

    const result = await fetchWrapper(
        RequestUri.user.getMe,
        { method: "GET" }
    )

    if (!result.success) {
        document.cookie = "token=; Max-Age=0";  // Remover o token se não for válido
        //localStorage.removeItem("token");
    }
}

// Chama o safety check e depois renderiza a app
validateTokenOnStart().finally(() => {
    const root = createRoot(document.getElementById('root') as HTMLElement)

    root.render(
        <SSEProvider>
            <App />
        </SSEProvider>
    )
})
