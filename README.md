# Smart Pantry Manager

Smart Pantry Manager is a Java Android app that helps reduce food waste. The user adds the ingredients they already have at home, and the app suggests only the recipes they can cook right now with those ingredients, so no shopping trip is needed.

## Features
- Add, edit and delete pantry items (name, quantity, unit and an optional expiry date)
- 20 pre-loaded recipes stored in the database
- Strict matching: a recipe is only suggested if every ingredient is in the pantry in at least the required quantity
- Matching handles plural names (tomato and tomatoes) and unit differences (for example kg and g, or tbsp and ml)
- Recipe detail screen with the ingredients and method
- Settings screen with an expiring-soon alert toggle
- A message is shown when no recipes match

## Database
I chose SQLite (SQLiteOpenHelper). It stores the data on the phone, so the app works without internet, the pantry is still there after the app is closed, and it fits the persistent data chapter of the module. Firebase and PostgreSQL would need an account or a backend server, which this small personal app does not need.

## How to run
1. Open Android Studio and choose File > Open, then select this project folder.
2. Wait for the Gradle sync to finish.
3. Connect an Android phone with USB debugging on, or start an emulator.
4. Press the green Run button.

The minimum SDK is API 24. The app is written in Java and does not use maps or location.
