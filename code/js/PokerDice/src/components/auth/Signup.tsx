import React, { useReducer, useState, useEffect } from "react";
import { Navigate, useLocation, Link, useNavigate } from "react-router-dom";
import { authService } from "../../services/api/auth";
import { isOk } from "../../services/api/utils";
import "../../styles/login.css";

// Tipos de estado para o formulário
type State =
  | { type: 'editing'; inputs: { username: string, password: string }; showPassword: boolean; error: string | null; shouldRedirect: boolean }
  | { type: 'submitting'; inputs: { username: string, password: string }; showPassword: boolean; error: string | null; isLoading: boolean; shouldRedirect: boolean }
  | { type: 'redirect' };

type Action =
  | { type: 'edit'; inputName: string; inputValue: string }
  | { type: 'submit'; inputs: { username: string, password: string } }
  | { type: 'togglePassword' }
  | { type: 'setError'; error: string | null }
  | { type: 'setLoading'; isLoading: boolean }
  | { type: 'setRedirect' };

// Função para redução do estado do formulário
function reduce(state: State, action: Action): State {
  switch (state.type) {
    case 'editing':
      switch(action.type){
        case 'edit':
          return { ...state, inputs: { ...state.inputs, [action.inputName]: action.inputValue } }
        case 'submit':
          return { type: 'submitting', inputs: action.inputs, showPassword: state.showPassword, error: null, isLoading: true, shouldRedirect: false };
        case 'togglePassword':
          return { ...state, showPassword: !state.showPassword }
        default:
          return state
      }
    case 'submitting':
      switch(action.type){
        case 'setError':
          return { type: 'editing', inputs: { ...state.inputs, password: '' }, showPassword: false, error: action.error, shouldRedirect: false }
        case 'setRedirect':
          return { type: 'redirect' }
        default:
          return state
      }
    default:
      return state
  }
}

export default function Signup() {

  const navigate = useNavigate();
  const [state, dispatch] = useReducer(reduce, {
    type: 'editing',
    inputs: { username: '', password: '' },
    showPassword: false,
    error: null,
    shouldRedirect: false,
  });

  // Fields
  const [username, setUsername] = useState("");
  const [name, setName] = useState("");
  const [age, setAge] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [inviteCode, setInviteCode] = useState("");

  // State
  const [isFirstUser, setIsFirstUser] = useState<boolean | null>(null);
  const [loadingCheck, setLoadingCheck] = useState(true);
  const [loadingSubmit, setLoadingSubmit] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);

  // 1. Fazer checkAdmin antes de renderizar o formulário
  useEffect(() => {
          const token = document.cookie.split(';').find(cookie => cookie.trim().startsWith('token='));
          if (token) {
            alert("Você já está logado!");
            navigate("/");  // Redireciona para a página principal ou qualquer outra página desejada
          }
    async function check() {
      const response = await authService.checkAdmin();
      if (isOk(response)) {
        setIsFirstUser(response.value.firstUser);
      } else {
        setError(response.error || "Error checking admin status");
      }
      setLoadingCheck(false);
    }

    check();
  }, []);

  if (loadingCheck) {
    return <p>Loading...</p>;
  }

  // Função para setar o cookie do token
  /*const setCookie = (name: string, value: string, days: number) => {
    const expires = new Date();
    expires.setTime(expires.getTime() + (days * 24 * 60 * 60 * 1000));
    document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/;Secure;SameSite=Strict`;
  };*/

  // 2. Submeter o formulário
  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    setResult(null);

    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    const parsedAge = Number(age);
    if (!Number.isInteger(parsedAge) || parsedAge <= 0) {
      setError("Age must be a positive integer");
      return;
    }

    setLoadingSubmit(true);

    let response;
    if (isFirstUser) {
      // Primeiro utilizador → signup admin (sem invite)
      response = await authService.signupAdmin({
        username,
        name,
        age: parsedAge,
        password,
      });
    } else {
      // Utilizador normal → signup com invite
      response = await authService.signup({
        username,
        name,
        age: parsedAge,
        password,
        inviteCode,
      });
    }

    if (isOk(response)) {
      setResult("Signup made with success. Redirecting to login...");

      // Aqui você deve armazenar o token no cookie após o signup com sucesso
     // setCookie("token", response.value.tokenValue, 1); // Armazenar o token por 1 dia

      setTimeout(() => {
        navigate("/login");
      }, 1000);
    } else {
      setError(response.error || "Unknown error");
    }

    setLoadingSubmit(false);
  }

  return (
    <div className="auth-container">
      <h1 className="auth-title">
        {isFirstUser ? "Create Admin Account" : "Sign Up"}
      </h1>

      {isFirstUser && (
        <p className="auth-text" style={{ marginBottom: "10px" }}>
          This will create the first user as <strong>admin</strong>.
        </p>
      )}

      <form onSubmit={handleSubmit}>
        {/* Desativa tudo enquanto está a submeter */}
        <fieldset disabled={loadingSubmit}>
          <div className="auth-form-group">
            <div>
              <label htmlFor="username" className="auth-label">
                Username
              </label>
              <input
                className="auth-input"
                type="text"
                id="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter your username"
                required
              />
            </div>

            <div>
              <label htmlFor="name" className="auth-label">
                Name
              </label>
              <input
                className="auth-input"
                type="text"
                id="name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Enter your name"
                required
              />
            </div>

            <div>
              <label htmlFor="age" className="auth-label">
                Age
              </label>
              <input
                className="auth-input"
                type="number"
                id="age"
                value={age}
                onChange={(e) => setAge(e.target.value)}
                min={1}
                placeholder="Enter your age"
                required
              />
            </div>

            <div>
              <label htmlFor="password" className="auth-label">
                Password
              </label>
              <input
                className="auth-input"
                type="password"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                required
              />
            </div>

            <div>
              <label htmlFor="confirmPassword" className="auth-label">
                Confirm Password
              </label>
              <input
                className="auth-input"
                type="password"
                id="confirmPassword"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Confirm your password"
                required
              />
            </div>

            {/* Só mostramos inviteCode se NÃO for primeiro user */}
            {!isFirstUser && (
              <div>
                <label htmlFor="inviteCode" className="auth-label">
                  Invite Code
                </label>
                <input
                  className="auth-input"
                  type="text"
                  id="inviteCode"
                  value={inviteCode}
                  onChange={(e) => setInviteCode(e.target.value)}
                  placeholder="Enter your invite code"
                  required
                />
              </div>
            )}

            <button
              type="submit"
              className="auth-submit"
              disabled={loadingSubmit}
            >
              {loadingSubmit
                ? "Submitting..."
                : isFirstUser
                ? "Create Admin"
                : "Sign Up"}
            </button>
          </div>
        </fieldset>

        {error && <div className="auth-error">{error}</div>}

        {result && (
          <p className="auth-info">
            {result}
          </p>
        )}
      </form>
    </div>
  );
}
