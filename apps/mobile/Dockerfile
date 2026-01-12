FROM thyrlian/android-sdk:latest AS base

WORKDIR /app

FROM base AS sdk-update

RUN /opt/android-sdk/cmdline-tools/tools/bin/sdkmanager --update

FROM base AS deps
COPY --from=sdk-update /opt/android-sdk /opt/android-sdk

COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/ gradle/
COPY app/build.gradle.kts app/
COPY app/release.keystore app/
COPY gradlew .

RUN ./gradlew dependencies --no-daemon

FROM base AS builder
COPY --from=deps /root/.gradle /root/.gradle
COPY . .

RUN ./gradlew assembleRelease --no-daemon

CMD ["sh", "-c", "cp app/build/outputs/apk/release/app-release.apk /output/client.apk && echo Release APK copied to /output/client.apk"]
