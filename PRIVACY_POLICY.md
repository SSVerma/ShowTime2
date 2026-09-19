# Privacy Policy for ShowTime

**Effective Date:** September 1, 2026  
**Last Updated:** September 19, 2026

ShowTime ("we", "our", or "the app") is a privacy-first, open-source movie and TV show exploration and cinema companion application. We are committed to absolute transparency and protecting your privacy. This Privacy Policy outlines what information ShowTime collects, how it is used, and the choices and rights you have concerning your data.

---

## 1. Core Principles: Local-First by Default

ShowTime is architected on a **local-first** foundation. You do not need to create an account, provide an email address, or log in to use the full core functionality of ShowTime:

- Your **Watchlist**, **Favorites**, **Watch History**, **Custom Collections**, and **Cinema Diary** (including custom ratings, reviews, tags, and watch dates) are stored strictly on your local device in a private SQLite/Room database.
- This data never leaves your device unless you explicitly and voluntarily enable our optional **Cloud Backup & Sync** service.

---

## 2. Information We Collect and How We Use It

### A. Cloud Backup & Sync (Optional Google Sign-In)
If you voluntarily choose to use **Cloud Backup & Sync** to preserve your library across devices:
- **Account Identity**: We receive your Google Account email address, display name, and avatar URL via Google Sign-In authentication.
- **Encrypted Library Snapshot**: We store a compressed, structured snapshot of your watchlist, favorites, custom lists, watch history, and cinema diary in our secure Google Cloud Firestore database.
- **Device Label**: We store an informational device model name (e.g., *"Pixel 8"* or *"Galaxy S24"*) so you can identify which device generated a given snapshot.
- **Purpose**: Exclusively to authenticate your identity and restore your personal movie collection when switching devices or reinstalling the app. We do not use your backup data for profiling, targeting, or analytics.

### B. Community Interactive Features & Daily Polls (Optional)
ShowTime offers interactive cinephile community features (such as daily polls, community curated lists, and discussions):
- **Guest Participation**: If you are not signed in, ShowTime generates a randomized, anonymous session token to record poll votes, list upvotes, and reactions without tracking your personal identity.
- **Signed-In Interactions**: If you choose to post comments or share public collections while signed in with Google, your Google display name and public avatar will be visibly credited on that content.

### C. Calendar Integration & Airing Reminders
- **Calendar File Export (.ics)**: When you export release reminders to your calendar, ShowTime generates the standard iCalendar (`.ics`) file entirely locally on your device. ShowTime does not read, access, or transmit your existing calendar events.
- **Airing Alerts**: Notification scheduling uses Android's local WorkManager and Firebase Cloud Messaging (FCM). You can enable or disable release alerts at any time in Android Settings or within the app.

### D. Home Screen Widgets
ShowTime's Android Glance widgets ("Up Next to Watch" and "My Watchlist") render data sourced entirely from your device's local database. No external tracking or network transmissions occur during widget refreshes.

---

## 3. Third-Party Services & Integrations

ShowTime integrates with select, industry-standard third-party providers:

1. **The Movie Database (TMDB)**: We fetch movie, TV show, cast, and crew metadata, posters, and backdrops from TMDB. TMDB does not receive your personal library or user identity.
2. **JustWatch**: Streaming availability information is powered by JustWatch via TMDB. No personal watch data is shared with JustWatch.
3. **Google Play In-App Billing (ShowTime Pro)**:
   - All transactions for ShowTime Pro (monthly, yearly subscriptions, and lifetime passes) are processed exclusively by **Google Play Billing**.
   - ShowTime **never sees, collects, processes, or stores** your credit card details, bank account numbers, or financial billing information.
   - Subscription lifecycle management (renewals, cancellations, refunds) is managed under Google Play Subscriptions policies.
4. **Google Firebase (Firestore & Crashlytics)**:
   - **Firestore**: Hosts user-initiated cloud backups and community discussions under strict security rules.
   - **Crashlytics**: Collects anonymized crash diagnostics, stack traces, and device OS versions to help us fix stability issues.
5. **Google AdMob (Free Tier Only)**:
   - The free tier may display non-intrusive native or rewarded video ads. AdMob may process anonymous device advertising identifiers in compliance with Google's Advertising Policies and COPPA requirements.
   - **ShowTime Pro members enjoy a 100% ad-free experience** with ad SDK initialization completely deactivated.

---

## 4. Data Sharing, Selling & Monetization

- **We DO NOT sell, rent, trade, or monetize your personal data.**
- We do not share your movie preferences or diary entries with data brokers or advertising networks.
- Cloud backups are private and accessible solely by the authenticated owner of the Google Account.

---

## 5. Data Retention, Control & Account Deletion

You retain complete ownership and control of your data:

- **Local Data Erasure**: You can wipe all local data at any time by selecting "Clear Data" in Android App Settings or by uninstalling ShowTime.
- **Cloud Backup Deletion**: You can delete your cloud backup snapshot directly within **Settings > Cloud Backup & Restore** with a single tap.
- **Complete Erasure Requests**: If you wish to permanently delete all server-side records associated with your Google Account, you can request immediate erasure by emailing [ssvermahmh@gmail.com](mailto:ssvermahmh@gmail.com) with the subject "Data Erasure Request".

---

## 6. Children's Privacy (COPPA & GDPR-K Compliance)

ShowTime is intended for general audiences and does not knowingly collect or solicit personal information from children under the age of 13 (or 16 in the European Economic Area). If we learn that we have collected personal data from a child without verified parental consent, we will delete that data immediately.

---

## 7. Security Safeguards

We implement rigorous technical safeguards to protect your information:
- All communications between the app and external services (TMDB, Firebase, Google Play) are encrypted in transit using **TLS/HTTPS**.
- Firebase Firestore is governed by strict, declarative security rules ensuring users can only read and write their own encrypted backup documents.

---

## 8. Open-Source Transparency

ShowTime is open source. Our privacy practices, data models, and backend security rules are publicly auditable:
- **GitHub Repository**: [https://github.com/SSVerma/ShowTime2](https://github.com/SSVerma/ShowTime2)
- **Live Privacy Policy**: [https://showtime.ssverma.in/privacy](https://showtime.ssverma.in/privacy)

---

## 9. Contact Us

If you have any questions, suggestions, or concerns regarding this Privacy Policy or your data rights, please reach out to us:

- **Email**: [ssvermahmh@gmail.com](mailto:ssvermahmh@gmail.com)
- **Developer / Maintainer**: [https://github.com/SSVerma/ShowTime2/issues](https://github.com/SSVerma/ShowTime2/issues)
