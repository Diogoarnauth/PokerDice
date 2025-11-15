import { fetchWrapper } from "./utils"
import { RequestUri } from "./RequestUri"

export const settingsService = {

  // fazer logout
  logout() {
    return fetchWrapper(RequestUri.user.logout, { method: "POST" })
  },

  // criar invite (se o teu backend realmente o usar)
  createInvite() {
    return fetchWrapper(RequestUri.user.invite, { method: "POST" })
  },

  // depositar créditos
  deposit(amount: number) {
    return fetchWrapper(RequestUri.user.deposit, {
      method: "POST",
      body: JSON.stringify({ amount })
    })
  },

  // obter bootstrap (info geral do sistema)
  bootstrap() {
    return fetchWrapper(RequestUri.user.bootstrap, {
      method: "GET"
    })
  }
}
