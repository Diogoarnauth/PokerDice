import { AppInviteCode } from "../../components/models/AppInviteCode";
import { RequestUri } from "./RequestUri";
import { fetchWrapper } from "./utils";

export const settingsService = {
    createAppInvite() {
        return fetchWrapper<AppInviteCode>(RequestUri.user.createAppInvite, { method: 'POST' })
    },

    logout() {
        return fetchWrapper(RequestUri.user.logout, { method: 'POST' })
    }
}