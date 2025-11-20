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
    deposit(payload: { value: number }): Promise<Result<any>> {
        console.log("Deposit payload:", payload); // Log do corpo enviado
        return fetchWrapper(
            RequestUri.user.deposit,
            {
                method: "POST",
                body: JSON.stringify(payload), // Aqui você envia o valor dentro de um objeto 'value'
            }
        );
    }
};
