import React, { createContext, useContext, useState } from 'react';

const ChannelContext = createContext({
    channelName: undefined,
    setChannelName: (_: string) => { },
    adminName: undefined,
    setAdminName: (_: string) => { },
    isPublic: undefined,
    setIsPublic: (_: boolean) => { },
});

export function ChannelProvider({ children }) {
    const [channelName, setChannelName] = useState<string|undefined>(undefined);
    const [adminName, setAdminName] = useState<string|undefined>(undefined);
    const [isPublic, setIsPublic] = useState<boolean|undefined>(undefined);

    const value = {
        channelName,
        setChannelName,
        adminName,
        setAdminName,
        isPublic,
        setIsPublic,
    };
    return (
        <ChannelContext.Provider value={value}>
            {children}
        </ChannelContext.Provider>
    );
}

export function channelContext() {
    const state = useContext(ChannelContext);
    return [
        state.channelName,
        (channelName: string) => {
            state.setChannelName(channelName);
        },
        state.adminName,
        (adminName: string) => {
            state.setAdminName(adminName);
        },
        state.isPublic,
        (isPublic: boolean) => {
            state.setIsPublic(isPublic);
        },
    ];
}