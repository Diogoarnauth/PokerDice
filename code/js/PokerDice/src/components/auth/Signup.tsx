import React, { useReducer} from 'react'
import { Navigate, useLocation, Link } from 'react-router-dom'
import { isOk } from '../../services/api/utils'
import { authService } from '../../services/api/auth'
import '../../styles/auth.css'

type State =
| {
    type: 'editingAdmin';
    inputs: {username: string; password: string; confirmPassword: string};
    showPassword: boolean;
    error: string | null;
    shouldRedirect: boolean;
    passwordCriteria: {
        minLength: boolean;
        maxLength: boolean;
        hasNumber: boolean;
        hasSpecialChar: boolean;
        hasUppercase: boolean;
    };
}
| {
    type: 'editingUser';
    inputs: {username: string; password: string; confirmPassword: string; inviteCode: string};
    showPassword: boolean;
    error: string | null;
    shouldRedirect: boolean;
    passwordCriteria: {
        minLength: boolean;
        maxLength: boolean;
        hasNumber: boolean;
        hasSpecialChar: boolean;
        hasUppercase: boolean;
    };
}
| {type: 'redirect'}
| {
    type: 'submittingAdmin';
    inputs: {username: string; password: string; confirmPassword: string};
    showPassword: boolean;
    error: string | null;
    isLoading: boolean;
    shouldRedirect: boolean;
    passwordCriteria: {
        minLength: boolean;
        maxLength: boolean;
        hasNumber: boolean;
        hasSpecialChar: boolean;
        hasUppercase: boolean;
    };
}
| {
    type: 'submittingUser';
    inputs: {username: string; password: string; confirmPassword: string; inviteCode: string};
    showPassword: boolean;
    error: string | null;
    isLoading: boolean;
    shouldRedirect: boolean;
    passwordCriteria: {
        minLength: boolean;
        maxLength: boolean;
        hasNumber: boolean;
        hasSpecialChar: boolean;
        hasUppercase: boolean;
    };
}


type Action =
| { type: 'edit'; inputName: string; inputValue: string }
| { type: 'submitAdmin'; inputs: {username: string; password: string; confirmPassword: string} }
| { type: 'submitUser'; inputs: {username: string; password: string; confirmPassword: string; inviteCode: string} }
| { type: 'togglePassword' }
| { type: 'setError'; error: string | null }
| { type: 'setRedirect' }
| { type: 'updatePasswordCriteria'; criteria: {
    minLength: boolean;
    maxLength: boolean;
    hasNumber: boolean;
    hasSpecialChar: boolean;
    hasUppercase: boolean;
}}

function logUnexpectedAction(state: State, action: Action) {
    console.log(`Unexpected action '${action.type} on state '${state.type}'`)
}


function reduce(state: State, action: Action): State {
  switch (state.type) {

    // ---------------------
    // ---------------------
    // EDITING ADMIN
    // ---------------------
    case "editingAdmin":
      switch (action.type) {
        case "edit":
          return {
            ...state,
            inputs: { ...state.inputs, [action.inputName]: action.inputValue }
          };

        case "togglePassword":
          return { ...state, showPassword: !state.showPassword };

        case "updatePasswordCriteria":
          return { ...state, passwordCriteria: action.criteria };

        case "submitAdmin":
          return {
            type: "submittingAdmin",
            inputs: action.inputs,
            showPassword: state.showPassword,
            error: null,
            isLoading: true,
            shouldRedirect: false,
            passwordCriteria: state.passwordCriteria
          };

        default:
          logUnexpectedAction(state, action);
          return state;
      }

    // ---------------------
    // EDITING USER
    // ---------------------
    case "editingUser":
      switch (action.type) {
        case "edit":
          return {
            ...state,
            inputs: { ...state.inputs, [action.inputName]: action.inputValue }
          };

        case "togglePassword":
          return { ...state, showPassword: !state.showPassword };

        case "updatePasswordCriteria":
          return { ...state, passwordCriteria: action.criteria };

        case "submitUser":
          return {
            type: "submittingUser",
            inputs: action.inputs,
            showPassword: state.showPassword,
            error: null,
            isLoading: true,
            shouldRedirect: false,
            passwordCriteria: state.passwordCriteria
          };

        default:
          logUnexpectedAction(state, action);
          return state;
      }

    // ---------------------
    // SUBMITTING ADMIN
    // ---------------------
    case "submittingAdmin":
      switch (action.type) {
        case "setError":
          return {
            type: "editingAdmin",
            inputs: { ...state.inputs, password: "", confirmPassword: "" },
            showPassword: false,
            error: action.error,
            shouldRedirect: false,
            passwordCriteria: {
              minLength: false,
              maxLength: false,
              hasNumber: false,
              hasSpecialChar: false,
              hasUppercase: false
            }
          };

        case "setRedirect":
          return { type: "redirect" };

        default:
          logUnexpectedAction(state, action);
          return state;
      }

    // ---------------------
    // SUBMITTING USER
    // ---------------------
    case "submittingUser":
      switch (action.type) {
        case "setError":
          return {
            type: "editingUser",
            inputs: { ...state.inputs, password: "", confirmPassword: "" },
            showPassword: false,
            error: action.error,
            shouldRedirect: false,
            passwordCriteria: {
              minLength: false,
              maxLength: false,
              hasNumber: false,
              hasSpecialChar: false,
              hasUppercase: false
            }
          };

        case "setRedirect":
          return { type: "redirect" };

        default:
          logUnexpectedAction(state, action);
          return state;
      }

    // ---------------------
    // REDIRECT
    // ---------------------
    case "redirect":
      // Não aceita nenhuma ação. Apenas ignora.
      return state;

    // Fallback
    default:
      logUnexpectedAction(state, action);
      return state;
  }
}
