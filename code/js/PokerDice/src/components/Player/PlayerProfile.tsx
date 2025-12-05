import React, { useEffect, useState } from "react";
import { playerProfileService } from "../../services/api/PlayerProfile";
import { PlayerProfilePayload, PlayerProfile } from "../models/PlayerProfile";

const TOKEN_KEY = "token"; // O nome do cookie

export default function PlayerProfileComponent() {
    const [profile, setProfile] = useState<PlayerProfile | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [depositAmount, setDepositAmount] = useState<number>(0);
    const [depositLoading, setDepositLoading] = useState<boolean>(false);
    const [depositSuccess, setDepositSuccess] = useState<string | null>(null);
    const [hasToken, setHasToken] = useState<boolean>(false);

    // Função para verificar o cookie
    const getCookie = (name: string): string | null => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
        return null;
    };

    // Verifica se o token está presente no cookie
    const token = getCookie(TOKEN_KEY);
    if (!token) {
        return <div className="already-not-logged">Para aceder ao seu perfil tem de efetuar o login</div>;
    }

    // Check token on mount
    useEffect(() => {
        const token = getCookie(TOKEN_KEY);
        setHasToken(!!token);
    }, []);

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
                // setError(result.error ?? "Erro ao obter perfil");
            }
            setLoading(false);
        }

        fetchProfile();
    }, []);

    // Logout handler
    function handleLogout() {
        // Remove token from cookie and update state
        document.cookie = `${TOKEN_KEY}=; Max-Age=0; path=/`; // Remover cookie
        setHasToken(false);
        setProfile(null);
    }

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

        const result = await playerProfileService.deposit({ value: depositAmount });
        if (result.success) {
            setDepositSuccess(`Depósito de ${depositAmount} realizado com sucesso!`);

            // 👉 Atualizar só o credit no profile existente
            setProfile(prev =>
                prev
                    ? {
                        ...prev,
                        credit: result.value.newBalance
                    }
                    : prev
            );
        } else {
            // setError(result?.error ?? "Erro ao realizar depósito");
        }

        setDepositLoading(false);
    }

    if (loading) return <p>Loading profile...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <div style={{ maxWidth: 400, margin: "auto", padding: 24 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <h1>Player Profile</h1>
                {hasToken && (
                    <button onClick={handleLogout}>
                        Logout
                    </button>
                )}
            </div>

            {profile && (
                <div>
                    <p><strong>Username:</strong> {profile.username}</p>
                    <p><strong>Name:</strong> {profile.name}</p>
                    <p><strong>Age:</strong> {profile.age}</p>
                    <p><strong>Lobby:</strong> {profile.lobbyId ? profile.lobbyId : "Nenhum"}</p>
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
