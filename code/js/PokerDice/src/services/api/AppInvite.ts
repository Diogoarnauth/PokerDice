import { fetchWrapper, Result } from "./utils";
import { RequestUri } from "./RequestUri";

// Se o endpoint nÃ£o precisa de email, usa um objeto vazio em data:
export const appInviteService = {
    invite(data: {} = {}): Promise<Result<any>> {
        return fetchWrapper(
            RequestUri.user.invite,
            {
                method: "POST",
                body: JSON.stringify(data),
            }
        );
    }
};