import * as React from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import { SSEProvider } from './providers/SSEContext';
import { RequestUri } from './services/api/RequestUri'
import { fetchWrapper } from './services/api/utils'

// safety check simples ao arrancar a app

async function validateTokenOnStart() {
    const token = localStorage.getItem("token")
    if (!token) return

    const result = await fetchWrapper(
        RequestUri.user.getMe,
        { method: "GET" }
    )

    if (!result.success) {
        localStorage.removeItem("token")
        //localStorage.removeItem("username")
    }
}


    // chama o safety check e depois renderiza a app
    validateTokenOnStart().finally(() => {
        const root = createRoot(document.getElementById('root') as HTMLElement)

        root.render(
            <SSEProvider>
                <App />
            </SSEProvider>
        )
})
