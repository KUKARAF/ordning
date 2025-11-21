FROM eclipse-temurin:17-jdk-jammy

# Install necessary packages
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    git \
    curl \
    bash \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Create directories
RUN mkdir -p $ANDROID_HOME/cmdline-tools

# Download and install Android SDK command line tools
# Using a specific version for reproducibility
RUN cd /tmp && \
    wget -O commandlinetools.zip https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip && \
    unzip commandlinetools.zip -d $ANDROID_HOME/cmdline-tools && \
    rm commandlinetools.zip

# Rename tools directory to "latest" as required by Android SDK
RUN mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest

# Accept Android licenses
RUN yes | sdkmanager --licenses

# Install Android SDK components
# Using the same versions as in your build.gradle
RUN sdkmanager \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "extras;android;m2repository" \
    "extras;google;m2repository"

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Make gradlew executable (if it exists)
RUN chmod +x ./gradlew 2>/dev/null || echo "No gradlew file found"

# Default command
CMD ["bash"]