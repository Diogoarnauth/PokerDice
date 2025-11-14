import React from 'react'
import {Link} from "react-router-dom"
import '../../styles/index.css'


interface SplitColumnLayoutProps {
    left: React.ReactNode
    right: React.ReactNode
    details?: React.ReactNode
    equalSplit?: boolean
}

export function SplitColumnLayout({ left, right, details, equalSplit }: SplitColumnLayoutProps) {
    return (
        <div className="flex w-full h-screen overflow-hidden bg-gray-50 fixed top-0 left-0">
            <div className="w-[350px] flex-shrink-0 flex flex-col bg-white shadow-md">
                {left}
            </div>
            <div className="absolute bottom-0 left-0 w-[350px] h-16 bg-gray-100 border-t border-gray-300 flex justify-around items-center">
                <Link to="/channels" className="text-xl text-gray-700 hover:text-purple-500 transition">📡</Link>
                <Link to="/community" className="text-xl text-gray-700 hover:text-purple-500 transition">🏘</Link>
                <Link to="/notifications" className="text-xl text-gray-700 hover:text-purple-500 transition">🔔</Link>
                <Link to="/settings" className="text-xl text-gray-700 hover:text-purple-500 transition">⚙</Link>
            </div>
            <div className={equalSplit ? "flex-1 min-w-0" : "flex-1 p-4 min-w-0 bg-white shadow-inner"}>
                {right}
            </div>
            {equalSplit && (
                <div className="flex-1 p-4 min-w-0 bg-gray-50">
                    {details}
                </div>
            )}
        </div>
    );
}
