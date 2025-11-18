import React, { useEffect, useState } from "react";
import { playerProfileService } from "../../services/api/PlayerProfile";
import {PlayerProfilePayload, PlayerProfile} from "../models/PlayerProfile";

export default function PlayerProfileComponent() {
    const [profile, setProfile] = useState<PlayerProfile | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function fetchProfile() {
            setLoading(true);
            setError(null);

            const result = await playerProfileService.getProfile();
            if (result.success) {
                const payload = new PlayerProfilePayload(result.value);
                setProfile(payload.profile);
            } else {
                //setError(result.error);
            }
            setLoading(false);
        }

        fetchProfile();
    }, []);

    if (loading) return <p>Loading profile...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <div style={{ maxWidth: 400, margin: "auto", padding: 24 }}>
            <h1>Player Profile</h1>
            {profile && (
                <div>
                    <p><strong>Username:</strong> {profile.username}</p>
                    <p><strong>Name:</strong> {profile.name}</p>
                </div>
            )}
        </div>
    );
}
