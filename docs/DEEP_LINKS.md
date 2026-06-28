# ShowTime Deep Links Documentation

ShowTime supports deep linking to navigate directly to movies, TV shows, and actor profiles.

## URI Scheme and Host

- **Scheme**: `showtime`
- **Host**: `www.ssverma.in`

## Supported URI Patterns

| Content Type        | URI Pattern                             | Example                                |
|:--------------------|:----------------------------------------|:---------------------------------------|
| **Movie Details**   | `showtime://www.ssverma.in/movie/{id}`  | `showtime://www.ssverma.in/movie/550`  |
| **TV Show Details** | `showtime://www.ssverma.in/tv/{id}`     | `showtime://www.ssverma.in/tv/1399`    |
| **Person Details**  | `showtime://www.ssverma.in/person/{id}` | `showtime://www.ssverma.in/person/287` |

## How to Test Manually

Use `adb` to trigger deep links from your terminal. Replace `<SERIAL>` with your device serial if
multiple devices are connected.

### 1. Movie Details

```bash
adb shell am start -W -a android.intent.action.VIEW -d "showtime://www.ssverma.in/movie/550" com.ssverma.showtime.debug
```

### 2. TV Show Details

```bash
adb shell am start -W -a android.intent.action.VIEW -d "showtime://www.ssverma.in/tv/1399" com.ssverma.showtime.debug
```

### 3. Person Details

```bash
adb shell am start -W -a android.intent.action.VIEW -d "showtime://www.ssverma.in/person/287" com.ssverma.showtime.debug
```

## Push Notifications (FCM)

### 1. Sending from Firebase Console

When creating a new notification campaign in the Firebase Console:

1. Navigate to **Compose Notification**.
2. Fill in the basic info (Title/Text).
3. In **Step 4 (Additional options)**, look for the **Custom data** section.
4. Add the following key-value pairs:

| Key        | Value Example                                |
|:-----------|:---------------------------------------------|
| `deepLink` | `showtime://www.ssverma.in/movie/550`        |
| `image`    | `https://image.tmdb.org/t/p/w500/poster.jpg` |

### 2. JSON Payload (API)

If sending via the FCM API, use this structure:

## Troubleshooting

- **Empty Page**: If a deep link opens a screen but it remains empty, check if the ID is valid.
  ShowTime will show an error indicator for invalid IDs.
- **Incorrect App Opening**: Ensure you are targeting the correct package name (
  `com.ssverma.showtime` for release, `com.ssverma.showtime.debug` for debug builds).
- **Logs**: Monitor logs for `DeepLinkHandler` to debug parsing issues:
  ```bash
  adb logcat -s DeepLinkHandler
  ```
