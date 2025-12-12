import React, {useState} from "react";
import {appInviteService} from "../../services/api/AppInvite";
import {isOk} from "../../services/api/utils";
import {AppInviteCode} from "../models/AppInviteCode";

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
        <div>
            <h1>App Invite</h1>
            <form onSubmit={handleSubmit} style={{maxWidth: 400}}>
                <button type="submit" disabled={loading}>
                    {loading ? "Sending..." : "Invite"}
                </button>
            </form>
            {error && <p style={{color: "red"}}>{error}</p>}
            {success && (
                <p style={{color: "green"}}>{success}</p>
            )}
            {inviteCode && (
                <div style={{
                    marginTop: "15px",
                    padding: "10px",
                    border: "1px solid #ccc",
                    display: "inline-flex",
                    alignItems: "center",
                    gap: "10px",
                    borderRadius: "5px"
                }}>
                    <span style={{fontFamily: "monospace", fontSize: "1.2em", fontWeight: "bold"}}>
                        {inviteCode}
                    </span>

                    <button
                        type="button"
                        onClick={handleCopy}
                        style={{cursor: "pointer"}}
                    >
                        {copyFeedback ? "Copied!" : "Copy"}
                    </button>
                </div>
            )}
        </div>
    );
}
