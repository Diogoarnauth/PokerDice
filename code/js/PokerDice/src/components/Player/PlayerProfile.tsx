import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { playerProfileService } from "../../services/api/PlayerProfile";
import { PlayerProfilePayload, PlayerProfile } from "../models/PlayerProfile";
import { useAuthentication } from "../../providers/Authentication";

import "../../styles/PlayerProfile.css";

export default function PlayerProfileComponent() {
    const [profile, setProfile] = useState<PlayerProfile | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const { username, isLoading: authLoading } = useAuthentication();
    const navigate = useNavigate();

    // Proteção de rota: se não estiver autenticado, vai para /login
    useEffect(() => {
        if (!authLoading && !username) {
            navigate("/login");
        }
    }, [authLoading, username, navigate]);

    useEffect(() => {
        // só carrega o profile se estiver autenticado
        if (authLoading || !username) return;

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
                    setError(result.error?.title || "Error fetching profile data.");
                }
            } catch (err) {
                if (isMounted) setError("Communication error with the server.");
            } finally {
                if (isMounted) setLoading(false);
            }
        }

        fetchProfile();
        return () => {
            isMounted = false;
        };
    }, [authLoading, username]);

    if (authLoading) return <p className="profile-loading">Loading...</p>;
    if (loading) return <p className="profile-loading">Loading profile...</p>;
    if (error && !profile) return <p className="profile-error">{error}</p>;

    return (
        <div className="playerProfile-page">
            <div className="playerProfile-card">
                <h1>Player Profile</h1>

                {profile && (
                    <div className="playerProfile-fields">
                        <p>
                            <strong>Username:</strong> {profile.username}
                        </p>
                        <p>
                            <strong>Name:</strong> {profile.name}
                        </p>
                        <p>
                            <strong>Age:</strong> {profile.age}
                        </p>
                        <p>
                            <strong>Lobby:</strong> {profile.lobbyId ? profile.lobbyId : "Nenhum"}
                        </p>
                        <p className="playerProfile-credit">
                            <strong>Credit:</strong> {profile.credit}
                        </p>
                        <p className="playerProfile-wins">
                            <strong>Win count:</strong> {profile.winCounter}
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
}
