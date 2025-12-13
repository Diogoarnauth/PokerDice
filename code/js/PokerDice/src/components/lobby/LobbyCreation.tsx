import React, { useState } from "react";
import { lobbyCreationService } from "../../services/api/LobbyCreate";
import { isOk } from "../../services/api/utils";
import { useNavigate } from "react-router-dom";
import "../../styles/LobbyCreation.css";

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
            const payload = { ...form };
            const result = await lobbyCreationService.createLobby(form);
            console.log("Payload sent:", payload);
            console.log("Lobby creation result:", result);
            if (isOk(result)) {
                setSuccess("Lobby created successfully!");
                setForm(defaultState);
                navigate(`/lobbies/`); // Redireciona para a página do jogo

            } else {
                const problem = result.error;
                setError(problem.detail || problem.title || "Unknown error creating lobby.");
            }
        } catch (err) {
            setError("Unexpected error occurred.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="lobby-page">
            <div className="lobby-card">
                <h1 className="lobby-title">Create New Lobby</h1>

                <form onSubmit={handleSubmit} className="lobby-form">
                    <div className="lobby-field">
                        <label className="lobby-label">Lobby Name</label>
                        <input
                            type="text"
                            name="name"
                            value={form.name}
                            onChange={handleChange}
                            required
                            className="lobby-input"
                        />
                    </div>

                    <div className="lobby-field">
                        <label className="lobby-label">Description</label>
                        <textarea
                            name="description"
                            value={form.description}
                            onChange={handleChange}
                            required
                            className="lobby-textarea"
                        />
                    </div>

                    <div className="lobby-grid">
                        <div className="lobby-field">
                            <label className="lobby-label">Min Users</label>
                            <input
                                type="number"
                                name="minUsers"
                                min={2}
                                value={form.minUsers}
                                onChange={handleChange}
                                required
                                className="lobby-input"
                            />
                        </div>
                        <div className="lobby-field">
                            <label className="lobby-label">Max Users</label>
                            <input
                                type="number"
                                name="maxUsers"
                                min={form.minUsers}
                                value={form.maxUsers}
                                onChange={handleChange}
                                required
                                className="lobby-input"
                            />
                        </div>
                    </div>

                    <div className="lobby-grid">
                        <div className="lobby-field">
                            <label className="lobby-label">Rounds</label>
                            <input
                                type="number"
                                name="rounds"
                                min={1}
                                value={form.rounds}
                                onChange={handleChange}
                                required
                                className="lobby-input"
                            />
                        </div>
                        <div className="lobby-field">
                            <label className="lobby-label">Min Credit To Participate</label>
                            <input
                                type="number"
                                name="minCreditToParticipate"
                                min={0}
                                value={form.minCreditToParticipate}
                                onChange={handleChange}
                                required
                                className="lobby-input"
                            />
                        </div>
                    </div>

                    <div className="lobby-field">
                        <label className="lobby-label">Turn Time (minutes)</label>
                        <input
                            type="number"
                            name="turnTime"
                            min={1}
                            value={form.turnTime}
                            onChange={handleChange}
                            required
                            className="lobby-input"
                        />
                    </div>

                    {error && <p className="lobby-message error">{error}</p>}
                    {success && <p className="lobby-message success">{success}</p>}

                    <button type="submit" disabled={loading} className="lobby-button">
                        {loading ? "Creating..." : "Create Lobby"}
                    </button>
                </form>
            </div>
        </div>
    );
}
