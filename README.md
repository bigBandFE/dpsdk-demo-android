# DPSDK Android Demo App

This project demonstrates the integration of DragonPass DPSDK into an Android application.

## Project Setup

Clone the repository and open the project in Android Studio. The DPSDK dependency is resolved automatically via JitPack:

```
implementation("com.github.bigBandFE:dpsdk-android:2.0.0")
```

Check [dpsdk-android](https://github.com/bigBandFE/dpsdk-android) for the latest version.

## Getting Client ID

Open an Issue at **[github.com/bigBandFE/dpsdk-contact](https://github.com/bigBandFE/dpsdk-contact)** with your name, company, and email address. The DPSDK team will respond with your Client ID.

## Setting Client ID

In `App.kt`, replace the placeholder:

```
.setClientId("CLIENT_ID")
```

## License

This project is provided as a reference integration sample.
