import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

// Funkcja wysyłania powiadomień
export const sendNotification = functions.https.onRequest(async (req, res) => {
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
    // Rzutowanie error na typ Error
    const errorMessage = error instanceof Error ? error.message : "Unknown error";
    console.error("Error sending notification:", errorMessage);
    res.status(500).send(`Error sending notification: ${errorMessage}`);
  }
});
