import React, { useEffect } from 'react'
import { useAuthentication } from '../../providers/authentication'
import { Navigate} from 'react-router-dom'
import { useSSEEmitter } from '../../providers/SSEContext'
import { useLocation } from 'react-router-dom'
import { messageCounters } from '../../services/storage/counterStorage'

export function RequireAuthentication({ children }) {
  const [username,setUsername] = useAuthentication()
  const hasCookie = document.cookie.includes('token')
  const [connectSSE, disconnectSSE, isSSEConnected] = useSSEEmitter()
  const location = useLocation()

  useEffect(() => {
    const connectIfNeeded = async () => {
      if (hasCookie && !isSSEConnected ) {
        try {
          await connectSSE()
        } catch (error) {
          console.error('Failed to connect to SSE:', error)
        }
      }
    }

    connectIfNeeded()

    return () => {
      if (!hasCookie && isSSEConnected) {
        disconnectSSE()
      }
    }
  }, [hasCookie])
  

  if (username && hasCookie) {
    return children
  } else {
    messageCounters.delete()
    setUsername(null)
    return <Navigate to="/" state={{ source: location.pathname }} replace={true} />
  }
}