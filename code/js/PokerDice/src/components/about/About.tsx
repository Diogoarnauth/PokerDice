// src/components/About.tsx
import React from "react";
import "./About.css";

export default function About() {
    return (
        <div className="about-page">
            <div className="about-card">
                <h1 className="about-title">About PokerDice</h1>

                <p className="about-text">
                    <strong>PokerDice</strong> was developed by students from ISEL.
                    The platform aims to bring fun and strategy to online dice games,
                    with a modern and intuitive interface.
                </p>

                <h2 className="about-section-title">Creators</h2>
                <ul className="about-list">
                    <li>Renata Castanheira — Developer</li>
                    <li>Diogo Leitão — Developer</li>
                    <li>Humberto Carvalho — Developer</li>
                </ul>

                <h2 className="about-section-title">Contact</h2>
                <p className="about-text">
                    For questions, suggestions, or feedback, contact us at:<br />
                    <strong>
                        <a href="mailto:pokerdice.team@gmail.com" className="about-link">
                            pokerdice.team@gmail.com
                        </a>
                    </strong>
                </p>
            </div>
        </div>
    );
}
