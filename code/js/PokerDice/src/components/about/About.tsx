// src/components/About.tsx

import React from "react";

export default function About() {
    return (
        <div style={{ maxWidth: 600, margin: "auto", padding: 32 }}>
            <h1>About PokerDice</h1>
            <p>
                <strong>PokerDice</strong> was developed by students from ISEL.
                The platform aims to bring fun and strategy to online dice games, with a modern and intuitive interface.
            </p>

            <h2>Creators</h2>
            <ul>
                <li>Renata Castanheira — Developer</li>
                <li>Diogo Leitão — Developer</li>
                <li>Humberto Carvalho — Developer</li>
            </ul>

            <h2>Contact</h2>
            <p>
                For questions, suggestions, or feedback, contact us at:<br />
                <strong>
                    <a href="mailto:pokerdice.team@gmail.com">pokerdice.team@gmail.com</a>
                </strong>
            </p>
        </div>
    );
}