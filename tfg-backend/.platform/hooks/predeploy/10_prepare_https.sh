#!/bin/bash
set -e

mkdir -p /var/www/letsencrypt/.well-known/acme-challenge

if ! rpm -q cronie >/dev/null 2>&1; then
    dnf install -y cronie
fi

if ! command -v certbot >/dev/null 2>&1; then
    dnf install -y python3.11 python3.11-pip

    python3.11 -m venv /opt/certbot
    /opt/certbot/bin/pip install --upgrade pip
    /opt/certbot/bin/pip install --upgrade certbot

    ln -sf /opt/certbot/bin/certbot /usr/local/bin/certbot
fi

mkdir -p /etc/letsencrypt/renewal-hooks/deploy

cat > /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh <<'EOF'
#!/bin/bash
systemctl reload nginx
EOF

chmod +x /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh

cat > /etc/cron.d/certbot-renew <<'EOF'
17 2,14 * * * root /usr/local/bin/certbot renew -q
EOF

chmod 644 /etc/cron.d/certbot-renew

systemctl enable crond
systemctl restart crond

echo "Preparación de HTTPS completada."