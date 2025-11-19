import { fetchWrapper, Result } from "./utils";
import { RequestUri } from "./RequestUri";

export const playerProfileService = {
    // GET /me → obter detalhes do player autenticado
    getProfile(): Promise<Result<any>> {
        return fetchWrapper(
            RequestUri.user.getMe,
            { method: "GET" }
        );
    },

    // POST /me/deposit → endpoint para depositar dinheiro
    deposit(amount: number): Promise<Result<any>> {
        const token = localStorage.getItem("token"); // adapta se diferente!
        return fetchWrapper(
            RequestUri.user.deposit,
            {
                method: "POST",
                body: JSON.stringify({ amount }),
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `bearer ${token}`,
                }
            }
        );
    }

};
