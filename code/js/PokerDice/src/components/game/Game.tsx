import React, { useEffect, useReducer } from "react";
import { gameService } from "../../services/api/Games";
import { isOk } from "../../services/api/utils";
import { GamePayload, Game } from "../models/Game";
import { lobbyDetailsService } from "../../services/api/LobbyDetails";
import { useParams } from "react-router-dom";
import {User} from "../lobby/LobbyDetails";

// Função para obter o token do cookie
function getTokenFromCookies() {
  const token = document.cookie.split(';').find(cookie => cookie.trim().startsWith('token='));
  return token ? token.split('=')[1] : null;
}

// Estado do jogo
type GameState =
  | { type: "waitingForTurn"; game: Game; currentUser: User | null; currentPlayer: string | null; loading: boolean; error: string | null; info: string | null }
  | { type: "yourTurn"; game: Game; currentUser: User | null; currentPlayer: string | null; loading: boolean; error: string | null; info: string | null }
  | { type: "rolling"; isSubmitting: boolean; game: Game; currentUser: User | null; currentPlayer: string | null; loading: boolean; error: string | null; info: string | null }
  | { type: "rerolling"; isSubmitting: boolean; game: Game; currentUser: User | null; currentPlayer: string | null; loading: boolean; error: string | null; info: string | null }
  | { type: "endTurn"; isSubmitting: boolean; game: Game; currentUser: User | null; currentPlayer: string | null; loading: boolean; error: string | null; info: string | null };

// Ações do jogo
type Action =
  | { type: "startTurn"; game: Game; currentUser: User | null; currentPlayer: string | null; }
  | { type: "startRoll"; game: Game; currentUser: User | null; currentPlayer: string | null; }
  | { type: "submitRoll"; game: Game; currentUser: User | null; currentPlayer: string | null; }
  | { type: "startReroll"; game: Game; currentUser: User | null; currentPlayer: string | null; }
  | { type: "submitReroll"; game: Game; currentUser: User | null; currentPlayer: string | null; }
  | { type: "endTurn"; game: Game; currentUser: User | null; currentPlayer: string | null; }
  | { type: "setLoading"; loading: boolean }
  | { type: "setError"; error: string | null }
  | { type: "setInfo"; info: string | null };

function gameReducer(state: GameState, action: Action): GameState {
  switch (state.type) {
    case "waitingForTurn":
      if (action.type === "startTurn") {
        return {
          type: "yourTurn",
          game: action.game,
          currentUser: action.currentUser,
          currentPlayer: action.currentPlayer,
          loading: false,
          error: null,
          info: null,
        };
      }
      return state;

    case "yourTurn":
      if (action.type === "startRoll") {
        return {
          type: "rolling",
          isSubmitting: false,
          game: action.game,
          currentUser: action.currentUser,
          currentPlayer: action.currentPlayer,
          loading: false,
          error: null,
          info: null,
        };
      }
      return state;

    case "rolling":
      if (action.type === "submitRoll") {
        return {
          type: "rerolling",
          isSubmitting: true,
          game: action.game,
          currentUser: action.currentUser,
          currentPlayer: action.currentPlayer,
          loading: false,
          error: null,
          info: null,
        };
      }
      return state;

    case "rerolling":
      if (action.type === "submitReroll") {
        return {
          type: "rerolling",
          isSubmitting: false,
          game: action.game,
          currentUser: action.currentUser,
          currentPlayer: action.currentPlayer,
          loading: false,
          error: null,
          info: null,
        };
      } else if (action.type === "endTurn") {
        return {
          type: "endTurn",
          isSubmitting: true,
          game: action.game,
          currentUser: action.currentUser,
          currentPlayer: action.currentPlayer,
          loading: false,
          error: null,
          info: null,
        };
      }
      return state;

    case "endTurn":
      if (action.type === "endTurn") {
        return {
          type: "waitingForTurn",
          game: action.game,
          currentUser: action.currentUser,
          currentPlayer: action.currentPlayer,
          loading: false,
          error: null,
          info: null,
        };
      }
      return state;

    case "setLoading":
      return { ...state, loading: action.loading };

    case "setError":
      return { ...state, error: action.error };

    case "setInfo":
      return { ...state, info: action.info };

    default:
      return state;
  }
}

