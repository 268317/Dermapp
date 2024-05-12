package com.example.dermapp

class StartDocActivity {

}



// to dodać do onCreate - do wyswietlania imienia użytkownika na headerze
//// Pobierz UID aktualnie zalogowanego użytkownika
//val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
//
//// Utwórz odwołanie do dokumentu użytkownika w Firestore
//val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserUid!!)
//
//// Pobierz dane użytkownika z Firestore
//userRef.get().addOnSuccessListener { documentSnapshot ->
//    if (documentSnapshot.exists()) {
//        // Konwertuj dane na obiekt użytkownika
//        val user = documentSnapshot.toObject(AppUser::class.java)
//
//        // Sprawdź, czy udało się pobrać dane użytkownika
//        user?.let {
//            // Ustaw imię użytkownika w nagłówku
//            val headerNameTextView: TextView = findViewById(R.id.firstNameTextView)
//            headerNameTextView.text = user.firstName
//        }
//    }
//}.addOnFailureListener { exception ->
//    // Obsłuż błędy pobierania danych z Firestore
//}