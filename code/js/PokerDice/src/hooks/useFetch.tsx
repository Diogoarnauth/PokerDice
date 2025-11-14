import { useEffect, useReducer } from "react";
import { useNavigate } from "react-router-dom";

type State =
  | { type: 'begin' }
  | { type: 'loading'; url: string }
  | { type: 'loaded'; payload: string; url: string }
  | { type: 'error'; error: Error; url: string };

type Action =
  | { type: 'start-loading'; url: string }
  | { type: 'loading-success'; payload: string; url: string }
  | { type: 'loading-error'; error: Error }


function unexpectedAction(action: Action, state: State) {
  console.log(`Unexpected action ${action.type} in state ${state.type}`);
  return state;
}

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'start-loading':
      return { type: 'loading', url: action.url };
    case 'loading-success':
      if (state.type !== 'loading') {
        return unexpectedAction(action, state);
      }
      return { type: 'loaded', payload: action.payload, url: state.url };
    case 'loading-error':
      if (state.type !== 'loading') {
        return unexpectedAction(action, state);
      }
      return { type: 'error', error: action.error, url: state.url };
  }
}

const firstState: State = { type: 'begin' };

type UseFetchResult = State;

export function useFetch(url: string): UseFetchResult {
  const [state, dispatch] = useReducer(reducer, firstState);
  const navigate = useNavigate();

  useEffect(() => {
    if (!url) {
      return;
    }
    let cancelled = false;
    const abortController = new AbortController();
    async function doFetch() {
      dispatch({ type: 'start-loading', url: url });

      const fetchOptions: RequestInit = {
        method: 'GET',
        signal: abortController.signal,
        headers: {
          'Content-Type': 'application/json',
        },
      };

      try {
        const resp = await fetch(url, fetchOptions);

        if (resp.status === 404) {
          navigate('/not-found');
          return;
        }

        if (!resp.ok) {
          const errorData = await resp.json();
          throw new Error(errorData.message);
        }

        let json = null;
        if (resp.status !== 204) {
          json = await resp.json();
        }

        if (!cancelled) {
          dispatch({ type: 'loading-success', url: url, payload: json });
        }
      } catch (error) {
        if (!cancelled) {
          dispatch({ type: 'loading-error', error: error});
        }
      }
    }

    doFetch();
    return () => {
      cancelled = true;
      abortController.abort();
    };
  }, [url]);
  return state
}