import React, { useReducer } from 'react'
import { Navigate, useLocation, Link } from 'react-router-dom'

import { useAuthentication } from '../../providers/authentication'
import { useSSEEmitter } from '../../providers/SSEContext'

import { authService } from '../../services/api/auth'
import { isOk } from '../../services/api/utils'

import '../../styles/login.css'

// ---------------------------
// Types for state machine
// ---------------------------

type State =
  | {
      type: 'editing'
      inputs: { username: string; password: string }
      showPassword: boolean
      error: string | null
      shouldRedirect: boolean
    }
  | {
      type: 'submitting'
      inputs: { username: string; password: string }
      showPassword: boolean
      error: string | null
      isLoading: boolean
      shouldRedirect: boolean
    }
  | { type: 'redirect' }

type Action =
  | { type: 'edit'; inputName: string; inputValue: string }
  | { type: 'submit'; inputs: { username: string; password: string } }
  | { type: 'togglePassword' }
  | { type: 'setError'; error: string | null }
  | { type: 'setRedirect' }

// ---------------------------
// Helper for unexpected transitions
// ---------------------------

function unexpectedAction(action: Action, state: State) {
  console.log(`Unexpected action ${action.type} in state ${state.type}`)
  return state
}

// ---------------------------
// Reducer logic (state machine)
// ---------------------------

function reduce(state: State, action: Action): State {
  switch (state.type) {
    case 'editing':
      switch (action.type) {
        case 'edit':
          return {
            ...state,
            inputs: { ...state.inputs, [action.inputName]: action.inputValue },
          }

        case 'submit':
          return {
            type: 'submitting',
            inputs: action.inputs,
            showPassword: state.showPassword,
            error: null,
            isLoading: true,
            shouldRedirect: false,
          }

        case 'togglePassword':
          return { ...state, showPassword: !state.showPassword }

        default:
          return unexpectedAction(action, state)
      }

    case 'submitting':
      switch (action.type) {
        case 'setError':
          return {
            type: 'editing',
            inputs: { ...state.inputs, password: '' },
            showPassword: false,
            error: action.error,
            shouldRedirect: false,
          }

        case 'setRedirect':
          return { type: 'redirect' }

        default:
          return unexpectedAction(action, state)
      }

    default:
      return unexpectedAction(action, state)
  }
}

// ---------------------------
// Component
// ---------------------------

export function Login() {
  // State machine
  const [state, dispatch] = useReducer(reduce, {
    type: 'editing',
    inputs: { username: '', password: '' },
    showPassword: false,
    error: null,
    shouldRedirect: false,
  })

  // Global authentication provider
  const [, setUsername] = useAuthentication()

  // To redirect after login
  const location = useLocation()

  // SSE connection (listen to backend events)
  const [connectSSE] = useSSEEmitter()

  // When login succeeds → redirect to /lobbies
  if (state.type === 'redirect') {
    return (
      <Navigate
        to={location.state?.source || '/lobbies'}
        replace={true}
      />
    )
  }

  // ---------------------------
  // Handle input changes
  // ---------------------------
  function handleChange(ev: React.ChangeEvent<HTMLInputElement>) {
    dispatch({
      type: 'edit',
      inputName: ev.target.name,
      inputValue: ev.target.value,
    })
  }

  // ---------------------------
  // Handle form submit (login)
  // ---------------------------
  async function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
    ev.preventDefault()

    if (state.type === 'editing') {
      dispatch({ type: 'submit', inputs: state.inputs })

      const result = await authService.login(state.inputs)

      if (isOk(result)) {
        // Save username in global app state
        setUsername(state.inputs.username)

        // Start SSE connection (receive events: lobby updates, game state, etc.)
        await connectSSE()

        // Trigger redirect
        dispatch({ type: 'setRedirect' })
      } else {
        dispatch({ type: 'setError', error: result.error })
      }
    }
  }

  // Extract fields depending on state
  const inputs =
    state.type === 'editing' || state.type === 'submitting'
      ? state.inputs
      : { username: '', password: '' }

  // ---------------------------
  // Render
  // ---------------------------

  return (
    <div className="container">
      <h1 className="title">PokerDice Login</h1>

      <form onSubmit={handleSubmit}>
        <fieldset disabled={state.type === 'submitting' && state.isLoading}>
          <div className="input-container">

            {/* Username */}
            <div>
              <label htmlFor="username" className="label">Username</label>
              <input
                className="input"
                id="username"
                type="text"
                name="username"
                value={inputs.username}
                onChange={handleChange}
                placeholder="Enter your username"
                required
              />
            </div>

            {/* Password */}
            <div>
              <label htmlFor="password" className="label">Password</label>
              <div className="password-container">
                <input
                  className="input"
                  id="password"
                  type={
                    state.type === 'editing' && state.showPassword
                      ? 'text'
                      : 'password'
                  }
                  name="password"
                  value={inputs.password}
                  onChange={handleChange}
                  placeholder="Enter your password"
                  required
                />

                <button
                  type="button"
                  onClick={() => dispatch({ type: 'togglePassword' })}
                  className="toggle-password"
                >
                  {state.type === 'editing' && state.showPassword
                    ? '🙉'
                    : '🙈'}
                </button>
              </div>
            </div>

            {/* Submit */}
            <button type="submit" className="submit-button">
              Sign In
            </button>
          </div>
        </fieldset>

        {/* Redirect to signup */}
        <div className="signup-container">
          <p className="signup-text">
            Don't have an account?{' '}
            <Link to="/signup" className="signup-link">
              Sign Up
            </Link>
          </p>
        </div>

        {/* Error message */}
        {state.type === 'editing' && state.error && (
          <div className="error">{state.error}</div>
        )}

        {/* Loading indicator */}
        {state.type === 'submitting' && <div className="loading">Loading...</div>}
      </form>
    </div>
  )
}
