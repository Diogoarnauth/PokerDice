import React, { useState } from "react";
import { playerProfileService } from "../../services/api/PlayerProfile";
import "../../styles/Deposit.css";

export default function DepositPage() {
    const [depositAmount, setDepositAmount] = useState<number>(0);
    const [depositLoading, setDepositLoading] = useState<boolean>(false);
    const [depositSuccess, setDepositSuccess] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    async function handleDeposit(e: React.FormEvent) {
        e.preventDefault();
        setDepositLoading(true);
        setDepositSuccess(null);
        setError(null);

        if (depositAmount <= 0) {
            setError("The deposit amount must be greater than zero.");
            setDepositLoading(false);
            return;
        }

        try {
            const result = await playerProfileService.deposit({ value: depositAmount });

            if (result.success) {
                setDepositSuccess(`Deposit of ${depositAmount} completed successfully!`);
            } else {
                setError(result?.error?.title || "Error while making the deposit.");
            }
        } catch (err) {
            setError("Unexpected error while processing the deposit.");
        } finally {
            setDepositLoading(false);
        }
    }

    return (
        <div className="deposit-page">
            <div className="deposit-card">
                <h1 className="deposit-title">Deposit</h1>

                {error && <p className="deposit-message error">{error}</p>}
                {depositSuccess && <p className="deposit-message success">{depositSuccess}</p>}

                <form onSubmit={handleDeposit} className="deposit-form">
                    <label className="deposit-label">
                        Value
                        <input
                            type="number"
                            value={depositAmount}
                            min={1}
                            onChange={e => setDepositAmount(Number(e.target.value))}
                            required
                            className="deposit-input"
                        />
                    </label>
                    <button
                        type="submit"
                        disabled={depositLoading}
                        className="deposit-button"
                    >
                        {depositLoading ? "Depositing..." : "Deposit"}
                    </button>
                </form>

                <p className="deposit-hint">
                    Min bet ready. Add chips to your stack and return to the table.
                </p>
            </div>
        </div>
    );
}
