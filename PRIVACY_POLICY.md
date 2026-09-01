# Privacy Policy for ShowTime

**Effective Date:** September 1, 2026  
**Last Updated:** September 1, 2026  

ShowTime ("we", "our", or "the app") is an open-source movie and TV show exploration application. We are committed to transparency and protecting your privacy. This Privacy Policy outlines what information ShowTime collects, how it is used, and the choices you have concerning your data.

---

## 1. Information We Collect and How We Use It

### A. Local Device Data (Default Behavior)
By default, ShowTime functions completely locally on your device without requiring an account or login:
- Your **Watchlist**, **Favorites**, **Watch History**, and **Custom Lists** are stored securely in a local database (Room / SQLite) on your phone.
- This data remains strictly on your device unless you choose to use our optional Cloud Backup & Sync service.

### B. Cloud Backup & Sync (Optional Google Sign-In)
If you voluntarily choose to use **Cloud Backup & Sync**, you can sign in with your Google Account. When enabled:
- **Account Identity**: We collect and store your Google Account email address, display name, and profile picture URL.
- **Library Snapshot**: We store a compressed snapshot of your movie/TV watchlist, favorites, custom lists, and watch history in our secure Google Cloud Firestore database.
- **Informational Device Identifier**: We store an informational device model label (e.g., *"Pixel 8"* or *"Samsung Galaxy S24"*) so you can easily identify which device created a particular backup snapshot.
- **Purpose**: Solely to authenticate your identity and restore your personal movie collection across your devices or after reinstalling the app.

### C. Community Features & Daily Polls (Optional)
ShowTime offers community interactive features (daily polls, vibe reactions, and discussions):
- Guest users receive a silent, randomized anonymous session ID to participate in polls and discussions without registration.
- If signed in with Google, your comments will display your verified Google display name and avatar.

---

## 2. Third-Party Services & Integrations

ShowTime integrates with trusted third-party providers to deliver app functionality:

1. **The Movie Database (TMDB)**: ShowTime uses the TMDB API for movie and TV show metadata, images, and cast information. TMDB does not receive your personal data from ShowTime.
2. **Trakt.tv (Optional)**: If you connect your Trakt account, your watchlist and history are synced with Trakt under your Trakt account credentials and subject to Trakt's privacy policy.
3. **Google Firebase (Firestore & Crashlytics)**: Used for cloud database storage and anonymous crash diagnostics to ensure app stability.
4. **Google AdMob**: Used to serve rewarded video ads for optional free-tier feature passes. AdMob may collect anonymous device identifiers in accordance with Google's Advertising Policies.
5. **Google Play In-App Billing**: All payments for ShowTime Pro are processed directly by Google Play. ShowTime never sees, collects, or stores your credit card or financial billing information.

---

## 3. Data Sharing & Disclosure

- **We do not sell, rent, or monetize your personal data.**
- Your data is never shared with third-party advertisers or data brokers.
- Cloud backups are strictly private and accessible only when authenticated under your personal Google account.

---

## 4. Data Retention and Deletion

You have full control over your data at all times:
- **Local Data**: You can delete all local data at any time by clearing app storage in Android Settings or uninstalling the app.
- **Cloud Backup**: You can delete your cloud snapshot and sign out directly within the **Cloud Backup & Sync** screen.
- **Account Deletion Requests**: To request manual deletion of any associated cloud data, open an issue or contact us via our GitHub repository.

---

## 5. Open Source & Transparency

ShowTime is an open-source project. You can inspect the source code, security rules, and data handling practices at any time:
- **Live Privacy Policy Web Page**: [https://showtime.ssverma.in/privacy](https://showtime.ssverma.in/privacy)
- **GitHub Repository**: [https://github.com/SSVerma/ShowTime](https://github.com/SSVerma/ShowTime)

---

## 6. Contact Us

If you have questions, feedback, or data privacy inquiries regarding ShowTime, please contact us:
- **Email**: [ssvermahmh@gmail.com](mailto:ssvermahmh@gmail.com)
- **Project Maintainer**: [https://github.com/SSVerma/ShowTime/issues](https://github.com/SSVerma/ShowTime/issues)