export default function GamePage() {
  const { lobbyId } = useParams<{ lobbyId: string }>();
  const [state, dispatch] = useReducer(gameReducer, {
    type: "waitingForTurn",
    game: { id: 0, currentPlayerUsername: "", dice: [], isFirstRoll: true },
    currentUser: null,
    currentPlayer: null,
    loading: true,
    error: null,
    info: null,
  });

  const token = getTokenFromCookies();

  // Função para garantir que é a vez do jogador
  function ensureMyTurn(): boolean {
    if (!state.game || !state.currentUser || state.currentUser.username !== state.currentPlayer) {
      dispatch({ type: "setInfo", info: "Não é a tua vez de jogar" });
      return false;
    }
    dispatch({ type: "setInfo", info: null }); // Limpa a mensagem de erro
    return true;
  }

  useEffect(() => {
    async function fetchGame() {
      if (!token) {
        dispatch({ type: "setError", error: "Token em falta." });
        dispatch({ type: "setLoading", loading: false });
        return;
      }

      dispatch({ type: "setLoading", loading: true });

      const meRes = await lobbyDetailsService.getMe(token);
      if (isOk(meRes)) {
        dispatch({ type: "setError", error: null });
      } else {
        dispatch({ type: "setError", error: "Não foi possível obter o utilizador autenticado." });
        dispatch({ type: "setLoading", loading: false });
        return;
      }

      const result = await gameService.getGameByLobbyId(Number(lobbyId));
      if (isOk(result)) {
        const payload = new GamePayload(result.value);
        const turnRes = await gameService.getPlayerTurn(payload.game.id);
        if (isOk(turnRes)) {
            console.log("turnRes.value.username", turnRes.value.username)
            console.log("meRes.value.username", meRes.value.username)

          if (turnRes.value.username === meRes.value.username) {
              console.log("entrou no startTurn, antes",state)
            dispatch({
              type: "startTurn",
              game: payload.game,
              currentUser: meRes.value,
              currentPlayer: turnRes.value.username,
            });
            console.log("entrou no startTurn, depois",state)

          } else {
             // console.log("entrou no waitingForTurn, antes",state)
            dispatch({
              type: "waitingForTurn",
              game: payload.game,
              currentUser: meRes.value,
              currentPlayer: turnRes.value.username,
            });
              //console.log("entrou no waitingForTurn, depois",state)

          }
        } else {
          dispatch({ type: "setError", error: turnRes.error ?? "Erro ao obter o jogador atual." });
        }
      } else {
        dispatch({ type: "setError", error: result.error ?? "Erro ao carregar o jogo." });
      }
      dispatch({ type: "setLoading", loading: false });
    }
    console.log("state final", state)
    fetchGame();
  }, [lobbyId, token]);

  async function handleRoll() {
    if (!state.game.id || !ensureMyTurn()) return;

    const result = await gameService.roll(state.game.id);
    if (isOk(result)) {
      const diceArray = result.value.dice.split(",");
      dispatch({
        type: "submitRoll",
        game: { ...state.game, dice: diceArray, isFirstRoll: false },
        currentUser: state.currentUser,
        currentPlayer: state.currentPlayer,
      });
    } else {
      dispatch({ type: "setError", error: "Erro ao rolar os dados." });
    }
  }

  if (state.loading) return <p>Loading game...</p>;
  if (state.error) return <p style={{ color: "red" }}>{state.error}</p>;

  return (
    <div style={{ padding: 24 }}>
      <h1>Game #{state.game.id}</h1>
      <p>Jogador atual: {state.game.currentPlayerUsername}</p>
      {state.info && <p style={{ color: "blue" }}>{state.info}</p>}
      <div>
        <h3>Dados</h3>
        {state.game.dice.length === 0 ? (
          <p>Ainda não há dados rolados.</p>
        ) : (
          <p>{state.game.dice.join(" - ")}</p>
        )}
      </div>

      <div>
        <button onClick={handleRoll} disabled={state.loading}>
          Roll
        </button>
      </div>
    </div>
  );
}
