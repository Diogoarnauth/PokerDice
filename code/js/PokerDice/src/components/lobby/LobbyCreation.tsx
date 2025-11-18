import React, { useState } from "react";
import { lobbyCreationService } from "../../services/api/LobbyCreate";
import { isOk } from "../../services/api/utils";
import { useNavigate } from "react-router-dom";

type FormState = {
    name: string;
    description: string;
    minUsers: number;
    maxUsers: number;
    rounds: number;
    minCreditToParticipate: number;
    turnTime: number;
};

const defaultState: FormState = {
    name: "",
    description: "",
    minUsers: 2,
    maxUsers: 10,
    rounds: 1,
    minCreditToParticipate: 10,
    turnTime: 2,
};

export default function LobbyCreation() {
    const [form, setForm] = useState<FormState>(defaultState);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [success, setSuccess] = useState<string | null>(null);
    const navigate = useNavigate();

    function handleChange(
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    ) {
        const { name, value } = e.target;
        setForm((prev) => ({
            ...prev,
            [name]:
                name === "minUsers" ||
                name === "maxUsers" ||
                name === "rounds" ||
                name === "minCreditToParticipate" ||
                name === "turnTime"
                    ? Number(value)
                    : value
        }));
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(null);

        if (!form.name || !form.description) {
            setError("Lobby name and description are required.");
            setLoading(false);
            return;
        }
        if (form.maxUsers < form.minUsers) {
            setError("Max users must be greater or equal to min users.");
            setLoading(false);
            return;
        }

        try {
            const payload = { ...form};
            const result = await lobbyCreationService.createLobby(form);
            console.log("Payload enviado:", payload);
            console.log("Lobby creation result:", result);
            if (isOk(result)) {
                setSuccess("Lobby created successfully!");
                setForm(defaultState);
            } else {
                setError(result.error ?? "Error creating lobby.");
            }
        } catch (err) {
            setError("Unexpected error occurred.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <h1>Create New Lobby</h1>
            <form onSubmit={handleSubmit} style={{ maxWidth: 400 }}>
                <div>
                    <label>Lobby Name</label><br />
                    <input
                        type="text"
                        name="name"
                        value={form.name}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>Description</label><br />
                    <textarea
                        name="description"
                        value={form.description}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>Min Users</label><br />
                    <input
                        type="number"
                        name="minUsers"
                        min={2}
                        value={form.minUsers}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>Max Users</label><br />
                    <input
                        type="number"
                        name="maxUsers"
                        min={form.minUsers}
                        value={form.maxUsers}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>Rounds</label><br />
                    <input
                        type="number"
                        name="rounds"
                        min={1}
                        value={form.rounds}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>Minimum Credit To Participate</label><br />
                    <input
                        type="number"
                        name="minCreditToParticipate"
                        min={0}
                        value={form.minCreditToParticipate}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label>Turn Time (minutes)</label><br />
                    <input
                        type="number"
                        name="turnTime"
                        min={1}
                        value={form.turnTime}
                        onChange={handleChange}
                        required
                    />
                </div>
                <button type="submit" disabled={loading}>
                    {loading ? "Creating..." : "Create Lobby"}
                </button>
                {error && <p style={{ color: "red" }}>{error}</p>}
                {success && <p style={{ color: "green" }}>{success}</p>}
            </form>
        </div>
    );
}