# Multi-stage build for JavaFX application

# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage with VNC server
FROM eclipse-temurin:17-jdk

# Install VNC server, window manager, and JavaFX dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    # VNC Server - use tightvncserver which includes vncpasswd
    tightvncserver \
    # Window Manager (lightweight)
    fluxbox \
    # JavaFX runtime
    openjfx \
    libopenjfx-java \
    libopenjfx-jni \
    # X11 and JavaFX dependencies
    xfonts-base \
    xfonts-75dpi \
    xfonts-100dpi \
    xfonts-scalable \
    fonts-liberation \
    # Mesa and graphics libraries for JavaFX software rendering
    libgl1 \
    libglu1-mesa \
    libglapi-mesa \
    mesa-utils \
    libgtk-3-0 \
    libgtk2.0-0 \
    libxtst6 \
    libxxf86vm1 \
    libasound2t64 \
    libavcodec-extra60 \
    x11-apps \
    x11-xserver-utils \
    dbus-x11 \
    # Utilities
    procps \
    net-tools \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Configure VNC font path and startup
RUN mkdir -p /root/.vnc && \
    echo "#!/bin/sh" > /root/.vnc/xstartup && \
    echo "unset SESSION_MANAGER" >> /root/.vnc/xstartup && \
    echo "unset DBUS_SESSION_BUS_ADDRESS" >> /root/.vnc/xstartup && \
    echo "[ -r \$HOME/.Xresources ] && xrdb \$HOME/.Xresources" >> /root/.vnc/xstartup && \
    echo "fluxbox &" >> /root/.vnc/xstartup && \
    chmod +x /root/.vnc/xstartup

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/rh-project-javafx-*.jar app.jar

# Create startup script directly to avoid Windows line ending issues
RUN echo '#!/bin/bash\n\
set -e\n\
export USER=root\n\
echo "========================================="\n\
echo "Starting RH JavaFX Application with VNC"\n\
echo "========================================="\n\
mkdir -p ~/.vnc\n\
printf "%s\\n%s\\n" "$VNC_PASSWORD" "$VNC_PASSWORD" | vncpasswd > /dev/null 2>&1\n\
chmod 600 ~/.vnc/passwd\n\
echo "Starting VNC server on display $DISPLAY with resolution $VNC_RESOLUTION..."\n\
echo "NOTE: VNC password limited to 8 characters"\n\
vncserver -kill $DISPLAY > /dev/null 2>&1 || true\n\
rm -rf /tmp/.X1-lock /tmp/.X11-unix/X1 > /dev/null 2>&1 || true\n\
vncserver $DISPLAY -geometry $VNC_RESOLUTION -depth 24 -rfbport 5901\n\
sleep 5\n\
echo ""\n\
echo "========================================="\n\
echo "VNC Server started successfully!"\n\
echo "========================================="\n\
echo "VNC Display: $DISPLAY"\n\
echo "VNC Port: 5901"\n\
echo "VNC Password: $VNC_PASSWORD"\n\
echo "Resolution: $VNC_RESOLUTION"\n\
echo ""\n\
echo "Connect using:"\n\
echo "  - VNC Viewer: localhost:5901"\n\
echo "  - Password: $VNC_PASSWORD"\n\
echo "========================================="\n\
echo ""\n\
echo "Waiting for MySQL database to be ready..."\n\
MAX_RETRIES=30\n\
RETRY_COUNT=0\n\
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do\n\
    if nc -z $DB_HOST $DB_PORT 2>/dev/null; then\n\
        echo "Database is ready!"\n\
        break\n\
    fi\n\
    RETRY_COUNT=$((RETRY_COUNT + 1))\n\
    echo "Waiting for database... ($RETRY_COUNT/$MAX_RETRIES)"\n\
    sleep 2\n\
done\n\
if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then\n\
    echo "WARNING: Could not connect to database after $MAX_RETRIES attempts"\n\
    echo "Application will start anyway and attempt to connect..."\n\
fi\n\
sleep 5\n\
echo ""\n\
echo "Starting JavaFX Application..."\n\
echo "Database: $DB_HOST:$DB_PORT/$DB_NAME"\n\
echo ""\n\
export DISPLAY=$DISPLAY\n\
export LD_LIBRARY_PATH=/usr/lib/jni:/usr/lib/x86_64-linux-gnu/jni:$LD_LIBRARY_PATH\n\
cd /app\n\
java -Djava.library.path=/usr/lib/jni:/usr/lib/x86_64-linux-gnu/jni -Dprism.order=sw -Dprism.verbose=true -Djavafx.platform=gtk --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar app.jar\n\
echo ""\n\
echo "Application has stopped. Keeping VNC server running..."\n\
tail -f /dev/null\n\
' > /app/docker-entrypoint.sh && chmod +x /app/docker-entrypoint.sh

# Environment variables for database connection
ENV DB_HOST=mysql
ENV DB_PORT=3306
ENV DB_NAME=rh_javafx_db
ENV DB_USER=root
ENV DB_PASSWORD=rootpass123

# VNC configuration
ENV DISPLAY=:1
ENV VNC_RESOLUTION=1920x1080
ENV VNC_PASSWORD=haFI99D#

# Expose VNC port
EXPOSE 5901

# Entry point
ENTRYPOINT ["/app/docker-entrypoint.sh"]
