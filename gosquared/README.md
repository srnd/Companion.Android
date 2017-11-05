# Unofficial GoSquared Android SDK

This unofficial SDK for GoSquared's live chat lets you interact with your customers live within your Android app.

## Usage

It's super easy to use. Wherever you want to connect to GoSquared, use this:

```kotlin
GoSquared.init(context, GoSquaredConfig(
        siteToken = "Your GS site token",
        chatName = "The name of your chat (e.g. 'Google Support')",
        notifChannel = "The notification channel for new messages to be sent through",
        notifIcon = R.drawable.your_small_icon,
        notifColor = R.color.your_brand_color
), User(
        // This user object is optional, but recommended.
        id = "Unique ID",
        name = "Cool Guy",
        email = "person@gmail.com"
        custom = mapOf(
                "customKey1" to "customValue1",
                "customKey2" to "customValue2"
        )
))
```

There will be more granular control in the future, but this is what you can do for now.

After you init the library, it will connect to GoSquared and retrieve any past messages from the chat history. After initialization, you can call `GoSquared.openChat` from anywhere to open the live chat.

```kotlin
// example
GoSquared.openChat(context)
```

If you don't want to use the pre-built live chat view, that's fine too! You can use these methods:

```kotlin
// holds any previous messages
GoSquared.cachedMessages

// MAKE SURE to unregister listeners with
// unregisterOnMessageListener once you're done with them
GoSquared.registerOnMessageListener { message ->
  Log.d("GS", message.content)
}

// to send messages
GoSquared.sendMessage(messageField.text.toString(), GoSquaredSession(
        title = "My App",
        href = "http://app/AndroidApp" // will show up in the chat as "from /AndroidApp"
))

// to prevent notifications from being sent
GoSquared.shouldNotifyForNewMessage = false

// once a user has read a message
GoSquared.readMessage(message)
```

That's about it for now. Stay tuned.

## TODO

- [ ] Custom chat bubble color