type StorageKeys = 'messageCounters';
type MessageCounters = Record<number, number>;

let countersCache: MessageCounters | null = null;

export const storage = {
    getItem: <T>(key: StorageKeys): T | null => {
        try {
            const stored = localStorage.getItem(key);
            return stored ? JSON.parse(stored) : null;
        } catch (error) {
            console.error(`Error reading from localStorage: ${error}`);
            return null;
        }
    },

    setItem: <T>(key: StorageKeys, value: T): void => {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch (error) {
            console.error(`Error writing to localStorage: ${error}`);
        }
    }
};

export const messageCounters = {
    get: (): MessageCounters => {
        if (countersCache === null) {
            countersCache = storage.getItem<MessageCounters>('messageCounters') || {};
        }
        return {...countersCache};
    },

    set: (counters: MessageCounters): void => {
        countersCache = {...counters};
        storage.setItem('messageCounters', counters);
    },

    reset: (channelId: number): void => {
        const counters = messageCounters.get();
        counters[channelId] = 0;
        messageCounters.set(counters);
    },

    increment: (channelId: number): void => {
        const counters = messageCounters.get();
        counters[channelId] = (counters[channelId] || 0) + 1;
        messageCounters.set(counters);
    },

    delete: (): void => {
        localStorage.removeItem('messageCounters');
        countersCache = null;
    }
};
