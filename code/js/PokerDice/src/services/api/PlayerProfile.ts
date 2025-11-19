import { fetchWrapper, Result } from "./utils";
import { RequestUri } from "./RequestUri";

export const playerProfileService = {
    // GET /me → obter detalhes do player autenticado
    getProfile(): Promise<Result<any>> {
        return fetchWrapper(
            RequestUri.user.getMe,
            { method: "GET" }//
        );
    }
};

