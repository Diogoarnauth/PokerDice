import React, {useEffect, useState} from "react";
import {playerProfileService} from "../../services/api/PlayerProfile";
import {PlayerProfilePayload, PlayerProfile} from "../models/PlayerProfile";
import {useAuthentication} from "../../providers/Authentication";

export default function PlayerProfileComponent() {
    const {setUsername} = useAuthentication();

    const [profile, setProfile] = useState<PlayerProfile | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const [depositAmount, setDepositAmount] = useState<number>(0);
    const [depositLoading, setDepositLoading] = useState<boolean>(false);
    const [depositSuccess, setDepositSuccess] = useState<string | null>(null);

    useEffect(() => {
        let isMounted = true;

        async function fetchProfile() {
            setLoading(true);
            setError(null);

            try {
                const result = await playerProfileService.getProfile();

                if (!isMounted) return;

                if (result.success) {
                    const payload = new PlayerProfilePayload(result.value);
                    setProfile(payload.profile);
                } else {
                    setError(result.error?.title || "Erro ao obter dados do perfil.");
                }
            } catch (err) {
                if (isMounted) setError("Erro de comunicação com o servidor.");
            } finally {
                if (isMounted) setLoading(false);
            }
        }

        fetchProfile();

        return () => {
            isMounted = false;
        };
    }, []);

    async function handleLogout() {
        try {
            await playerProfileService.logout();
        } catch (err) {
            console.error("Erro no logout backend", err);
        } finally {
            setUsername(null);
        }
    }

    async function handleDeposit(e: React.FormEvent) {
        e.preventDefault();
        setDepositLoading(true);
        setDepositSuccess(null);
        setError(null);

        if (depositAmount <= 0) {
            setError("O valor do depósito deve ser maior que zero.");
            setDepositLoading(false);
            return;
        }

        try {
            const result = await playerProfileService.deposit({ value: depositAmount });

            if (result.success) {
                setDepositSuccess(`Depósito de ${depositAmount} realizado com sucesso!`);

                setProfile(prev =>
                    prev
                        ? {
                            ...prev,
                            credit: result.value.newBalance
                        }
                        : prev
                );
            } else {
                setError(result?.error?.title || "Erro ao realizar depósito");
            }
        } catch (err) {
            setError("Erro inesperado ao processar o depósito.");
        } finally {
            setDepositLoading(false);
        }
    }

    if (loading) return <p>Loading profile...</p>;

    if (error && !profile) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <div style={{ maxWidth: 400, margin: "auto", padding: 24 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <h1>Player Profile</h1>
                {/* Botão de Logout sempre visível aqui, pois estamos autenticados */}
                <button onClick={handleLogout}>
                    Logout
                </button>
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

            {/* Adicionei a visualização de erros aqui, para não partir o layout se der erro no depósito */}
            {error && <p style={{ color: "red", marginBottom: "10px" }}>{error}</p>}

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
