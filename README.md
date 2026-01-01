# Network Monitor SDK

A lightweight Android SDK for monitoring and tracking network requests in your Android application. The SDK automatically intercepts HTTP/HTTPS requests, stores them in a local SQLite database, and uploads them to [engineerakash.com](https://engineerakash.com) at regular intervals or in real-time via Socket.IO. View your network logs by logging into the dashboard at [engineerakash.com](https://engineerakash.com).

## Features

- 🔍 **Automatic Network Interception** - Automatically tracks all network requests via OkHttp Interceptor
- 💾 **Local Storage** - Stores requests, responses, headers, and metadata in SQLite database (Room)
- ⏱️ **Periodic Uploads** - Automatically uploads collected data to engineerakash.com at configurable intervals (default: 1 minute)
- ⚡ **Real-time Uploads** - Server-controlled Socket.IO integration (automatically enabled when your device/userId is searched on the website)
- 📊 **Web Dashboard** - View all network logs by logging into engineerakash.com
- 🎯 **User Tracking** - Associate network requests with user IDs and custom properties
- 🔒 **Privacy First** - Configurable URL filtering and data size limits
- 📊 **MVVM Architecture** - Built with modern Android architecture patterns
- 🚀 **Zero Boilerplate** - Simple initialization, works out of the box

## Requirements

- Android API Level 21 (Android 5.0) or higher
- OkHttp for network requests
- Kotlin Coroutines support

## Installation

### Add to your project

Add the SDK to your app's `build.gradle`:

```gradle
dependencies {
    implementation 'com.engineerakash:networkmonitorsdk:1.0.0'
    
    // Required dependencies
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // Socket.IO for real-time uploads (included automatically)
    implementation 'io.socket:socket.io-client:2.1.0'
}
```

### Sync Gradle

Sync your project to download the dependencies.

## Quick Start

### 1. Initialize in Application Class

```kotlin
import android.app.Application
import com.engineerakash.networkmonitorsdk.NetworkMonitor
import com.engineerakash.networkmonitorsdk.config.MonitorConfig

class MyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Network Monitor SDK
        // Logs are automatically uploaded to https://engineerakash.com/api/network-logs
        // Real-time uploads are automatically enabled by the server when needed
        val config = MonitorConfig.Builder()
            .apiKey("your-api-key") // Optional: for authentication
            .build()
        
        NetworkMonitor.init(this, config)
    }
}
```

Don't forget to register your Application class in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    ...>
</application>
```

### 2. Add Interceptor to OkHttpClient

```kotlin
import com.engineerakash.networkmonitorsdk.NetworkMonitor
import okhttp3.OkHttpClient

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(NetworkMonitor.getInterceptor())
    .build()
```

### 3. (Optional) Set User ID and Properties

```kotlin
import com.engineerakash.networkmonitorsdk.NetworkMonitor

// Set user ID
NetworkMonitor.setUserId("user123")

// Set custom properties (will be attached to all requests)
NetworkMonitor.setProperty("app_version", "1.0.0")
NetworkMonitor.setProperty("environment", "production")

// Reset user ID
NetworkMonitor.resetUserId()

// Reset all properties
NetworkMonitor.resetProperties()
```

## Configuration Options

### MonitorConfig.Builder()

| Method | Type | Default | Description |
|--------|------|---------|-------------|
| `apiKey(key: String?)` | String? | `null` | API key/token for authentication (optional) |
| `uploadIntervalMinutes(minutes: Long)` | Long | `1` | Upload interval in minutes (optional, defaults to 1 minute) |
| `maxRequestBodySize(size: Long)` | Long | `1048576` (1MB) | Maximum request body size to capture (bytes) |
| `maxResponseBodySize(size: Long)` | Long | `1048576` (1MB) | Maximum response body size to capture (bytes) |
| `includeUrlPatterns(patterns: List<String>)` | List<String> | `emptyList()` | Regex patterns for URLs to include |
| `excludeUrlPatterns(patterns: List<String>)` | List<String> | `emptyList()` | Regex patterns for URLs to exclude |
| `enabled(enable: Boolean)` | Boolean | `true` | Enable/disable monitoring |

### Configuration Examples

#### Basic Configuration

```kotlin
// Minimal configuration - logs upload to https://engineerakash.com/api/network-logs
val config = MonitorConfig.Builder()
    .build()
```

#### Advanced Configuration with Filtering

```kotlin
val config = MonitorConfig.Builder()
    .uploadIntervalMinutes(2)
    .maxRequestBodySize(512 * 1024) // 512KB
    .maxResponseBodySize(512 * 1024) // 512KB
    .excludeUrlPatterns(listOf(
        ".*\\.googleapis\\.com.*",  // Exclude Google APIs
        ".*\\.facebook\\.com.*"      // Exclude Facebook APIs
    ))
    .apiKey("your-api-key") // Optional: for authentication
    .build()
```

> **Note:** 
> - Upload endpoints are automatically set to `https://engineerakash.com/api/network-logs` (batch) and `https://engineerakash.com` (Socket.IO). These cannot be configured by users.
> - Regular batch uploads continue automatically at the configured interval (default: 1 minute).
> - Real-time uploads are automatically enabled by the server when someone searches for your device/userId on the website. You don't need to configure this - it's handled internally.

## API Reference

### NetworkMonitor

Main SDK singleton object.

#### Methods

##### `init(context: Context, config: MonitorConfig)`

Initialize the SDK. Must be called before using any other methods.

```kotlin
NetworkMonitor.init(context, config)
```

##### `getInterceptor(): NetworkMonitorInterceptor`

Returns the OkHttp interceptor to add to your OkHttpClient.

```kotlin
val interceptor = NetworkMonitor.getInterceptor()
```

##### `setUserId(userId: String)`

Set or update the user ID that will be associated with all network requests.

```kotlin
NetworkMonitor.setUserId("user123")
```

##### `resetUserId()`

Reset the user ID.

```kotlin
NetworkMonitor.resetUserId()
```

##### `setProperty(key: String, value: String)`

Set a custom property that will be attached to all network requests.

```kotlin
NetworkMonitor.setProperty("app_version", "1.0.0")
NetworkMonitor.setProperty("environment", "production")
```

##### `resetProperties()`

Reset all custom properties.

```kotlin
NetworkMonitor.resetProperties()
```

##### `isInitialized(): Boolean`

Check if the SDK is initialized.

```kotlin
if (NetworkMonitor.isInitialized()) {
    // SDK is ready to use
}
```

## Data Model

### NetworkRequest

Each network request is captured with the following information:

```kotlin
data class NetworkRequest(
    val id: Long?,                    // Database ID
    val timestamp: Long,              // Request timestamp (milliseconds)
    val url: String,                  // Request URL
    val method: String,               // HTTP method (GET, POST, etc.)
    val requestHeaders: Map<String, String>,  // Request headers
    val requestBody: String?,         // Request body
    val responseCode: Int?,           // HTTP status code
    val responseHeaders: Map<String, String>, // Response headers
    val responseBody: String?,        // Response body
    val duration: Long,               // Request duration (milliseconds)
    val userId: String?,              // Associated user ID
    val properties: Map<String, String> // Custom properties
)
```

## Viewing Network Logs

All network logs are automatically uploaded to [engineerakash.com](https://engineerakash.com). To view your network logs:

1. Log in to your account at [engineerakash.com](https://engineerakash.com)
2. Navigate to the Network Monitor dashboard
3. Search for a device/userId to view its network requests
4. When you search for a device/userId, real-time uploads are automatically enabled for that device (if it's currently active)

The SDK automatically associates logs with your account based on the API key provided during configuration (if set). Regular batch uploads continue at the configured interval (default: 1 minute), and real-time uploads are automatically enabled when that device/userId is being monitored on the website.

## Upload Format

### Batch Upload (HTTP POST)

The SDK uploads data to `https://engineerakash.com/api/network-logs` via HTTP POST. The request body format:

```json
{
  "requests": [
    {
      "timestamp": 1704067200000,
      "url": "https://api.example.com/users",
      "method": "GET",
      "requestHeaders": {
        "Content-Type": "application/json",
        "Authorization": "Bearer token"
      },
      "requestBody": null,
      "responseCode": 200,
      "responseHeaders": {
        "Content-Type": "application/json"
      },
      "responseBody": "{\"users\": []}",
      "duration": 150,
      "userId": "user123",
      "properties": {
        "app_version": "1.0.0",
        "environment": "production"
      }
    }
  ]
}
```

### Real-time Upload (Socket.IO)

When real-time upload is automatically enabled by the server (when someone searches for your device/userId on the website), each request is sent immediately via Socket.IO to `https://engineerakash.com` with the event name `network_request`:

```javascript
socket.on('network_request', (data) => {
  // data contains the NetworkRequest object
  console.log(data);
});
```

## Architecture

The SDK follows MVVM architecture pattern:

- **Models**: Data classes representing network requests
- **Repository**: Data access layer (Room database operations)
- **Database**: Room database with entities and DAOs
- **Upload**: Managers for batch and real-time uploads
- **Interceptor**: OkHttp interceptor for request/response capture

## Best Practices

1. **Initialize Early**: Initialize the SDK in your Application class `onCreate()` method
2. **Filter Sensitive Data**: Use `excludeUrlPatterns` to exclude sensitive endpoints
3. **Set Size Limits**: Configure appropriate size limits for request/response bodies
4. **Real-time Uploads**: Real-time uploads are automatically controlled by the server - they're enabled only when someone searches for your device/userId on the website, so you don't need to worry about battery consumption
5. **Handle User Privacy**: Reset user ID when users log out

## Privacy & Security

- Request/response bodies are stored locally in SQLite database
- Data is encrypted at rest (SQLite encryption can be enabled)
- No sensitive data is transmitted without your server's consent
- Use URL filtering to exclude sensitive endpoints
- Set appropriate body size limits to prevent storing large payloads

## Troubleshooting

### SDK not capturing requests

1. Ensure SDK is initialized before creating OkHttpClient
2. Verify interceptor is added to OkHttpClient
3. Check that monitoring is enabled in config

### Uploads not working

1. Check network connectivity
2. Verify you can access https://engineerakash.com
3. Check API key/authentication if required
4. Review server logs for errors

### Real-time uploads not working

1. Real-time uploads are automatically enabled by the server when your device/userId is searched on the website
2. Verify Socket.IO endpoint at https://engineerakash.com is accessible
3. Check network connectivity and firewall settings
4. Ensure someone has searched for your device/userId on the website dashboard

## License

[Add your license here]

## Contributing

[Add contributing guidelines if applicable]

## Support

For issues, questions, or feature requests, please [create an issue](https://github.com/your-repo/issues).

---

**Built with ❤️ for Android Developers**

