#!/bin/bash
set -e

echo "========================================="
echo "Starting RH JavaFX Application with VNC"
echo "========================================="

# Create VNC directory
mkdir -p ~/.vnc

# Set VNC password
echo "$VNC_PASSWORD" | vncpasswd -f > ~/.vnc/passwd
chmod 600 ~/.vnc/passwd

# Create xstartup file for VNC
cat > ~/.vnc/xstartup << 'EOF'
#!/bin/bash
unset SESSION_MANAGER
unset DBUS_SESSION_BUS_ADDRESS
exec fluxbox &
EOF

chmod +x ~/.vnc/xstartup

# Start VNC server
echo "Starting VNC server on display $DISPLAY with resolution $VNC_RESOLUTION..."
vncserver $DISPLAY -geometry $VNC_RESOLUTION -depth 24 -SecurityTypes VncAuth,TLSVnc

# Wait for VNC server to start
sleep 3

# Display connection information
echo ""
echo "========================================="
echo "VNC Server started successfully!"
echo "========================================="
echo "VNC Display: $DISPLAY"
echo "VNC Port: 5901"
echo "VNC Password: $VNC_PASSWORD"
echo "Resolution: $VNC_RESOLUTION"
echo ""
echo "Connect using:"
echo "  - VNC Viewer: localhost:5901"
echo "  - Password: $VNC_PASSWORD"
echo "========================================="
echo ""

# Wait for database to be ready
echo "Waiting for MySQL database to be ready..."
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if nc -z $DB_HOST $DB_PORT 2>/dev/null; then
        echo "Database is ready!"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "Waiting for database... ($RETRY_COUNT/$MAX_RETRIES)"
    sleep 2
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "WARNING: Could not connect to database after $MAX_RETRIES attempts"
    echo "Application will start anyway and attempt to connect..."
fi

# Additional wait to ensure database is fully initialized
sleep 5

# Start the JavaFX application
echo ""
echo "Starting JavaFX Application..."
echo "Database: $DB_HOST:$DB_PORT/$DB_NAME"
echo ""

export DISPLAY=$DISPLAY
cd /app
java -jar app.jar

# Keep container running if application exits
echo ""
echo "Application has stopped. Keeping VNC server running..."
tail -f /dev/null
