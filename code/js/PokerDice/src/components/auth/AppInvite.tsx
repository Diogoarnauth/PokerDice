import React, { useState } from "react";
import { appInviteService } from "../../services/api/AppInvite";
import { isOk } from "../../services/api/utils";
import { AppInviteCode } from "../models/AppInviteCode";

export default function AppInvite() {
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [success, setSuccess] = useState<string | null>(null);
    const [inviteCode, setInviteCode] = useState<string | null>(null);

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(null);
        setInviteCode(null);

        try {
            const result = await appInviteService.invite({});
            if (isOk(result)) {
                const payload = new AppInviteCode(result.value);
                setInviteCode(payload.inviteCode ?? null);
                setSuccess("Convite enviado com sucesso!");
            } else {
                setError(result.error ?? "Erro ao enviar convite.");
            }
        } catch (err) {
            setError("Erro inesperado ao enviar convite.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <h1>App Invite</h1>
            <form onSubmit={handleSubmit} style={{ maxWidth: 400 }}>
                <button type="submit" disabled={loading}>
                    {loading ? "A enviar..." : "Convidar"}
                </button>
            </form>
            {error && <p style={{ color: "red" }}>{error}</p>}
            {success && (
                <p style={{ color: "green" }}>{success}</p>
            )}
            {inviteCode && (
                <p style={{ color: "blue" }}>
                    {"Invite Code: " + inviteCode}
                </p>
            )}
        </div>
    );
}
