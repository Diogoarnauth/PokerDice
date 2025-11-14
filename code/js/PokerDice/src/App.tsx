import React from 'react'
import {createBrowserRouter, RouterProvider} from 'react-router-dom'
import {Home} from './components/layout/Home'
import {Login} from './components/auth/Login'
import {Signup} from './components/auth/Signup'
import {ChannelsMenu} from './components/channels/ChannelsMenu'
import {Channel} from './components/channels/Channel'
import {Notifications} from './components/notifications/Notifications'
import {Settings} from './components/settings/Settings'
import {AuthenticationProvider} from './providers/authentication'
import {CommunityMenu} from "./components/channels/Community"
import {CommunityChannel} from "./components/channels/CommunityChannel"
import {ChannelDetails} from "./components/channels/ChannelDetails";
import {CreateChannel} from "./components/channels/CreateChannel";
import {ChannelProvider} from "./providers/ChannelContext";
import {RequireAuthentication} from './components/auth/RequireAuthentication'
import {CommunityChannelDetails} from "./components/channels/CommunityChannelDetails";
import ErrorPage from './components/error/ErrorPage'


const router = createBrowserRouter([
    {
        path: '/',
        element: <Home/>,
        children: [
            {
                index: true,
                element: <Login/>
            },
            {
                path: 'signup',
                element: <Signup/>,
            },
        ]
    },
    {
        path: 'channels',
        element:
            <ChannelProvider>
                <RequireAuthentication>
                    <ChannelsMenu/>
                </RequireAuthentication>
            </ChannelProvider>,
        children: [
            {
                path: ':channelId',
                element: <Channel/>,
                children: [
                    {
                        path: 'details',
                        element: <ChannelDetails/>
                    }
                ]
            },
            {
                path: 'create',
                element: <CreateChannel/>
            }
        ]
    },
    {
        path: 'community',
        element:
            <ChannelProvider>
                <RequireAuthentication>
                    <CommunityMenu/>
                </RequireAuthentication>,
            </ChannelProvider>,
        children: [
            {
                path: ':channelId',
                element: <CommunityChannel/>,
                children: [
                    {
                        path: 'details',
                        element: <CommunityChannelDetails/>
                    }
                ]
            }
        ]
    },
    {
        path: 'notifications',
        element:
            <RequireAuthentication>
                <Notifications/>
            </RequireAuthentication>
    },
    {
        path: 'settings',
        element:
            <RequireAuthentication>
                <Settings/>
            </RequireAuthentication>
    },
    {
        path: '*',
        element: <ErrorPage/>
    }
])


export function App() {
    return (
        <AuthenticationProvider>
            <RouterProvider router={router}/>
        </AuthenticationProvider>
    )
}