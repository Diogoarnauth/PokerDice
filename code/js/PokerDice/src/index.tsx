import * as React from 'react'
import { createRoot } from 'react-dom/client'
import { App } from './App'
import { SSEEmitterProvider } from './providers/SSEContext'

const root = createRoot(document.getElementById('main-div') as HTMLElement)
root.render(
    <SSEEmitterProvider>
        <App />
    </SSEEmitterProvider>
)