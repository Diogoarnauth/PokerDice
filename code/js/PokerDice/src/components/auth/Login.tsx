import React, { useReducer, useState, useEffect } from 'react';
import { Navigate, useLocation, Link, useNavigate } from 'react-router-dom'; // Corrigido: useNavigate
import { useAuthentication } from '../../providers/authentication';
import { authService } from '../../services/api/auth';
import { isOk } from '../../services/api/utils';
import '../../styles/login.css';

// Tipos de estado para o formulário
type State =
  | { type: 'editing'; inputs: { username: string; password: string }; showPassword: boolean; error: string | null; shouldRedirect: boolean }
  | { type: 'submitting'; inputs: { username: string; password: string }; showPassword: boolean; error: string | null; isLoading: boolean; shouldRedirect: boolean }
  | { type: 'redirect' };

type Action =
  | { type: 'edit'; inputName: string; inputValue: string }
  | { type: 'submit'; inputs: { username: string; password: string } }
  | { type: 'togglePassword' }
  | { type: 'setError'; error: string | null }
  | { type: 'setLoading'; isLoading: boolean }
  | { type: 'setRedirect' };

// Função para redução do estado do formulário
function reduce(state: State, action: Action): State {
  switch (state.type) {
    case 'editing':
      switch (action.type) {
        case 'edit':
          return { ...state, inputs: { ...state.inputs, [action.inputName]: action.inputValue } };
        case 'submit':
          return { type: 'submitting', inputs: action.inputs, showPassword: state.showPassword, error: null, isLoading: true, shouldRedirect: false };
        case 'togglePassword':
          return { ...state, showPassword: !state.showPassword };
        default:
          return state;
      }
    case 'submitting':
      switch (action.type) {
        case 'setError':
          return { type: 'editing', inputs: { ...state.inputs, password: '' }, showPassword: false, error: action.error, shouldRedirect: false };
        case 'setRedirect':
          return { type: 'redirect' };
        default:
          return state;
      }
    default:
      return state;
  }
}

export default function Login() {
  const [state, dispatch] = useReducer(reduce, {
    type: 'editing',
    inputs: { username: '', password: '' },
    showPassword: false,
    error: null,
    shouldRedirect: false,
  });
  const [, setUsername] = useAuthentication();
  const location = useLocation(); // Para obter a localização da página anterior
  const navigate = useNavigate(); // Corrigido para usar o hook useNavigate



  // Se o estado for 'redirect', navegue para a página anterior
  if (state.type === 'redirect') {
      console.log("olaaaaaaa")
    return <Navigate to={location.state?.source ?? '/home'} replace={true} />;
  }

const token = localStorage.getItem("token");
if (token) {
    return <div className="already-logged">Já estás com o login feito</div>;
}

  // Manipula as mudanças nos campos do formulário
  function handleChange(ev: React.ChangeEvent<HTMLInputElement>) {
    dispatch({ type: 'edit', inputName: ev.target.name, inputValue: ev.target.value });
  }

  // Submete o formulário
  async function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
    ev.preventDefault();
    if (state.type === 'editing') {
      dispatch({ type: 'submit', inputs: state.inputs });
      const result = await authService.login(state.inputs);

      if (isOk(result)) {
        // Armazena o token no localStorage
        if (result.value.token) {
          localStorage.setItem("token", result.value.token); // Salva o token no localStorage
        }

        setUsername(state.inputs.username); // Armazena o nome de usuário após login
        dispatch({ type: 'setRedirect' }); // Redireciona o usuário

        // Aguarda e redireciona
        //setTimeout(() => {
          //navigate(location.state?.source ?? '/home');
        //}, 1000);

      } else {
        dispatch({ type: 'setError', error: result.error });
      }
    }
  }

  const inputs = state.type === 'editing' || state.type === 'submitting' ? state.inputs : { username: '', password: '' };

  return (
    <div className="container">
      <h1 className="title">Login</h1>
      <form onSubmit={handleSubmit}>
        <fieldset disabled={state.type === 'submitting' && state.isLoading}>
          <div className="input-container">
            <div>
              <label htmlFor="username" className="label">
                Username
              </label>
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

            <div>
              <label htmlFor="password" className="label">
                Password
              </label>
              <div className="password-container">
                <input
                  className="input"
                  id="password"
                  type={state.showPassword ? 'text' : 'password'}
                  name="password"
                  value={inputs.password}
                  onChange={handleChange}
                  placeholder="Enter your password"
                  required
                />
                <button type="button" onClick={() => dispatch({ type: 'togglePassword' })} className="toggle-password">
                  {state.showPassword ? '🙉' : '🙈'}
                </button>
              </div>
            </div>

            <button type="submit" className="submit-button">
              {state.type === 'submitting' && state.isLoading ? 'Loading...' : 'Sign In'}
            </button>
          </div>
        </fieldset>

        <div className="signup-container">
          <p className="signup-text">
            Don't have an account?{' '}
            <Link to="/signup" className="signup-link">
              Sign Up
            </Link>
          </p>
        </div>

        {state.error && <div className="error">{state.error}</div>}
        {state.type === 'submitting' && state.isLoading && <div className="loading">Loading...</div>}
      </form>
    </div>
  );
}
