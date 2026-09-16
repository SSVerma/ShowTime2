# Google Play Billing & ShowTime Pro Subscriptions Guide

This document is the authoritative guide for setting up, configuring, and testing Google Play Subscriptions for ShowTime Pro in the Google Play Console.

---

## 1. Google Play Console Setup Checklist

### Step 1: Build & Upload to Internal Testing
Google Play Console will not allow creating or configuring subscriptions until an Android App Bundle (`.aab`) with billing permissions has been uploaded to at least **Internal testing**.

1. **Check Version Code**:
   In `app/build.gradle.kts`, ensure `versionCode` is incremented higher than any previously uploaded bundle:
   ```kotlin
   versionCode = 18 // Increment before building
   versionName = "1.0.13"
   ```
2. **Build Release Bundle**:
   ```bash
   ./gradlew bundleRelease
   ```
   The output bundle is generated at:
   `app/build/outputs/bundle/release/app-release.aab`

3. **Upload to Play Console**:
   - Go to **Google Play Console** → Select ShowTime.
   - Under **Testing**, open **Internal testing**.
   - Create a new release and upload `app-release.aab`.

---

### Step 2: Create Subscriptions & Base Plans

Navigate to **Monetize** → **Products** → **Subscriptions**.
Create the 4 subscription products matching `BillingConstants.kt`:

| Subscription Product ID | Recommended Base Plan ID | Billing Period | Type | Plan Notes |
|---|---|---|---|---|
| `showtime_pro_yearly` | `yearly-plan` | 1 Year | Auto-renewing | Best Value / Primary Annual Tier |
| `showtime_pro_six_months` | `six-months-plan` | 6 Months | Auto-renewing | Semi-annual Tier |
| `showtime_pro_three_months` | `three-months-plan` | 3 Months | Auto-renewing | Quarterly Tier |
| `showtime_pro_monthly` | `monthly-plan` | 1 Month | Auto-renewing | Standard Monthly Tier |

#### How to configure each subscription:
1. Click **Create subscription**.
2. Set **Product ID** exactly as listed in the table above (e.g., `showtime_pro_yearly`).
3. Set **Name** (e.g., `ShowTime Pro - Annual`).
4. Under **Base plans**, click **Add base plan**:
   - **Base plan ID**: e.g., `yearly-plan`.
   - **Type**: Auto-renewing.
   - **Billing period**: Select matching duration (e.g., 1 year).
   - **Pricing**: Set default price in your primary currency (Play Console can auto-convert for all regions).
5. Click **Save** and then **Activate** the base plan.

---

### Step 3: Configure License Testing (Zero-Cost Testing)

To test real Google Play billing flows without spending actual money:

1. **Add License Testers**:
   - In Google Play Console, go to **Setup** → **License testing**.
   - Add your Gmail address(es) to the list of license testers.
   - Set **License test response** to `RESPOND_NORMALLY`.
2. **Internal Testing Track Access**:
   - Under **Testing** → **Internal testing** → **Testers** tab:
   - Select your email list.
   - Copy the **"Join on the web" / "Join on Android"** link.
   - Open the link on your test device using the tester Google account and click **Accept Invite**.
3. **Download & Test on Device**:
   - On the test device, open Google Play Store logged into that tester Gmail account.
   - Install the app via the internal test link (or install release build signed with the same key).
   - When tapping "Upgrade to Pro", Google Play will show:
     - `[Test card, always approves]`
     - Accelerated test renewal intervals (e.g., 1-month subscriptions renew every 5 minutes).

---

## 2. Release vs. Debug Builds for Billing Testing

| Aspect | Debug Build (`./gradlew assembleDebug`) | Release Build (`./gradlew bundleRelease`) |
|---|---|---|
| **Package Name** | `com.ssverma.showtime.debug` (has `.debug` suffix) | `com.ssverma.showtime` (exact match) |
| **Play Billing Connection** | ❌ Fails with `DEVELOPER_ERROR` / `ITEM_UNAVAILABLE` | ✅ Connects successfully to Google Play |
| **Sandbox Experience** | ✅ Built-in Dev Sandbox (toggles Pro in Dev Panel) | ❌ Zero sandbox code included (pure prod) |
| **Play Console Upload** | ❌ Rejected by Play Console | ✅ Accepted for Internal / Production |

> **Important**:
> Because `app/build.gradle.kts` specifies `applicationIdSuffix = ".debug"` for the debug build type, debug builds cannot communicate with Google Play Subscriptions. Always use the **Release build** uploaded to an internal testing track when testing real Google Play dialogs.

---

## 3. Tokens, Secrets & API Keys FAQ

- **Are any API tokens or keys needed for Google Play Billing?**
  **No.** Google Play Billing uses Android IPC via Google Play Services. Authentication is based on your application ID (`com.ssverma.showtime`) and your app's release signing key.
- **Do I need to put anything in `core.properties`?**
  **No.** `core.properties` is only for TMDB access token and Trakt client ID.
- **Is a server backend required to acknowledge purchases?**
  **No.** Client-side auto-acknowledgment is handled in `BillingClientWrapper.kt`. All purchases are immediately acknowledged upon receipt.

---

## 4. Architecture & Key Classes

- `BillingConstants.kt`: Declares all product SKU constants.
- `BillingClientWrapper.kt`: Manages `BillingClient` lifecycle, queries, purchases, upgrades, and acknowledgments.
- `BillingEntitlementStorage.kt`: Caches entitlement state offline securely.
- `ProPaywallBottomSheet.kt`: Bottom sheet paywall with in-sheet error snackbar host and benefits accordion.
- `ProPaywallScreen.kt`: Full screen paywall alternative.
- `DebugBillingSandboxProvider.kt`: Debug-only sandbox simulator.
- `ReleaseBillingSandboxProvider.kt`: Release no-op sandbox ensuring zero test data in production.
