import React, {useReducer, useState, useEffect} from 'react';
import {Navigate, useLocation, Link, useNavigate} from 'react-router-dom';
import {useAuthentication} from '../../providers/Authentication';
import {authService} from '../../services/api/auth';
import {isOk} from '../../services/api/utils';
import '../../styles/Login.css';

// Tipos de estado para o formulário
type State =
    | {
    type: 'editing';
    inputs: { username: string; password: string };
    showPassword: boolean;
    error: string | null;
    shouldRedirect: boolean
}
    | {
    type: 'submitting';
    inputs: { username: string; password: string };
    showPassword: boolean;
    error: string | null;
    isLoading: boolean;
    shouldRedirect: boolean
}
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
                    return {...state, inputs: {...state.inputs, [action.inputName]: action.inputValue}}
                case 'submit':
                    return {
                        type: 'submitting',
                        inputs: action.inputs,
                        showPassword: state.showPassword,
                        error: null,
                        isLoading: true,
                        shouldRedirect: false
                    }
                case 'togglePassword':
                    return {...state, showPassword: !state.showPassword}
                default:
                    return state
            }
        case 'submitting':
            switch (action.type) {
                case 'setError':
                    return {
                        type: 'editing',
                        inputs: {...state.inputs, password: ''},
                        showPassword: false,
                        error: action.error,
                        shouldRedirect: false
                    }
                case 'setRedirect':
                    return {type: 'redirect'}
                default:
                    return state
            }
        default:
            return state
    }
}

export default function Login() {
    const [state, dispatch] = useReducer(reduce, {
        type: 'editing',
        inputs: {username: '', password: ''},
        showPassword: false,
        error: null,
        shouldRedirect: false,
    });
    const {username, setUsername} = useAuthentication();
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        if (username) {
            const destination = location.state?.source || "/";
            navigate(destination, {replace: true});
        }
    }, [username, navigate, location]);

    if (state.type === 'redirect') {
        return <Navigate to={location.state?.source ?? '/'} replace={true}/>;
    }

    // Manipula as mudanças nos campos do formulário
    function handleChange(ev: React.ChangeEvent<HTMLInputElement>) {
        dispatch({type: 'edit', inputName: ev.target.name, inputValue: ev.target.value});
    }

    // Submete o formulário
    async function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault();
        if (state.type === 'editing') {
            dispatch({type: 'submit', inputs: state.inputs});
            const result = await authService.login(state.inputs);

            if (isOk(result)) {
                setUsername(state.inputs.username);
                dispatch({type: 'setRedirect'});

            } else {
                const problem = result.error;

                if (problem.status && problem.status >= 500) {
                    navigate('/error', {state: {error: problem}});
                } else {

                    const msg = problem.detail || problem.title || "Erro no login";
                    dispatch({type: 'setError', error: msg});
                }
            }
        }
    }

    const inputs = state.type === 'editing' || state.type === 'submitting'
        ? state.inputs
        : {username: '', password: ''};

    return (
        <div className="login-page">
            <div className="login-card">
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
                                    <button
                                        type="button"
                                        onClick={() => dispatch({type: 'togglePassword'})}
                                        className="toggle-password"
                                    >
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

                    {state.error && (
                        <div className="auth-error">
                            {state.error}
                        </div>
                    )}
                </form>
            </div>
        </div>
    );
}
