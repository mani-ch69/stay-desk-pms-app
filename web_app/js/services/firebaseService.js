import { FIREBASE_CONFIG, HOTEL_ID } from '../utils/config.js';

// Initialize Firebase
try {
    if (!firebase.apps.length) {
        firebase.initializeApp(FIREBASE_CONFIG);
    }
} catch (e) {
    console.error("Firebase Init Error:", e);
}

export const db = firebase.database(FIREBASE_CONFIG.databaseURL);
export const auth = firebase.auth();
export const getHotelId = () => HOTEL_ID;

export const signInAnonymously = () => {
    return new Promise((resolve, reject) => {
        auth.onAuthStateChanged(user => {
            if (user) {
                resolve(user);
            } else {
                auth.signInAnonymously()
                    .then(({ user }) => resolve(user))
                    .catch(reject);
            }
        });
    });
};
