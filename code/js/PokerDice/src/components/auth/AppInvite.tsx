import React, { useState } from "react";
import { appInviteService } from "../../services/api/AppInvite";
import { isOk } from "../../services/api/utils";
import { AppInviteCode } from "../models/AppInviteCode";
import "./AppInvite.css";

export default function AppInvite() {
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [success, setSuccess] = useState<string | null>(null);
    const [inviteCode, setInviteCode] = useState<string | null>(null);
    const [copyFeedback, setCopyFeedback] = useState<boolean>(false);

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(null);
        setInviteCode(null);
        setCopyFeedback(false);

        try {
            const result = await appInviteService.invite({});
            if (isOk(result)) {
                const payload = new AppInviteCode(result.value);
                setInviteCode(payload.inviteCode ?? null);
                setSuccess("Invite sent successfully!");
            } else {
                setError(result.error.title ?? "Error sending invite.");
            }
        } catch (err) {
            setError("Unexpected error while sending invite.");
        } finally {
            setLoading(false);
        }
    }

    const handleCopy = async () => {
        if (inviteCode) {
            try {
                await navigator.clipboard.writeText(inviteCode);
                setCopyFeedback(true);
                setTimeout(() => setCopyFeedback(false), 2000);
            } catch (err) {
                console.error("Failed to copy.", err);
            }
        }
    };

    return (
        <div className="invite-page">
            <div className="invite-card">
                <h1 className="invite-title">App Invite</h1>

                <form onSubmit={handleSubmit} className="invite-form">
                    <button
                        type="submit"
                        disabled={loading}
                        className="invite-button"
                    >
                        {loading ? "Sending..." : "Generate Invite"}
                    </button>
                </form>

                {error && <p className="invite-message error">{error}</p>}
                {success && <p className="invite-message success">{success}</p>}

                {inviteCode && (
                    <div className="invite-code-box">
                        <span className="invite-code">{inviteCode}</span>

                        <button
                            type="button"
                            onClick={handleCopy}
                            className="invite-copy-button"
                        >
                            {copyFeedback ? "Copied!" : "Copy"}
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}
