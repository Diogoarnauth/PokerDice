const BASE_API_URL = 'http://localhost:8080/api'

export const RequestUri ={
    user: {
        login: `${BASE_API_URL}/login`,
        signup: `${BASE_API_URL}/register`,
        logout: `${BASE_API_URL}/logout`,
        createAppInvite: `${BASE_API_URL}/invite`,
        searchSuggestions: `${BASE_API_URL}/users/search?query=`,
        listen: `${BASE_API_URL}/users/listen`,
    },
    channels:{
        decline: (channelId: number) => `${BASE_API_URL}/channels/${channelId}/decline`,
        searchPublic: `${BASE_API_URL}/channels/search/public?name=`,
        channel: (channelId: number) => `${BASE_API_URL}/channels/${channelId}`,
        getMessages: (channelId: number,offset:number) => `${BASE_API_URL}/channels/${channelId}/messages?offset=${offset}`,
        sendMessages: (channelId: number) => `${BASE_API_URL}/channels/${channelId}/messages`,
        join: (channelId: number) => `${BASE_API_URL}/channels/${channelId}/join`,
        search: `${BASE_API_URL}/channels/search?name=`,
        update: (channelId: number) => `${BASE_API_URL}/channels/${channelId}/update`,
        leave: (channelId: number) => `${BASE_API_URL}/channels/${channelId}/leave`,
        invite: (channelId: number) => `${BASE_API_URL}/channels/${channelId}/invite`,
        channnelInfo: (channelId: number) => `${BASE_API_URL}/channels/${channelId}/info`,
        members: (channelId: number) => `${BASE_API_URL}/channels/${channelId}/users`,
        channels: `${BASE_API_URL}/channels`,
        create: `${BASE_API_URL}/channels/create`,
        getUserChannels: `${BASE_API_URL}/channels`,
    },
    notifications: {
        getIvites: `${BASE_API_URL}/users/invited`,
    }
}