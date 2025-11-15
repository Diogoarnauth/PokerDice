import * as React from "react"
import { Outlet } from "react-router-dom"
import '../../styles/index.css'

export function Home() {
    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-600 via-purple-600 to-purple-700">
            <div className="container mx-auto px-4 py-8 flex items-center justify-center min-h-screen">
                <div className="bg-white rounded-2xl shadow-2xl flex flex-col md:flex-row w-full max-w-6xl">
                    <div className="md:w-1/2 p-16 text-center">
                        <div className="flex flex-col h-full justify-between">
                            <div>
                                <img
                                    src="/images/logo.jpg"
                                    alt="App Logo"
                                    className="w-40 h-40 mx-auto rounded-2xl shadow-lg"
                                />
                                <h1 className="text-4xl font-bold text-gray-800 mt-8">
                                    Welcome to IM System
                                </h1>
                            </div>
                            <Footer />
                        </div>
                    </div>
                    <div className="md:w-1/2 bg-gray-50 rounded-r-2xl p-16">
                        <Outlet />
                    </div>
                </div>
            </div>
        </div>
    );
}

function Footer() {
    return (
        <footer className="mt-auto">
            <p className="text-gray-600 text-sm">Created by:</p>
            <Contributors names={["Diogo Leitão", "Renata Catanheira", "Humberto Carvalho"]} />
        </footer>
    );
}

function Contributors({ names }: { names: string[] }) {
    return (
        <span className="block text-gray-800 text-sm mt-2">
            {names.join(' • ')}
        </span>
    );
}