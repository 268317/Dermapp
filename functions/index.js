const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Inicjalizacja Firebase Admin SDK
admin.initializeApp();

// Funkcja wysyłania powiadomień push
exports.sendNotification = functions.https.onRequest(async (req, res) => {
    const { token, title, body } = req.body;

    if (!token || !title || !body) {
        res.status(400).send("Missing parameters");
        return;
    }

    const message = {
        token: token,
        notification: {
            title: title,
            body: body,
        },
    };

    try {
        const response = await admin.messaging().send(message);
        res.status(200).send(`Notification sent successfully: ${response}`);
    } catch (error) {
        console.error("Error sending notification:", error);
        res.status(500).send(`Error sending notification: ${error.message}`);
    }
});
