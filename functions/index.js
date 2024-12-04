const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Inicjalizacja Firebase Admin SDK
admin.initializeApp();

// Funkcja wysyłania powiadomień push
exports.sendNotification = functions.https.onRequest(async (req, res) => {
    console.log("Żądanie odebrane z danymi:", req.body); // Logowanie danych wejściowych

    const { token, title, body } = req.body;

    if (!token || !title || !body) {
        console.error("Brakujące parametry:", req.body); // Logowanie błędnych danych
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
        console.log("Powiadomienie wysłane:", response); // Logowanie sukcesu
        res.status(200).send(`Notification sent successfully: ${response}`);
    } catch (error) {
        console.error("Błąd wysyłania powiadomienia:", error); // Logowanie błędów
        res.status(500).send(`Error sending notification: ${error.message}`);
    }
});


