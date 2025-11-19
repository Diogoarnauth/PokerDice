import React, { useEffect, useState } from "react";
import { playerProfileService } from "../../services/api/PlayerProfile";
import {PlayerProfilePayload, PlayerProfile} from "../models/PlayerProfile";

export default function PlayerProfileComponent() {
    const [profile, setProfile] = useState<PlayerProfile | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [depositAmount, setDepositAmount] = useState<number>(0);
    const [depositLoading, setDepositLoading] = useState<boolean>(false);
    const [depositSuccess, setDepositSuccess] = useState<string | null>(null);

    // Fetch profile
    useEffect(() => {
        async function fetchProfile() {
            setLoading(true);
            setError(null);

            const result = await playerProfileService.getProfile();
            if (result.success) {
                const payload = new PlayerProfilePayload(result.value);
                setProfile(payload.profile);
            } else {
                //setError(result.error ?? "Erro ao obter perfil");
            }
            setLoading(false);
        }

        fetchProfile();
    }, []);

    // Handler do formulário de depósito
    async function handleDeposit(e: React.FormEvent) {
        e.preventDefault();
        setDepositLoading(true);
        setDepositSuccess(null);

        if (depositAmount <= 0) {
            setError("O valor do depósito deve ser maior que zero.");
            setDepositLoading(false);
            return;
        }

        const result = await playerProfileService.deposit(depositAmount);
        if (result.success) {
            setDepositSuccess(`Depósito de ${depositAmount} realizado com sucesso!`);
            // Atualiza o perfil para ver o novo saldo!
            const updatedProfile = new PlayerProfilePayload(result.value).profile;
            setProfile(updatedProfile);
        } else {
            //setError(result.error ?? "Erro ao realizar depósito");
        }
        setDepositLoading(false);
    }

    if (loading) return <p>Loading profile...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <div style={{ maxWidth: 400, margin: "auto", padding: 24 }}>
            <h1>Player Profile</h1>
            {profile && (
                <div>
                    <p><strong>Username:</strong> {profile.username}</p>
                    <p><strong>Name:</strong> {profile.name}</p>
                    <p><strong>Age:</strong> {profile.age}</p>
                    <p><strong>Lobby:</strong> {profile.lobbyId}</p>
                    <p><strong>Credit:</strong> {profile.credit}</p>
                    <p><strong>Win count:</strong> {profile.winCounter}</p>
                </div>
            )}

            <hr style={{ margin: "20px 0" }} />

            <h2>Depositar dinheiro</h2>
            <form onSubmit={handleDeposit}>
                <label>
                    Valor:
                    <input
                        type="number"
                        value={depositAmount}
                        min={1}
                        onChange={e => setDepositAmount(Number(e.target.value))}
                        style={{ marginLeft: 8 }}
                        required
                    />
                </label>
                <button type="submit" disabled={depositLoading} style={{ marginLeft: 8 }}>
                    {depositLoading ? "Depositando..." : "Depositar"}
                </button>
            </form>
            {depositSuccess && <p style={{ color: "green" }}>{depositSuccess}</p>}
        </div>
    );
}
